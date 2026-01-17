package com.springweb.controller;

import com.springweb.entity.Order;
import com.springweb.entity.OrderItem;
import com.springweb.entity.Product;
import com.springweb.repository.OrderRepository;
import com.springweb.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/simple-orders")
@CrossOrigin(origins = "*")
public class SimpleOrderController {

       @Autowired
       private OrderRepository orderRepository;

       @Autowired
       private ProductRepository productRepository;

       // Helper method to check authentication
       private boolean isAuthenticated(HttpSession session) {
              return session.getAttribute("authenticatedUser") != null;
       }

       @GetMapping
       public ResponseEntity<List<Order>> getAllOrders(HttpSession session) {
              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }
              return ResponseEntity.ok(orderRepository.findAll());
       }

       @GetMapping("/{id}")
       public ResponseEntity<Order> getOrderById(@PathVariable Long id, HttpSession session) {
              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Optional<Order> order = orderRepository.findById(id);
              return order.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
       }

       @PostMapping
       public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData,
                     HttpSession session) {
              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Map<String, Object> response = new HashMap<>();
              try {
                     @SuppressWarnings("unchecked")
                     List<Map<String, Object>> orderItemsData = (List<Map<String, Object>>) orderData.get("orderItems");

                     if (orderItemsData == null || orderItemsData.isEmpty()) {
                            response.put("success", false);
                            response.put("message", "Order items are required");
                            return ResponseEntity.badRequest().body(response);
                     }

                     Order order = new Order();
                     List<OrderItem> orderItems = new ArrayList<>();
                     BigDecimal totalAmount = BigDecimal.ZERO;

                     for (Map<String, Object> itemData : orderItemsData) {
                            Long productId = Long.valueOf(itemData.get("productId").toString());
                            Integer quantity = Integer.valueOf(itemData.get("quantity").toString());

                            Optional<Product> productOpt = productRepository.findById(productId);
                            if (!productOpt.isPresent()) {
                                   response.put("success", false);
                                   response.put("message", "Product not found: " + productId);
                                   return ResponseEntity.badRequest().body(response);
                            }

                            Product product = productOpt.get();
                            if (product.getStockQuantity() < quantity) {
                                   response.put("success", false);
                                   response.put("message", "Insufficient stock for product: " + product.getName());
                                   return ResponseEntity.badRequest().body(response);
                            }

                            OrderItem orderItem = new OrderItem();
                            orderItem.setProduct(product);
                            orderItem.setQuantity(quantity);
                            orderItem.setUnitPrice(product.getPrice());
                            orderItem.setOrder(order);

                            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                            totalAmount = totalAmount.add(itemTotal);

                            // Update stock
                            product.setStockQuantity(product.getStockQuantity() - quantity);
                            productRepository.save(product);

                            orderItems.add(orderItem);
                     }

                     order.setOrderItems(orderItems);
                     order.setTotalAmount(totalAmount);
                     order.setStatus(Order.OrderStatus.PENDING); // Use enum instead of string
                     order.setPlacedAt(LocalDateTime.now());

                     Order savedOrder = orderRepository.save(order);

                     response.put("success", true);
                     response.put("message", "Order created successfully");
                     response.put("order", savedOrder);
                     return ResponseEntity.ok(response);

              } catch (Exception e) {
                     response.put("success", false);
                     response.put("message", "Error creating order: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }

       @PutMapping("/{id}/status")
       public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long id,
                     @RequestBody Map<String, String> statusData, HttpSession session) {
              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Map<String, Object> response = new HashMap<>();
              try {
                     Optional<Order> orderOpt = orderRepository.findById(id);
                     if (!orderOpt.isPresent()) {
                            response.put("success", false);
                            response.put("message", "Order not found");
                            return ResponseEntity.status(404).body(response);
                     }

                     Order order = orderOpt.get();
                     String newStatus = statusData.get("status");

                     // Validate and convert string to enum
                     Order.OrderStatus orderStatus;
                     try {
                            orderStatus = Order.OrderStatus.valueOf(newStatus.toUpperCase());
                     } catch (IllegalArgumentException e) {
                            response.put("success", false);
                            response.put("message", "Invalid status. Valid statuses: "
                                          + Arrays.toString(Order.OrderStatus.values()));
                            return ResponseEntity.badRequest().body(response);
                     }

                     order.setStatus(orderStatus);
                     orderRepository.save(order);

                     response.put("success", true);
                     response.put("message", "Order status updated successfully");
                     response.put("order", order);
                     return ResponseEntity.ok(response);

              } catch (Exception e) {
                     response.put("success", false);
                     response.put("message", "Error updating order status: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }

       @DeleteMapping("/{id}")
       public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable Long id, HttpSession session) {
              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Map<String, Object> response = new HashMap<>();
              try {
                     if (!orderRepository.existsById(id)) {
                            response.put("success", false);
                            response.put("message", "Order not found");
                            return ResponseEntity.status(404).body(response);
                     }

                     orderRepository.deleteById(id);

                     response.put("success", true);
                     response.put("message", "Order deleted successfully");
                     return ResponseEntity.ok(response);

              } catch (Exception e) {
                     response.put("success", false);
                     response.put("message", "Error deleting order: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }
}