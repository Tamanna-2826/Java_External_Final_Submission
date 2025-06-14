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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author LENOVO
 */
@Entity
@Table(name = "videos_quality")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "VideosQuality.findAll", query = "SELECT v FROM VideosQuality v"),
    @NamedQuery(name = "VideosQuality.findByQualityID", query = "SELECT v FROM VideosQuality v WHERE v.qualityID = :qualityID"),
    @NamedQuery(name = "VideosQuality.findByResolution", query = "SELECT v FROM VideosQuality v WHERE v.resolution = :resolution"),
    @NamedQuery(name = "VideosQuality.findByVideourl", query = "SELECT v FROM VideosQuality v WHERE v.videourl = :videourl"),
    @NamedQuery(name = "VideosQuality.findByFilesize", query = "SELECT v FROM VideosQuality v WHERE v.filesize = :filesize"),
    @NamedQuery(name = "VideosQuality.findByBitrate", query = "SELECT v FROM VideosQuality v WHERE v.bitrate = :bitrate"),
    @NamedQuery(name = "VideosQuality.findByCodec", query = "SELECT v FROM VideosQuality v WHERE v.codec = :codec")})
public class VideosQuality implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Quality_ID")
    private Integer qualityID;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "Resolution")
    private String resolution;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "Video_url")
    private String videourl;
    @Basic(optional = false)
    @NotNull
    @Column(name = "File_size")
    private long filesize;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Bitrate")
    private int bitrate;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "Codec")
    private String codec;
    @JoinColumn(name = "Video_ID", referencedColumnName = "Video_ID")
    @ManyToOne(optional = false)
    private Videos videoID;

    public VideosQuality() {
    }

    public VideosQuality(Integer qualityID) {
        this.qualityID = qualityID;
    }

    public VideosQuality(Integer qualityID, String resolution, String videourl, long filesize, int bitrate, String codec) {
        this.qualityID = qualityID;
        this.resolution = resolution;
        this.videourl = videourl;
        this.filesize = filesize;
        this.bitrate = bitrate;
        this.codec = codec;
    }

    public Integer getQualityID() {
        return qualityID;
    }

    public void setQualityID(Integer qualityID) {
        this.qualityID = qualityID;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getVideourl() {
        return videourl;
    }

    public void setVideourl(String videourl) {
        this.videourl = videourl;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
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
        hash += (qualityID != null ? qualityID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof VideosQuality)) {
            return false;
        }
        VideosQuality other = (VideosQuality) object;
        if ((this.qualityID == null && other.qualityID != null) || (this.qualityID != null && !this.qualityID.equals(other.qualityID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entities.VideosQuality[ qualityID=" + qualityID + " ]";
    }
    
}
