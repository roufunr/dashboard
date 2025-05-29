package net.jobdistributor.dashboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_resets")
public class PasswordReset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reset_token", length = 128, nullable = false, unique = true)
    private String resetToken;

    @Column(name = "token_expires_at", nullable = false)
    private LocalDateTime tokenExpiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "reset_ip", length = 45)
    private String resetIp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public PasswordReset() {}

    public PasswordReset(Long userId, String resetToken, LocalDateTime tokenExpiresAt, String resetIp) {
        this.userId = userId;
        this.resetToken = resetToken;
        this.tokenExpiresAt = tokenExpiresAt;
        this.resetIp = resetIp;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public String getResetIp() { return resetIp; }
    public void setResetIp(String resetIp) { this.resetIp = resetIp; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}