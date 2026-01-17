package com.springweb.repository;

import com.springweb.entity.Transaction;
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
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByOrderId(Long orderId);

    Page<Transaction> findByOrderId(Long orderId, Pageable pageable);

    List<Transaction> findByTransactionStatus(Transaction.TransactionStatus status);

    Page<Transaction> findByTransactionStatus(Transaction.TransactionStatus status, Pageable pageable);

    // Alias for backward compatibility
    default List<Transaction> findByStatus(Transaction.TransactionStatus status) {
        return findByTransactionStatus(status);
    }

    List<Transaction> findByTransactionType(Transaction.TransactionType transactionType);

    Page<Transaction> findByTransactionType(Transaction.TransactionType transactionType, Pageable pageable);

    List<Transaction> findByPaymentMethod(Transaction.PaymentMethod paymentMethod);

    Page<Transaction> findByPaymentMethod(Transaction.PaymentMethod paymentMethod, Pageable pageable);

    Optional<Transaction> findByTransactionNumber(String transactionNumber);

    // For backward compatibility with string-based searches
    @Query("SELECT t FROM Transaction t WHERE CAST(t.transactionType AS string) = :type")
    List<Transaction> findByType(@Param("type") String type);

    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate")
    Page<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.amount BETWEEN :minAmount AND :maxAmount")
    List<Transaction> findByAmountRange(@Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount);

    @Query("SELECT t FROM Transaction t WHERE t.transactionStatus = :status AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByStatusAndDateRange(@Param("status") Transaction.TransactionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE t.transactionType = :transactionType AND t.transactionStatus = :status")
    List<Transaction> findByTransactionTypeAndStatus(
            @Param("transactionType") Transaction.TransactionType transactionType,
            @Param("status") Transaction.TransactionStatus status);

    @Query("SELECT t FROM Transaction t ORDER BY t.transactionDate DESC")
    List<Transaction> findAllOrderByTransactionDateDesc();

    @Query("SELECT t FROM Transaction t ORDER BY t.amount DESC")
    List<Transaction> findAllOrderByAmountDesc();

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.transactionStatus = :status")
    long countByStatus(@Param("status") Transaction.TransactionStatus status);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.transactionType = :transactionType")
    long countByTransactionType(@Param("transactionType") Transaction.TransactionType transactionType);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.transactionType = :transactionType AND t.transactionStatus = 'COMPLETED'")
    BigDecimal sumAmountByTransactionTypeAndStatus(
            @Param("transactionType") Transaction.TransactionType transactionType);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.transactionStatus = 'COMPLETED' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumCompletedAmountByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT t.paymentMethod FROM Transaction t")
    List<Transaction.PaymentMethod> findAllPaymentMethods();

    @Query("SELECT t FROM Transaction t WHERE t.order IS NULL")
    List<Transaction> findTransactionsWithoutOrder();

    @Query("SELECT t FROM Transaction t WHERE t.order.id = :orderId AND t.transactionStatus = :status")
    List<Transaction> findByOrderIdAndStatus(@Param("orderId") Long orderId,
            @Param("status") Transaction.TransactionStatus status);

    // Get transactions with order details
    @Query("SELECT DISTINCT t FROM Transaction t LEFT JOIN FETCH t.order WHERE t.id = :id")
    Optional<Transaction> findByIdWithOrder(@Param("id") Long id);

    @Query("SELECT DISTINCT t FROM Transaction t LEFT JOIN FETCH t.order")
    List<Transaction> findAllWithOrder();

    // Financial reporting queries
    @Query("SELECT t.transactionType, SUM(t.amount) FROM Transaction t WHERE t.transactionStatus = 'COMPLETED' GROUP BY t.transactionType")
    List<Object[]> getRevenueByTransactionType();

    @Query("SELECT t.paymentMethod, COUNT(t), SUM(t.amount) FROM Transaction t WHERE t.transactionStatus = 'COMPLETED' GROUP BY t.paymentMethod")
    List<Object[]> getStatsByPaymentMethod();
}
