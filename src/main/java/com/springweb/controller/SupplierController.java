package com.springweb.controller;

import com.springweb.entity.Supplier;
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
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "*")
public class SupplierController {

       private static final String AUTHENTICATED_USER = "authenticatedUser";
       private static final String SUCCESS = "success";
       private static final String MESSAGE = "message";

       @Autowired
       private SupplierRepository supplierRepository;

       // Helper method to check authentication
       private boolean isAuthenticated(HttpSession session) {
              return session.getAttribute(AUTHENTICATED_USER) != null;
       }

       @GetMapping
       public ResponseEntity<Map<String, Object>> getAllSuppliers(
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
                            // Return all suppliers without pagination
                            List<Supplier> suppliers;
                            if (search != null && !search.trim().isEmpty()) {
                                   suppliers = supplierRepository
                                                 .findByNameContainingIgnoreCaseOrContactNameContainingIgnoreCase(
                                                               search.trim(), search.trim());
                            } else {
                                   suppliers = supplierRepository.findAllOrderByName();
                            }
                            response.put(SUCCESS, true);
                            response.put("suppliers", suppliers);
                            response.put("totalElements", suppliers.size());
                            return ResponseEntity.ok(response);
                     } else {
                            // Paginated results
                            Pageable pageable = PageRequest.of(page, size);
                            Page<Supplier> supplierPage;

                            if (search != null && !search.trim().isEmpty()) {
                                   supplierPage = supplierRepository
                                                 .findByNameContainingIgnoreCaseOrContactNameContainingIgnoreCase(
                                                               search.trim(), search.trim(), pageable);
                            } else {
                                   supplierPage = supplierRepository.findAll(pageable);
                            }

                            response.put(SUCCESS, true);
                            response.put("suppliers", supplierPage.getContent());
                            response.put("currentPage", supplierPage.getNumber());
                            response.put("totalItems", supplierPage.getTotalElements());
                            response.put("totalPages", supplierPage.getTotalPages());
                            return ResponseEntity.ok(response);
                     }
              } catch (Exception e) {
                     response.put(SUCCESS, false);
                     response.put(MESSAGE, "Error fetching suppliers: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }

       @GetMapping("/{id}")
       public ResponseEntity<Map<String, Object>> getSupplierById(@PathVariable Long id, HttpSession session) {
              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Map<String, Object> response = new HashMap<>();
              try {
                     Optional<Supplier> supplier = supplierRepository.findById(id);
                     if (supplier.isPresent()) {
                            response.put(SUCCESS, true);
                            response.put("supplier", supplier.get());
                            return ResponseEntity.ok(response);
                     } else {
                            response.put(SUCCESS, false);
                            response.put(MESSAGE, "Supplier not found");
                            return ResponseEntity.status(404).body(response);
                     }
              } catch (Exception e) {
                     response.put(SUCCESS, false);
                     response.put(MESSAGE, "Error fetching supplier: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }

       @PostMapping
       public ResponseEntity<Map<String, Object>> createSupplier(@RequestBody Supplier supplier, HttpSession session) {
              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Map<String, Object> response = new HashMap<>();
              try {
                     // Validate required fields
                     if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
                            response.put(SUCCESS, false);
                            response.put(MESSAGE, "Supplier name is required");
                            return ResponseEntity.badRequest().body(response);
                     }

                     if (supplier.getContactName() == null || supplier.getContactName().trim().isEmpty()) {
                            response.put(SUCCESS, false);
                            response.put(MESSAGE, "Contact person is required");
                            return ResponseEntity.badRequest().body(response);
                     }

                     if (supplier.getContactEmail() == null || supplier.getContactEmail().trim().isEmpty()) {
                            response.put(SUCCESS, false);
                            response.put(MESSAGE, "Email is required");
                            return ResponseEntity.badRequest().body(response);
                     }

                     if (supplier.getPhone() == null || supplier.getPhone().trim().isEmpty()) {
                            response.put(SUCCESS, false);
                            response.put(MESSAGE, "Phone is required");
                            return ResponseEntity.badRequest().body(response);
                     }

                     // Check if name already exists
                     Optional<Supplier> existingSupplier = supplierRepository.findByName(supplier.getName().trim());
                     if (existingSupplier.isPresent()) {
                            response.put(SUCCESS, false);
                            response.put(MESSAGE, "Supplier name already exists");
                            return ResponseEntity.badRequest().body(response);
                     }

                     // Check if email already exists
                     Optional<Supplier> emailCheck = supplierRepository
                                   .findByContactEmail(supplier.getContactEmail().trim());
                     if (emailCheck.isPresent()) {
                            response.put(SUCCESS, false);
                            response.put(MESSAGE, "Email address already exists");
                            return ResponseEntity.badRequest().body(response);
                     }

                     // Check if phone already exists
                     Optional<Supplier> phoneCheck = supplierRepository.findByPhone(supplier.getPhone().trim());
                     if (phoneCheck.isPresent()) {
                            response.put(SUCCESS, false);
                            response.put(MESSAGE, "Phone number already exists");
                            return ResponseEntity.badRequest().body(response);
                     }

                     // Trim all string fields
                     supplier.setName(supplier.getName().trim());
                     supplier.setContactName(supplier.getContactName().trim());
                     supplier.setContactEmail(supplier.getContactEmail().trim());
                     supplier.setPhone(supplier.getPhone().trim());
                     if (supplier.getAddress() != null) {
                            supplier.setAddress(supplier.getAddress().trim());
                     }

                     Supplier savedSupplier = supplierRepository.save(supplier);

                     response.put(SUCCESS, true);
                     response.put(MESSAGE, "Supplier created successfully");
                     response.put("supplier", savedSupplier);
                     return ResponseEntity.ok(response);

              } catch (Exception e) {
                     response.put(SUCCESS, false);
                     response.put(MESSAGE, "Error creating supplier: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }

       @PutMapping("/{id}")
       public ResponseEntity<Map<String, Object>> updateSupplier(@PathVariable Long id,
                     @RequestBody Supplier supplierDetails, HttpSession session) {
              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Map<String, Object> response = new HashMap<>();
              try {
                     Optional<Supplier> optionalSupplier = supplierRepository.findById(id);
                     if (!optionalSupplier.isPresent()) {
                            response.put(SUCCESS, false);
                            response.put(MESSAGE, "Supplier not found");
                            return ResponseEntity.status(404).body(response);
                     }

                     Supplier existingSupplier = optionalSupplier.get();

                     // Validate and update name if provided
                     if (supplierDetails.getName() != null && !supplierDetails.getName().trim().isEmpty()) {
                            String newName = supplierDetails.getName().trim();

                            // Check if name already exists (but not the current supplier)
                            Optional<Supplier> nameCheck = supplierRepository.findByName(newName);
                            if (nameCheck.isPresent() && !nameCheck.get().getId().equals(id)) {
                                   response.put(SUCCESS, false);
                                   response.put(MESSAGE, "Supplier name already exists");
                                   return ResponseEntity.badRequest().body(response);
                            }
                            existingSupplier.setName(newName);
                     }

                     // Validate and update contact name if provided
                     if (supplierDetails.getContactName() != null
                                   && !supplierDetails.getContactName().trim().isEmpty()) {
                            existingSupplier.setContactName(supplierDetails.getContactName().trim());
                     }

                     // Validate and update email if provided
                     if (supplierDetails.getContactEmail() != null
                                   && !supplierDetails.getContactEmail().trim().isEmpty()) {
                            String newEmail = supplierDetails.getContactEmail().trim();

                            // Check if email already exists (but not the current supplier)
                            Optional<Supplier> emailCheck = supplierRepository.findByContactEmail(newEmail);
                            if (emailCheck.isPresent() && !emailCheck.get().getId().equals(id)) {
                                   response.put(SUCCESS, false);
                                   response.put(MESSAGE, "Email address already exists");
                                   return ResponseEntity.badRequest().body(response);
                            }
                            existingSupplier.setContactEmail(newEmail);
                     }

                     // Validate and update phone if provided
                     if (supplierDetails.getPhone() != null && !supplierDetails.getPhone().trim().isEmpty()) {
                            String newPhone = supplierDetails.getPhone().trim();

                            // Check if phone already exists (but not the current supplier)
                            Optional<Supplier> phoneCheck = supplierRepository.findByPhone(newPhone);
                            if (phoneCheck.isPresent() && !phoneCheck.get().getId().equals(id)) {
                                   response.put(SUCCESS, false);
                                   response.put(MESSAGE, "Phone number already exists");
                                   return ResponseEntity.badRequest().body(response);
                            }
                            existingSupplier.setPhone(newPhone);
                     }

                     // Update address if provided
                     if (supplierDetails.getAddress() != null) {
                            existingSupplier.setAddress(supplierDetails.getAddress().trim());
                     }

                     Supplier savedSupplier = supplierRepository.save(existingSupplier);

                     response.put(SUCCESS, true);
                     response.put(MESSAGE, "Supplier updated successfully");
                     response.put("supplier", savedSupplier);
                     return ResponseEntity.ok(response);

              } catch (Exception e) {
                     response.put(SUCCESS, false);
                     response.put(MESSAGE, "Error updating supplier: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }

       @DeleteMapping("/{id}")
       public ResponseEntity<Map<String, Object>> deleteSupplier(@PathVariable Long id, HttpSession session) {
              if (!isAuthenticated(session)) {
                     return ResponseEntity.status(401).build();
              }

              Map<String, Object> response = new HashMap<>();
              try {
                     Optional<Supplier> optionalSupplier = supplierRepository.findById(id);
                     if (!optionalSupplier.isPresent()) {
                            response.put(SUCCESS, false);
                            response.put(MESSAGE, "Supplier not found");
                            return ResponseEntity.status(404).body(response);
                     }

                     supplierRepository.deleteById(id);

                     response.put(SUCCESS, true);
                     response.put(MESSAGE, "Supplier deleted successfully");
                     return ResponseEntity.ok(response);

              } catch (Exception e) {
                     response.put(SUCCESS, false);
                     response.put(MESSAGE, "Error deleting supplier: " + e.getMessage());
                     return ResponseEntity.internalServerError().body(response);
              }
       }
}