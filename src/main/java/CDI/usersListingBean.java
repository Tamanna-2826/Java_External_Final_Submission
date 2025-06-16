/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package CDI;

import Entities.Users;
import Entities.WatchHistory;
import Enums.UserRole;
import Services.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author LENOVO
 */
@Named(value = "usersListingBean")
@SessionScoped
public class usersListingBean implements Serializable {

    private List<Users> users;
    private List<Users> filteredUsers; // For search functionality
    private Users selectedUser; // For detailed view
    private String searchTerm; // For search input
    private List<UserActivityDisplayModel> userWatchHistory; // For detailed view of user's watch 

    @Inject
    private UserService userService;
    private EntityManager em;

    /**
     * Creates a new instance of usersListingBean
     */
    @PostConstruct
    public void init() {
        fetchUsers();
    }

    public usersListingBean() {
    }

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

    public void searchUsers() {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            filteredUsers = new ArrayList<>(users);
            return;
        }

        filteredUsers = new ArrayList<>();
        String lowerSearchTerm = searchTerm.toLowerCase();
        for (Users user : users) {
            if (user.getFullName().toLowerCase().contains(lowerSearchTerm)
                    || user.getEmail().toLowerCase().contains(lowerSearchTerm)) {
                filteredUsers.add(user);
            }
        }
    }

    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", message));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }

    public void viewUserDetails(Users user) {
        this.selectedUser = user;
        loadUserWatchHistory();
        FacesContext.getCurrentInstance().getPartialViewContext().getEvalScripts().add("PF('userDetailsDialog').show();");
    }

    private void loadUserWatchHistory() {
        userWatchHistory = new ArrayList<>();
        if (selectedUser != null) {
            TypedQuery<WatchHistory> query = em.createQuery(
                    "SELECT w FROM WatchHistory w WHERE w.userID = :userID ORDER BY w.watchedAt DESC",
                    WatchHistory.class
            );
            query.setParameter("userID", selectedUser.getUserID());
            List<WatchHistory> watchHistoryList = query.getResultList();

            for (WatchHistory watch : watchHistoryList) {
                UserActivityDisplayModel model = new UserActivityDisplayModel();
                model.setWatchHistory(watch);
                model.setFormattedWatchedAt(formatDate(watch.getWatchedAt()));
                userWatchHistory.add(model);
            }
        }
    }

    // Get total videos watched by user
    public Long getTotalVideosWatched(Users user) {
        if (user == null) {
            return 0L;
        }
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(w) FROM WatchHistory w WHERE w.userID = :userID",
                Long.class
        );
        query.setParameter("userID", user.getUserID());
        return query.getSingleResult();
    }

    // Get total likes given by user
    public Long getTotalLikesGiven(Users user) {
        if (user == null) {
            return 0L;
        }
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(l) FROM Likes l WHERE l.userID = :userID",
                Long.class
        );
        query.setParameter("userID", user);
        return query.getSingleResult();
    }

    // Get total comments made by user
    public Long getTotalCommentsMade(Users user) {
        if (user == null) {
            return 0L;
        }
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Comments c WHERE c.userID = :userID",
                Long.class
        );
        query.setParameter("userID", user.getUserID());
        return query.getSingleResult();
    }

    // Utility method to format view counts
    public String formatCount(long count) {
        if (count >= 1000000) {
            return String.format("%.1fM", count / 1000000.0);
        } else if (count >= 1000) {
            return String.format("%.1fK", count / 1000.0);
        } else {
            return String.valueOf(count);
        }
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        return sdf.format(date);
    }

    // Getters and Setters
    public List<Users> getUsers() {
        return users;
    }

    public List<Users> getFilteredUsers() {
        return filteredUsers;
    }

    public void setFilteredUsers(List<Users> filteredUsers) {
        this.filteredUsers = filteredUsers;
    }

    public Users getSelectedUser() {
        return selectedUser;
    }

    public List<UserActivityDisplayModel> getUserWatchHistory() {
        return userWatchHistory;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public static class UserActivityDisplayModel implements Serializable {

        private WatchHistory watchHistory;
        private String formattedWatchedAt;

        public WatchHistory getWatchHistory() {
            return watchHistory;
        }

        public void setWatchHistory(WatchHistory watchHistory) {
            this.watchHistory = watchHistory;
        }

        public String getFormattedWatchedAt() {
            return formattedWatchedAt;
        }

        public void setFormattedWatchedAt(String formattedWatchedAt) {
            this.formattedWatchedAt = formattedWatchedAt;
        }

        public int getVideoID() {
            return watchHistory.getVideoID();
        }

        public int getWatchDuration() {
            return watchHistory.getWatchduration();
        }

        public String getDeviceInfo() {
            return watchHistory.getDeviceinfo();
        }
    }
}
