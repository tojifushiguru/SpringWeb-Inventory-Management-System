package com.springweb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "settings")
public class Setting {

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

       @Column(name = "setting_key", nullable = false, unique = true, length = 100)
       private String settingKey;

       @Column(name = "setting_value", nullable = false, columnDefinition = "TEXT")
       private String settingValue;

       @Column(name = "updated_at")
       private LocalDateTime updatedAt;

       // Default constructor
       public Setting() {
       }

       // Constructor
       public Setting(String settingKey, String settingValue) {
              this.settingKey = settingKey;
              this.settingValue = settingValue;
       }

       @PrePersist
       @PreUpdate
       protected void onUpdate() {
              updatedAt = LocalDateTime.now();
       }

       // Getters and Setters
       public Long getId() {
              return id;
       }

       public void setId(Long id) {
              this.id = id;
       }

       public String getSettingKey() {
              return settingKey;
       }

       public void setSettingKey(String settingKey) {
              this.settingKey = settingKey;
       }

       public String getSettingValue() {
              return settingValue;
       }

       public void setSettingValue(String settingValue) {
              this.settingValue = settingValue;
       }

       public LocalDateTime getUpdatedAt() {
              return updatedAt;
       }

       public void setUpdatedAt(LocalDateTime updatedAt) {
              this.updatedAt = updatedAt;
       }

       @Override
       public String toString() {
              return "Setting{" +
                            "id=" + id +
                            ", settingKey='" + settingKey + '\'' +
                            ", settingValue='" + settingValue + '\'' +
                            ", updatedAt=" + updatedAt +
                            '}';
       }
}