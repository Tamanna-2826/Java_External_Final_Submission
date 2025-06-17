/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package EJB;

import Entities.Likes;
import Entities.Users;
import Entities.Videos;
import jakarta.ejb.Stateless;
import jakarta.ejb.LocalBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

/**
 *
 * @author LENOVO
 */
@Stateless
@LocalBean
public class LikesBean {

    @PersistenceContext(unitName = "eduott")
    private EntityManager em;

    public boolean likeVideo(int userId, int videoId) {
        // Check if the like already exists to avoid duplicates
        Long count = em.createQuery("SELECT COUNT(l) FROM Likes l WHERE l.userID.userID = :uid AND l.videoID.videoID = :vid", Long.class)
                .setParameter("uid", userId)
                .setParameter("vid", videoId)
                .getSingleResult();

        if (count == 0) {
            Likes like = new Likes();
            like.setUserID(em.find(Users.class, userId));
            like.setVideoID(em.find(Videos.class, videoId));
            like.setCreatedAt(new Date());
            em.persist(like);
            return true;
        }
        return false;
    }

    public boolean hasUserLiked(int userId, int videoId) {
        Long count = em.createQuery(
                "SELECT COUNT(l) FROM Likes l WHERE l.userID.userID = :uid AND l.videoID.videoID = :vid",
                Long.class
        )
                .setParameter("uid", userId)
                .setParameter("vid", videoId)
                .getSingleResult();

        return count > 0;
    }

    public boolean toggleLike(int userId, int videoId) {
        TypedQuery<Likes> query = em.createQuery(
                "SELECT l FROM Likes l WHERE l.userID.userID = :uid AND l.videoID.videoID = :vid",
                Likes.class
        );
        query.setParameter("uid", userId);
        query.setParameter("vid", videoId);

        List<Likes> existingLikes = query.getResultList();

        if (!existingLikes.isEmpty()) {
            // Unlike (delete existing like)
            for (Likes like : existingLikes) {
                em.remove(like);
            }
            return false; // Indicating "unliked"
        } else {
            // Like (add new)
            Likes like = new Likes();
            like.setUserID(em.find(Users.class, userId));
            like.setVideoID(em.find(Videos.class, videoId));
            like.setCreatedAt(new Date());
            em.persist(like);
            return true; // Indicating "liked"
        }
    }

    public long countLikes(int videoId) {
        return em.createQuery("SELECT COUNT(l) FROM Likes l WHERE l.videoID.videoID = :videoId", Long.class)
                .setParameter("videoId", videoId)
                .getSingleResult();
    }

    public boolean hasUserLikedVideo(int userId, int videoId) {
        Long count = em.createQuery("SELECT COUNT(l) FROM Likes l WHERE l.userID.userID = :uid AND l.videoID.videoID = :vid", Long.class)
                .setParameter("uid", userId)
                .setParameter("vid", videoId)
                .getSingleResult();
        return count > 0;
    }
}
