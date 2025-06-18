/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package EJB;

import Entities.Users;
import jakarta.ejb.Stateless;
import jakarta.ejb.LocalBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 *
 * @author LENOVO
 */
@Stateless
@LocalBean
public class UsersBean {

    @PersistenceContext(unitName = "eduott")
    private EntityManager em;

    public List<Users> getAllUsers() {
        return em.createNamedQuery("Users.findAll", Users.class).getResultList();
    }

    public List<Users> searchUsers(String searchTerm) {
        return em.createQuery("SELECT u FROM Users u WHERE LOWER(u.fullName) LIKE :term OR LOWER(u.email) LIKE :term", Users.class)
                .setParameter("term", "%" + searchTerm.toLowerCase() + "%")
                .getResultList();
    }

    public long getTotalUserCount() {
        return em.createQuery("SELECT COUNT(u) FROM Users u", Long.class).getSingleResult();
    }

}
