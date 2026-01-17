package com.springweb.controller;

import com.springweb.entity.*;
import com.springweb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

       private static final String AUTHENTICATED_USER = "authenticatedUser";
       private static final String SUCCESS = "success";
       private static final String MESSAGE = "message";

       @Autowired
       private OrderRepository orderRepository;

       @Autowired
       private ProductRepository productRepository;

       @Autowired
       private TransactionRepository transactionRepository;

       @Autowired
       private CategoryRepository categoryRepository;

       // Helper method to check authentication
       private boolean isAuthenticated(HttpSession session) {
              return session.getAttribute(AUTHENTICATED_USER) != null;
       }

       // SALES & REVENUE REPORTS

       @GetMapping("/sales")
       public ResponseEntity<Map<String, Object>> generateSalesReport(
                     @RequestParam(required = false) String startDate,
                     @RequestParam(required = false) String endDate,
                     @RequestParam(required = false) String category,
                     HttpSession session) {

              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Map<String, Object> response = new HashMap<>();
              try {
                     LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate + "T00:00:00")
                                   : LocalDateTime.now().minusMonths(1);
                     LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate + "T23:59:59")
                                   : LocalDateTime.now();

                     List<Order> orders = orderRepository.findByDateRange(start, end);

                     // Filter by category if specified
                     if (category != null && !category.isEmpty()) {
                            orders = orders.stream()
                                          .filter(order -> order.getOrderItems().stream()
                                                        .anyMatch(item -> item.getProduct().getCategory().getName()
                                                                      .equalsIgnoreCase(category)))
                                          .collect(Collectors.toList());
                     }

                     // Calculate metrics
                     BigDecimal totalSales = orders.stream()
                                   .map(Order::getTotalAmount)
                                   .reduce(BigDecimal.ZERO, BigDecimal::add);

                     long totalOrders = orders.size();

                     // Group sales by category
                     Map<String, BigDecimal> salesByCategory = new HashMap<>();
                     orders.forEach(order -> {
                            order.getOrderItems().forEach(item -> {
                                   String cat = item.getProduct().getCategory().getName();
                                   BigDecimal amount = item.getProduct().getPrice()
                                                 .multiply(new BigDecimal(item.getQuantity()));
                                   salesByCategory.merge(cat, amount, BigDecimal::add);
                            });
                     });

                     // Create report data
                     Map<String, Object> reportData = new HashMap<>();
                     reportData.put("totalSales", totalSales);
                     reportData.put("totalOrders", totalOrders);
                     reportData.put("salesByCategory", salesByCategory);
                     reportData.put("startDate", start);
                     reportData.put("endDate", end);

                     response.put(SUCCESS, true);
                     response.put("report", reportData);
                     return ResponseEntity.ok(response);

              } catch (Exception e) {
                     response.put(SUCCESS, false);
                     response.put(MESSAGE, "Error generating sales report: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }

       @GetMapping("/revenue")
       public ResponseEntity<Map<String, Object>> generateRevenueAnalysis(
                     @RequestParam(required = false) String startDate,
                     @RequestParam(required = false) String endDate,
                     @RequestParam(required = false) String groupBy, // daily, weekly, monthly
                     HttpSession session) {

              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Map<String, Object> response = new HashMap<>();
              try {
                     LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate + "T00:00:00")
                                   : LocalDateTime.now().minusMonths(1);
                     LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate + "T23:59:59")
                                   : LocalDateTime.now();

                     List<Transaction> transactions = transactionRepository.findByDateRange(start, end);

                     // Calculate revenue metrics
                     BigDecimal totalRevenue = transactions.stream()
                                   .filter(t -> t.getStatus() == Transaction.TransactionStatus.COMPLETED)
                                   .map(Transaction::getAmount)
                                   .reduce(BigDecimal.ZERO, BigDecimal::add);

                     BigDecimal totalRefunds = transactions.stream()
                                   .filter(t -> t.getTransactionType() == Transaction.TransactionType.REFUND)
                                   .map(Transaction::getAmount)
                                   .reduce(BigDecimal.ZERO, BigDecimal::add);

                     // Group transactions by time period
                     Map<String, BigDecimal> revenueByPeriod = new TreeMap<>();
                     DateTimeFormatter formatter = getFormatterForGrouping(groupBy);

                     transactions.forEach(transaction -> {
                            String period = transaction.getCreatedAt().format(formatter);
                            if (transaction.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                                   revenueByPeriod.merge(period, transaction.getAmount(), BigDecimal::add);
                            }
                     });

                     // Create report data
                     Map<String, Object> reportData = new HashMap<>();
                     reportData.put("totalRevenue", totalRevenue);
                     reportData.put("totalRefunds", totalRefunds);
                     reportData.put("netRevenue", totalRevenue.subtract(totalRefunds));
                     reportData.put("revenueByPeriod", revenueByPeriod);
                     reportData.put("startDate", start);
                     reportData.put("endDate", end);

                     response.put(SUCCESS, true);
                     response.put("report", reportData);
                     return ResponseEntity.ok(response);

              } catch (Exception e) {
                     response.put(SUCCESS, false);
                     response.put(MESSAGE, "Error generating revenue analysis: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }

       // INVENTORY REPORTS

       @GetMapping("/inventory")
       public ResponseEntity<Map<String, Object>> generateInventoryReport(
                     @RequestParam(required = false) String category,
                     HttpSession session) {

              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Map<String, Object> response = new HashMap<>();
              try {
                     List<Product> products = productRepository.findAll();

                     // Filter by category if specified
                     if (category != null && !category.isEmpty()) {
                            products = products.stream()
                                          .filter(product -> product.getCategory().getName().equalsIgnoreCase(category))
                                          .collect(Collectors.toList());
                     }

                     // Calculate inventory metrics
                     long totalProducts = products.size();
                     BigDecimal totalValue = products.stream()
                                   .map(product -> product.getPrice()
                                                 .multiply(new BigDecimal(product.getStockQuantity())))
                                   .reduce(BigDecimal.ZERO, BigDecimal::add);

                     // Group products by category
                     Map<String, List<Map<String, Object>>> productsByCategory = products.stream()
                                   .collect(Collectors.groupingBy(
                                                 product -> product.getCategory().getName(),
                                                 Collectors.mapping(
                                                               product -> {
                                                                      Map<String, Object> productData = new HashMap<>();
                                                                      productData.put("id", product.getId());
                                                                      productData.put("name", product.getName());
                                                                      productData.put("stock",
                                                                                    product.getStockQuantity());
                                                                      productData.put("price", product.getPrice());
                                                                      productData.put("value", product.getPrice()
                                                                                    .multiply(new BigDecimal(product
                                                                                                  .getStockQuantity())));
                                                                      return productData;
                                                               },
                                                               Collectors.toList())));

                     // Create report data
                     Map<String, Object> reportData = new HashMap<>();
                     reportData.put("totalProducts", totalProducts);
                     reportData.put("totalInventoryValue", totalValue);
                     reportData.put("productsByCategory", productsByCategory);
                     reportData.put("generatedAt", LocalDateTime.now());

                     response.put(SUCCESS, true);
                     response.put("report", reportData);
                     return ResponseEntity.ok(response);

              } catch (Exception e) {
                     response.put(SUCCESS, false);
                     response.put(MESSAGE, "Error generating inventory report: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }

       @GetMapping("/low-stock")
       public ResponseEntity<Map<String, Object>> generateLowStockAlert(
                     @RequestParam(defaultValue = "10") int threshold,
                     HttpSession session) {

              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Map<String, Object> response = new HashMap<>();
              try {
                     List<Product> lowStockProducts = productRepository.findByStockLessThan(threshold);

                     // Group low stock products by category
                     Map<String, List<Map<String, Object>>> lowStockByCategory = lowStockProducts.stream()
                                   .collect(Collectors.groupingBy(
                                                 product -> product.getCategory().getName(),
                                                 Collectors.mapping(
                                                               product -> {
                                                                      Map<String, Object> productData = new HashMap<>();
                                                                      productData.put("id", product.getId());
                                                                      productData.put("name", product.getName());
                                                                      productData.put("stock",
                                                                                    product.getStockQuantity());
                                                                      productData.put("threshold", threshold);
                                                                      return productData;
                                                               },
                                                               Collectors.toList())));

                     // Create report data
                     Map<String, Object> reportData = new HashMap<>();
                     reportData.put("totalLowStockProducts", lowStockProducts.size());
                     reportData.put("threshold", threshold);
                     reportData.put("lowStockByCategory", lowStockByCategory);
                     reportData.put("generatedAt", LocalDateTime.now());

                     response.put(SUCCESS, true);
                     response.put("report", reportData);
                     return ResponseEntity.ok(response);

              } catch (Exception e) {
                     response.put(SUCCESS, false);
                     response.put(MESSAGE, "Error generating low stock alert: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }

       // FINANCIAL REPORTS

       @GetMapping("/transaction-summary")
       public ResponseEntity<Map<String, Object>> generateTransactionSummary(
                     @RequestParam(required = false) String startDate,
                     @RequestParam(required = false) String endDate,
                     HttpSession session) {

              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Map<String, Object> response = new HashMap<>();
              try {
                     LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate + "T00:00:00")
                                   : LocalDateTime.now().minusMonths(1);
                     LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate + "T23:59:59")
                                   : LocalDateTime.now();

                     List<Transaction> transactions = transactionRepository.findByDateRange(start, end);

                     // Calculate transaction metrics
                     long totalTransactions = transactions.size();

                     // Group by transaction type
                     Map<Transaction.TransactionType, List<Transaction>> byType = transactions.stream()
                                   .collect(Collectors.groupingBy(Transaction::getTransactionType));

                     // Group by payment method
                     Map<Transaction.PaymentMethod, List<Transaction>> byPaymentMethod = transactions.stream()
                                   .collect(Collectors.groupingBy(Transaction::getPaymentMethod));

                     // Calculate totals by type
                     Map<String, BigDecimal> totalsByType = new HashMap<>();
                     byType.forEach((type, txList) -> {
                            BigDecimal total = txList.stream()
                                          .map(Transaction::getAmount)
                                          .reduce(BigDecimal.ZERO, BigDecimal::add);
                            totalsByType.put(type.toString(), total);
                     });

                     // Calculate totals by payment method
                     Map<String, BigDecimal> totalsByPaymentMethod = new HashMap<>();
                     byPaymentMethod.forEach((method, txList) -> {
                            BigDecimal total = txList.stream()
                                          .map(Transaction::getAmount)
                                          .reduce(BigDecimal.ZERO, BigDecimal::add);
                            totalsByPaymentMethod.put(method.toString(), total);
                     });

                     // Create report data
                     Map<String, Object> reportData = new HashMap<>();
                     reportData.put("totalTransactions", totalTransactions);
                     reportData.put("totalsByType", totalsByType);
                     reportData.put("totalsByPaymentMethod", totalsByPaymentMethod);
                     reportData.put("startDate", start);
                     reportData.put("endDate", end);

                     response.put(SUCCESS, true);
                     response.put("report", reportData);
                     return ResponseEntity.ok(response);

              } catch (Exception e) {
                     response.put(SUCCESS, false);
                     response.put(MESSAGE, "Error generating transaction summary: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }

       @GetMapping("/financial-summary")
       public ResponseEntity<Map<String, Object>> generateFinancialSummary(
                     @RequestParam(required = false) String startDate,
                     @RequestParam(required = false) String endDate,
                     HttpSession session) {

              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Map<String, Object> response = new HashMap<>();
              try {
                     LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate + "T00:00:00")
                                   : LocalDateTime.now().minusMonths(1);
                     LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate + "T23:59:59")
                                   : LocalDateTime.now();

                     // Get orders and transactions for the period
                     List<Order> orders = orderRepository.findByDateRange(start, end);
                     List<Transaction> transactions = transactionRepository.findByDateRange(start, end);

                     // Calculate revenue metrics
                     BigDecimal totalRevenue = transactions.stream()
                                   .filter(t -> t.getStatus() == Transaction.TransactionStatus.COMPLETED)
                                   .map(Transaction::getAmount)
                                   .reduce(BigDecimal.ZERO, BigDecimal::add);

                     BigDecimal totalRefunds = transactions.stream()
                                   .filter(t -> t.getTransactionType() == Transaction.TransactionType.REFUND)
                                   .map(Transaction::getAmount)
                                   .reduce(BigDecimal.ZERO, BigDecimal::add);

                     BigDecimal netRevenue = totalRevenue.subtract(totalRefunds);

                     // Calculate order metrics
                     long totalOrders = orders.size();
                     BigDecimal averageOrderValue = totalOrders > 0
                                   ? totalRevenue.divide(new BigDecimal(totalOrders), 2, BigDecimal.ROUND_HALF_UP)
                                   : BigDecimal.ZERO;

                     // Create report data
                     Map<String, Object> reportData = new HashMap<>();
                     reportData.put("totalRevenue", totalRevenue);
                     reportData.put("totalRefunds", totalRefunds);
                     reportData.put("netRevenue", netRevenue);
                     reportData.put("totalOrders", totalOrders);
                     reportData.put("averageOrderValue", averageOrderValue);
                     reportData.put("startDate", start);
                     reportData.put("endDate", end);

                     response.put(SUCCESS, true);
                     response.put("report", reportData);
                     return ResponseEntity.ok(response);

              } catch (Exception e) {
                     response.put(SUCCESS, false);
                     response.put(MESSAGE, "Error generating financial summary: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }

       // Helper method for revenue analysis
       private DateTimeFormatter getFormatterForGrouping(String groupBy) {
              if (groupBy == null) {
                     groupBy = "daily";
              }

              switch (groupBy.toLowerCase()) {
                     case "weekly":
                            return DateTimeFormatter.ofPattern("yyyy-'W'ww");
                     case "monthly":
                            return DateTimeFormatter.ofPattern("yyyy-MM");
                     case "daily":
                     default:
                            return DateTimeFormatter.ofPattern("yyyy-MM-dd");
              }
       }
}