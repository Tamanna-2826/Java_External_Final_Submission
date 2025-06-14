/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
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
@Table(name = "playlists")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Playlists.findAll", query = "SELECT p FROM Playlists p"),
    @NamedQuery(name = "Playlists.findByPlaylistID", query = "SELECT p FROM Playlists p WHERE p.playlistID = :playlistID"),
    @NamedQuery(name = "Playlists.findByName", query = "SELECT p FROM Playlists p WHERE p.name = :name"),
    @NamedQuery(name = "Playlists.findByIsPublic", query = "SELECT p FROM Playlists p WHERE p.isPublic = :isPublic"),
    @NamedQuery(name = "Playlists.findByCreatedAt", query = "SELECT p FROM Playlists p WHERE p.createdAt = :createdAt")})
public class Playlists implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Playlist_ID")
    private Integer playlistID;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "Name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 65535)
    @Column(name = "Description")
    private String description;
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsPublic")
    private boolean isPublic;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CreatedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @JoinColumn(name = "User_ID", referencedColumnName = "User_ID")
    @ManyToOne(optional = false)
    private Users userID;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "playlistID")
    private Collection<PlaylistVideos> playlistVideosCollection;

    public Playlists() {
    }

    public Playlists(Integer playlistID) {
        this.playlistID = playlistID;
    }

    public Playlists(Integer playlistID, String name, String description, boolean isPublic, Date createdAt) {
        this.playlistID = playlistID;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
    }

    public Integer getPlaylistID() {
        return playlistID;
    }

    public void setPlaylistID(Integer playlistID) {
        this.playlistID = playlistID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Users getUserID() {
        return userID;
    }

    public void setUserID(Users userID) {
        this.userID = userID;
    }

    @JsonbTransient
    public Collection<PlaylistVideos> getPlaylistVideosCollection() {
        return playlistVideosCollection;
    }

    public void setPlaylistVideosCollection(Collection<PlaylistVideos> playlistVideosCollection) {
        this.playlistVideosCollection = playlistVideosCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (playlistID != null ? playlistID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Playlists)) {
            return false;
        }
        Playlists other = (Playlists) object;
        if ((this.playlistID == null && other.playlistID != null) || (this.playlistID != null && !this.playlistID.equals(other.playlistID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entities.Playlists[ playlistID=" + playlistID + " ]";
    }
    
}
