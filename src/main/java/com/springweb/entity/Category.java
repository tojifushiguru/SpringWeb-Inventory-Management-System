package com.springweb.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    // Default constructor
    public Category() {
    }

    // Constructor without products (to avoid circular reference)
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Set createdBy from current user context if available
        // This will be handled in the service layer
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedAt = LocalDateTime.now();
        // Set lastModifiedBy from current user context if available
        // This will be handled in the service layer
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    // toString() method without products to avoid circular reference
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", lastModifiedAt=" + lastModifiedAt +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                '}';
    }
}
