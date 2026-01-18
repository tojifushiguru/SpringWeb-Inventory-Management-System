
package com.springweb.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class User {

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

       @Column(unique = true, nullable = false)
       private String username;

       @Column(name = "access_code", length = 2, nullable = false)
       private String accessCode;

       @Column(nullable = false)
       private String name;

       @Column(nullable = false)
       private String gender;
       @Column(name = "profile_image")
       private String profileImage;

       @Column(columnDefinition = "TEXT")
       private String bio;

       @Column(name = "facebook_url")
       private String facebookUrl;

       @Column(name = "instagram_url")
       private String instagramUrl;

       @Column(name = "twitter_url")
       private String twitterUrl;

       @Column(name = "github_url")
       private String githubUrl;

       @Column(name = "youtube_url")
       private String youtubeUrl;

       // Default constructor
       public User() {
       }

       // Constructor
       public User(String username, String accessCode, String name, String gender, String profileImage) {
              this.username = username;
              this.accessCode = accessCode;
              this.name = name;
              this.gender = gender;
              this.profileImage = profileImage;
       }

       // Extended constructor
       public User(String username, String accessCode, String name, String gender, String profileImage,
                     String bio, String facebookUrl, String instagramUrl, String twitterUrl,
                     String githubUrl, String youtubeUrl) {
              this.username = username;
              this.accessCode = accessCode;
              this.name = name;
              this.gender = gender;
              this.profileImage = profileImage;
              this.bio = bio;
              this.facebookUrl = facebookUrl;
              this.instagramUrl = instagramUrl;
              this.twitterUrl = twitterUrl;
              this.githubUrl = githubUrl;
              this.youtubeUrl = youtubeUrl;
       } // Getters and Setters

       public Long getId() {
              return id;
       }

       public void setId(Long id) {
              this.id = id;
       }

       public String getUsername() {
              return username;
       }

       public void setUsername(String username) {
              this.username = username;
       }

       public String getAccessCode() {
              return accessCode;
       }

       public void setAccessCode(String accessCode) {
              this.accessCode = accessCode;
       }

       public String getName() {
              return name;
       }

       public void setName(String name) {
              this.name = name;
       }

       public String getGender() {
              return gender;
       }

       public void setGender(String gender) {
              this.gender = gender;
       }

       public String getProfileImage() {
              return profileImage;
       }

       public void setProfileImage(String profileImage) {
              this.profileImage = profileImage;
       }

       public String getBio() {
              return bio;
       }

       public void setBio(String bio) {
              this.bio = bio;
       }

       public String getFacebookUrl() {
              return facebookUrl;
       }

       public void setFacebookUrl(String facebookUrl) {
              this.facebookUrl = facebookUrl;
       }

       public String getInstagramUrl() {
              return instagramUrl;
       }

       public void setInstagramUrl(String instagramUrl) {
              this.instagramUrl = instagramUrl;
       }

       public String getTwitterUrl() {
              return twitterUrl;
       }

       public void setTwitterUrl(String twitterUrl) {
              this.twitterUrl = twitterUrl;
       }

       public String getGithubUrl() {
              return githubUrl;
       }

       public void setGithubUrl(String githubUrl) {
              this.githubUrl = githubUrl;
       }

       public String getYoutubeUrl() {
              return youtubeUrl;
       }

       public void setYoutubeUrl(String youtubeUrl) {
              this.youtubeUrl = youtubeUrl;
       }

       @Override
       public String toString() {
              return "User{" +
                            "id=" + id +
                            ", username='" + username + '\'' +
                            ", name='" + name + '\'' +
                            ", gender='" + gender + '\'' +
                            ", profileImage='" + profileImage + '\'' +
                            '}';
       }
}