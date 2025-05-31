package net.jobdistributor.dashboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
public class TokenBlacklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", length = 128, nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "blacklisted_at", nullable = false)
    private LocalDateTime blacklistedAt;

    @Column(name = "reason", length = 50)
    private String reason; // "LOGOUT", "SECURITY", "ADMIN"

    // Constructors
    public TokenBlacklist() {}

    public TokenBlacklist(String tokenHash, Long userId, LocalDateTime expiresAt, String reason) {
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.blacklistedAt = LocalDateTime.now();
        this.reason = reason;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getBlacklistedAt() { return blacklistedAt; }
    public void setBlacklistedAt(LocalDateTime blacklistedAt) { this.blacklistedAt = blacklistedAt; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}