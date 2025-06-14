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
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author LENOVO
 */
@Entity
@Table(name = "reports")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Reports.findAll", query = "SELECT r FROM Reports r"),
    @NamedQuery(name = "Reports.findByReportId", query = "SELECT r FROM Reports r WHERE r.reportId = :reportId"),
    @NamedQuery(name = "Reports.findByReason", query = "SELECT r FROM Reports r WHERE r.reason = :reason"),
    @NamedQuery(name = "Reports.findByCrtaedAt", query = "SELECT r FROM Reports r WHERE r.crtaedAt = :crtaedAt")})
public class Reports implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "reportId")
    private Integer reportId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "reason")
    private String reason;
    @Basic(optional = false)
    @NotNull
    @Column(name = "crtaedAt")
    @Temporal(TemporalType.DATE)
    private Date crtaedAt;
    @JoinColumn(name = "videoId", referencedColumnName = "Video_ID")
    @ManyToOne(optional = false)
    private Videos videoId;
    @JoinColumn(name = "reportedBy", referencedColumnName = "User_ID")
    @ManyToOne(optional = false)
    private Users reportedBy;

    public Reports() {
    }

    public Reports(Integer reportId) {
        this.reportId = reportId;
    }

    public Reports(Integer reportId, String reason, Date crtaedAt) {
        this.reportId = reportId;
        this.reason = reason;
        this.crtaedAt = crtaedAt;
    }

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getCrtaedAt() {
        return crtaedAt;
    }

    public void setCrtaedAt(Date crtaedAt) {
        this.crtaedAt = crtaedAt;
    }

    public Videos getVideoId() {
        return videoId;
    }

    public void setVideoId(Videos videoId) {
        this.videoId = videoId;
    }

    public Users getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Users reportedBy) {
        this.reportedBy = reportedBy;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (reportId != null ? reportId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Reports)) {
            return false;
        }
        Reports other = (Reports) object;
        if ((this.reportId == null && other.reportId != null) || (this.reportId != null && !this.reportId.equals(other.reportId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entities.Reports[ reportId=" + reportId + " ]";
    }
    
}
