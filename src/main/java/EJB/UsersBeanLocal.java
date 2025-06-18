package EJB;

import Entities.Users;
import java.util.List;
import jakarta.ejb.Local;

@Local
public interface UsersBeanLocal {
    List<Users> getAllUsers();
    List<Users> searchUsers(String searchTerm);
    long getTotalUserCount();

}
