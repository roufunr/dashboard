package net.jobdistributor.dashboard.controller;

import net.jobdistributor.dashboard.dto.LogoutResult;
import net.jobdistributor.dashboard.service.LogoutService;
import net.jobdistributor.dashboard.service.JwtService;
import net.jobdistributor.dashboard.util.AuthenticationUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LogoutController {

    @Autowired
    private LogoutService logoutService;

    @Autowired
    private JwtService jwtService;

    /**
     * Logout current session (invalidate current JWT token)
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        Long currentUserId = AuthenticationUtil.requireCurrentUserId();

        // Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);

        if (token == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "No token provided");
            return ResponseEntity.badRequest().body(response);
        }

        LogoutResult result = logoutService.logout(token, currentUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        response.put("userId", currentUserId);
        response.put("scope", "current_device_only"); // ADDED: Clarify logout scope

        if (result.isSuccess()) {
            response.put("note", "Token has been invalidated. Please login again to access protected endpoints.");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Logout from all devices (invalidate all user tokens)
     */
    @PostMapping("/logout-all")
    public ResponseEntity<Map<String, Object>> logoutAllDevices() {
        Long currentUserId = AuthenticationUtil.requireCurrentUserId();

        LogoutResult result = logoutService.logoutAllDevices(currentUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        response.put("userId", currentUserId);
        response.put("scope", "all_devices"); // ADDED: Clarify logout scope

        if (result.isSuccess()) {
            // ADDED: More detailed response for logout-all
            response.put("action_required", "Re-login required on all devices");
            response.put("note", "All active sessions have been terminated. You will need to log in again on each device.");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Check current session status
     * UPDATED: Uses enhanced validation that includes generation check
     */
    @GetMapping("/session-status")
    public ResponseEntity<Map<String, Object>> getSessionStatus(HttpServletRequest request) {
        Long currentUserId = AuthenticationUtil.getCurrentUserId();

        Map<String, Object> response = new HashMap<>();

        if (currentUserId == null) {
            response.put("authenticated", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.ok(response);
        }

        // Check if current token is valid (includes blacklist AND generation check)
        String authHeader = request.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);

        // UPDATED: Use the enhanced validateToken method that checks:
        // 1. JWT signature and expiration
        // 2. Token blacklist (individual logout)
        // 3. Token generation (logout-all)
        boolean tokenValid = token != null && jwtService.validateToken(token);

        response.put("authenticated", tokenValid);
        response.put("userId", currentUserId);
        response.put("tokenValid", tokenValid);

        if (tokenValid) {
            response.put("message", "Session active");
            // ADDED: Include token info for debugging
            try {
                response.put("tokenInfo", Map.of(
                        "issuedAt", jwtService.getIssuedAtFromToken(token),
                        "expiresAt", jwtService.getExpirationFromToken(token)
                ));
            } catch (Exception e) {
                // Token info extraction failed, but validation passed
            }
        } else {
            // UPDATED: More specific error messages
            if (token == null) {
                response.put("message", "No token provided");
            } else {
                response.put("message", "Session invalid - token expired, blacklisted, or invalidated by logout-all");
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * ADDED: New endpoint to get information about active sessions
     */
    @GetMapping("/session-info")
    public ResponseEntity<Map<String, Object>> getSessionInfo(HttpServletRequest request) {
        Long currentUserId = AuthenticationUtil.requireCurrentUserId();
        String authHeader = request.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);

        if (token == null || !jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Invalid or missing token"
            ));
        }

        try {
            Long tokenGeneration = jwtService.getTokenGenerationFromToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", currentUserId);
            response.put("tokenGeneration", tokenGeneration);
            response.put("issuedAt", jwtService.getIssuedAtFromToken(token));
            response.put("expiresAt", jwtService.getExpirationFromToken(token));
            response.put("email", jwtService.getEmailFromToken(token));
            response.put("status", "active");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to extract token information"
            ));
        }
    }
}