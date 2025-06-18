/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package CDI;

import EJB.LikesBean;
import EJB.VideoBean;
import EJB.commentBean;
import Entities.Categories;
import Entities.Comments;
import Entities.Users;
import Entities.Videos;
import Entities.WatchHistory;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
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
    private int userId;
    private Map<Integer, Long> videoLikes; // videoID -> like count
    private Map<Integer, List<Comments>> videoComments; // videoID -> list of comments
    private Map<Integer, String> newCommentTexts = new HashMap<>();
    private int selectedCategoryId;

    private String selectedVideoId;
    private Videos selectedVideo;

    // Existing fields...
    private String categorySearchText; // New field for search text
    private List<Categories> filteredCategories; // New field for filtered categories
    private Users currentUser;

    public userDashboardBean() {
    }

    private void loadCurrentUser() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        if (session != null) {
            currentUser = (Users) session.getAttribute("currentUser");
        }

        if (currentUser == null) {
            // Redirect to login if no user in session
            try {
                context.getExternalContext().redirect("uploaderLogin.xhtml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void resetState() {
        categorySearchText = "";
        filterCategories(); // Recompute filtered categories
    }

    public String getSelectedVideoId() {
        return selectedVideoId;
    }

    public Videos getSelectedVideo() {
        return selectedVideo;
    }

    public VideoBean getVideosBean() {
        return videosBean;
    }

    public void setSelectedVideoId(String selectedVideoId) {
        this.selectedVideoId = selectedVideoId;
        this.selectedVideo = null;

        if (selectedVideoId != null) {
            try {
                int videoId = Integer.parseInt(selectedVideoId);
                this.selectedVideo = videosBean.findVideo(videoId);
            } catch (NumberFormatException e) {
                System.out.println("Invalid video ID format: " + selectedVideoId);
            }
        }

        if (this.selectedVideo == null && selectedVideoId != null) {
            System.out.println("Warning: No video found for videoID: " + selectedVideoId);
        }
    }

    @PostConstruct
    public void init() {
        loadCurrentUser();
        userName = currentUser.getFullName();
        userProgress = "45%";

        userId = currentUser.getUserID(); // Replace with actual user ID from session
        categorySearchText = "";
        filteredCategories = categoryBean.getCategories();
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
// Getter and Setter for categorySearchText

    public String getCategorySearchText() {
        return categorySearchText;
    }

    public void setCategorySearchText(String categorySearchText) {
        this.categorySearchText = categorySearchText;
        filterCategories(); // Trigger filtering when search text changes
    }
// Method to filter categories based on search text

    public void filterCategories() {
        List<Categories> allCategories = categoryBean.getCategories();
        if (categorySearchText == null || categorySearchText.trim().isEmpty()) {
            filteredCategories = allCategories; // Show all categories if no search text
        } else {
            String searchLower = categorySearchText.trim().toLowerCase();
            filteredCategories = allCategories.stream()
                    .filter(category -> category.getCategoryName().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }
    }

    public List<Categories> getCategories() {
        return filteredCategories;
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

//    public List<Categories> getCategories() {
//        return categoryBean.getCategories();
//    }
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

    public void watchVideoForUser(Videos video) {
        // Implement navigation to video player page
        // Increment view count
        videosBean.addWatchHistoryEntry(userId, video.getVideoID(), "Desktop"); // Replace "Desktop" with actual device info

        videosBean.incrementViewCount(video);
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

//    public String getDynamicColor(String categoryName) {
//        // Hash category name to an integer
//        int hash = Math.abs(categoryName.hashCode());
//
//        // Create color components using hash
//        int r = (hash & 0xFF0000) >> 16;
//        int g = (hash & 0x00FF00) >> 8;
//        int b = hash & 0x0000FF;
//
//        // Optional: Make colors a bit softer/pastel
//        r = (r + 200) / 2;
//        g = (g + 200) / 2;
//        b = (b + 200) / 2;
//
//        // Return CSS style string
//        return String.format("background-color: rgb(%d, %d, %d);", r, g, b);
//    }
//    public String getDynamicColor(String categoryName) {
//        // Define a set of modern gradient color combinations
//        String[][] gradientPairs = {
//            {"#667eea", "#764ba2"}, // Purple to Blue
//            {"#f093fb", "#f5576c"}, // Pink to Red
//            {"#4facfe", "#00f2fe"}, // Blue to Cyan
//            {"#43e97b", "#38f9d7"}, // Green to Mint
//            {"#fa709a", "#fee140"}, // Pink to Yellow
//            {"#a8edea", "#fed6e3"}, // Mint to Pink
//            {"#ff9a9e", "#fecfef"}, // Coral to Pink
//            {"#667eea", "#764ba2"}, // Purple gradient
//            {"#f093fb", "#f5576c"}, // Magenta gradient
//            {"#4facfe", "#00f2fe"}, // Ocean gradient
//            {"#43e97b", "#38f9d7"}, // Nature gradient
//            {"#fa709a", "#fee140"}, // Sunset gradient
//            {"#a8edea", "#fed6e3"}, // Pastel gradient
//            {"#ffecd2", "#fcb69f"}, // Peach gradient
//            {"#ff8a80", "#ff5722"}, // Fire gradient
//            {"#81c784", "#4caf50"}, // Forest gradient
//            {"#64b5f6", "#2196f3"}, // Sky gradient
//            {"#ba68c8", "#9c27b0"}, // Violet gradient
//            {"#ffb74d", "#ff9800"}, // Orange gradient
//            {"#4db6ac", "#009688"} // Teal gradient
//        };
//
//        // Use category name hash to select a gradient pair
//        int hash = Math.abs(categoryName.hashCode());
//        int index = hash % gradientPairs.length;
//
//        String[] selectedGradient = gradientPairs[index];
//
//        // Return CSS gradient background
//        return String.format("background: linear-gradient(135deg, %s 0%%, %s 100%%);",
//                selectedGradient[0], selectedGradient[1]);
//    }
    public String getDynamicColor(String categoryName) {
        int hash = Math.abs(categoryName.hashCode());
        // Limit to avoid too bright colors
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = (hash & 0x0000FF);

        // Boost color intensity and reduce too-light colors
        r = (r + 128) / 2;
        g = (g + 128) / 2;
        b = (b + 128) / 2;

        return String.format("background-color: rgb(%d, %d, %d); color: white;", r, g, b);
    }

// Alternative method for more dynamic gradients
    public String getDynamicColorAdvanced(String categoryName) {
        // Hash the category name
        int hash = Math.abs(categoryName.hashCode());

        // Generate vibrant colors
        int hue1 = (hash % 360);
        int hue2 = ((hash * 7) % 360);

        // Ensure good saturation and lightness for vibrant colors
        String color1 = String.format("hsl(%d, 70%%, 60%%)", hue1);
        String color2 = String.format("hsl(%d, 75%%, 65%%)", hue2);

        // Random angle for variety
        int angle = 135 + (hash % 90); // Between 135-225 degrees

        return String.format("background: linear-gradient(%ddeg, %s 0%%, %s 100%%);",
                angle, color1, color2);
    }

    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "userLogin.xhtml?faces-redirect=true";
    }
}
