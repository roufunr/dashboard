package net.jobdistributor.dashboard.controller;

import net.jobdistributor.dashboard.service.AuthService;
import net.jobdistributor.dashboard.service.JwtService;
import net.jobdistributor.dashboard.dto.*;
import net.jobdistributor.dashboard.util.AuthenticationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request,
                                                 HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);

        AuthService.SignupResult result = authService.signup(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getOrganization(),
                request.getJobRole(),
                request.getProfileUrl(),
                request.getPassword(),
                ipAddress
        );

        return ResponseEntity.ok(new SignupResponse(
                result.isSuccess(),
                result.getMessage(),
                result.getUserId()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthService.LoginResult result = authService.login(
                request.getEmail(),
                request.getPassword(),
                ipAddress,
                userAgent
        );

        if (result.isSuccess()) {
            // Generate JWT token
            String token = jwtService.generateToken(result.getUser());

            return ResponseEntity.ok(new LoginResponse(
                    true,
                    result.getMessage(),
                    token,
                    UserDto.fromUser(result.getUser())
            ));
        }

        return ResponseEntity.badRequest().body(new LoginResponse(
                false,
                result.getMessage(),
                null,
                null
        ));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        boolean success = authService.verifyEmail(request.getToken());

        return ResponseEntity.ok(new ApiResponse(
                success,
                success ? "Email verified successfully" : "Invalid or expired token"
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                      HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);

        // Always return success to prevent email enumeration
        authService.initiatePasswordReset(request.getEmail(), ipAddress);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "If the email exists, a password reset link has been sent"
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean success = authService.resetPassword(request.getToken(), request.getNewPassword());


        return ResponseEntity.ok(new ApiResponse(
                success,
                success ? "Password reset successfully" : "Invalid or expired token"
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        // Get current authenticated user ID (throws exception if not authenticated)
        Long userId = AuthenticationUtil.requireCurrentUserId();

        boolean success = authService.updateProfile(
                userId,
                request.getFirstName(),
                request.getLastName(),
                request.getOrganization(),
                request.getJobRole(),
                request.getProfileUrl()
        );

        return ResponseEntity.ok(new ApiResponse(
                success,
                success ? "Profile updated successfully" : "Failed to update profile"
        ));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}