package com.springweb.repository;

import com.springweb.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByNameContainingIgnoreCase(String name);

    List<Supplier> findByContactEmailContainingIgnoreCase(String contactEmail);

    List<Supplier> findByNameContainingIgnoreCaseOrContactNameContainingIgnoreCase(String name, String contactName);

    Page<Supplier> findByNameContainingIgnoreCaseOrContactNameContainingIgnoreCase(String name, String contactName,
            Pageable pageable);

    Optional<Supplier> findByName(String name);

    Optional<Supplier> findByContactEmail(String contactEmail);

    Optional<Supplier> findByPhone(String phone);

    @Query("SELECT s FROM Supplier s WHERE s.contactName LIKE %:contactName%")
    List<Supplier> searchByContactName(@Param("contactName") String contactName);

    @Query("SELECT s FROM Supplier s WHERE s.address LIKE %:address%")
    List<Supplier> searchByAddress(@Param("address") String address);

    @Query("SELECT s FROM Supplier s ORDER BY s.name ASC")
    List<Supplier> findAllOrderByName();

    @Query("SELECT s FROM Supplier s WHERE s.contactEmail IS NOT NULL AND s.contactEmail != ''")
    List<Supplier> findAllWithContactEmail();

    // Method to find suppliers with product count (if needed later)
    @Query("SELECT s, COUNT(p) FROM Supplier s LEFT JOIN Product p ON p.supplier = s GROUP BY s")
    List<Object[]> findAllWithProductCount();
}
