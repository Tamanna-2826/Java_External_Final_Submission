/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities;
import Converters.UserRoleConverter;
import Enums.UserRole;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author LENOVO
 */
@Entity
@Table(name = "users")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Users.findAll", query = "SELECT u FROM Users u"),
    @NamedQuery(name = "Users.findByUserID", query = "SELECT u FROM Users u WHERE u.userID = :userID"),
    @NamedQuery(name = "Users.findByFullName", query = "SELECT u FROM Users u WHERE u.fullName = :fullName"),
    @NamedQuery(name = "Users.findByEmail", query = "SELECT u FROM Users u WHERE u.email = :email"),
    @NamedQuery(name = "Users.findByPasswordHash", query = "SELECT u FROM Users u WHERE u.passwordHash = :passwordHash"),
    @NamedQuery(name = "Users.findByRole", query = "SELECT u FROM Users u WHERE u.role = :role"),
    @NamedQuery(name = "Users.findBySubscriptionStatus", query = "SELECT u FROM Users u WHERE u.subscriptionStatus = :subscriptionStatus"),
    @NamedQuery(name = "Users.findByCreatedAt", query = "SELECT u FROM Users u WHERE u.createdAt = :createdAt")})
public class Users implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "FullName")
    private String fullName;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Basic(optional = false)
    @NotNull()
    @Size(min = 1, max = 50)
    @Column(name = "Email")
    private String email;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "PasswordHash")
    private String passwordHash;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "Role")
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @Basic(optional = false)
    @NotNull
    @Column(name = "SubscriptionStatus")
    private boolean subscriptionStatus;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CreatedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "isActive")
    private Boolean isActive;
    @Column(name = "updatedAt")
    @Temporal(TemporalType.DATE)
    private Date updatedAt;
    @Size(max = 10)
    @Column(name = "phoneNo")
    private String phoneNo;
    @Column(name = "loginAttempts")
    private Integer loginAttempts;
    @Column(name = "accountLocked")
    private Boolean accountLocked;
    @Column(name = "lockTime")
    @Temporal(TemporalType.DATE)
    private Date lockTime;
    @Column(name = "lastLogin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "User_ID")
    private Integer userID;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "reportedBy")
    private Collection<Reports> reportsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userID")
    private Collection<Playlists> playlistsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userID")
    private Collection<Videos> videosCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userID")
    private Collection<Likes> likesCollection;

    public Users() {
    }

    public Users(Integer userID) {
        this.userID = userID;
    }

    public Users(Integer userID, String fullName, String email, String passwordHash, UserRole role, boolean subscriptionStatus, Date createdAt) {
        this.userID = userID;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.subscriptionStatus = subscriptionStatus;
        this.createdAt = createdAt;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    @JsonbTransient
    public Collection<Reports> getReportsCollection() {
        return reportsCollection;
    }

    public void setReportsCollection(Collection<Reports> reportsCollection) {
        this.reportsCollection = reportsCollection;
    }

    @JsonbTransient
    public Collection<Playlists> getPlaylistsCollection() {
        return playlistsCollection;
    }

    public void setPlaylistsCollection(Collection<Playlists> playlistsCollection) {
        this.playlistsCollection = playlistsCollection;
    }

    @JsonbTransient
    public Collection<Videos> getVideosCollection() {
        return videosCollection;
    }

    public void setVideosCollection(Collection<Videos> videosCollection) {
        this.videosCollection = videosCollection;
    }

    @JsonbTransient
    public Collection<Likes> getLikesCollection() {
        return likesCollection;
    }

    public void setLikesCollection(Collection<Likes> likesCollection) {
        this.likesCollection = likesCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userID != null ? userID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Users)) {
            return false;
        }
        Users other = (Users) object;
        if ((this.userID == null && other.userID != null) || (this.userID != null && !this.userID.equals(other.userID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entities.Users[ userID=" + userID + " ]";
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(boolean subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public Integer getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(Integer loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public Date getLockTime() {
        return lockTime;
    }

    public void setLockTime(Date lockTime) {
        this.lockTime = lockTime;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = new Date();
    }

    // Business methods
    public boolean isAdmin() {
        return role != null && role.isAdmin();

    }

    public boolean canUpload() {
        return role != null && role.canUpload();
    }

    public void updateLastLogin() {
        this.lastLogin = new Date();
        this.loginAttempts = 0; // Reset login attempts on successful login
    }

    public void incrementLoginAttempts() {
        this.loginAttempts++;
    }

    public void lockAccount() {
        this.accountLocked = true;
        this.lockTime = new Date();
    }

    public void unlockAccount() {
        this.accountLocked = false;
        this.lockTime = null;
        this.loginAttempts = 0;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

}
