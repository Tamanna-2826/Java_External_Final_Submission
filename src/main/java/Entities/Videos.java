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
@Table(name = "videos")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Videos.findAll", query = "SELECT v FROM Videos v"),
    @NamedQuery(name = "Videos.findByVideoID", query = "SELECT v FROM Videos v WHERE v.videoID = :videoID"),
    @NamedQuery(name = "Videos.findByTitle", query = "SELECT v FROM Videos v WHERE v.title = :title"),
    @NamedQuery(name = "Videos.findByDescription", query = "SELECT v FROM Videos v WHERE v.description = :description"),
    @NamedQuery(name = "Videos.findByVideourl", query = "SELECT v FROM Videos v WHERE v.videourl = :videourl"),
    @NamedQuery(name = "Videos.findByThumbnailurl", query = "SELECT v FROM Videos v WHERE v.thumbnailurl = :thumbnailurl"),
    @NamedQuery(name = "Videos.findByUploaddate", query = "SELECT v FROM Videos v WHERE v.uploaddate = :uploaddate"),
    @NamedQuery(name = "Videos.findByViewscount", query = "SELECT v FROM Videos v WHERE v.viewscount = :viewscount"),
    @NamedQuery(name = "Videos.findByDuration", query = "SELECT v FROM Videos v WHERE v.duration = :duration"),
    @NamedQuery(name = "Videos.findByQuality", query = "SELECT v FROM Videos v WHERE v.quality = :quality"),
    @NamedQuery(name = "Videos.findByStatus", query = "SELECT v FROM Videos v WHERE v.status = :status"),
    @NamedQuery(name = "Videos.findByIspremium", query = "SELECT v FROM Videos v WHERE v.ispremium = :ispremium")})
public class Videos implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Video_ID")
    private Integer videoID;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 1000)
    @Column(name = "Title")
    private String title;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 350)
    @Column(name = "Description")
    private String description;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 500)
    @Column(name = "Video_url")
    private String videourl;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 500)
    @Column(name = "Thumbnail_url")
    private String thumbnailurl;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Upload_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date uploaddate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Views_count")
    private int viewscount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Duration")
    private int duration;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "Quality")
    private String quality;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "Status")
    private String status;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Is_premium")
    private boolean ispremium;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "videoId")
    private Collection<Reports> reportsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "videoID")
    private Collection<VideosQuality> videosQualityCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "videoID")
    private Collection<PlaylistVideos> playlistVideosCollection;
    @JoinColumn(name = "Category_ID", referencedColumnName = "Category_ID")
    @ManyToOne(optional = false)
    private Categories categoryID;
    @JoinColumn(name = "User_ID", referencedColumnName = "User_ID")
    @ManyToOne(optional = false)
    private Users userID;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "videoID")
    private Collection<Likes> likesCollection;

    public Videos() {
    }

    public Videos(Integer videoID) {
        this.videoID = videoID;
    }

    public Videos(Integer videoID, String title, String description, String videourl, String thumbnailurl, Date uploaddate, int viewscount, int duration, String quality, String status, boolean ispremium) {
        this.videoID = videoID;
        this.title = title;
        this.description = description;
        this.videourl = videourl;
        this.thumbnailurl = thumbnailurl;
        this.uploaddate = uploaddate;
        this.viewscount = viewscount;
        this.duration = duration;
        this.quality = quality;
        this.status = status;
        this.ispremium = ispremium;
    }

    public Integer getVideoID() {
        return videoID;
    }

    public void setVideoID(Integer videoID) {
        this.videoID = videoID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideourl() {
        return videourl;
    }

    public void setVideourl(String videourl) {
        this.videourl = videourl;
    }

    public String getThumbnailurl() {
        return thumbnailurl;
    }

    public void setThumbnailurl(String thumbnailurl) {
        this.thumbnailurl = thumbnailurl;
    }

    public Date getUploaddate() {
        return uploaddate;
    }

    public void setUploaddate(Date uploaddate) {
        this.uploaddate = uploaddate;
    }

    public int getViewscount() {
        return viewscount;
    }

    public void setViewscount(int viewscount) {
        this.viewscount = viewscount;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean getIspremium() {
        return ispremium;
    }

    public void setIspremium(boolean ispremium) {
        this.ispremium = ispremium;
    }

    @JsonbTransient
    public Collection<Reports> getReportsCollection() {
        return reportsCollection;
    }

    public void setReportsCollection(Collection<Reports> reportsCollection) {
        this.reportsCollection = reportsCollection;
    }

    @JsonbTransient
    public Collection<VideosQuality> getVideosQualityCollection() {
        return videosQualityCollection;
    }

    public void setVideosQualityCollection(Collection<VideosQuality> videosQualityCollection) {
        this.videosQualityCollection = videosQualityCollection;
    }

    @JsonbTransient
    public Collection<PlaylistVideos> getPlaylistVideosCollection() {
        return playlistVideosCollection;
    }

    public void setPlaylistVideosCollection(Collection<PlaylistVideos> playlistVideosCollection) {
        this.playlistVideosCollection = playlistVideosCollection;
    }

    public Categories getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(Categories categoryID) {
        this.categoryID = categoryID;
    }

    public Users getUserID() {
        return userID;
    }

    public void setUserID(Users userID) {
        this.userID = userID;
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
        hash += (videoID != null ? videoID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Videos)) {
            return false;
        }
        Videos other = (Videos) object;
        if ((this.videoID == null && other.videoID != null) || (this.videoID != null && !this.videoID.equals(other.videoID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entities.Videos[ videoID=" + videoID + " ]";
    }
    
}
