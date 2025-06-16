/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package CDI;

import EJB.VideoBean;
import Entities.Videos;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;

/**
 *
 * @author LENOVO
 */
@Named(value = "watchVideoBean")
@SessionScoped
public class watchVideoBean implements Serializable {

    @EJB
    private VideoBean videoService;

    private Videos video;

    @PostConstruct
    public void init() {
        String videoId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("videoId");
        if (videoId != null) {
            try {
                video = videoService.findVideo(Integer.parseInt(videoId));
            } catch (NumberFormatException e) {
                video = null;
            }
        }
    }

    public Videos getVideo() {
        return video;
    }

    public void setVideo(Videos video) {
        this.video = video;
    }

    /**
     * Creates a new instance of watchVideoBean
     */
    public watchVideoBean() {
    }

}
