package com.springweb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

       @Column(name = "report_type", nullable = false, length = 100)
       private String reportType;

       @ManyToOne(fetch = FetchType.LAZY)
       @JoinColumn(name = "generated_by")
       private User generatedBy;

       @Column(name = "generated_at", updatable = false)
       private LocalDateTime generatedAt;

       @Column(name = "content", columnDefinition = "TEXT")
       private String content;

       // Default constructor
       public Report() {
       }

       // Constructor
       public Report(String reportType, User generatedBy, String content) {
              this.reportType = reportType;
              this.generatedBy = generatedBy;
              this.content = content;
       }

       @PrePersist
       protected void onCreate() {
              generatedAt = LocalDateTime.now();
       }

       // Getters and Setters
       public Long getId() {
              return id;
       }

       public void setId(Long id) {
              this.id = id;
       }

       public String getReportType() {
              return reportType;
       }

       public void setReportType(String reportType) {
              this.reportType = reportType;
       }

       public User getGeneratedBy() {
              return generatedBy;
       }

       public void setGeneratedBy(User generatedBy) {
              this.generatedBy = generatedBy;
       }

       public LocalDateTime getGeneratedAt() {
              return generatedAt;
       }

       public void setGeneratedAt(LocalDateTime generatedAt) {
              this.generatedAt = generatedAt;
       }

       public String getContent() {
              return content;
       }

       public void setContent(String content) {
              this.content = content;
       }

       @Override
       public String toString() {
              return "Report{" +
                            "id=" + id +
                            ", reportType='" + reportType + '\'' +
                            ", generatedBy=" + (generatedBy != null ? generatedBy.getId() : null) +
                            ", generatedAt=" + generatedAt +
                            ", content='" + content + '\'' +
                            '}';
       }
}