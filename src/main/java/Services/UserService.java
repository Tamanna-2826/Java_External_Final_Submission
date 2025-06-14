/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Services;

import Entities.Users;
import Enums.UserRole;
import Utilities.PasswordUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 *
 * @author LENOVO
 */
@ApplicationScoped
@Transactional
public class UserService {

    @PersistenceContext(unitName = "eduott")
    private EntityManager entityManager;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_HOURS = 1;

    /**
     * Find user by email
     */
    public Users findByEmail(String email) {
        try {
            TypedQuery<Users> query = entityManager.createNamedQuery("Users.findByEmail", Users.class);
            query.setParameter("email", email.toLowerCase().trim());
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find user by ID
     */
    public Users findById(Long id) {
        return entityManager.find(Users.class, id);
    }

    /**
     * Get all users with specific role
     */
    public List<Users> findByRole(UserRole role) {
        TypedQuery<Users> query = entityManager.createNamedQuery("Users.findByRole", Users.class);
        query.setParameter("role", role);
        return query.getResultList();
    }

    /**
     * Get all active users
     */
    public List<Users> findActiveUserss() {
        TypedQuery<Users> query = entityManager.createNamedQuery("Users.findActiveUserss", Users.class);
        return query.getResultList();
    }

    /**
     * Get all users with pagination
     */
    public List<Users> findAll(int offset, int limit) {
        TypedQuery<Users> query = entityManager.createQuery("SELECT u FROM Users u ORDER BY u.createdAt DESC", Users.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    /**
     * Get total user count
     */
    public long getTotalUsersCount() {
        TypedQuery<Long> query = entityManager.createQuery("SELECT COUNT(u) FROM Users u", Long.class);
        return query.getSingleResult();
    }

    /**
     * Create new user
     */
    public Users createUsers(String name, String email, String password, UserRole role) {
        // Check if user already exists
        if (findByEmail(email) != null) {
            throw new IllegalArgumentException("Users with this email already exists");
        }

        // Validate password
        if (!PasswordUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Password does not meet security requirements");
        }

        // Create user
        Users user = new Users();
        user.setFullName(name);
        user.setEmail(email.toLowerCase().trim());
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedAt(new Date());

        entityManager.persist(user);
        entityManager.flush();

        return user;
    }

    /**
     * Update user
     */
    public Users updateUsers(Users user) {
        user.setUpdatedAt(new Date());
        return entityManager.merge(user);
    }

    /**
     * Update user password
     */
    public boolean updatePassword(Long userId, String currentPassword, String newPassword) {
        Users user = findById(userId);
        if (user == null) {
            return false;
        }

        // Verify current password
        if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
            return false;
        }

        // Validate new password
        if (!PasswordUtil.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("New password does not meet security requirements");
        }

        // Update password
        user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        entityManager.merge(user);

        return true;
    }

    /**
     * Reset user password (admin function)
     */
    public String resetPassword(Long userId) {
        Users user = findById(userId);
        if (user == null) {
            return null;
        }

        // Generate temporary password
        String tempPassword = PasswordUtil.generateRandomPassword(12);
        user.setPasswordHash(PasswordUtil.hashPassword(tempPassword));
        entityManager.merge(user);

        return tempPassword;
    }

    /**
     * Activate user account
     */
    public boolean activateUsers(Long userId) {
        Users user = findById(userId);
        if (user != null) {
            user.setIsActive(true);
            user.unlockAccount();
            updateUsers(user);
            return true;
        }
        return false;
    }

    /**
     * Deactivate user account
     */
    public boolean deactivateUsers(Long userId) {
        Users user = findById(userId);
        if (user != null) {
            user.setIsActive(false);
            updateUsers(user);
            return true;
        }
        return false;
    }

    /**
     * Delete user (soft delete by deactivating)
     */
    public boolean deleteUsers(Long userId) {
        return deactivateUsers(userId);
    }

//    /**
//     * Update last login time
//     */
    public void updateLastLogin(Long userId) {
        Users user = findById(userId);
        if (user != null) {
            user.updateLastLogin();
            entityManager.merge(user);
        }
    }

    /**
     * Handle failed login attempt
     */
    public void handleFailedLogin(String email) {
        Users user = findByEmail(email);
        if (user != null) {
            user.incrementLoginAttempts();

            // Lock account if max attempts reached
            if (user.getLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
                user.lockAccount();
            }

            entityManager.merge(user);
        }
    }

    /**
     * Check if account should be unlocked (after lock duration)
     */
    public boolean shouldUnlockAccount(Users user) {
        if (!user.isAccountLocked() || user.getLockTime() == null) {
            return false;
        }

        // Add 1 day (24 * 60 * 60 * 1000 milliseconds) to lockTime
        long lockTimeMillis = user.getLockTime().getTime();
        long unlockTimeMillis = lockTimeMillis + (1 * 24 * 60 * 60 * 1000L); // 1 day = 86400000 ms

        Date unlockTime = new Date(unlockTimeMillis);
        Date now = new Date();

        return now.after(unlockTime);
    }

    /**
     * Unlock account if lock duration has passed
     */
    public void checkAndUnlockAccount(String email) {
        Users user = findByEmail(email);
        if (user != null && shouldUnlockAccount(user)) {
            user.unlockAccount();
            entityManager.merge(user);
        }
    }

    /**
     * Search users by name or email
     */
    public List<Users> searchUserss(String searchTerm, int offset, int limit) {
        String jpql = "SELECT u FROM Users u WHERE "
                + "LOWER(u.name) LIKE LOWER(:searchTerm) OR "
                + "LOWER(u.email) LIKE LOWER(:searchTerm) "
                + "ORDER BY u.name";

        TypedQuery<Users> query = entityManager.createQuery(jpql, Users.class);
        query.setParameter("searchTerm", "%" + searchTerm + "%");
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }

    /**
     * Get user statistics
     */
    public UsersStats getUsersStats() {
        UsersStats stats = new UsersStats();

        // Total users
        stats.totalUserss = getTotalUsersCount();

        // Active users
        TypedQuery<Long> activeQuery = entityManager.createQuery(
                "SELECT COUNT(u) FROM Users u WHERE u.active = true", Long.class);
        stats.activeUserss = activeQuery.getSingleResult();

        // Userss by role
        for (UserRole role : UserRole.values()) {
            TypedQuery<Long> roleQuery = entityManager.createQuery(
                    "SELECT COUNT(u) FROM Users u WHERE u.role = :role", Long.class);
            roleQuery.setParameter("role", role);
            stats.usersByRole.put(role, roleQuery.getSingleResult());
        }

        return stats;
    }

    /**
     * Inner class for user statistics
     */
    public static class UsersStats {

        public long totalUserss;
        public long activeUserss;
        public java.util.Map<UserRole, Long> usersByRole = new java.util.HashMap<>();
    }
}
