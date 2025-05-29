package net.jobdistributor.dashboard.service;

import net.jobdistributor.dashboard.entity.*;
import net.jobdistributor.dashboard.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ========================================
    // SIGNUP
    // ========================================

    public SignupResult signup(String email, String firstName, String lastName,
                               String organization, String jobRole, String profileUrl,
                               String password, String ipAddress) {

        // Check if email already exists
        if (emailRepository.findByEmailAddress(email).isPresent()) {
            return new SignupResult(false, "Email already exists", null);
        }

        // Create user
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setOrganization(organization);
        user.setJobRole(jobRole);
        user.setProfileUrl(profileUrl);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Create email
        Email userEmail = new Email();
        userEmail.setUser(savedUser);
        userEmail.setEmailAddress(email);
        userEmail.setIsPrimary(true);
        userEmail.setVerificationToken(generateToken());
        userEmail.setTokenExpiresAt(LocalDateTime.now().plusHours(24));
        userEmail.setCreatedAt(LocalDateTime.now());
        userEmail.setUpdatedAt(LocalDateTime.now());

        emailRepository.save(userEmail);

        // Send verification email
        try {
            emailService.sendVerificationEmail(email, userEmail.getVerificationToken());
        } catch (Exception e) {
            // Log error but don't fail signup
            logger.error("Failed to send verification email to: {}", email, e);
        }

        // Create password
        Password userPassword = new Password();
        userPassword.setUser(savedUser);
        userPassword.setEncryptedPassword(passwordEncoder.encode(password));
        userPassword.setCreatedAt(LocalDateTime.now());
        userPassword.setUpdatedAt(LocalDateTime.now());

        passwordRepository.save(userPassword);

        return new SignupResult(true, "User created successfully", savedUser.getId());
    }

    // ========================================
    // EMAIL VERIFICATION
    // ========================================

    public boolean verifyEmail(String token) {
        Optional<Email> emailOpt = emailRepository.findByVerificationToken(token);

        if (emailOpt.isEmpty()) {
            return false;
        }

        Email email = emailOpt.get();

        // Check if token expired
        if (email.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Verify email
        email.setVerifiedAt(LocalDateTime.now());
        email.setVerificationToken(null);
        email.setTokenExpiresAt(null);
        emailRepository.save(email);

        // Update user status
        User user = email.getUser();
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }

    // ========================================
    // LOGIN
    // ========================================

    public LoginResult login(String email, String password, String ipAddress, String userAgent) {

        // Record login attempt
        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(email);
        attempt.setIpAddress(ipAddress);
        attempt.setUserAgent(userAgent);
        attempt.setCreatedAt(LocalDateTime.now());

        try {
            // Find user by email
            Optional<User> userOpt = userRepository.findByPrimaryEmail(email);

            if (userOpt.isEmpty()) {
                attempt.setSuccess(false);
                attempt.setFailureReason("User not found");
                loginAttemptRepository.save(attempt);
                return new LoginResult(false, "Invalid credentials", null);
            }

            User user = userOpt.get();
            attempt.setUserId(user.getId());

            // Check account status
            if (user.getStatus() != UserStatus.ACTIVE) {
                attempt.setSuccess(false);
                attempt.setFailureReason("Account not active");
                loginAttemptRepository.save(attempt);
                return new LoginResult(false, "Account not verified or suspended", null);
            }

            // Check if account is locked
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                attempt.setSuccess(false);
                attempt.setFailureReason("Account locked");
                loginAttemptRepository.save(attempt);
                return new LoginResult(false, "Account is temporarily locked", null);
            }

            // Get latest password
            Optional<Password> passwordOpt = passwordRepository.findLatestByUserId(user.getId());

            if (passwordOpt.isEmpty()) {
                attempt.setSuccess(false);
                attempt.setFailureReason("No password found");
                loginAttemptRepository.save(attempt);
                return new LoginResult(false, "Invalid credentials", null);
            }

            // Verify password
            if (!passwordEncoder.matches(password, passwordOpt.get().getEncryptedPassword())) {
                // Increment failed attempts
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

                // Lock account after 5 failed attempts
                if (user.getFailedLoginAttempts() >= 5) {
                    user.setLockedUntil(LocalDateTime.now().plusHours(1));
                }

                userRepository.save(user);

                attempt.setSuccess(false);
                attempt.setFailureReason("Invalid password");
                loginAttemptRepository.save(attempt);
                return new LoginResult(false, "Invalid credentials", null);
            }

            // Successful login
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(ipAddress);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            attempt.setSuccess(true);
            loginAttemptRepository.save(attempt);

            return new LoginResult(true, "Login successful", user);

        } catch (Exception e) {
            attempt.setSuccess(false);
            attempt.setFailureReason("System error");
            loginAttemptRepository.save(attempt);
            return new LoginResult(false, "Login failed", null);
        }
    }

    // ========================================
    // FORGOT PASSWORD
    // ========================================

    public boolean initiatePasswordReset(String email, String ipAddress) {
        Optional<User> userOpt = userRepository.findByPrimaryEmail(email);

        if (userOpt.isEmpty()) {
            return false; // Don't reveal if email exists
        }

        User user = userOpt.get();

        // Delete any existing reset tokens
        passwordResetRepository.deleteByUserIdAndUsedAtIsNull(user.getId());

        // Create new reset token
        PasswordReset reset = new PasswordReset();
        reset.setUserId(user.getId());
        reset.setResetToken(generateToken());
        reset.setTokenExpiresAt(LocalDateTime.now().plusHours(1));
        reset.setResetIp(ipAddress);
        reset.setCreatedAt(LocalDateTime.now());

        passwordResetRepository.save(reset);

        // TODO: Send email with reset link

        return true;
    }

    // ========================================
    // RESET PASSWORD
    // ========================================

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordReset> resetOpt = passwordResetRepository
                .findByResetTokenAndTokenExpiresAtAfterAndUsedAtIsNull(token, LocalDateTime.now());

        if (resetOpt.isEmpty()) {
            return false;
        }

        PasswordReset reset = resetOpt.get();

        // Create new password
        Password newPasswordEntity = new Password();
        newPasswordEntity.setUser(userRepository.findById(reset.getUserId()).orElse(null));
        newPasswordEntity.setEncryptedPassword(passwordEncoder.encode(newPassword));
        newPasswordEntity.setCreatedAt(LocalDateTime.now());
        newPasswordEntity.setUpdatedAt(LocalDateTime.now());

        passwordRepository.save(newPasswordEntity);

        // Mark reset token as used
        reset.setUsedAt(LocalDateTime.now());
        passwordResetRepository.save(reset);

        return true;
    }

    // ========================================
    // UPDATE PROFILE
    // ========================================

    public boolean updateProfile(Long userId, String firstName, String lastName,
                                 String organization, String jobRole, String profileUrl) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setOrganization(organization);
        user.setJobRole(jobRole);
        user.setProfileUrl(profileUrl);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        return true;
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // ========================================
    // RESULT CLASSES
    // ========================================

    public static class SignupResult {
        private final boolean success;
        private final String message;
        private final Long userId;

        public SignupResult(boolean success, String message, Long userId) {
            this.success = success;
            this.message = message;
            this.userId = userId;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Long getUserId() { return userId; }
    }

    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final User user;

        public LoginResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
    }
}