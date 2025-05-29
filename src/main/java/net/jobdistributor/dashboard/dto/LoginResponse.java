package net.jobdistributor.dashboard.dto;

public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private UserDto user;

    public LoginResponse(boolean success, String message, String token, UserDto user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public UserDto getUser() { return user; }
}