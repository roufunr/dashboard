package net.jobdistributor.dashboard.controller;

import net.jobdistributor.dashboard.entity.User;
import net.jobdistributor.dashboard.repository.UserRepository;
import net.jobdistributor.dashboard.util.AuthenticationUtil;
import net.jobdistributor.dashboard.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get current user's complete profile information
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile() {
        Long currentUserId = AuthenticationUtil.requireCurrentUserId();

        Optional<User> userOpt = userRepository.findById(currentUserId);
        if (userOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.badRequest().body(response);
        }

        User user = userOpt.get();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Profile retrieved successfully");
        response.put("profile", UserDto.fromUser(user));

        return ResponseEntity.ok(response);
    }

    /**
     * Get basic user information (alternative to /api/test/me)
     */
    @GetMapping("/basic")
    public ResponseEntity<Map<String, Object>> getBasicUserInfo() {
        Long currentUserId = AuthenticationUtil.requireCurrentUserId();

        Optional<User> userOpt = userRepository.findById(currentUserId);
        if (userOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.badRequest().body(response);
        }

        User user = userOpt.get();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", user.getId());
        response.put("email", user.getEmails().stream()
                .filter(email -> email.getIsPrimary() && !email.getIsDeleted())
                .map(email -> email.getEmailAddress())
                .findFirst()
                .orElse("no-email"));
        response.put("fullName", user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : ""));
        response.put("status", user.getStatus());
        response.put("authenticated", true);

        return ResponseEntity.ok(response);
    }

    /**
     * Get user's email information
     */
    @GetMapping("/emails")
    public ResponseEntity<Map<String, Object>> getUserEmails() {
        Long currentUserId = AuthenticationUtil.requireCurrentUserId();

        Optional<User> userOpt = userRepository.findById(currentUserId);
        if (userOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.badRequest().body(response);
        }

        User user = userOpt.get();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", user.getId());

        // Get all active emails
        var emails = user.getEmails().stream()
                .filter(email -> !email.getIsDeleted())
                .map(email -> {
                    Map<String, Object> emailInfo = new HashMap<>();
                    emailInfo.put("emailAddress", email.getEmailAddress());
                    emailInfo.put("isPrimary", email.getIsPrimary());
                    emailInfo.put("isVerified", email.getVerifiedAt() != null);
                    emailInfo.put("verifiedAt", email.getVerifiedAt());
                    return emailInfo;
                })
                .toList();

        response.put("emails", emails);

        return ResponseEntity.ok(response);
    }
}
