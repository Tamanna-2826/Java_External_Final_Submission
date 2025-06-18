package EJB;

import Entities.Users;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class UsersBean implements UsersBeanLocal {

    @PersistenceContext(unitName = "eduott")
    private EntityManager em;

    @Override
    public List<Users> getAllUsers() {
        return em.createNamedQuery("Users.findAll", Users.class).getResultList();
    }

    @Override
    public List<Users> searchUsers(String searchTerm) {
        return em.createQuery("SELECT u FROM Users u WHERE LOWER(u.fullName) LIKE :term OR LOWER(u.email) LIKE :term", Users.class)
                .setParameter("term", "%" + searchTerm.toLowerCase() + "%")
                .getResultList();
    }

    @Override
    public long getTotalUserCount() {
        return em.createQuery("SELECT COUNT(u) FROM Users u", Long.class).getSingleResult();
    }

}
