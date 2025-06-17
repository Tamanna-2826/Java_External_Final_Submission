/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package CDI;

import EJB.LikesBean;
import EJB.VideoBean;
import EJB.commentBean;
import Entities.Comments;
import Entities.Videos;
import Entities.WatchHistory;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author LENOVO
 */
@Named(value = "userDashboardBean")
@SessionScoped
public class userDashboardBean implements Serializable {

    @Inject
    private CategoryBean categoryBean; // For categories

    @EJB
    private VideoBean videosBean; // For videos and watch history

    @EJB
    private LikesBean likesBean;

    @EJB
    private commentBean commentBean;

    private List<Videos> featuredVideos;
    private List<WatchHistory> watchHistory;
    private List<Videos> trendingVideos;
    private String userProgress;
    private String userName;
    private int userId; // Add userId field
    private Map<Integer, Long> videoLikes; // videoID -> like count
    private Map<Integer, List<Comments>> videoComments; // videoID -> list of comments
    private Map<Integer, String> newCommentTexts = new HashMap<>();
    private int selectedCategoryId;

    public userDashboardBean() {
    }

    public VideoBean getVideosBean() {
        return videosBean;
    }

    @PostConstruct
    public void init() {
        // Mock user data (replace with actual user session data)
        userName = "John Doe";
        userProgress = "45%";
        userId = 14; // Replace with actual user ID from session

        // Featured Videos: Get the 3 most recent videos
        List<Videos> allVideos = videosBean.getAllVideos();
        featuredVideos = allVideos.stream()
                .filter(v -> "published".equals(v.getStatus()))
                .limit(3)
                .collect(Collectors.toList());
        for (Videos v : featuredVideos) {
            System.out.println("Featured Video: " + v);
        }

        // Watch History: Fetch for the current user
        watchHistory = videosBean.getWatchHistoryByUser(userId);
        // Trending Videos: Sort by views (top 3)
        trendingVideos = allVideos.stream()
                .filter(v -> "published".equals(v.getStatus()))
                .sorted((v1, v2) -> {
                    int views1 = videosBean.getVideoStatistics(v1) != null
                            ? ((Integer) videosBean.getVideoStatistics(v1).get("views")).intValue() : 0;
                    int views2 = videosBean.getVideoStatistics(v2) != null
                            ? ((Integer) videosBean.getVideoStatistics(v2).get("views")).intValue() : 0;
                    return Integer.compare(views2, views1); // Descending order
                })
                .limit(3)
                .collect(Collectors.toList());
        videoLikes = videosBean.getAllVideos().stream()
                .collect(Collectors.toMap(
                        Videos::getVideoID,
                        v -> likesBean.countLikes(v.getVideoID())
                ));

        videoComments = videosBean.getAllVideos().stream()
                .collect(Collectors.toMap(
                        Videos::getVideoID,
                        v -> commentBean.getCommentsByVideoId(v.getVideoID())
                ));
    }

    public String goToCategory(int categoryId) {
        this.selectedCategoryId = categoryId;
        return "categoryVideos.xhtml?faces-redirect=true";
    }

    public int getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public List<Videos> getVideosBySelectedCategory() {
        return videosBean.getVideosByCategory(selectedCategoryId);
    }

    public long getLikesForVideo(int videoId) {
//        return videoLikes.getOrDefault(videoId, 0L);
        long count = likesBean.countLikes(videoId);
        videoLikes.put(videoId, count); // Update local map too
        return count;
    }

    public List<Comments> getCommentsForVideo(int videoId) {
        return videoComments.getOrDefault(videoId, new ArrayList<>());
    }

    public void likeVideo(Videos video) {
        boolean liked = likesBean.toggleLike(userId, video.getVideoID());
        videoLikes.put(video.getVideoID(), getLikesForVideo(video.getVideoID())); // Refresh count

        if (liked) {
            System.out.println("User liked the video.");
        } else {
            System.out.println("User unliked the video.");
        }
    }

    public boolean hasUserLiked(int videoId) {
        return likesBean.hasUserLiked(userId, videoId);
    }

    public void addComment(int videoId) {
        String commentText = newCommentTexts.get(videoId);
        System.out.println("video ID to Comment with Text : " + videoId + " " + commentText);

        if (commentText != null && !commentText.trim().isEmpty()) {
            commentBean.addComment(videoId, userId, commentText.trim());
            videoComments.computeIfAbsent(videoId, k -> new ArrayList<>()).add(
                    commentBean.getLatestCommentFor(videoId, userId)
            );
            newCommentTexts.put(videoId, ""); // Clear the comment box
        }
    }

    public Map<Integer, String> getNewCommentTexts() {
        return newCommentTexts;
    }

    public void setNewCommentTexts(Map<Integer, String> newCommentTexts) {
        this.newCommentTexts = newCommentTexts;
    }

    // Getters and Setters
    public List<Videos> getFeaturedVideos() {
        System.out.println("Featured Videos : " + featuredVideos);
        return featuredVideos;
    }

    public List<Entities.Categories> getCategories() {
        return categoryBean.getCategories();
    }

    public List<WatchHistory> getWatchHistory() {
        return watchHistory;
    }

    public List<Videos> getTrendingVideos() {
        return trendingVideos;
    }

    public String getUserProgress() {
        return userProgress;
    }

    public String getUserName() {
        return userName;
    }

    // Navigation methods
    public String watchVideo(Videos video) {
        // Implement navigation to video player page
        // Increment view count
        videosBean.addWatchHistoryEntry(userId, video.getVideoID(), "Desktop"); // Replace "Desktop" with actual device info

        videosBean.incrementViewCount(video);
        return "watchVideo.xhtml?faces-redirect=true&videoId=" + video.getVideoID();

    }

    public void watchVideoById(int videoId) {
        // Fetch video and navigate
        Videos video = videosBean.findVideo(videoId);
        if (video != null) {
            watchVideo(video);
        }
    }

    public int getUserProgressValue() {
        // Parse the userProgress string (e.g., "45%") to an int
        try {
            return Integer.parseInt(userProgress.replace("%", ""));
        } catch (NumberFormatException e) {
            return 0; // Fallback to 0 if parsing fails
        }
    }

    public String getDynamicColor(String categoryName) {
        // Hash category name to an integer
        int hash = Math.abs(categoryName.hashCode());

        // Create color components using hash
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;

        // Optional: Make colors a bit softer/pastel
        r = (r + 200) / 2;
        g = (g + 200) / 2;
        b = (b + 200) / 2;

        // Return CSS style string
        return String.format("background-color: rgb(%d, %d, %d);", r, g, b);
    }
}
