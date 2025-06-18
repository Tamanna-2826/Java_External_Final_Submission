/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package CDI;

import EJB.VideoBean;
import Entities.Categories;
import Entities.Playlists;
import Entities.Users;
import Entities.Videos;
import Utilities.ThumbnailUtil;
import Utilities.VideoDurationCalculator;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author LENOVO
 */
@Named(value = "uploaderBean")
@SessionScoped
public class uploaderBean implements Serializable {

    @EJB
    private VideoBean videoService;

    @Inject
    private CategoryBean categoryBean;

    public uploaderBean() {

    }
// Current logged-in user
    private Users currentUser;

    // Dashboard Analytics
    private Long totalVideos;
    private String totalViews;
    private String totalLikes;
    private Long totalComments;

    // Video Management
    private List<Videos> videos;
    private List<VideoDisplayModel> videoDisplayList;
    private Videos selectedVideo;

    // Playlist Management
    private List<Playlists> playlists;
    private Playlists selectedPlaylist;

    // Upload Form Fields
    private UploadedFile uploadedFile;
    private String videoTitle;
    private String videoDescription;
    private String videoCategory;
    private String videoTags;
    private boolean isPremium;
    private long totalVideoCount; // New field for total videos
    private String videoPercentageChange; // New field for percentage change

    // Chart Model
//    private LineChartModel viewsChartModel;
    // Categories
    private List<Categories> categories;

    private String fullFilePath; // Field to store full path for duration calculation

    @PostConstruct
    public void init() {
        loadCurrentUser();
        if (currentUser != null) {
            loadDashboardData();
            loadVideos();
            loadPlaylists();
//            loadCategories();
//            createViewsChart();

        }
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

    private void loadDashboardData() {
        totalVideos = videoService.getTotalVideosByUploader(currentUser);

        Long viewsCount = videoService.getTotalViewsByUploader(currentUser);
        totalViews = videoService.formatViewCount(viewsCount.intValue());

        Long likesCount = videoService.getTotalLikesByUploader(currentUser);
        totalLikes = videoService.formatViewCount(likesCount.intValue());

        totalComments = videoService.getTotalCommentsByUploader(currentUser);
        // Load total video count and percentage change
        totalVideoCount = videoService.countTotalVideos();
        long previousVideoCount = videoService.countTotalVideosPreviousDay();
        if (previousVideoCount > 0) {
            double change = ((double) (totalVideoCount - previousVideoCount) / previousVideoCount) * 100;
            videoPercentageChange = String.format("%+.1f%% today", change);
        } else {
            videoPercentageChange = "N/A";
        }
    }

    private void loadVideos() {
        videos = videoService.getVideosByUploader(currentUser);
        videoDisplayList = new ArrayList<>();

        for (Videos video : videos) {
            VideoDisplayModel displayModel = new VideoDisplayModel();
            displayModel.setVideo(video);
            displayModel.setFormattedViews(videoService.formatViewCount(video.getViewscount()));
            displayModel.setFormattedDuration(videoService.formatDuration(video.getDuration()));
            displayModel.setFormattedUploadDate(formatDate(video.getUploaddate()));

            // Get video statistics
            Map<String, Object> stats = videoService.getVideoStatistics(video);
            displayModel.setLikesCount(((Long) stats.get("likes")).intValue());
            displayModel.setCommentsCount(((Long) stats.get("comments")).intValue());
            displayModel.setSelected(selectedVideo != null && video.equals(selectedVideo));
            videoDisplayList.add(displayModel);
        }
    }

    private void loadPlaylists() {
        playlists = videoService.getPlaylistsByUploader(currentUser);
    }

    private void loadCategories() {
        categories = videoService.getAllCategories();
    }

    public List<Categories> getAvailableCategories() {
        return categoryBean.getCategories(); // Use CategoryBean's categories
    }

    // Video Upload Method
    public void uploadVideo() {
        Logger logger = LoggerFactory.getLogger(uploaderBean.class);
        try {
            if (uploadedFile == null || uploadedFile.getSize() == 0) {
                showMessage("Error", "Please select a video file to upload.", FacesMessage.SEVERITY_ERROR);
                return;
            }

            if (videoTitle == null || videoTitle.trim().isEmpty()) {
                showMessage("Error", "Please enter a video title.", FacesMessage.SEVERITY_ERROR);
                return;
            }

            if (videoCategory == null || videoCategory.trim().isEmpty()) {
                showMessage("Error", "Please select a category.", FacesMessage.SEVERITY_ERROR);
                return;
            }

            // Save uploaded file
            String filePath = saveUploadedFile(uploadedFile);
            if (filePath == null) {
                showMessage("Error", "Failed to save video file.", FacesMessage.SEVERITY_ERROR);
                return;
            }
            String fullVideoPath = getFullFilePath();
            // Verify file exists
            File videoFile = new File(fullVideoPath);
            logger.info("Attempting to calculate duration for file: {}", fullVideoPath);
            if (!videoFile.exists() || !videoFile.canRead()) {
                logger.error("File does not exist or is not readable: {}", fullVideoPath);
                showMessage("Error", "Saved file is not accessible.", FacesMessage.SEVERITY_ERROR);
                Files.deleteIfExists(Paths.get(fullVideoPath));
                return;
            }

            // Calculate video duration using Humble Video
            int durationInSeconds = VideoDurationCalculator.getVideoDurationUsingHumbleVideo(fullVideoPath);
            if (durationInSeconds < 0) {
                logger.error("Failed to calculate duration for: {}", fullVideoPath);
                showMessage("Error", "Failed to calculate video duration. Please try another file.", FacesMessage.SEVERITY_ERROR);
                Files.deleteIfExists(Paths.get(fullVideoPath));
                return;
            }

            // Generate thumbnail path
            Path videoPath = Paths.get(fullVideoPath);
            String videoFileName = videoPath.getFileName().toString();
            String thumbnailFileName = videoFileName.replace(".mp4", ".jpg");
            Path thumbnailDir = videoPath.getParent().getParent().resolve("thumbnails");
            String absoluteThumbnailPath = thumbnailDir.resolve(thumbnailFileName).toString();
            logger.info("Thumbnail will be saved at: {}", absoluteThumbnailPath);

            // Generate thumbnail
            boolean thumbnailGenerated = ThumbnailUtil.generateThumbnail(fullVideoPath, absoluteThumbnailPath);
            if (!thumbnailGenerated) {
                logger.error("Failed to generate thumbnail for: {}", fullVideoPath);
                showMessage("Error", "Failed to generate thumbnail.", FacesMessage.SEVERITY_ERROR);
                Files.deleteIfExists(Paths.get(fullVideoPath));
                return;
            }

            File thumbCheck = new File(absoluteThumbnailPath);
            if (!thumbCheck.exists()) {
                logger.error("Thumbnail file not found after generation: {}", absoluteThumbnailPath);
                showMessage("Error", "Thumbnail file missing after creation.", FacesMessage.SEVERITY_ERROR);
                Files.deleteIfExists(Paths.get(fullVideoPath));
                return;
            }

            // Create video entity
            Videos video = new Videos();
            video.setTitle(videoTitle.trim());
            video.setDescription(videoDescription != null ? videoDescription.trim() : "");
            video.setVideourl(filePath);
            video.setThumbnailurl("/uploads/thumbnails/" + thumbnailFileName);
            video.setUploaddate(new Date());
            video.setViewscount(0);
            video.setDuration(durationInSeconds);
            video.setQuality("HD");
            video.setStatus("published");
            video.setIspremium(isPremium);
            video.setUserID(currentUser);

            logger.info("VIDEO: {}", video);

            // Find and set category
            Categories category = videoService.findCategoryByName(videoCategory);
            logger.info("CATEGORY: {}", category);

            if (category == null) {
                logger.error("Category not found: {}", videoCategory);
                showMessage("Error", "Category '" + videoCategory + "' not found.", FacesMessage.SEVERITY_ERROR);
                Files.deleteIfExists(Paths.get(fullVideoPath));
                return;
            }

            video.setCategoryID(category);

            // Save video
            videoService.createVideo(video);

            // Refresh data
            loadDashboardData();
            loadVideos();

            // Clear form
            clearUploadForm();

            showMessage("Success", "Video uploaded successfully!", FacesMessage.SEVERITY_INFO);

        } catch (Exception e) {
            logger.error("Upload failed", e);
            showMessage("Error", "An error occurred while uploading the video: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    private String saveUploadedFile(UploadedFile file) {
        Logger logger = LoggerFactory.getLogger(uploaderBean.class);
        try {
            // Use hardcoded path for storing videos
            String uploadDir = "D:\\Sem 8\\Java_Project\\Java_External_Final_Submission\\src\\main\\webapp\\uploads\\videos\\";
            logger.info("Using upload directory: {}", uploadDir);

            // Ensure directory exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath);
            }

            // Generate unique filename
            String originalFileName = file.getFileName();
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFileName);
            logger.info("Attempting to save file to: {}", filePath.toAbsolutePath().normalize());

            long bytesWritten = 0;
            try (InputStream input = file.getInputStream(); FileOutputStream output = new FileOutputStream(filePath.toFile())) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                    bytesWritten += length;
                }
                output.flush();
            }

            // Verify file exists and has content
            File savedFile = filePath.toFile();
            if (!savedFile.exists()) {
                logger.error("File was not created: {}", filePath);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "File was not saved to disk."));
                return null;
            }
            if (savedFile.length() != bytesWritten) {
                logger.error("File size mismatch: expected {} bytes, but file size is {} bytes", bytesWritten, savedFile.length());
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "File was not saved correctly."));
                return null;
            }

            logger.info("Successfully saved file to: {} (size: {} bytes)", filePath, savedFile.length());

            // Store absolute path for Humble Video
            fullFilePath = filePath.toAbsolutePath().normalize().toString();
            logger.info("Absolute path for Humble Video: {}", fullFilePath);

            // Return relative path for database
            return "/uploads/videos/" + uniqueFileName;

        } catch (IOException e) {
            logger.error("Failed to save uploaded file: {}", file.getFileName(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to save file: " + e.getMessage()));
            return null;
        }
    }
//    private String saveUploadedFile(UploadedFile file) {
//        Logger logger = LoggerFactory.getLogger(uploaderBean.class);
//        try {
//            // Attempt to resolve path within webapp
//            String uploadDir = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/uploads/videos/");
//            if (uploadDir == null) {
//                // Fallback for development or if getRealPath fails
//                String contextPath = FacesContext.getCurrentInstance().getExternalContext().getContextName();
//                uploadDir = System.getProperty("catalina.base", System.getProperty("user.dir"))
//                        + "/webapps/" + contextPath.replace("/", "") + "/uploads/videos/";
//                logger.warn("getRealPath returned null, falling back to: {}", uploadDir);
//            }
//
//            // Normalize path for Windows
//            uploadDir = Paths.get(uploadDir).normalize().toString();
//            logger.info("Using upload directory: {}", uploadDir);
//
//            // Ensure directory exists
//            Path uploadPath = Paths.get(uploadDir);
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//                logger.info("Created upload directory: {}", uploadPath);
//            }
//
//            // Generate unique filename
//            String originalFileName = file.getFileName();
//            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
//            String uniqueFileName = UUID.randomUUID().toString() + extension;
//
//            // Save file
//            Path filePath = uploadPath.resolve(uniqueFileName);
//            logger.info("Attempting to save file to: {}", filePath.toAbsolutePath().normalize());
//
//            long bytesWritten = 0;
//            try (InputStream input = file.getInputStream(); FileOutputStream output = new FileOutputStream(filePath.toFile())) {
//                byte[] buffer = new byte[1024];
//                int length;
//                while ((length = input.read(buffer)) > 0) {
//                    output.write(buffer, 0, length);
//                    bytesWritten += length;
//                }
//                output.flush();
//            }
//
//            // Verify file exists and has content
//            File savedFile = filePath.toFile();
//            if (!savedFile.exists()) {
//                logger.error("File was not created: {}", filePath);
//                FacesContext.getCurrentInstance().addMessage(null,
//                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "File was not saved to disk."));
//                return null;
//            }
//            if (savedFile.length() != bytesWritten) {
//                logger.error("File size mismatch: expected {} bytes, but file size is {} bytes", bytesWritten, savedFile.length());
//                FacesContext.getCurrentInstance().addMessage(null,
//                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "File was not saved correctly."));
//                return null;
//            }
//
//            logger.info("Successfully saved file to: {} (size: {} bytes)", filePath, savedFile.length());
//
//            // Store absolute path for Humble Video
//            fullFilePath = filePath.toAbsolutePath().normalize().toString();
//            logger.info("Absolute path for Humble Video: {}", fullFilePath);
//
//            // Return relative path for database
//            return "/uploads/videos/" + uniqueFileName;
//
//        } catch (IOException e) {
//            logger.error("Failed to save uploaded file: {}", file.getFileName(), e);
//            FacesContext.getCurrentInstance().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to save file: " + e.getMessage()));
//            return null;
//        }
//    }

    private String generateThumbnailPath(String videoPath) {
        // Generate thumbnail path based on video path
        // You might want to implement actual thumbnail generation here
        return videoPath.replace("/videos/", "/thumbnails/").replace(".mp4", ".jpg");
    }

//    public void editVideo(Videos video) {
//                System.out.println("V T E : "+video);
//
//        selectedVideo = video;
//        // Navigate to edit page or open edit dialog
//        showMessage("Info", "Edit functionality to be implemented.", FacesMessage.SEVERITY_INFO);
//    }
    public void editVideo(Videos video) {
        System.out.println("Editing video: {}" + video);
        Logger logger = LoggerFactory.getLogger(uploaderBean.class);

        try {
            logger.info("Editing video: {}", video);
            this.selectedVideo = video; // Directly use the selected video
            // Open the edit dialog
            FacesContext.getCurrentInstance().getPartialViewContext().getEvalScripts().add("PF('editDialog').show();");
        } catch (Exception e) {
            logger.error("Error preparing video for edit: {}", video, e);
            showMessage("Error", "Failed to load video for editing: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    public void saveEditedVideo() {
        Logger logger = LoggerFactory.getLogger(uploaderBean.class);
        System.out.println("Selected Video for edit: " + selectedVideo);

        try {
            if (selectedVideo == null) {
                logger.error("No video selected for editing");
                showMessage("Error", "No video selected for editing.", FacesMessage.SEVERITY_ERROR);
                return;
            }

            // Validate required fields
            if (selectedVideo.getTitle() == null || selectedVideo.getTitle().trim().isEmpty()) {
                showMessage("Error", "Video title is required.", FacesMessage.SEVERITY_ERROR);
                return;
            }

            // Handle category validation and setting
            String categoryName = null;
            if (selectedVideo.getCategoryID() != null) {
                categoryName = selectedVideo.getCategoryID().getCategoryName();
            }

            if (categoryName == null || categoryName.trim().isEmpty()) {
                showMessage("Error", "Please select a category.", FacesMessage.SEVERITY_ERROR);
                return;
            }

            Categories category = videoService.findCategoryByName(categoryName);
            if (category == null) {
                logger.error("Category not found: {}", categoryName);
                showMessage("Error", "Category '" + categoryName + "' not found.", FacesMessage.SEVERITY_ERROR);
                return;
            }
            selectedVideo.setCategoryID(category);

            // Handle video file replacement if a new file is uploaded
            if (uploadedFile != null && uploadedFile.getSize() > 0) {
                logger.info("New video file uploaded, processing replacement...");

                // Get old file paths for cleanup
                String oldVideoUrl = selectedVideo.getVideourl();
                String oldThumbnailUrl = selectedVideo.getThumbnailurl();

                // Save new video file
                String newFilePath = saveUploadedFile(uploadedFile);
                if (newFilePath == null) {
                    showMessage("Error", "Failed to save new video file.", FacesMessage.SEVERITY_ERROR);
                    return;
                }

                String fullVideoPath = getFullFilePath();
                File videoFile = new File(fullVideoPath);

                if (!videoFile.exists() || !videoFile.canRead()) {
                    logger.error("File does not exist or is not readable: {}", fullVideoPath);
                    showMessage("Error", "Saved file is not accessible.", FacesMessage.SEVERITY_ERROR);
                    Files.deleteIfExists(Paths.get(fullVideoPath));
                    return;
                }

                // Calculate duration for new video
                int durationInSeconds = VideoDurationCalculator.getVideoDurationUsingHumbleVideo(fullVideoPath);
                if (durationInSeconds < 0) {
                    logger.error("Failed to calculate duration for: {}", fullVideoPath);
                    showMessage("Error", "Failed to calculate video duration.", FacesMessage.SEVERITY_ERROR);
                    Files.deleteIfExists(Paths.get(fullVideoPath));
                    return;
                }

                // Generate new thumbnail
                Path videoPath = Paths.get(fullVideoPath);
                String videoFileName = videoPath.getFileName().toString();
                String thumbnailFileName = videoFileName.replace(".mp4", ".jpg");

                // Create thumbnails directory if it doesn't exist
                Path thumbnailDir = videoPath.getParent().getParent().resolve("thumbnails");
                if (!Files.exists(thumbnailDir)) {
                    Files.createDirectories(thumbnailDir);
                }

                String absoluteThumbnailPath = thumbnailDir.resolve(thumbnailFileName).toString();

                boolean thumbnailGenerated = ThumbnailUtil.generateThumbnail(fullVideoPath, absoluteThumbnailPath);
                if (!thumbnailGenerated) {
                    logger.error("Failed to generate thumbnail for: {}", fullVideoPath);
                    showMessage("Error", "Failed to generate thumbnail.", FacesMessage.SEVERITY_ERROR);
                    Files.deleteIfExists(Paths.get(fullVideoPath));
                    return;
                }

                File thumbCheck = new File(absoluteThumbnailPath);
                if (!thumbCheck.exists()) {
                    logger.error("Thumbnail file not found after generation: {}", absoluteThumbnailPath);
                    showMessage("Error", "Thumbnail file missing after creation.", FacesMessage.SEVERITY_ERROR);
                    Files.deleteIfExists(Paths.get(fullVideoPath));
                    return;
                }

                // Update video with new file paths and duration
                selectedVideo.setVideourl(newFilePath);
                selectedVideo.setThumbnailurl("/uploads/thumbnails/" + thumbnailFileName);
                selectedVideo.setDuration(durationInSeconds);

                // Clean up old files after successful processing (optional - be careful with this)
                try {
                    if (oldVideoUrl != null && !oldVideoUrl.equals(newFilePath)) {
                        // Convert relative path to absolute path for deletion
                        String oldVideoAbsolutePath = "D:\\Sem 8\\Java_Project\\Java_External_Final_Submission\\src\\main\\webapp" + oldVideoUrl;
                        Files.deleteIfExists(Paths.get(oldVideoAbsolutePath));
                    }
                    if (oldThumbnailUrl != null) {
                        String oldThumbnailAbsolutePath = "D:\\Sem 8\\Java_Project\\Java_External_Final_Submission\\src\\main\\webapp" + oldThumbnailUrl;
                        Files.deleteIfExists(Paths.get(oldThumbnailAbsolutePath));
                    }
                } catch (Exception cleanupException) {
                    logger.warn("Failed to cleanup old files: {}", cleanupException.getMessage());
                    // Don't fail the operation if cleanup fails
                }
            }

            // Ensure description is not null
            if (selectedVideo.getDescription() == null) {
                selectedVideo.setDescription("");
            }

            // Trim text fields
            selectedVideo.setTitle(selectedVideo.getTitle().trim());
            selectedVideo.setDescription(selectedVideo.getDescription().trim());

            // Update the video in the database
            videoService.updateVideo(selectedVideo);
            logger.info("Video updated successfully: {}", selectedVideo);

            // Refresh the data
            loadVideos();
            loadDashboardData();

            // Show success message
            showMessage("Success", "Video updated successfully!", FacesMessage.SEVERITY_INFO);

            // Clear selection and uploaded file
            selectedVideo = null;
            uploadedFile = null;

        } catch (Exception e) {
            logger.error("Error updating video: {}", selectedVideo, e);
            showMessage("Error", "Error updating video: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    public void viewAnalytics(Videos video) {
        selectedVideo = video;
        // Navigate to analytics page
        showMessage("Info", "Analytics page to be implemented.", FacesMessage.SEVERITY_INFO);
    }

    public void deleteVideo(Videos video) {
        System.out.println("V T D : " + video);
        Logger logger = LoggerFactory.getLogger(uploaderBean.class);

        try {
            logger.info("VIDEO To del : " + video);
            videoService.deleteVideo(video);
            loadDashboardData();
            loadVideos();
            showMessage("Success", "Video deleted successfully!", FacesMessage.SEVERITY_INFO);
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error", "Error deleting video: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "uploaderLogin.xhtml?faces-redirect=true";
    }

    private void clearUploadForm() {
        uploadedFile = null;
        videoTitle = null;
        videoDescription = null;
        videoCategory = null;
        videoTags = null;
        isPremium = false;
    }

    private void showMessage(String summary, String detail, FacesMessage.Severity severity) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage message = new FacesMessage(severity, summary, detail);
        context.addMessage(null, message);
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        return sdf.format(date);
    }
// New getters

    public long getTotalVideoCount() {
        return totalVideoCount;
    }

    public void setTotalVideoCount(long totalVideoCounts) {
        this.totalVideoCount = totalVideoCounts;
    }

    public void setVideoPercentageChange(String videoPercentageChange) {
        this.videoPercentageChange = videoPercentageChange;
    }

    public String getVideoPercentageChange() {
        return videoPercentageChange;
    }

    // Getters and Setters
    public String getUploaderName() {
        return currentUser != null ? currentUser.getFullName() : "Unknown";
    }

    public Long getTotalVideos() {
        return totalVideos;
    }

    public String getTotalViews() {
        return totalViews;
    }

    public String getTotalLikes() {
        return totalLikes;
    }

    public Long getTotalComments() {
        return totalComments;
    }

    public List<VideoDisplayModel> getVideos() {
        return videoDisplayList;
    }

    public List<Playlists> getPlaylists() {
        return playlists;
    }

//    public LineChartModel getViewsChartModel() {
//        return viewsChartModel;
//    }
    public UploadedFile getUploadedFile() {
        System.out.println("Uploaded file for edit : " + uploadedFile);
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getVideoDescription() {
        return videoDescription;
    }

    public void setVideoDescription(String videoDescription) {
        this.videoDescription = videoDescription;
    }

    public String getVideoCategory() {
        return videoCategory;
    }

    public void setVideoCategory(String videoCategory) {
        this.videoCategory = videoCategory;
    }

    public String getVideoTags() {
        return videoTags;
    }

    public void setVideoTags(String videoTags) {
        this.videoTags = videoTags;
    }

    public boolean isIsPremium() {
        return isPremium;
    }

    public void setIsPremium(boolean isPremium) {
        this.isPremium = isPremium;
    }

    public String getFullFilePath() {
        return fullFilePath;
    }

    public List<Categories> getCategories() {
        return categories;
    }

    public Videos getSelectedVideo() {
        return selectedVideo;
    }

    public void setSelectedVideo(Videos selectedVideo) {
        this.selectedVideo = selectedVideo;
    }

    // Inner class for display model
    public static class VideoDisplayModel implements Serializable {

        private Videos video;
        private String formattedViews;
        private String formattedDuration;
        private String formattedUploadDate;
        private int likesCount;
        private int commentsCount;
        private boolean isSelected; // New field to track if this video is the selected one

        // Getter and Setter for isSelected
        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
        }

        // Getters and Setters
        public Videos getVideo() {
            return video;
        }

        public void setVideo(Videos video) {
            this.video = video;
        }

        public String getTitle() {
            return video.getTitle();
        }

        public String getDescription() {
            return video.getDescription();
        }

        public String getThumbnailUrl() {
            String thumbnailDirPath = "D:/payara6/glassfish/domains/domain1/webapps";
            return video.getThumbnailurl();
        }

        public String getStatus() {
            return video.getStatus();
        }

        public String getCategoryName() {
            return video.getCategoryID() != null ? video.getCategoryID().getCategoryName() : "Uncategorized";
        }

        public String getFormattedViews() {
            return formattedViews;
        }

        public void setFormattedViews(String formattedViews) {
            this.formattedViews = formattedViews;
        }

        public String getFormattedDuration() {
            return formattedDuration;
        }

        public void setFormattedDuration(String formattedDuration) {
            this.formattedDuration = formattedDuration;
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

        // Convenience methods for the UI
        public String getViews() {
            return formattedViews;
        }

        public String getLikes() {
            return String.valueOf(likesCount);
        }

        public String getComments() {
            return String.valueOf(commentsCount);
        }

        public String getDuration() {
            return formattedDuration;
        }

        public String getUploadDate() {
            return formattedUploadDate;
        }

        public String getCategory() {
            return getCategoryName();
        }
    }
}
