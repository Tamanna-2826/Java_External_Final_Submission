/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package Enums;

/**
 *
 * @author LENOVO
 */
public enum UserRole {
    ADMIN("admin", "System Administrator with full access"),
    UPLOADER("uploader", "Content uploader with limited access"),
    USER("user", "Regular user with basic access");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this role has admin privileges
     */
    public boolean isAdmin() {
        return this == ADMIN || this == ADMIN;
    }

    /**
     * Check if this role can upload content
     */
    public boolean canUpload() {
        return this == UPLOADER || this == ADMIN;
    }

    /**
     * Check if this role has higher priority than another role
     */
    public boolean hasHigherPriorityThan(UserRole other) {
        return this.getPriority() > other.getPriority();
    }

    /**
     * Get priority level of the role
     */
    public int getPriority() {
        switch (this) {
            case ADMIN:
                return 1;
            case UPLOADER:
                return 2;
            case USER:
                return 3;
            default:
                return 0;
        }
    }

    /**
     * Get role by name (case insensitive)
     */
    public static UserRole fromString(String role) {
        if (role == null) {
            return null;
        }

        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
