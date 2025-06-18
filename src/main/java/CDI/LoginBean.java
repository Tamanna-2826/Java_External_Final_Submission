/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package CDI;

import Entities.Users;
import Enums.UserRole;
import static Enums.UserRole.UPLOADER;
import Services.JwtService;
import Services.UserService;
import Utilities.PasswordUtil;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author LENOVO
 */
@Named(value = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {
//This is COmment to check git

    private static final long serialVersionUID = 1L;

    // Form fields
    private String email;
    private String password;
    private String fullName;

    private String selectedRole = "ADMIN";

    // Session data
    private Users currentUser;
    private String jwtToken;
    private boolean isAuthenticated = false;
    private String actionType = "login";
    private String percentageChange;

    @Inject
    private UserService userService;

    @Inject
    private JwtService jwtService;

    // New fields for storing users by role
    private List<Users> uploaders;
    private List<Users> users;
    // New fields for counts
    private long uploaderCount;
    private long userCount;
    private long totalUserCount;
    private boolean showIcon = true;

    public LoginBean() {
//        refreshCounts();
    }

    @PostConstruct
    public void init() {
        refreshCounts(); // Call refreshCounts after injection
    }

    public boolean isShowIcon() {
        return showIcon;
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }

    public void refreshCounts() {
        try {
            if (userService != null) {
                uploaderCount = userService.countUploaders();
                userCount = userService.countUsers();
                totalUserCount = uploaderCount + userCount;

                long previousCount = userService.countTotalUsersPreviousDay();
                if (previousCount > 0) {
                    double change = ((double) (totalUserCount - previousCount) / previousCount) * 100;
                    percentageChange = String.format("%+.1f%% today", change);
                } else {
                    percentageChange = "N/A";
                }
            } else {
                uploaderCount = 0;
                userCount = 0;
                totalUserCount = 0;
                percentageChange = "N/A";
                addErrorMessage("User service not available.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            uploaderCount = 0;
            userCount = 0;
            totalUserCount = 0;
            percentageChange = "N/A";
            addErrorMessage("Error fetching user counts.");
        }
    }

    /**
     * Fetch all active uploaders
     */
    public void fetchUploaders() {
        try {
            uploaders = userService.findUsersByRole(UserRole.UPLOADER);
            if (uploaders == null || uploaders.isEmpty()) {
                addInfoMessage("No active uploaders found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error fetching uploaders.");
        }
    }

    /**
     * Fetch all active users (assuming UserRole.USER exists)
     */
    public void fetchUsers() {
        try {
            users = userService.findUsersByRole(UserRole.USER);
            if (users == null || users.isEmpty()) {
                addInfoMessage("No active users found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error fetching users.");
        }
    }

    /**
     * Main login method for both Admin and Uploader
     */
    public String login() {
        try {
            // Clear previous messages
            // FacesContext.getCurrentInstance().getMessageList().clear();

            // Validate input
            if (!isValidInput()) {
                return null;
            }

            // Authenticate user
            Users user = authenticateUser();
            System.out.println("Auth USER :  " + user);
            if (user == null) {
                addErrorMessage("Invalid email or password");
                return null;
            }
            System.out.println("User Role Access: " + hasRoleAccess(user));

            // Verify role access
            if (!hasRoleAccess(user)) {
                addErrorMessage("Access denied for this role");
                return null;
            }

            // Generate JWT token
            String token = generateJwtToken(user);
            if (token == null) {
                addErrorMessage("Authentication failed. Please try again.");
                return null;
            }

            // Set session data
            setSessionData(user, token);

            // Set JWT in HTTP header
            setJwtInResponse(token);
            refreshCounts();
            // Navigate based on role
            return getNavigationOutcome(user.getRole());

        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("An error occurred during login. Please try again.");
            return null;
        }
    }

    /**
     * Validate login input
     */
    private boolean isValidInput() {
        if (email == null || email.trim().isEmpty()) {
            addErrorMessage("Email is required");
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            addErrorMessage("Password is required");
            return false;
        }

        if (!isValidEmail(email)) {
            addErrorMessage("Please enter a valid email address");
            return false;
        }

        return true;
    }

    /**
     * Authenticate user credentials
     */
    private Users authenticateUser() {
        try {
            Users user = userService.findByEmail(email.trim().toLowerCase());
            System.out.println("User : " + user);

            if (user != null && user.getIsActive()) {
                System.out.println("Entered Password: " + password); // Only for debug
                System.out.println("Stored Password Hash: " + user.getPasswordHash()); // Only for debug
                if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                    return user;
                }
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if user has access to selected role
     */
    private boolean hasRoleAccess(Users user) {
        UserRole userRole = user.getRole();
        System.out.println("ROLE : " + userRole);
        // Admin can access admin panel
        if ("ADMIN".equals(selectedRole) && userRole == UserRole.ADMIN) {
            return true;
        }

        // Uploader can access uploader panel
        if ("UPLOADER".equals(selectedRole) && userRole == UserRole.UPLOADER) {
            return true;
        }
        if ("USER".equals(selectedRole) && userRole == UserRole.USER) {
            return true;
        }
        return false;
    }

    /**
     * Generate JWT token for authenticated user
     */
    private String generateJwtToken(Users user) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getUserID());
            claims.put("email", user.getEmail());
            claims.put("role", user.getRole().name());
            claims.put("name", user.getFullName());
            claims.put("loginTime", System.currentTimeMillis());

            return jwtService.generateToken(user.getEmail(), claims);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Set session data after successful authentication
     */
    private void setSessionData(Users user, String token) {
        this.currentUser = user;
        this.jwtToken = token;
        this.isAuthenticated = true;

        // Store in HTTP session
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.getSessionMap().put("currentUser", user);
        externalContext.getSessionMap().put("jwtToken", token);
        externalContext.getSessionMap().put("isAuthenticated", true);
    }

    /**
     * Set JWT token in HTTP response header
     */
    private void setJwtInResponse(String token) {
        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
            response.setHeader("Authorization", "Bearer " + token);
            response.setHeader("Access-Control-Expose-Headers", "Authorization");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get navigation outcome based on user role
     */
    private String getNavigationOutcome(UserRole role) {
        System.err.println(role);
        switch (role) {
            case ADMIN:
                return "sidebar?faces-redirect=true";
            case UPLOADER:
                return "uploaderDashboard?faces-redirect=true";
            case USER:
                return "userDashboard?faces-redirect=true";
            default:
                return "userLogin?faces-redirect=true";
        }
    }

    /**
     * Logout method
     */
    public String logout() {
        try {
            // Clear session data
            this.currentUser = null;
            this.jwtToken = null;
            this.isAuthenticated = false;
            this.email = null;
            this.password = null;

            // Clear HTTP session
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            externalContext.getSessionMap().clear();
            externalContext.invalidateSession();

            // Add logout message
            addInfoMessage("You have been logged out successfully");

            return "adminLogin?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error during logout");
            return null;
        }
    }

    /**
     * Check if current session is valid
     */
    public boolean isSessionValid() {
        try {
            if (!isAuthenticated || jwtToken == null || currentUser == null) {
                return false;
            }

            // Validate JWT token
            return jwtService.isTokenValid(jwtToken);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Refresh JWT token
     */
    public String refreshToken() {
        try {
            if (currentUser == null) {
                return logout();
            }

            String newToken = generateJwtToken(currentUser);
            if (newToken != null) {
                this.jwtToken = newToken;
                setJwtInResponse(newToken);

                // Update session
                ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
                externalContext.getSessionMap().put("jwtToken", newToken);

                return "success";
            }

            return logout();

        } catch (Exception e) {
            e.printStackTrace();
            return logout();
        }
    }

    public String getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(String perChange) {
        this.percentageChange = perChange;
    }

    // New getters for counts
    public void setUploaderCount(long uploaders) {
        this.uploaderCount = uploaders;
    }

    public void setUserCount(long users) {
        this.userCount = users;
    }

    public long getUploaderCount() {
        return uploaderCount;
    }

    public long getUserCount() {
        return userCount;
    }

    public void setTotalUserCount(long allUsers) {
        this.totalUserCount = allUsers;
    }
    // New getter for total count

    public long getTotalUserCount() {
        return totalUserCount;
    }

    /**
     * Get current user's role for UI rendering
     */
    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole().name() : null;
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().name().equals(role);
    }

    /**
     * Utility method to validate email format
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Add error message to faces context
     */
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }

    /**
     * Add info message to faces context
     */
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", message));
    }

    // Getters and Setters
    // Getters for the new fields
    public List<Users> getUploaders() {
        return uploaders;
    }

    public List<Users> getUsers() {
        return users;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(String selectedRole) {
        this.selectedRole = selectedRole;
    }

    public Users getCurrentUser() {
        return currentUser;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getFullName() : null;
    }

    public String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : null;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String register() {
        if (email == null || password == null || fullName == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "All fields are required.", null));
            return null;
        }

        try {
            UserRole role = "USER".equals(selectedRole) ? UserRole.USER : UserRole.UPLOADER;
            userService.createUsers(fullName.trim(), email.trim(), password, role);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Registration successful. Please login.", null));

            // Clear fields and switch to login mode
            this.fullName = "";
            this.email = "";
            this.password = "";
            this.actionType = "login";
            return null;

        } catch (IllegalArgumentException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
            return null;
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
            return null;
        }
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Get user by ID with role USER
     */
    public String getUserById(Integer userId) {
        System.out.println("UserID for comment: " + userId);
        try {
            if (userId == null) {
                addErrorMessage("User ID cannot be null.");
                return null;
            }
            Users user = userService.findUserNameById(userId);
            if (user != null && user.getRole() == UserRole.USER && user.getIsActive()) {
                return user.getFullName();
            } else {
                addErrorMessage("User not found or does not have USER role.");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error fetching user by ID.");
            return null;
        }
    }
}
