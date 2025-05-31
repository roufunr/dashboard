package net.jobdistributor.dashboard.dto;

import java.time.LocalDateTime;

public class ApiResponse {
    private boolean success;
    private String message;
    private String error;           // Add error code
    private LocalDateTime timestamp; // Add timestamp
    private String path;            // Add request path
    private String suggestion;      // Add helpful suggestion

    // Existing constructors
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Enhanced constructor
    public ApiResponse(boolean success, String message, String error, String path, String suggestion) {
        this.success = success;
        this.message = message;
        this.error = error;
        this.path = path;
        this.suggestion = suggestion;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
}