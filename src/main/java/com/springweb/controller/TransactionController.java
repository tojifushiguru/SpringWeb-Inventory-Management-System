package com.springweb.controller;

import com.springweb.entity.Order;
import com.springweb.entity.Transaction;
import com.springweb.repository.OrderRepository;
import com.springweb.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private static final String AUTHENTICATED_USER = "authenticatedUser";
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Helper method to check authentication
    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute(AUTHENTICATED_USER) != null;
    }

    // Helper method to generate transaction number
    private String generateTransactionNumber() {
        return "TXN" + System.currentTimeMillis();
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpSession session) {

        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            if (page == -1) {
                // Return all transactions without pagination
                List<Transaction> transactions = transactionRepository.findAll(sort);

                // Apply filters
                if (search != null && !search.trim().isEmpty()) {
                    transactions = transactions.stream()
                            .filter(tx -> tx.getTransactionNumber().toLowerCase().contains(search.toLowerCase()) ||
                                    (tx.getDescription() != null
                                            && tx.getDescription().toLowerCase().contains(search.toLowerCase())))
                            .toList();
                }

                if (status != null && !status.trim().isEmpty()) {
                    try {
                        Transaction.TransactionStatus txStatus = Transaction.TransactionStatus
                                .valueOf(status.toUpperCase());
                        transactions = transactions.stream()
                                .filter(tx -> tx.getStatus() == txStatus)
                                .toList();
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                if (type != null && !type.trim().isEmpty()) {
                    try {
                        Transaction.TransactionType targetType = Transaction.TransactionType
                                .valueOf(type.toUpperCase());
                        transactions = transactions.stream()
                                .filter(tx -> tx.getTransactionType().equals(targetType))
                                .toList();
                    } catch (IllegalArgumentException ignored) {
                        // Invalid transaction type, filter will return empty result
                    }
                }

                if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
                    try {
                        Transaction.PaymentMethod txPayment = Transaction.PaymentMethod
                                .valueOf(paymentMethod.toUpperCase());
                        transactions = transactions.stream()
                                .filter(tx -> tx.getPaymentMethod() == txPayment)
                                .toList();
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                response.put(SUCCESS, true);
                response.put("transactions", transactions);
            } else {
                // Return paginated results
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<Transaction> transactionPage = transactionRepository.findAll(pageable);

                response.put(SUCCESS, true);
                response.put("transactions", transactionPage.getContent());
                response.put("totalElements", transactionPage.getTotalElements());
                response.put("totalPages", transactionPage.getTotalPages());
                response.put("currentPage", page);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching transactions: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTransactionById(@PathVariable Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Transaction> transaction = transactionRepository.findById(id);
            if (transaction.isPresent()) {
                response.put(SUCCESS, true);
                response.put("transaction", transaction.get());
                return ResponseEntity.ok(response);
            } else {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Transaction not found");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching transaction: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTransaction(@RequestBody Map<String, Object> transactionData,
            HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            // Validate required fields
            Object amountObj = transactionData.get("amount");
            String typeStr = (String) transactionData.get("transactionType"); // Changed from "type" to match frontend
            String paymentMethodStr = (String) transactionData.get("paymentMethod");
            String description = (String) transactionData.get("description");
            Object orderIdObj = transactionData.get("orderId");

            if (amountObj == null) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Amount is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (typeStr == null || typeStr.trim().isEmpty()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Transaction type is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (paymentMethodStr == null || paymentMethodStr.trim().isEmpty()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Payment method is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Parse and validate amount with better error handling
            BigDecimal amount;
            try {
                if (amountObj instanceof BigDecimal) {
                    amount = (BigDecimal) amountObj;
                } else if (amountObj instanceof Double || amountObj instanceof Integer) {
                    amount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
                } else {
                    amount = new BigDecimal(amountObj.toString());
                }

                // Validate amount is greater than zero and has valid scale
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Amount must be greater than 0");
                    return ResponseEntity.badRequest().body(response);
                }

                // Set scale to 2 decimal places
                amount = amount.setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException | ArithmeticException e) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Invalid amount format");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate and parse enums
            Transaction.TransactionType type;
            Transaction.PaymentMethod paymentMethod;
            try {
                type = Transaction.TransactionType.valueOf(typeStr.toUpperCase());
                paymentMethod = Transaction.PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Invalid transaction type or payment method");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate order if provided
            Order order = null;
            if (orderIdObj != null) {
                try {
                    Long orderId = Long.valueOf(orderIdObj.toString());
                    Optional<Order> orderOpt = orderRepository.findById(orderId);
                    if (!orderOpt.isPresent()) {
                        response.put(SUCCESS, false);
                        response.put(MESSAGE, "Order not found: " + orderId);
                        return ResponseEntity.badRequest().body(response);
                    }
                    order = orderOpt.get();
                } catch (NumberFormatException e) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Invalid order ID format");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Create transaction
            Transaction transaction = new Transaction();
            transaction.setTransactionNumber(generateTransactionNumber());
            transaction.setOrder(order);
            transaction.setAmount(amount);
            transaction.setTransactionType(type);
            transaction.setPaymentMethod(paymentMethod);
            transaction.setTransactionStatus(Transaction.TransactionStatus.PENDING);
            transaction.setDescription(description != null ? description.trim() : null);
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setUpdatedAt(LocalDateTime.now());

            Transaction savedTransaction = transactionRepository.save(transaction);

            // Create success response with transaction details
            response.put(SUCCESS, true);
            response.put(MESSAGE, "Transaction created successfully");
            response.put("transaction", savedTransaction);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error creating transaction: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTransaction(@PathVariable Long id,
            @RequestBody Map<String, Object> transactionData, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Transaction> optionalTransaction = transactionRepository.findById(id);
            if (!optionalTransaction.isPresent()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Transaction not found");
                return ResponseEntity.status(404).body(response);
            }

            Transaction existingTransaction = optionalTransaction.get();

            // Update amount if provided
            Object amountObj = transactionData.get("amount");
            if (amountObj != null) {
                try {
                    BigDecimal amount = new BigDecimal(amountObj.toString());
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        response.put(SUCCESS, false);
                        response.put(MESSAGE, "Amount must be greater than 0");
                        return ResponseEntity.badRequest().body(response);
                    }
                    existingTransaction.setAmount(amount);
                } catch (NumberFormatException e) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Invalid amount format");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Update type if provided
            String typeStr = (String) transactionData.get("transactionType"); // Changed from "type" to match frontend
            if (typeStr != null && !typeStr.trim().isEmpty()) {
                try {
                    Transaction.TransactionType type = Transaction.TransactionType.valueOf(typeStr.toUpperCase());
                    existingTransaction.setTransactionType(type);
                } catch (IllegalArgumentException e) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Invalid transaction type: " + typeStr);
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Update payment method if provided
            String paymentMethodStr = (String) transactionData.get("paymentMethod");
            if (paymentMethodStr != null && !paymentMethodStr.trim().isEmpty()) {
                try {
                    Transaction.PaymentMethod paymentMethod = Transaction.PaymentMethod
                            .valueOf(paymentMethodStr.toUpperCase());
                    existingTransaction.setPaymentMethod(paymentMethod);
                } catch (IllegalArgumentException e) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Invalid payment method: " + paymentMethodStr);
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Update description
            String description = (String) transactionData.get("description");
            if (description != null) {
                existingTransaction.setDescription(description.trim().isEmpty() ? null : description.trim());
            }

            existingTransaction.setUpdatedAt(LocalDateTime.now());

            Transaction savedTransaction = transactionRepository.save(existingTransaction);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Transaction updated successfully");
            response.put("transaction", savedTransaction);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error updating transaction: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateTransactionStatus(@PathVariable Long id,
            @RequestBody Map<String, String> statusData, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Transaction> optionalTransaction = transactionRepository.findById(id);
            if (!optionalTransaction.isPresent()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Transaction not found");
                return ResponseEntity.status(404).body(response);
            }

            String statusStr = statusData.get("status");
            if (statusStr == null || statusStr.trim().isEmpty()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Status is required");
                return ResponseEntity.badRequest().body(response);
            }

            try {
                Transaction.TransactionStatus newStatus = Transaction.TransactionStatus
                        .valueOf(statusStr.toUpperCase());
                Transaction existingTransaction = optionalTransaction.get();

                existingTransaction.setStatus(newStatus);
                existingTransaction.setUpdatedAt(LocalDateTime.now());

                // If transaction is completed, update order status if associated
                if ("COMPLETED".equals(newStatus) && existingTransaction.getOrder() != null) {
                    Order order = existingTransaction.getOrder();
                    if (order.getStatus() == Order.OrderStatus.PENDING) {
                        order.setStatus(Order.OrderStatus.CONFIRMED);
                        order.setUpdatedAt(LocalDateTime.now());
                        orderRepository.save(order);
                    }
                }

                Transaction savedTransaction = transactionRepository.save(existingTransaction);

                response.put(SUCCESS, true);
                response.put(MESSAGE, "Transaction status updated successfully");
                response.put("transaction", savedTransaction);
                return ResponseEntity.ok(response);

            } catch (IllegalArgumentException e) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Invalid status: " + statusStr);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error updating transaction status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTransaction(@PathVariable Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            if (!transactionRepository.existsById(id)) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Transaction not found");
                return ResponseEntity.status(404).body(response);
            }

            transactionRepository.deleteById(id);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Transaction deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error deleting transaction: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Map<String, Object>> getTransactionsByOrder(@PathVariable Long orderId, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            List<Transaction> transactions = transactionRepository.findByOrderId(orderId);
            response.put(SUCCESS, true);
            response.put("transactions", transactions);
            response.put("count", transactions.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching transactions by order: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getTransactionsByStatus(@PathVariable String status,
            HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Transaction.TransactionStatus transactionStatus = Transaction.TransactionStatus
                    .valueOf(status.toUpperCase());
            List<Transaction> transactions = transactionRepository.findByStatus(transactionStatus);
            response.put(SUCCESS, true);
            response.put("transactions", transactions);
            response.put("count", transactions.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Invalid status: " + status);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching transactions by status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getTransactionsByType(@PathVariable String type, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            String transactionType = type.toUpperCase();
            List<Transaction> transactions = transactionRepository.findByType(transactionType);
            response.put(SUCCESS, true);
            response.put("transactions", transactions);
            response.put("count", transactions.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Invalid type: " + type);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching transactions by type: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/by-transaction-number/{transactionNumber}")
    public ResponseEntity<Map<String, Object>> getTransactionByNumber(@PathVariable String transactionNumber,
            HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Transaction> transaction = transactionRepository.findByTransactionNumber(transactionNumber);
            if (transaction.isPresent()) {
                response.put(SUCCESS, true);
                response.put("transaction", transaction.get());
                return ResponseEntity.ok(response);
            } else {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Transaction not found with number: " + transactionNumber);
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching transaction: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getTransactionsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpSession session) {

        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

            List<Transaction> transactions = transactionRepository.findByDateRange(start, end);
            response.put(SUCCESS, true);
            response.put("transactions", transactions);
            response.put("count", transactions.size());
            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Invalid date format. Use YYYY-MM-DD");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching transactions by date range: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTransactionStatistics(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            List<Transaction> allTransactions = transactionRepository.findAll();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTransactions", allTransactions.size());

            // Count by status
            long pending = allTransactions.stream().filter(t -> "PENDING".equals(t.getTransactionStatus()))
                    .count();
            long completed = allTransactions.stream()
                    .filter(t -> "COMPLETED".equals(t.getTransactionStatus())).count();
            long failed = allTransactions.stream().filter(t -> "FAILED".equals(t.getTransactionStatus()))
                    .count();
            long refunded = allTransactions.stream()
                    .filter(t -> "REFUNDED".equals(t.getTransactionStatus())).count();

            stats.put("pendingTransactions", pending);
            stats.put("completedTransactions", completed);
            stats.put("failedTransactions", failed);
            stats.put("refundedTransactions", refunded);

            // Calculate totals by type
            BigDecimal totalSales = allTransactions.stream()
                    .filter(t -> "SALE".equals(t.getTransactionType())
                            && "COMPLETED".equals(t.getTransactionStatus()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalRefunds = allTransactions.stream()
                    .filter(t -> "REFUND".equals(t.getTransactionType())
                            && "COMPLETED".equals(t.getTransactionStatus()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            stats.put("totalSalesAmount", totalSales);
            stats.put("totalRefundsAmount", totalRefunds);
            stats.put("netRevenue", totalSales.subtract(totalRefunds));

            response.put(SUCCESS, true);
            response.put("statistics", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching transaction statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
