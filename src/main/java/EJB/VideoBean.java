/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package EJB;

import Entities.Categories;
import Entities.PlaylistVideos;
import Entities.Playlists;
import Entities.Users;
import Entities.Videos;
import Entities.WatchHistory;
import jakarta.ejb.Stateless;
import jakarta.ejb.LocalBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author LENOVO
 */
@Stateless
@LocalBean
public class VideoBean {

    @PersistenceContext(unitName = "eduott")
    EntityManager em;

    // Video CRUD Operations
    public void createVideo(Videos video) {
        em.persist(video);
    }

    public Videos findVideo(Integer videoId) {
        return em.find(Videos.class, videoId);
    }

    public void updateVideo(Videos video) {
        em.merge(video);
    }

    public void deleteVideo(Videos video) {
        em.remove(em.merge(video));
    }

    // Get videos by uploader (user)
    public List<Videos> getVideosByUploader(Users uploader) {
        TypedQuery<Videos> query = em.createQuery(
                "SELECT v FROM Videos v WHERE v.userID = :uploader ORDER BY v.uploaddate DESC",
                Videos.class
        );
        query.setParameter("uploader", uploader);
        return query.getResultList();
    }

    // Get video count by uploader
    public Long getTotalVideosByUploader(Users uploader) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(v) FROM Videos v WHERE v.userID = :uploader",
                Long.class
        );
        query.setParameter("uploader", uploader);
        return query.getSingleResult();
    }

    // Get total views by uploader
    public Long getTotalViewsByUploader(Users uploader) {
        TypedQuery<Object> query = em.createQuery(
                "SELECT COALESCE(SUM(v.viewscount), 0) FROM Videos v WHERE v.userID = :uploader",
                Object.class
        );
        query.setParameter("uploader", uploader);
        Object result = query.getSingleResult();

        // Handle both BigDecimal and Long return types
        if (result instanceof BigDecimal) {
            return ((BigDecimal) result).longValue();
        } else if (result instanceof Long) {
            return (Long) result;
        } else if (result instanceof Integer) {
            return ((Integer) result).longValue();
        } else {
            return 0L;
        }
    }

    // Get total likes by uploader
    public Long getTotalLikesByUploader(Users uploader) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(l) FROM Likes l JOIN l.videoID v WHERE v.userID = :uploader",
                Long.class
        );
        query.setParameter("uploader", uploader);
        return query.getSingleResult();
    }

    // Get total comments by uploader (assuming you have Comments entity)
    public Long getTotalCommentsByUploader(Users uploader) {
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(c) FROM Comments c JOIN c.videoID v WHERE v.userID = :uploader",
                    Long.class
            );
            query.setParameter("uploader", uploader);
            return query.getSingleResult();
        } catch (Exception e) {
            // If Comments entity doesn't exist, return 0
            return 0L;
        }
    }

    // Get views analytics for the last 7 days
    public Map<String, Integer> getViewsAnalytics(Users uploader) {
        Map<String, Integer> analytics = new HashMap<>();
        Calendar cal = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, -i);
            Date startDate = cal.getTime();

            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date endDate = cal.getTime();

            // This is a simplified version - you might need to track daily views separately
            // For now, we'll return sample data
            String dayKey = String.format("Jan %d", 7 - i);
            analytics.put(dayKey, 250 + (int) (Math.random() * 500));
        }

        return analytics;
    }

    // Playlist Operations
    public void createPlaylist(Playlists playlist) {
        em.persist(playlist);
    }

    public List<Playlists> getPlaylistsByUploader(Users uploader) {
        TypedQuery<Playlists> query = em.createQuery(
                "SELECT p FROM Playlists p WHERE p.userID = :uploader ORDER BY p.createdAt DESC",
                Playlists.class
        );
        query.setParameter("uploader", uploader);
        return query.getResultList();
    }

    public void updatePlaylist(Playlists playlist) {
        em.merge(playlist);
    }

    public void deletePlaylist(Playlists playlist) {
        em.remove(em.merge(playlist));
    }

    // Playlist Videos Operations
    public void addVideoToPlaylist(Videos video, Playlists playlist) {
        PlaylistVideos pv = new PlaylistVideos();
        pv.setVideoID(video);
        pv.setPlaylistID(playlist);
        pv.setCreatedAt(new Date());
        em.persist(pv);
    }

    public void removeVideoFromPlaylist(Videos video, Playlists playlist) {
        TypedQuery<PlaylistVideos> query = em.createQuery(
                "SELECT pv FROM PlaylistVideos pv WHERE pv.videoID = :video AND pv.playlistID = :playlist",
                PlaylistVideos.class
        );
        query.setParameter("video", video);
        query.setParameter("playlist", playlist);

        List<PlaylistVideos> results = query.getResultList();
        for (PlaylistVideos pv : results) {
            em.remove(pv);
        }
    }

    public List<Videos> getVideosInPlaylist(Playlists playlist) {
        TypedQuery<Videos> query = em.createQuery(
                "SELECT pv.videoID FROM PlaylistVideos pv WHERE pv.playlistID = :playlist ORDER BY pv.createdAt",
                Videos.class
        );
        query.setParameter("playlist", playlist);
        return query.getResultList();
    }

    // Category Operations
    public List<Categories> getAllCategories() {
        TypedQuery<Categories> query = em.createQuery("SELECT c FROM Categories c ORDER BY c.categoryName", Categories.class);
        return query.getResultList();
    }

    public Categories findCategoryByName(String categoryName) {
        try {
            TypedQuery<Categories> query = em.createQuery(
                    "SELECT c FROM Categories c WHERE c.categoryName = :categoryName",
                    Categories.class
            );
            query.setParameter("categoryName", categoryName);
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    // Utility method to format view counts
    public String formatViewCount(int views) {
        if (views >= 1000000) {
            return String.format("%.1fM", views / 1000000.0);
        } else if (views >= 1000) {
            return String.format("%.1fK", views / 1000.0);
        } else {
            return String.valueOf(views);
        }
    }

    // Utility method to format duration (seconds to mm:ss)
    public String formatDuration(int durationInSeconds) {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // Search videos by uploader
    public List<Videos> searchVideosByUploader(Users uploader, String searchTerm) {
        TypedQuery<Videos> query = em.createQuery(
                "SELECT v FROM Videos v WHERE v.userID = :uploader AND "
                + "(LOWER(v.title) LIKE LOWER(:searchTerm) OR LOWER(v.description) LIKE LOWER(:searchTerm)) "
                + "ORDER BY v.uploaddate DESC",
                Videos.class
        );
        query.setParameter("uploader", uploader);
        query.setParameter("searchTerm", "%" + searchTerm + "%");
        return query.getResultList();
    }

    // Get video statistics
    public Map<String, Object> getVideoStatistics(Videos video) {
        System.out.println("VIDEO Statistics: " + video);
        Map<String, Object> stats = new HashMap<>();

        if (video == null) {
            stats.put("likes", 0L);
            stats.put("views", 0);
            stats.put("comments", 0L);
            return stats;
        }
        // Get like count
        TypedQuery<Long> likeQuery = em.createQuery(
                "SELECT COUNT(l) FROM Likes l WHERE l.videoID = :video",
                Long.class
        );
        likeQuery.setParameter("video", video);
        Long likeCount = likeQuery.getSingleResult();

        TypedQuery<Long> commentQuery = em.createQuery(
                "SELECT COUNT(c) FROM Comments c WHERE c.videoID = :videoId",
                Long.class
        );
        commentQuery.setParameter("videoId", video.getVideoID());
        Long commentCount = commentQuery.getSingleResult();

        stats.put("likes", likeCount);
        stats.put("views", video.getViewscount());
        stats.put("comments", commentCount); // Placeholder if Comments entity exists

        return stats;
    }

    // Update video status
    public void updateVideoStatus(Videos video, String status) {
        video.setStatus(status);
        em.merge(video);
    }

    // Increment view count
    public void incrementViewCount(Videos video) {
        video.setViewscount(video.getViewscount() + 1);
        em.merge(video);
    }
    // New method to count total videos

    public long countTotalVideos() {
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(v) FROM Videos v WHERE v.status = :status", Long.class);
            query.setParameter("status", "published");
            return query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // New method to count total videos for the previous day
    public long countTotalVideosPreviousDay() {
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(v) FROM Videos v WHERE v.status = :status AND v.uploaddate < :startOfDay", Long.class);
            query.setParameter("status", "published");
            LocalDateTime startOfDay = LocalDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDate().atStartOfDay();
            query.setParameter("startOfDay", Date.from(startOfDay.atZone(ZoneId.of("Asia/Kolkata")).toInstant()));
            return query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    // Get all videos

    public List<Videos> getAllVideos() {
        TypedQuery<Videos> query = em.createQuery(
                "SELECT v FROM Videos v ORDER BY v.uploaddate DESC",
                Videos.class
        );
        return query.getResultList();
    }

    public List<WatchHistory> getWatchHistoryByUser(int userId) {
        TypedQuery<WatchHistory> query = em.createQuery(
                "SELECT w FROM WatchHistory w WHERE w.userID = :userId ORDER BY w.watchedAt DESC",
                WatchHistory.class
        );
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public void addWatchHistoryEntry(int userId, int videoId, String deviceinfo) {
        WatchHistory watchHistory = new WatchHistory();
        watchHistory.setUserID(userId);
        watchHistory.setVideoID(videoId);
        watchHistory.setWatchedAt(new Date());
        watchHistory.setWatchduration(0); // Initial duration; update later if needed
        watchHistory.setDeviceinfo(deviceinfo != null ? deviceinfo : "Unknown");
        em.persist(watchHistory);
    }

    public List<Videos> getVideosByCategory(int categoryId) {
        return em.createQuery("SELECT v FROM Videos v WHERE v.category.categoryID = :catId AND v.status = 'published'", Videos.class)
                .setParameter("catId", categoryId)
                .getResultList();
    }

}
