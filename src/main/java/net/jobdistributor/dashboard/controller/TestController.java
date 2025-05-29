package net.jobdistributor.dashboard.controller;

import net.jobdistributor.dashboard.dto.ApiResponse;
import net.jobdistributor.dashboard.util.AuthenticationUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * Test endpoint - requires authentication
     */
    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint() {
        Long userId = AuthenticationUtil.getCurrentUserId();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "You are authenticated!");
        response.put("userId", userId);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Public test endpoint - no authentication required
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse> publicEndpoint() {
        return ResponseEntity.ok(new ApiResponse(true, "This is a public endpoint"));
    }





    /**
     * Test current user info
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Long userId = AuthenticationUtil.requireCurrentUserId();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("authenticated", AuthenticationUtil.isAuthenticated());

        return ResponseEntity.ok(response);
    }
}