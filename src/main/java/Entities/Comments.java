/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Basic;
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
@Table(name = "comments")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Comments.findAll", query = "SELECT c FROM Comments c"),
    @NamedQuery(name = "Comments.findByCommentsID", query = "SELECT c FROM Comments c WHERE c.commentsID = :commentsID"),
    @NamedQuery(name = "Comments.findByVideoID", query = "SELECT c FROM Comments c WHERE c.videoID = :videoID"),
    @NamedQuery(name = "Comments.findByUserID", query = "SELECT c FROM Comments c WHERE c.userID = :userID"),
    @NamedQuery(name = "Comments.findByCreatedAt", query = "SELECT c FROM Comments c WHERE c.createdAt = :createdAt")})
public class Comments implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Comments_ID")
    private Integer commentsID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Video_ID")
    private int videoID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "User_ID")
    private int userID;
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 65535)
    @Column(name = "CommentText")
    private String commentText;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CreatedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @OneToMany(mappedBy = "parentCommentID")
    private Collection<Comments> commentsCollection;
    @JoinColumn(name = "ParentCommentID", referencedColumnName = "Comments_ID")
    @ManyToOne
    private Comments parentCommentID;

    public Comments() {
    }

    public Comments(Integer commentsID) {
        this.commentsID = commentsID;
    }

    public Comments(Integer commentsID, int videoID, int userID, String commentText, Date createdAt) {
        this.commentsID = commentsID;
        this.videoID = videoID;
        this.userID = userID;
        this.commentText = commentText;
        this.createdAt = createdAt;
    }

    public Integer getCommentsID() {
        return commentsID;
    }

    public void setCommentsID(Integer commentsID) {
        this.commentsID = commentsID;
    }

    public int getVideoID() {
        return videoID;
    }

    public void setVideoID(int videoID) {
        this.videoID = videoID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @JsonbTransient
    public Collection<Comments> getCommentsCollection() {
        return commentsCollection;
    }

    public void setCommentsCollection(Collection<Comments> commentsCollection) {
        this.commentsCollection = commentsCollection;
    }

    public Comments getParentCommentID() {
        return parentCommentID;
    }

    public void setParentCommentID(Comments parentCommentID) {
        this.parentCommentID = parentCommentID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (commentsID != null ? commentsID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Comments)) {
            return false;
        }
        Comments other = (Comments) object;
        if ((this.commentsID == null && other.commentsID != null) || (this.commentsID != null && !this.commentsID.equals(other.commentsID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entities.Comments[ commentsID=" + commentsID + " ]";
    }
    
}
