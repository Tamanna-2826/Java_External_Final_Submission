/*
 * Click nbproject://nbproject/nbproject.xml to edit this template
 */
package CDI;

import EJB.VideoBean;
import Entities.Categories;
import Entities.Videos;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author LENOVO
 */
@Named(value = "videoLisingBean")
@SessionScoped
public class videoLisingBean implements Serializable {

    private List<Videos> videos;
    private List<Videos> filteredVideos; // For search functionality
    private Videos selectedVideo; // For detailed view
    private String searchTerm; // For search input
    private String searchType; // To determine the type of search (all, title, category)
    private Integer selectedCategoryId; // For category-based search
    private List<Categories> categories; // To store available categories
    private String videoUrl; // To store the video URL for playback

    @EJB
    private VideoBean videoService;

    @PostConstruct
    public void init() {
        searchType = "all"; // Default search type
        selectedCategoryId = null; // Default to no category selected
        fetchVideos();
        fetchCategories(); // Load categories for search dropdown
    }

    public videoLisingBean() {
    }

    public void fetchVideos() {
        try {
            videos = videoService.getAllVideos();
            filteredVideos = new ArrayList<>(videos); // Initialize filtered list
            if (videos == null || videos.isEmpty()) {
                addInfoMessage("No videos found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error fetching videos.");
        }
    }

    public void fetchCategories() {
        try {
            categories = videoService.getAllCategories();
            if (categories == null || categories.isEmpty()) {
                addInfoMessage("No categories found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error fetching categories.");
        }
    }

    public void searchVideos() {
        try {
            if (searchType == null || searchType.equals("all")) {
                // If search term is empty, show all videos
                if (searchTerm == null || searchTerm.trim().isEmpty()) {
                    filteredVideos = new ArrayList<>(videos);
                } else {
                    filteredVideos = videoService.searchVideosByKeyword(searchTerm);
                }
            } else if (searchType.equals("category") && selectedCategoryId != null) {
                // Search by category and optional keyword
                filteredVideos = videoService.searchVideosByKeywordAndCategory(searchTerm, selectedCategoryId);
            } else if (searchType.equals("title")) {
                // Search by title only
                filteredVideos = videoService.searchVideosByKeyword(searchTerm);
            } else {
                filteredVideos = new ArrayList<>(videos); // Fallback to all videos
            }

            if (filteredVideos.isEmpty()) {
                addInfoMessage("No videos found for the search criteria.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error performing search.");
        }
    }

    public void clearSearch() {
        searchTerm = null;
        searchType = "all";
        selectedCategoryId = null;
        filteredVideos = new ArrayList<>(videos); // Reset to all videos
        addInfoMessage("Search cleared.");
    }

    public void viewVideoDetails(Videos video) {
        this.selectedVideo = video;
        System.out.println("VIDEO : " + video);
        System.out.println("Selecetd VIDEO : " + selectedVideo);

        if (video != null && video.getVideourl() != null) {
            String videoFileName = video.getVideourl().substring(video.getVideourl().lastIndexOf("\\") + 1);
            this.videoUrl = videoFileName;
        } else {
            this.videoUrl = null;
        }
        FacesContext.getCurrentInstance().getPartialViewContext().getEvalScripts().add("PF('videoDetailsDialog').show();");
    }

    public String formatDate(Date date) {
        if (date == null) {
            return "Not Available";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        return sdf.format(date);
    }

    public Map<String, Object> getVideoStatistics(Videos video) {
        System.out.println("Video in Listing Bean : " + video);
        return videoService.getVideoStatistics(video);
    }

    public String formatViewCount(int views) {
        return videoService.formatViewCount(views);
    }

    public String formatDuration(int durationInSeconds) {
        return videoService.formatDuration(durationInSeconds);
    }

    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", message));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }

    // Getters and Setters
    public List<Videos> getVideos() {
        return videos;
    }

    public List<Videos> getFilteredVideos() {
        return filteredVideos;
    }

    public void setFilteredVideos(List<Videos> filteredVideos) {
        this.filteredVideos = filteredVideos;
    }

    public Videos getSelectedVideo() {
        return selectedVideo;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public Integer getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public void setSelectedCategoryId(Integer selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
    }

    public List<Categories> getCategories() {
        return categories;
    }

    public String getVideoUrl() {
        System.out.println("Video URL : " + videoUrl);
        return videoUrl;
    }
}