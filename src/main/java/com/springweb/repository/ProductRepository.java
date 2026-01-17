package com.springweb.repository;

import com.springweb.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Product> findByNameContainingIgnoreCase(String name);

    Optional<Product> findBySku(String sku);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findBySupplierId(Long supplierId);

    List<Product> findByIsActiveTrue();

    List<Product> findByIsActiveFalse();

    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold")
    List<Product> findByStockLessThan(@Param("threshold") int threshold);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold AND p.isActive = true")
    List<Product> findLowStockActiveProducts(@Param("threshold") int threshold);

    @Query("SELECT p.category.name, COUNT(p), SUM(p.stockQuantity), SUM(p.price * p.stockQuantity) FROM Product p GROUP BY p.category.name")
    List<Object[]> getInventoryStatsByCategory();
}
