package com.springweb.controller;

import com.springweb.entity.Product;
import com.springweb.repository.ProductRepository;
import com.springweb.repository.CategoryRepository;
import com.springweb.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private static final String AUTHENTICATED_USER = "authenticatedUser";
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    // Helper method to check authentication
    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute(AUTHENTICATED_USER) != null;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            HttpSession session) {

        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            // Handle page=-1 to return all products
            if (page == -1) {
                List<Product> allProducts;
                if (search != null && !search.trim().isEmpty()) {
                    allProducts = productRepository.findByNameContainingIgnoreCase(search);
                } else {
                    allProducts = productRepository.findAll();
                }

                response.put(SUCCESS, true);
                response.put("products", allProducts);
                response.put("totalElements", allProducts.size());
                response.put("totalPages", 1);
                response.put("currentPage", 0);
                return ResponseEntity.ok(response);
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Product> productPage;

            if (search != null && !search.trim().isEmpty()) {
                productPage = productRepository.findByNameContainingIgnoreCase(search, pageable);
            } else {
                productPage = productRepository.findAll(pageable);
            }

            response.put(SUCCESS, true);
            response.put("products", productPage.getContent());
            response.put("totalElements", productPage.getTotalElements());
            response.put("totalPages", productPage.getTotalPages());
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching products: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Product> product = productRepository.findById(id);
            if (product.isPresent()) {
                response.put(SUCCESS, true);
                response.put("product", product.get());
                return ResponseEntity.ok(response);
            } else {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Product not found");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching product: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody Product product, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            // Validate required fields
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Product name is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Product price must be greater than zero");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if category exists
            if (product.getCategory() == null || product.getCategory().getId() == null) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Category is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (!categoryRepository.existsById(product.getCategory().getId())) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Category not found");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if supplier exists (if provided)
            if (product.getSupplier() != null && product.getSupplier().getId() != null) {
                if (!supplierRepository.existsById(product.getSupplier().getId())) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Supplier not found");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Check SKU uniqueness if provided
            if (product.getSku() != null && !product.getSku().trim().isEmpty()) {
                Optional<Product> existingProduct = productRepository.findBySku(product.getSku());
                if (existingProduct.isPresent()) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "SKU already exists");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Set default values
            if (product.getStockQuantity() == null) {
                product.setStockQuantity(0);
            }
            if (product.getIsActive() == null) {
                product.setIsActive(true);
            }

            Product savedProduct = productRepository.save(product);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Product created successfully");
            response.put("product", savedProduct);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error creating product: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable Long id, @RequestBody Product product,
            HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Product> existingProductOpt = productRepository.findById(id);
            if (!existingProductOpt.isPresent()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Product not found");
                return ResponseEntity.status(404).body(response);
            }

            Product existingProduct = existingProductOpt.get();

            // Validate required fields
            if (product.getName() != null && !product.getName().trim().isEmpty()) {
                existingProduct.setName(product.getName());
            }

            if (product.getPrice() != null && product.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
                existingProduct.setPrice(product.getPrice());
            }

            // Check SKU uniqueness if being updated
            if (product.getSku() != null && !product.getSku().equals(existingProduct.getSku())) {
                Optional<Product> skuCheck = productRepository.findBySku(product.getSku());
                if (skuCheck.isPresent() && !skuCheck.get().getId().equals(id)) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "SKU already exists");
                    return ResponseEntity.badRequest().body(response);
                }
                existingProduct.setSku(product.getSku());
            }

            // Update other fields
            if (product.getDescription() != null) {
                existingProduct.setDescription(product.getDescription());
            }

            if (product.getStockQuantity() != null) {
                existingProduct.setStockQuantity(product.getStockQuantity());
            }

            if (product.getIsActive() != null) {
                existingProduct.setIsActive(product.getIsActive());
            }

            // Update category if provided
            if (product.getCategory() != null && product.getCategory().getId() != null) {
                if (categoryRepository.existsById(product.getCategory().getId())) {
                    existingProduct.setCategory(product.getCategory());
                } else {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Category not found");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Update supplier if provided
            if (product.getSupplier() != null && product.getSupplier().getId() != null) {
                if (supplierRepository.existsById(product.getSupplier().getId())) {
                    existingProduct.setSupplier(product.getSupplier());
                } else {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Supplier not found");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            Product savedProduct = productRepository.save(existingProduct);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Product updated successfully");
            response.put("product", savedProduct);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error updating product: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            if (!productRepository.existsById(id)) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Product not found");
                return ResponseEntity.status(404).body(response);
            }

            productRepository.deleteById(id);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Product deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error deleting product: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveProducts(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            List<Product> activeProducts = productRepository.findByIsActiveTrue();
            response.put(SUCCESS, true);
            response.put("products", activeProducts);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching active products: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold,
            HttpSession session) {

        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            List<Product> lowStockProducts = productRepository.findByStockLessThan(threshold);
            response.put(SUCCESS, true);
            response.put("products", lowStockProducts);
            response.put("threshold", threshold);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching low stock products: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam String name, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
            response.put(SUCCESS, true);
            response.put("products", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error searching products: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(@PathVariable Long categoryId,
            HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            List<Product> products = productRepository.findByCategoryId(categoryId);
            response.put(SUCCESS, true);
            response.put("products", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching products by category: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<Map<String, Object>> getProductsBySupplier(@PathVariable Long supplierId,
            HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            List<Product> products = productRepository.findBySupplierId(supplierId);
            response.put(SUCCESS, true);
            response.put("products", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching products by supplier: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
