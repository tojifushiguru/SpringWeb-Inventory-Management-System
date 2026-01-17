package com.springweb.controller;

import com.springweb.entity.User;
import com.springweb.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Controller
public class WebController {

    private static final String AUTHENTICATED_USER = "authenticatedUser";
    private static final String USERNAME = "username";
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";
    private static final String GENDER = "gender";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String FEMALE = "female";

    private final UserRepository userRepository;
    private final Random random = new Random();

    public WebController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String login(HttpSession session) {
        // If user is already authenticated, redirect to dashboard
        if (session.getAttribute(AUTHENTICATED_USER) != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        // Check if user is authenticated
        if (session.getAttribute(AUTHENTICATED_USER) == null) {
            return "redirect:/";
        }
        return "dashboard";
    }

    @PostMapping("/api/verify-access")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyAccess(@RequestBody Map<String, String> request,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = request.get(USERNAME);
            String accessCode = request.get("accessCode");

            // Validate input
            if (username == null || accessCode == null || accessCode.length() != 2) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Invalid input parameters");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if user exists and access code matches
            Optional<User> userOptional = userRepository.findByUsernameAndAccessCode(username, accessCode);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Set session attributes for authentication
                session.setAttribute(AUTHENTICATED_USER, user.getUsername());
                session.setAttribute("userId", user.getId());
                session.setAttribute("userRole", "USER"); // Add role-based access if needed

                response.put(SUCCESS, true);
                response.put("user", Map.of(
                        USERNAME, user.getUsername(),
                        "name", user.getName(),
                        GENDER, user.getGender(),
                        "profileImage", user.getProfileImage()));
                return ResponseEntity.ok(response);
            } else {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Invalid credentials");
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Authentication error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/api/auth/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAuthStatus(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (isAuthenticated(session)) {
            response.put("authenticated", true);
            response.put(USERNAME, session.getAttribute(AUTHENTICATED_USER));
        } else {
            response.put("authenticated", false);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/logout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Invalidate the session
            session.invalidate();

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error during logout: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Helper method to check authentication for API endpoints
    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute(AUTHENTICATED_USER) != null;
    }

    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<List<User>> getAllUsers(HttpSession session) {
        // Check authentication
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<User> getUserById(@PathVariable Long id, HttpSession session) {
        // Check authentication
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        try {
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isPresent()) {
                return ResponseEntity.ok(userOptional.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id,
            @RequestBody Map<String, String> request, HttpSession session) {
        // Check authentication
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<User> userOptional = userRepository.findById(id);
            if (!userOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOptional.get();

            // Validate access code format
            String accessCode = request.get("accessCode");
            if (accessCode != null && (!accessCode.matches("\\d{2}") || accessCode.length() != 2)) {
                response.put("success", false);
                response.put("message", "Access code must be exactly 2 digits");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if username is already taken by another user
            String username = request.get("username");
            if (username != null && !username.equals(user.getUsername())) {
                Optional<User> existingUser = userRepository.findByUsername(username);
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    response.put("success", false);
                    response.put("message", "Username already exists");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Update user fields
            if (request.get("name") != null) {
                user.setName(request.get("name"));
            }
            if (username != null) {
                user.setUsername(username);
            }
            if (accessCode != null) {
                user.setAccessCode(accessCode);
            }
            if (request.get("gender") != null) {
                user.setGender(request.get("gender"));
            }

            userRepository.save(user);

            response.put("success", true);
            response.put("message", "User updated successfully");
            response.put("user", user);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating user: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PatchMapping("/api/users/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id, HttpSession session) {
        // Check authentication
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<User> userOptional = userRepository.findById(id);
            if (!userOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOptional.get();

            // Note: Since we don't have an 'active' field in the User entity,
            // this is a placeholder. In a real application, you would add this field.
            response.put("success", true);
            response.put("message", "User status toggled successfully");
            response.put("user", user);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating user status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PatchMapping("/api/users/{id}/reset-access-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetAccessCode(@PathVariable Long id, HttpSession session) {
        // Check authentication
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<User> userOptional = userRepository.findById(id);
            if (!userOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOptional.get();

            // Generate new random 2-digit access code
            String newAccessCode = String.format("%02d", random.nextInt(100));

            // Ensure it's different from current one
            while (newAccessCode.equals(user.getAccessCode())) {
                newAccessCode = String.format("%02d", random.nextInt(100));
            }

            user.setAccessCode(newAccessCode);
            userRepository.save(user);

            response.put("success", true);
            response.put("message", "Access code reset successfully");
            response.put("newAccessCode", newAccessCode);
            response.put("user", user);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error resetting access code: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Public endpoint for login page to get users
    @GetMapping("/api/public/users")
    @ResponseBody
    public ResponseEntity<List<User>> getPublicUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
