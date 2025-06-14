/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/SessionLocal.java to edit this template
 */
package EJB;

import Entities.Categories;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author LENOVO
 */
@Local
public interface CategoryBeanLocal {

    void create(Categories category);

    void update(Categories category);

    void delete(int categoryId);

    Categories findById(int categoryId);

    List<Categories> findAll();

    public Categories findCategoryByName(String categoryName);

}
