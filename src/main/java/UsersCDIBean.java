/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */

import EJB.UsersBean;
import Entities.Users;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.Dependent;
import java.util.List;

/**
 *
 * @author LENOVO
 */
@Named(value = "usersCDIBean")
@Dependent
public class UsersCDIBean {

    /**
     * Creates a new instance of UsersCDIBean
     */
    public UsersCDIBean() {
    }
    @EJB
    private UsersBean usersBean;

    private List<Users> userList;
    private String searchTerm;

    @jakarta.annotation.PostConstruct
    public void init() {
        userList = usersBean.getAllUsers();
    }

    public void searchUsers() {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            userList = usersBean.getAllUsers();
        } else {
            userList = usersBean.searchUsers(searchTerm.trim());
        }
    }

    // Getters and Setters
    public List<Users> getUserList() {
        return userList;
    }

    public void setUserList(List<Users> userList) {
        this.userList = userList;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public long getTotalUsers() {
        return usersBean.getTotalUserCount();
    }

}
