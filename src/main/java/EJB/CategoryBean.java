/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package EJB;

import Entities.Categories;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author LENOVO
 */
@Stateless
public class CategoryBean implements CategoryBeanLocal {

    @PersistenceContext(unitName = "eduott")
    EntityManager em;

    @Override
    public void create(Categories category) {
        em.persist(category);
    }

    @Override
    public void update(Categories category) {
        em.merge(category);
    }

    @Override
    public void delete(int categoryId) {
        Categories category = em.find(Categories.class, categoryId);
        if (category != null) {
            em.remove(category);
        }
    }

    @Override
    public Categories findById(int categoryId) {
        return em.find(Categories.class, categoryId);
    }

    @Override
    public List<Categories> findAll() {
        return em.createNamedQuery("Categories.findAll", Categories.class).getResultList();
    }

    public Categories findCategoryByName(String categoryName) {
        try {
            TypedQuery<Categories> query = em.createQuery(
                    "SELECT c FROM Categories c WHERE c.categoryName = :categoryName",
                    Categories.class
            );
            query.setParameter("categoryName", categoryName);
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
}
