package com.springweb.repository;

import com.springweb.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

        List<Order> findByStatus(Order.OrderStatus status);

        Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

        List<Order> findByCustomerNameContainingIgnoreCase(String customerName);

        Optional<Order> findByOrderNumber(String orderNumber);

        @Query("SELECT o FROM Order o WHERE o.placedAt BETWEEN :startDate AND :endDate")
        List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT o FROM Order o WHERE o.placedAt BETWEEN :startDate AND :endDate")
        Page<Order> findByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        @Query("SELECT o FROM Order o WHERE o.totalAmount BETWEEN :minAmount AND :maxAmount")
        List<Order> findByTotalAmountRange(@Param("minAmount") BigDecimal minAmount,
                        @Param("maxAmount") BigDecimal maxAmount);

        @Query("SELECT o FROM Order o WHERE o.status = :status AND o.placedAt BETWEEN :startDate AND :endDate")
        List<Order> findByStatusAndDateRange(@Param("status") Order.OrderStatus status,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT o FROM Order o ORDER BY o.placedAt DESC")
        List<Order> findAllOrderByPlacedAtDesc();

        @Query("SELECT o FROM Order o ORDER BY o.totalAmount DESC")
        List<Order> findAllOrderByTotalAmountDesc();

        @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
        long countByStatus(@Param("status") Order.OrderStatus status);

        @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status != 'CANCELLED'")
        BigDecimal sumTotalRevenue();

        @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status != 'CANCELLED' AND o.placedAt BETWEEN :startDate AND :endDate")
        BigDecimal sumRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Enhanced search with customer information
        @Query("SELECT o FROM Order o WHERE o.customerName LIKE %:query% OR o.customerEmail LIKE %:query% OR o.orderNumber LIKE %:query% OR CAST(o.id AS string) LIKE %:query%")
        List<Order> searchOrders(@Param("query") String query);

        // Orders with their items for detailed view
        @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
        Optional<Order> findByIdWithItems(@Param("id") Long id);

        @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH o.user LEFT JOIN FETCH oi.product")
        List<Order> findAllWithItems();

        @Query(value = "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH o.user LEFT JOIN FETCH oi.product", countQuery = "SELECT COUNT(DISTINCT o) FROM Order o")
        Page<Order> findAllWithItemsPaged(Pageable pageable);

        @Query("SELECT DISTINCT o FROM Order o " +
                        "LEFT JOIN FETCH o.orderItems oi " +
                        "LEFT JOIN FETCH o.user " +
                        "LEFT JOIN FETCH oi.product " +
                        "WHERE o.customerName LIKE %:search% OR o.customerEmail LIKE %:search% OR o.orderNumber LIKE %:search%")
        List<Order> findAllWithSearch(@Param("search") String search);
}
