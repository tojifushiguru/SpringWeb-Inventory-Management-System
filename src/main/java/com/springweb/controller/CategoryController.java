package com.springweb.controller;

import com.springweb.entity.Category;
import com.springweb.repository.CategoryRepository;
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
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private static final String AUTHENTICATED_USER = "authenticatedUser";
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";

    @Autowired
    private CategoryRepository categoryRepository;

    // Helper method to check authentication
    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute(AUTHENTICATED_USER) != null;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            HttpSession session) {

        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            if (page == -1) {
                // Return all categories without pagination
                List<Category> categories;
                if (search != null && !search.trim().isEmpty()) {
                    categories = categoryRepository.findByNameContainingIgnoreCase(search);
                } else {
                    categories = categoryRepository.findAll();
                }
                response.put(SUCCESS, true);
                response.put("categories", categories);
            } else {
                // Return paginated results
                Pageable pageable = PageRequest.of(page, size);
                Page<Category> categoryPage;

                if (search != null && !search.trim().isEmpty()) {
                    categoryPage = categoryRepository.findByNameContainingIgnoreCase(search, pageable);
                } else {
                    categoryPage = categoryRepository.findAll(pageable);
                }

                response.put(SUCCESS, true);
                response.put("categories", categoryPage.getContent());
                response.put("totalElements", categoryPage.getTotalElements());
                response.put("totalPages", categoryPage.getTotalPages());
                response.put("currentPage", page);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching categories: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Category> category = categoryRepository.findById(id);
            if (category.isPresent()) {
                response.put(SUCCESS, true);
                response.put("category", category.get());
                return ResponseEntity.ok(response);
            } else {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Category not found");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching category: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody Category category, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            // Validate required fields
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Category name is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Trim and validate the name
            String trimmedName = category.getName().trim();

            // Additional validation for name length
            if (trimmedName.length() < 2) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Category name must be at least 2 characters long");
                return ResponseEntity.badRequest().body(response);
            }

            if (trimmedName.length() > 100) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Category name must not exceed 100 characters");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if name already exists (case-insensitive)
            Optional<Category> existingCategory = categoryRepository.findByName(trimmedName);
            if (existingCategory.isPresent()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "A category with this name already exists");
                return ResponseEntity.badRequest().body(response);
            }

            // Set the trimmed name
            category.setName(trimmedName);

            // Trim description if provided
            if (category.getDescription() != null) {
                category.setDescription(category.getDescription().trim());
            }

            // Set the current user as creator
            String currentUser = (String) session.getAttribute(AUTHENTICATED_USER);
            if (currentUser != null) {
                category.setCreatedBy(currentUser);
            }

            Category savedCategory = categoryRepository.save(category);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Category created successfully");
            response.put("category", savedCategory);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error creating category: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id,
            @RequestBody Category categoryDetails, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Category> optionalCategory = categoryRepository.findById(id);
            if (!optionalCategory.isPresent()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Category not found");
                return ResponseEntity.status(404).body(response);
            }

            Category existingCategory = optionalCategory.get();

            // Validate and update name if provided
            if (categoryDetails.getName() != null && !categoryDetails.getName().trim().isEmpty()) {
                String newName = categoryDetails.getName().trim();

                // Check if name already exists (but not the current category)
                Optional<Category> nameCheck = categoryRepository.findByName(newName);
                if (nameCheck.isPresent() && !nameCheck.get().getId().equals(id)) {
                    response.put(SUCCESS, false);
                    response.put(MESSAGE, "Category name already exists");
                    return ResponseEntity.badRequest().body(response);
                }

                existingCategory.setName(newName);
            }

            // Update description
            if (categoryDetails.getDescription() != null) {
                existingCategory.setDescription(categoryDetails.getDescription());
            }

            // Set the current user as last modifier
            String currentUser = (String) session.getAttribute(AUTHENTICATED_USER);
            if (currentUser != null) {
                existingCategory.setLastModifiedBy(currentUser);
            }

            Category savedCategory = categoryRepository.save(existingCategory);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Category updated successfully");
            response.put("category", savedCategory);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error updating category: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            if (!categoryRepository.existsById(id)) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Category not found");
                return ResponseEntity.status(404).body(response);
            }

            // Check if category has products - you might want to prevent deletion
            // This can be implemented based on business rules

            categoryRepository.deleteById(id);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Category deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error deleting category: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCategories(@RequestParam String name, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
            response.put(SUCCESS, true);
            response.put("categories", categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error searching categories: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/with-product-count")
    public ResponseEntity<Map<String, Object>> getCategoriesWithProductCount(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        try {
            List<Category> categories = categoryRepository.findAll();
            // You can add custom queries to get product counts per category
            response.put(SUCCESS, true);
            response.put("categories", categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error fetching categories with product count: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
