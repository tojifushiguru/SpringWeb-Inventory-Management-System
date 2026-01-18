package com.springweb.config;

import com.springweb.entity.User;
import com.springweb.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

       private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
       private final UserRepository userRepository;
       private static boolean initialized = false;

       public DataInitializer(UserRepository userRepository) {
              this.userRepository = userRepository;
       }

       @Override
       @Transactional
       public void run(String... args) throws Exception {
              // Prevent multiple executions
              if (initialized) {
                     logger.info("DataInitializer already executed, skipping...");
                     return;
              }

              // Check if users already exist to avoid duplicates
              long userCount = userRepository.count();
              if (userCount == 0) {
                     logger.info("Initializing default users...");
                     createDefaultUsers();
                     initialized = true;
              } else {
                     logger.info("Users already exist ({}), skipping initialization", userCount);
                     initialized = true;
              }
       }

       private void createDefaultUsers() {
              // Create default users with their information, bio and social media
              User[] defaultUsers = {
                            new User("Mark Joseph", "09", "Mark Joseph G. De Guia", "male", "userP/DeGuiaMark.jpg",
                                          "None",
                                          "None", "None",
                                          "None", "https://github.com/tojifushiguru",
                                          "None"),

                            new User("Justine", "13", "Justine I. Inovero", "male", "userP/Inovero.jpg",
                                          "None",
                                          "None", "None",
                                          "None", "https://github.com/itsace06",
                                          "None"),
                            new User("Edelbert", "09", "Edelbert M. Gobrin", "male", "userP/Gobrin.jpg",
                                          "None",
                                          "None", "None",
                                          "None", "https://github.com/edelbertgobrin5-star",
                                          "None"),
                            new User("Mark Ace", "04", "Mark Ace A. Labitag", "male", "userP/Labitag.jpeg",
                                          "None",
                                          "None", "None",
                                          "None", "https://github.com/markAce2",
                                          "None"),
                            new User("Kurt", "24", "Kurt Harvy L. Sarzata", "male", "userP/Sarzata.jpeg",
                                          "None",
                                          "None", "None",
                                          "None", "https://github.com/kurtsarzata310-hub",
                                          "None")
              };

              try {
                     for (User user : defaultUsers) {
                            userRepository.save(user);
                            logger.info("Created user: {} with username: {}", user.getName(), user.getUsername());
                     }
                     logger.info("Default users initialized successfully!");
              } catch (Exception e) {
                     logger.error("Error creating default users: ", e);
              }
       }
}