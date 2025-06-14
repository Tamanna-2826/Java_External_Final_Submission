/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author LENOVO
 */
@Entity
@Table(name = "watch_history")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "WatchHistory.findAll", query = "SELECT w FROM WatchHistory w"),
    @NamedQuery(name = "WatchHistory.findByWatchID", query = "SELECT w FROM WatchHistory w WHERE w.watchID = :watchID"),
    @NamedQuery(name = "WatchHistory.findByUserID", query = "SELECT w FROM WatchHistory w WHERE w.userID = :userID"),
    @NamedQuery(name = "WatchHistory.findByVideoID", query = "SELECT w FROM WatchHistory w WHERE w.videoID = :videoID"),
    @NamedQuery(name = "WatchHistory.findByWatchedAt", query = "SELECT w FROM WatchHistory w WHERE w.watchedAt = :watchedAt"),
    @NamedQuery(name = "WatchHistory.findByWatchduration", query = "SELECT w FROM WatchHistory w WHERE w.watchduration = :watchduration"),
    @NamedQuery(name = "WatchHistory.findByDeviceinfo", query = "SELECT w FROM WatchHistory w WHERE w.deviceinfo = :deviceinfo")})
public class WatchHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Watch_ID")
    private Integer watchID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "User_ID")
    private int userID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Video_ID")
    private int videoID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "WatchedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date watchedAt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Watch_duration")
    private int watchduration;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "Device_info")
    private String deviceinfo;

    public WatchHistory() {
    }

    public WatchHistory(Integer watchID) {
        this.watchID = watchID;
    }

    public WatchHistory(Integer watchID, int userID, int videoID, Date watchedAt, int watchduration, String deviceinfo) {
        this.watchID = watchID;
        this.userID = userID;
        this.videoID = videoID;
        this.watchedAt = watchedAt;
        this.watchduration = watchduration;
        this.deviceinfo = deviceinfo;
    }

    public Integer getWatchID() {
        return watchID;
    }

    public void setWatchID(Integer watchID) {
        this.watchID = watchID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getVideoID() {
        return videoID;
    }

    public void setVideoID(int videoID) {
        this.videoID = videoID;
    }

    public Date getWatchedAt() {
        return watchedAt;
    }

    public void setWatchedAt(Date watchedAt) {
        this.watchedAt = watchedAt;
    }

    public int getWatchduration() {
        return watchduration;
    }

    public void setWatchduration(int watchduration) {
        this.watchduration = watchduration;
    }

    public String getDeviceinfo() {
        return deviceinfo;
    }

    public void setDeviceinfo(String deviceinfo) {
        this.deviceinfo = deviceinfo;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (watchID != null ? watchID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof WatchHistory)) {
            return false;
        }
        WatchHistory other = (WatchHistory) object;
        if ((this.watchID == null && other.watchID != null) || (this.watchID != null && !this.watchID.equals(other.watchID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entities.WatchHistory[ watchID=" + watchID + " ]";
    }
    
}
