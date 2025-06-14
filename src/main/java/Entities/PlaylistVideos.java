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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author LENOVO
 */
@Entity
@Table(name = "playlist_videos")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PlaylistVideos.findAll", query = "SELECT p FROM PlaylistVideos p"),
    @NamedQuery(name = "PlaylistVideos.findByPlaylistVideoID", query = "SELECT p FROM PlaylistVideos p WHERE p.playlistVideoID = :playlistVideoID"),
    @NamedQuery(name = "PlaylistVideos.findByCreatedAt", query = "SELECT p FROM PlaylistVideos p WHERE p.createdAt = :createdAt")})
public class PlaylistVideos implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Playlist_Video_ID")
    private Integer playlistVideoID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CreatedAt")
    @Temporal(TemporalType.DATE)
    private Date createdAt;
    @JoinColumn(name = "Playlist_ID", referencedColumnName = "Playlist_ID")
    @ManyToOne(optional = false)
    private Playlists playlistID;
    @JoinColumn(name = "Video_ID", referencedColumnName = "Video_ID")
    @ManyToOne(optional = false)
    private Videos videoID;

    public PlaylistVideos() {
    }

    public PlaylistVideos(Integer playlistVideoID) {
        this.playlistVideoID = playlistVideoID;
    }

    public PlaylistVideos(Integer playlistVideoID, Date createdAt) {
        this.playlistVideoID = playlistVideoID;
        this.createdAt = createdAt;
    }

    public Integer getPlaylistVideoID() {
        return playlistVideoID;
    }

    public void setPlaylistVideoID(Integer playlistVideoID) {
        this.playlistVideoID = playlistVideoID;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Playlists getPlaylistID() {
        return playlistID;
    }

    public void setPlaylistID(Playlists playlistID) {
        this.playlistID = playlistID;
    }

    public Videos getVideoID() {
        return videoID;
    }

    public void setVideoID(Videos videoID) {
        this.videoID = videoID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (playlistVideoID != null ? playlistVideoID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PlaylistVideos)) {
            return false;
        }
        PlaylistVideos other = (PlaylistVideos) object;
        if ((this.playlistVideoID == null && other.playlistVideoID != null) || (this.playlistVideoID != null && !this.playlistVideoID.equals(other.playlistVideoID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entities.PlaylistVideos[ playlistVideoID=" + playlistVideoID + " ]";
    }
    
}
