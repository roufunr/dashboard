package net.jobdistributor.dashboard.exception;

import net.jobdistributor.dashboard.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        logger.warn("Validation error: {}", message);

        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, message));
    }

    /**
     * Handle authentication errors - ENHANCED
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {

        String path = extractPath(request);
        logger.warn("Authentication error on {}: {}", path, ex.getMessage());

        ApiResponse response = new ApiResponse(
                false,
                "Authentication failed - token missing, invalid, or expired",
                "AUTHENTICATION_FAILED",
                path,
                "Please login again or check your Authorization header format: 'Bearer <token>'"
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle access denied errors - ENHANCED
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        String path = extractPath(request);
        logger.warn("Access denied on {}: {}", path, ex.getMessage());

        // Provide more specific guidance based on the path
        String suggestion = getSuggestionForPath(path);

        ApiResponse response = new ApiResponse(
                false,
                "Access denied - insufficient permissions or endpoint not found",
                "ACCESS_DENIED",
                path,
                suggestion
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handle security exceptions (from AuthenticationUtil) - ENHANCED
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse> handleSecurityException(
            SecurityException ex, WebRequest request) {

        String path = extractPath(request);
        logger.warn("Security error on {}: {}", path, ex.getMessage());

        ApiResponse response = new ApiResponse(
                false,
                ex.getMessage(),
                "SECURITY_ERROR",
                path,
                "Ensure you're authenticated before accessing protected endpoints"
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        String path = extractPath(request);
        logger.warn("Illegal argument on {}: {}", path, ex.getMessage());

        ApiResponse response = new ApiResponse(
                false,
                ex.getMessage(),
                "INVALID_ARGUMENT",
                path,
                "Check your request parameters and try again"
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        String path = extractPath(request);
        logger.error("Runtime exception on {}: {}", path, ex.getMessage(), ex);

        ApiResponse response = new ApiResponse(
                false,
                "An error occurred processing your request",
                "RUNTIME_ERROR",
                path,
                "Please try again or contact support if the problem persists"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(
            Exception ex, WebRequest request) {

        String path = extractPath(request);
        logger.error("Unexpected exception on {}: {}", path, ex.getMessage(), ex);

        ApiResponse response = new ApiResponse(
                false,
                "An unexpected error occurred",
                "INTERNAL_ERROR",
                path,
                "Please contact support with the timestamp and path information"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ====================
    // HELPER METHODS
    // ====================

    /**
     * Extract clean path from WebRequest
     */
    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.replace("uri=", "");
    }

    /**
     * Provide specific suggestions based on the endpoint being accessed
     */
    private String getSuggestionForPath(String path) {
        if (path.contains("/api/auth/")) {
            return "Auth endpoints: Check if you're using the correct HTTP method (GET/POST)";
        }

        if (path.contains("/api/profile/")) {
            return "Profile endpoints: Ensure you're authenticated and endpoint exists. Try /api/debug/auth-status first";
        }

        if (path.contains("/api/admin/")) {
            return "Admin endpoints: Requires administrator privileges. Contact your admin";
        }

        if (path.contains("/logout")) {
            return "Logout endpoints: Ensure valid token and correct method (POST)";
        }

        return "Check if the endpoint exists and you have proper authentication/authorization";
    }
}