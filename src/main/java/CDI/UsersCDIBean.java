package CDI;

import EJB.UsersBeanLocal;
import Entities.Users;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.ejb.EJB;
import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class UsersCDIBean implements Serializable {

    @EJB
    private UsersBeanLocal usersBean;

    private List<Users> userList;
    private String searchTerm;

    public UsersCDIBean() {
    }

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
