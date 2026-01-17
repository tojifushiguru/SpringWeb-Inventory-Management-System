package com.springweb.repository;

import com.springweb.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByNameContainingIgnoreCase(String name);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Category> findByName(String name);

    @Query("SELECT c FROM Category c WHERE c.name LIKE %:name%")
    List<Category> searchByName(@Param("name") String name);

    @Query("SELECT c FROM Category c WHERE c.description LIKE %:description%")
    List<Category> searchByDescription(@Param("description") String description);

    @Query("SELECT c FROM Category c ORDER BY c.name ASC")
    List<Category> findAllOrderByName();

    // Method to find categories with product count (if needed later)
    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN Product p ON p.category = c GROUP BY c")
    List<Object[]> findAllWithProductCount();
}
