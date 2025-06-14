package CDI;

import EJB.CategoryBeanLocal;
import Entities.Categories;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.stream.Collectors;

/**
 *
 * @author LENOVO
 */
@Named(value = "categoryBean")
@SessionScoped
public class CategoryBean implements Serializable {

    @EJB
    private CategoryBeanLocal categoryEJB;

    private List<Categories> categories;
    private List<Categories> filteredCategories;
    private Categories selectedCategory;
    private String searchTerm;
    private boolean editMode;
    private boolean showAddCategoryDialog = false;

    // Statistics
    private int totalCategories;
    private String mostPopularCategory;
    private int totalVideos;
    private int avgVideosPerCategory;

    public CategoryBean() {
    }

    @PostConstruct
    public void init() {
        loadCategories();
        calculateStatistics();
        selectedCategory = new Categories();
    }

    /**
     * Load all categories from database
     */
    public void loadCategories() {
        try {
            categories = categoryEJB.findAll();
            filteredCategories = categories;
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error loading categories: " + e.getMessage());
        }
    }

    /**
     * Calculate statistics for dashboard
     */
    private void calculateStatistics() {
        if (categories != null && !categories.isEmpty()) {
            totalCategories = categories.size();

            // Find most popular category (assuming you have a videoCount field)
            Categories mostPopular = categories.stream()
                    .max((c1, c2) -> Integer.compare(getVideoCount(c1), getVideoCount(c2)))
                    .orElse(null);

            mostPopularCategory = mostPopular != null ? mostPopular.getCategoryName() : "N/A";

            // Calculate total videos
            totalVideos = categories.stream()
                    .mapToInt(this::getVideoCount)
                    .sum();

            // Calculate average
            avgVideosPerCategory = totalCategories > 0 ? totalVideos / totalCategories : 0;
        }
    }

    /**
     * Get video count for a category (placeholder - implement based on your entity relationship)
     */
    public int getVideoCount(Categories category) {
        // If you have a relationship with Videos entity, implement accordingly
        // For now, returning a mock value based on category name
        if (category.getCategoryName() != null) {
            switch (category.getCategoryName().toLowerCase()) {
                case "programming":
                    return 145;
                case "design":
                    return 89;
                case "marketing":
                    return 67;
                case "business":
                    return 78;
                case "data science":
                    return 92;
                case "devops":
                    return 54;
                case "cybersecurity":
                    return 43;
                case "finance":
                    return 36;
                case "ai":
                    return 62;
                case "communication":
                    return 28;
                default:
                    return (int) (Math.random() * 100) + 10;
            }
        }
        return 0;
    }

    /**
     * Search categories based on search term
     */
    public void search() {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            filteredCategories = categories;
        } else {
            String searchLower = searchTerm.toLowerCase().trim();
            filteredCategories = categories.stream()
                    .filter(category ->
                            (category.getCategoryName() != null
                                    && category.getCategoryName().toLowerCase().contains(searchLower))
                                    || (category.getDescription() != null
                                    && category.getDescription().toLowerCase().contains(searchLower))
                    )
                    .collect(Collectors.toList());
        }
    }

    /**
     * Prepare for adding new category
     */
    public void prepareNew() {
        selectedCategory = new Categories();
        selectedCategory.setCreatedAt(new Date());
        editMode = false;
        this.showAddCategoryDialog = true; // Show the dialog
    }

    /**
     * Prepare for editing existing category
     */
    public void edit(Categories category) {
        selectedCategory = new Categories();
        selectedCategory.setCategoryID(category.getCategoryID());
        selectedCategory.setCategoryName(category.getCategoryName());
        selectedCategory.setDescription(category.getDescription());
        selectedCategory.setCreatedAt(category.getCreatedAt());
        editMode = true;
        this.showAddCategoryDialog = true; // Show the dialog
    }

    /**
     * Save category (create or update)
     */
    public void save() {
        try {
            if (selectedCategory.getCategoryName() == null
                    || selectedCategory.getCategoryName().trim().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Category name is required");
                return;
            }

            if (editMode) {
                categoryEJB.update(selectedCategory);
                addMessage(FacesMessage.SEVERITY_INFO, "Category updated successfully");
            } else {
                selectedCategory.setCreatedAt(new Date());
                categoryEJB.create(selectedCategory);
                addMessage(FacesMessage.SEVERITY_INFO, "Category created successfully");
            }

            loadCategories(); // Refresh the categories list
            calculateStatistics();
            selectedCategory = new Categories();
            editMode = false;
            this.showAddCategoryDialog = false; // Close the dialog

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error saving category: " + e.getMessage());
        }
    }

    /**
     * Delete category
     */
    public void delete(int categoryId) { // Changed int to Long to match typical JPA ID type
        try {
            categoryEJB.delete(categoryId);
            addMessage(FacesMessage.SEVERITY_INFO, "Category deleted successfully");
            loadCategories();
            calculateStatistics();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error deleting category: " + e.getMessage());
        }
    }

    /**
     * Cancel editing
     */
    public void cancel() {
        selectedCategory = new Categories();
        editMode = false;
        this.showAddCategoryDialog = false; // Close the dialog
    }

    /**
     * Add faces message
     */
    private void addMessage(FacesMessage.Severity severity, String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, message, null));
    }

    // Getters and Setters
    public List<Categories> getCategories() {
        
        return filteredCategories; // Use filteredCategories for display in the table
    }

    public void setCategories(List<Categories> categories) {
        this.categories = categories;
        this.filteredCategories = categories;
    }

    public List<Categories> getFilteredCategories() {
        return filteredCategories;
    }

    public void setFilteredCategories(List<Categories> filteredCategories) {
        this.filteredCategories = filteredCategories;
    }

    public Categories getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(Categories selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public int getTotalCategories() {
        return totalCategories;
    }

    public String getMostPopularCategory() {
        return mostPopularCategory;
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public int getAvgVideosPerCategory() {
        return avgVideosPerCategory;
    }

    public boolean isShowAddCategoryDialog() {
        return showAddCategoryDialog;
    }

    public void setShowAddCategoryDialog(boolean showAddCategoryDialog) {
        this.showAddCategoryDialog = showAddCategoryDialog;
    }
}