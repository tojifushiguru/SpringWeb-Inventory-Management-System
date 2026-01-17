package com.springweb.controller;

import com.springweb.entity.Order;
import com.springweb.entity.OrderItem;
import com.springweb.entity.Product;
import com.springweb.entity.User;
import com.springweb.repository.OrderRepository;
import com.springweb.repository.ProductRepository;
import com.springweb.repository.UserRepository;
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
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private static final String AUTHENTICATED_USER = "authenticatedUser";
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // Helper method to check authentication
    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute(AUTHENTICATED_USER) != null;
    }

    // Helper method to validate email
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    // Helper method to generate order number
    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis();
    }

    // Helper method to calculate order total
    private BigDecimal calculateOrderTotal(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "placedAt") String sortBy,
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

            List<Order> orders;
            if (page == -1) {
                // Return all orders without pagination using the optimized query
                orders = orderRepository.findAllWithItems();

                // Apply filters if needed
                if (search != null && !search.trim().isEmpty()) {
                    orders = orderRepository.findAllWithSearch(search);
                }

                if (status != null && !status.trim().isEmpty()) {
                    try {
                        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                        orders = orders.stream()
                                .filter(order -> order.getStatus() == orderStatus)
                                .toList();
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                response.put(SUCCESS, true);
                response.put("orders", orders);
            } else {
                // Use the new paginated query with fetched relationships
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<Order> orderPage = orderRepository.findAllWithItemsPaged(pageable);
                orders = orderPage.getContent();

                // Apply filters if needed
                if (search != null && !search.trim().isEmpty()) {
                    orders = orderRepository.findAllWithSearch(search);
                }

                if (status != null && !status.trim().isEmpty()) {
                    try {
                        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                        orders = orders.stream()
                                .filter(order -> order.getStatus() == orderStatus)
                                .toList();
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                response.put(SUCCESS, true);
                response.put("orders", orders);
                response.put("totalElements", orderPage.getTotalElements());
                response.put("totalPages", orderPage.getTotalPages());
                response.put("currentPage", page);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching orders: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Order> order = orderRepository.findByIdWithItems(id);
            if (order.isPresent()) {
                response.put(SUCCESS, true);
                response.put("order", order.get());
                return ResponseEntity.ok(response);
            } else {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Order not found");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching order: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData,
            HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            // Extract customer information from request
            String customerName = (String) orderData.get("customerName");
            String customerEmail = (String) orderData.get("customerEmail");
            String customerPhone = (String) orderData.get("customerPhone");
            String customerAddress = (String) orderData.get("customerAddress");

            // Validate required fields - simplified
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderItemsData = (List<Map<String, Object>>) orderData.get("orderItems");

            if (orderItemsData == null || orderItemsData.isEmpty()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Order must have at least one item");
                return ResponseEntity.badRequest().body(response);
            }

            if (customerName == null || customerName.trim().isEmpty()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Customer name is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Create order
            Order order = new Order();
            order.setOrderNumber(generateOrderNumber());
            order.setCustomerName(customerName.trim());
            order.setCustomerEmail(customerEmail != null ? customerEmail.trim() : null);
            order.setCustomerPhone(customerPhone != null ? customerPhone.trim() : null);
            order.setCustomerAddress(customerAddress != null ? customerAddress.trim() : null);
            order.setStatus(Order.OrderStatus.PENDING);
            order.setPlacedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            // Get the authenticated user and set it for the order
            String authenticatedUser = (String) session.getAttribute(AUTHENTICATED_USER);
            if (authenticatedUser != null) {
                Optional<User> userOpt = userRepository.findByUsername(authenticatedUser);
                if (userOpt.isPresent()) {
                    order.setUser(userOpt.get());
                } else {
                    // Fallback: create a default user or use first available user
                    List<User> users = userRepository.findAll();
                    if (!users.isEmpty()) {
                        order.setUser(users.get(0));
                    }
                }
            }

            // Create order items
            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (Map<String, Object> itemData : orderItemsData) {
                Long productId = Long.valueOf(itemData.get("productId").toString());
                Integer quantity = Integer.valueOf(itemData.get("quantity").toString());

                if (quantity <= 0) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Quantity must be greater than 0");
                    return ResponseEntity.badRequest().body(response);
                }

                Optional<Product> productOpt = productRepository.findById(productId);
                if (!productOpt.isPresent()) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Product not found: " + productId);
                    return ResponseEntity.badRequest().body(response);
                }

                Product product = productOpt.get();

                // Check stock availability
                if (product.getStockQuantity() < quantity) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Insufficient stock for product: " + product.getName());
                    return ResponseEntity.badRequest().body(response);
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(quantity);
                orderItem.setUnitPrice(product.getPrice());

                // Calculate and set total price for this item (will also be set by @PrePersist)
                BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                orderItem.setTotalPrice(itemTotal);

                // Set the order relationship
                orderItem.setOrder(order);
                orderItems.add(orderItem);

                // Add to order total
                totalAmount = totalAmount.add(itemTotal);

                // Update product stock
                product.setStockQuantity(product.getStockQuantity() - quantity);
                productRepository.save(product);
            }

            order.setOrderItems(orderItems);
            order.setTotalAmount(totalAmount);

            Order savedOrder = orderRepository.save(order);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Order created successfully");
            response.put("order", savedOrder);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error creating order: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateOrder(@PathVariable Long id,
            @RequestBody Map<String, Object> orderData, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Order> optionalOrder = orderRepository.findById(id);
            if (!optionalOrder.isPresent()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Order not found");
                return ResponseEntity.status(404).body(response);
            }

            Order existingOrder = optionalOrder.get();

            // Update customer information
            String customerName = (String) orderData.get("customerName");
            if (customerName != null && !customerName.trim().isEmpty()) {
                existingOrder.setCustomerName(customerName.trim());
            }

            String customerEmail = (String) orderData.get("customerEmail");
            if (customerEmail != null) {
                if (!customerEmail.trim().isEmpty() && !isValidEmail(customerEmail)) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Invalid email format");
                    return ResponseEntity.badRequest().body(response);
                }
                existingOrder.setCustomerEmail(customerEmail.trim().isEmpty() ? null : customerEmail.trim());
            }

            String customerPhone = (String) orderData.get("customerPhone");
            if (customerPhone != null) {
                existingOrder.setCustomerPhone(customerPhone.trim().isEmpty() ? null : customerPhone.trim());
            }

            String customerAddress = (String) orderData.get("customerAddress");
            if (customerAddress != null) {
                existingOrder.setCustomerAddress(customerAddress.trim().isEmpty() ? null : customerAddress.trim());
            }

            // Handle order items update
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderItemsData = (List<Map<String, Object>>) orderData.get("orderItems");
            if (orderItemsData != null) {
                if (orderItemsData.isEmpty()) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Order must have at least one item");
                    return ResponseEntity.badRequest().body(response);
                }

                // Restore stock for existing items
                for (OrderItem existingItem : existingOrder.getOrderItems()) {
                    Product product = existingItem.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + existingItem.getQuantity());
                    productRepository.save(product);
                }

                // Create new order items
                List<OrderItem> newOrderItems = new ArrayList<>();

                // Update order items
                BigDecimal newTotalAmount = BigDecimal.ZERO;

                for (Map<String, Object> itemData : orderItemsData) {
                    Long productId = Long.valueOf(itemData.get("productId").toString());
                    Integer quantity = Integer.valueOf(itemData.get("quantity").toString());

                    if (quantity <= 0) {
                        response.put(SUCCESS, false);
                        response.put(MESSAGE, "Quantity must be greater than 0");
                        return ResponseEntity.badRequest().body(response);
                    }

                    Optional<Product> productOpt = productRepository.findById(productId);
                    if (!productOpt.isPresent()) {
                        response.put(SUCCESS, false);
                        response.put(MESSAGE, "Product not found: " + productId);
                        return ResponseEntity.badRequest().body(response);
                    }

                    Product product = productOpt.get();

                    // Check stock availability
                    if (product.getStockQuantity() < quantity) {
                        response.put(SUCCESS, false);
                        response.put(MESSAGE, "Insufficient stock for product: " + product.getName());
                        return ResponseEntity.badRequest().body(response);
                    }

                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(product);
                    orderItem.setQuantity(quantity);
                    orderItem.setUnitPrice(product.getPrice());

                    // Calculate and set total price for this item (will also be set by @PrePersist)
                    BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                    orderItem.setTotalPrice(itemTotal);

                    // Set the order relationship
                    orderItem.setOrder(existingOrder);
                    newOrderItems.add(orderItem);

                    // Add to order total
                    newTotalAmount = newTotalAmount.add(itemTotal);

                    // Update product stock
                    product.setStockQuantity(product.getStockQuantity() - quantity);
                    productRepository.save(product);
                }

                existingOrder.setOrderItems(newOrderItems);
                existingOrder.setTotalAmount(newTotalAmount);
            }

            existingOrder.setUpdatedAt(LocalDateTime.now());

            Order savedOrder = orderRepository.save(existingOrder);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Order updated successfully");
            response.put("order", savedOrder);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error updating order: " + e.getMessage());
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
            Optional<Order> optionalOrder = orderRepository.findById(id);
            if (!optionalOrder.isPresent()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Order not found");
                return ResponseEntity.status(404).body(response);
            }

            String statusStr = statusData.get("status");
            if (statusStr == null || statusStr.trim().isEmpty()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Status is required");
                return ResponseEntity.badRequest().body(response);
            }

            try {
                Order.OrderStatus newStatus = Order.OrderStatus.valueOf(statusStr.toUpperCase());
                Order existingOrder = optionalOrder.get();

                // If cancelling order, restore stock
                if (newStatus == Order.OrderStatus.CANCELLED
                        && existingOrder.getStatus() != Order.OrderStatus.CANCELLED) {
                    for (OrderItem item : existingOrder.getOrderItems()) {
                        Product product = item.getProduct();
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);
                    }
                }

                Order.OrderStatus oldStatus = existingOrder.getStatus();
                existingOrder.setStatus(newStatus);

                // If changing from a completed state back to processing/pending, restore stock
                if ((oldStatus == Order.OrderStatus.COMPLETED || oldStatus == Order.OrderStatus.DELIVERED)
                        && (newStatus == Order.OrderStatus.PROCESSING || newStatus == Order.OrderStatus.PENDING)) {
                    for (OrderItem item : existingOrder.getOrderItems()) {
                        Product product = item.getProduct();
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);
                    }
                }
                // If changing to completed/delivered state, update stock
                else if ((oldStatus == Order.OrderStatus.PROCESSING || oldStatus == Order.OrderStatus.PENDING)
                        && (newStatus == Order.OrderStatus.COMPLETED || newStatus == Order.OrderStatus.DELIVERED)) {
                    for (OrderItem item : existingOrder.getOrderItems()) {
                        Product product = item.getProduct();
                        product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                        productRepository.save(product);
                    }
                }
                existingOrder.setUpdatedAt(LocalDateTime.now());

                Order savedOrder = orderRepository.save(existingOrder);

                response.put(SUCCESS, true);
                response.put(MESSAGE, "Order status updated successfully");
                response.put("order", savedOrder);
                return ResponseEntity.ok(response);

            } catch (IllegalArgumentException e) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Invalid status: " + statusStr);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error updating order status: " + e.getMessage());
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
            Optional<Order> optionalOrder = orderRepository.findById(id);
            if (!optionalOrder.isPresent()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Order not found");
                return ResponseEntity.status(404).body(response);
            }

            Order order = optionalOrder.get();

            // Restore stock for all items
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }

            orderRepository.deleteById(id);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Order deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error deleting order: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchOrders(@RequestParam String query, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            List<Order> orders = orderRepository.findByCustomerNameContainingIgnoreCase(query);
            response.put(SUCCESS, true);
            response.put("orders", orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error searching orders: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getOrdersByStatus(@PathVariable String status, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderRepository.findByStatus(orderStatus);
            response.put(SUCCESS, true);
            response.put("orders", orders);
            response.put("count", orders.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Invalid status: " + status);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching orders by status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/by-order-number/{orderNumber}")
    public ResponseEntity<Map<String, Object>> getOrderByOrderNumber(@PathVariable String orderNumber,
            HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Order> order = orderRepository.findByOrderNumber(orderNumber);
            if (order.isPresent()) {
                response.put(SUCCESS, true);
                response.put("order", order.get());
                return ResponseEntity.ok(response);
            } else {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Order not found with order number: " + orderNumber);
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching order: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOrderStatistics(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            List<Order> allOrders = orderRepository.findAll();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", allOrders.size());
            stats.put("pendingOrders",
                    allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.PENDING).count());
            stats.put("completedOrders",
                    allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED).count());
            stats.put("cancelledOrders",
                    allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED).count());

            BigDecimal totalRevenue = allOrders.stream()
                    .filter(o -> o.getStatus() != Order.OrderStatus.CANCELLED)
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalRevenue", totalRevenue);

            response.put(SUCCESS, true);
            response.put("statistics", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching order statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
