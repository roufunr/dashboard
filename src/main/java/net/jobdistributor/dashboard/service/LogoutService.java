package net.jobdistributor.dashboard.service;

import net.jobdistributor.dashboard.dto.LogoutResult;
import net.jobdistributor.dashboard.entity.TokenBlacklist;
import net.jobdistributor.dashboard.repository.TokenBlacklistRepository;
import net.jobdistributor.dashboard.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LogoutService {

    private static final Logger logger = LoggerFactory.getLogger(LogoutService.class);

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    /**
     * Logout from current device (blacklist specific token)
     */
    @Transactional
    public LogoutResult logout(String token, Long userId) {
        try {
            String tokenHash = jwtService.hashToken(token);
            LocalDateTime expiresAt = jwtService.getExpirationFromToken(token) != null
                    ? jwtService.getExpirationFromToken(token).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                    : LocalDateTime.now().plusDays(1);

            TokenBlacklist blacklistEntry = new TokenBlacklist(tokenHash, userId, expiresAt, "LOGOUT");
            tokenBlacklistRepository.save(blacklistEntry);

            logger.info("User {} logged out successfully", userId);
            return new LogoutResult(true, "Logged out successfully");

        } catch (Exception e) {
            logger.error("Logout failed for user {}: {}", userId, e.getMessage(), e);
            return new LogoutResult(false, "Logout failed");
        }
    }

    /**
     * Logout from all devices (increment token generation)
     */
    @Transactional
    public LogoutResult logoutAllDevices(Long userId) {
        try {
            // Increment token generation - this invalidates ALL existing tokens
            userRepository.incrementTokenGeneration(userId);

            logger.info("User {} logged out from all devices", userId);
            return new LogoutResult(true, "Logged out from all devices. New logins required.");

        } catch (Exception e) {
            logger.error("Logout all devices failed for user {}: {}", userId, e.getMessage(), e);
            return new LogoutResult(false, "Logout from all devices failed");
        }
    }
}