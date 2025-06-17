/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package EJB;

import Entities.Comments;
import jakarta.ejb.Stateless;
import jakarta.ejb.LocalBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

/**
 *
 * @author LENOVO
 */
@Stateless
@LocalBean
public class commentBean {

    @PersistenceContext(unitName = "eduott")
    private EntityManager em;

    public void addComment(int videoId, int userId, String commentText) {
        Comments comment = new Comments();
        comment.setVideoID(videoId);
        comment.setUserID(userId);
        comment.setCommentText(commentText);
        comment.setCreatedAt(new Date());
        em.persist(comment);
    }

    public List<Comments> getCommentsByVideoId(int videoId) {
        return em.createQuery("SELECT c FROM Comments c WHERE c.videoID = :vid ORDER BY c.createdAt DESC", Comments.class)
                .setParameter("vid", videoId)
                .getResultList();
    }

    public Comments getLatestCommentFor(int videoId, int userId) {
        List<Comments> results = em.createQuery(
                "SELECT c FROM Comments c WHERE c.videoID = :vid AND c.userID = :uid ORDER BY c.createdAt DESC", Comments.class)
                .setParameter("vid", videoId)
                .setParameter("uid", userId)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? null : results.get(0);
    }
}
