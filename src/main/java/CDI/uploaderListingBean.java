/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package CDI;

import EJB.VideoBean;
import Entities.Users;
import Entities.Videos;
import Enums.UserRole;
import Services.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import jakarta.faces.application.FacesMessage;

/**
 *
 * @author LENOVO
 */
@Named(value = "uploaderListingBean")
@SessionScoped
public class uploaderListingBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Users> uploaders;
    private List<Users> filteredUploaders; // For search functionality
    private Users selectedUploader; // For detailed view
    private List<UploaderVideoDisplayModel> uploaderVideos; // For detailed view of uploader's videos
    private String searchTerm; // For search input

    @Inject
    private UserService userService;

    @Inject
    private VideoBean videoService;

    @PostConstruct
    public void init() {
        fetchUploaders();
    }

    /**
     * Creates a new instance of uploaderListingBean
     */
    public uploaderListingBean() {
    }

    public void fetchUploaders() {
        try {
            uploaders = userService.findUsersByRole(UserRole.UPLOADER);
            filteredUploaders = new ArrayList<>(uploaders); // Initialize filtered list
            if (uploaders == null || uploaders.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No active uploaders found."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error fetching uploaders."));
        }
    }

    public void searchUploaders() {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            filteredUploaders = new ArrayList<>(uploaders);
            return;
        }

        filteredUploaders = new ArrayList<>();
        String lowerSearchTerm = searchTerm.toLowerCase();
        for (Users uploader : uploaders) {
            if (uploader.getFullName().toLowerCase().contains(lowerSearchTerm)
                    || uploader.getEmail().toLowerCase().contains(lowerSearchTerm)) {
                filteredUploaders.add(uploader);
            }
        }
    }

    public void viewUploaderDetails(Users uploader) {
        this.selectedUploader = uploader;
        loadUploaderVideos();
        FacesContext.getCurrentInstance().getPartialViewContext().getEvalScripts().add("PF('uploaderDetailsDialog').show();");
    }

    private void loadUploaderVideos() {
        uploaderVideos = new ArrayList<>();
        if (selectedUploader != null) {
            List<Videos> videos = videoService.getVideosByUploader(selectedUploader);
            for (Videos video : videos) {
                UploaderVideoDisplayModel model = new UploaderVideoDisplayModel();
                model.setVideo(video);
                model.setFormattedUploadDate(formatDate(video.getUploaddate()));

                Map<String, Object> stats = videoService.getVideoStatistics(video);
                model.setLikesCount(((Long) stats.get("likes")).intValue());
                model.setCommentsCount(((Long) stats.get("comments")).intValue());
                uploaderVideos.add(model);
            }
        }
    }
// Wrapper methods to expose VideoBean methods to JSF

    public Long getTotalVideosForUploader(Users uploader) {
        if (videoService == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "VideoBean not injected."));
            return 0L;
        }
        return videoService.getTotalVideosByUploader(uploader);
    }

    public Long getTotalViewsForUploader(Users uploader) {
        if (videoService == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "VideoBean not injected."));
            return 0L;
        }
        return videoService.getTotalViewsByUploader(uploader);
    }

    public Long getTotalLikesForUploader(Users uploader) {
        if (videoService == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "VideoBean not injected."));
            return 0L;
        }
        return videoService.getTotalLikesByUploader(uploader);
    }

    public Long getTotalCommentsForUploader(Users uploader) {
        if (videoService == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "VideoBean not injected."));
            return 0L;
        }
        return videoService.getTotalCommentsByUploader(uploader);
    }

    public String formatViewCountForUploader(int views) {
        if (videoService == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "VideoBean not injected."));
            return "0";
        }
        return videoService.formatViewCount(views);
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        return sdf.format(date);
    }

    // Getters and Setters
    public List<Users> getUploaders() {
        return uploaders;
    }

    public List<Users> getFilteredUploaders() {
        return filteredUploaders;
    }

    public void setFilteredUploaders(List<Users> filteredUploaders) {
        this.filteredUploaders = filteredUploaders;
    }

    public Users getSelectedUploader() {
        return selectedUploader;
    }

    public List<UploaderVideoDisplayModel> getUploaderVideos() {
        return uploaderVideos;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public static class UploaderVideoDisplayModel implements Serializable {

        private Videos video;
        private String formattedUploadDate;
        private int likesCount;
        private int commentsCount;

        public Videos getVideo() {
            return video;
        }

        public void setVideo(Videos video) {
            this.video = video;
        }

        public String getTitle() {
            return video.getTitle();
        }

        public String getFormattedUploadDate() {
            return formattedUploadDate;
        }

        public void setFormattedUploadDate(String formattedUploadDate) {
            this.formattedUploadDate = formattedUploadDate;
        }

        public int getLikesCount() {
            return likesCount;
        }

        public void setLikesCount(int likesCount) {
            this.likesCount = likesCount;
        }

        public int getCommentsCount() {
            return commentsCount;
        }

        public void setCommentsCount(int commentsCount) {
            this.commentsCount = commentsCount;
        }

        public String getCategoryName() {
            return video.getCategoryID() != null ? video.getCategoryID().getCategoryName() : "Uncategorized";
        }
    }
}
