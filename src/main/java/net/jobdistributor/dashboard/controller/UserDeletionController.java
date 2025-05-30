package net.jobdistributor.dashboard.controller;

import net.jobdistributor.dashboard.service.UserDeletionService;
import net.jobdistributor.dashboard.util.AuthenticationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserDeletionController {

    @Autowired
    private UserDeletionService userDeletionService;

    // ========================================
    // SOFT DELETE - Reversible Deactivation
    // ========================================

    /**
     * SOFT DELETE: Deactivate account (data preserved, reversible)
     * ✅ SAFE: Data remains in database, can be restored
     *
     * What happens:
     * - User status → INACTIVE
     * - Emails marked as deleted
     * - Data preserved for recovery
     * - Account can be restored later
     */
    @PutMapping("/deactivate")
    public ResponseEntity<Map<String, Object>> softDeleteAccount() {
        Long currentUserId = AuthenticationUtil.requireCurrentUserId();

        UserDeletionService.UserDeletionResult result =
                userDeletionService.softDeleteUser(currentUserId, currentUserId);

        Map<String, Object> response = buildResponse(result, "🟡 SOFT DELETE");

        if (result.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========================================
    // HARD DELETE - Permanent Removal
    // ========================================

    /**
     * HARD DELETE: Permanently delete account and ALL data
     * ⚠️ DANGER: This action is IRREVERSIBLE and PERMANENT
     *
     * What gets deleted:
     * - User record (completely removed)
     * - All emails (permanently deleted)
     * - All passwords (permanently deleted)
     * - All login attempts (permanently deleted)
     * - All password reset tokens (permanently deleted)
     *
     * After this operation:
     * - JWT token becomes invalid
     * - Data cannot be recovered
     * - User must signup again to create new account
     */
    @DeleteMapping("/delete-permanently")
    public ResponseEntity<Map<String, Object>> hardDeleteAccount() {
        Long currentUserId = AuthenticationUtil.requireCurrentUserId();

        UserDeletionService.UserDeletionResult result =
                userDeletionService.hardDeleteUser(currentUserId, currentUserId);

        Map<String, Object> response = buildResponse(result, "🔴 HARD DELETE");

        if (result.isSuccess()) {
            // Add warning about token invalidation
            response.put("warning", "Your session token is now invalid. You have been logged out.");
            response.put("nextAction", "Account permanently deleted. Create new account if needed.");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========================================
    // RESTORE - Reverse Soft Delete
    // ========================================

    /**
     * RESTORE: Reactivate a soft-deleted account
     * ✅ RECOVERY: Only works if account was soft deleted
     *
     * What happens:
     * - User status → ACTIVE
     * - Emails marked as active
     * - Account becomes fully functional again
     *
     * Note: Only works for soft-deleted accounts, not hard-deleted ones
     */
    @PutMapping("/restore")
    public ResponseEntity<Map<String, Object>> restoreAccount() {
        Long currentUserId = AuthenticationUtil.requireCurrentUserId();

        UserDeletionService.UserDeletionResult result =
                userDeletionService.restoreUser(currentUserId, currentUserId);

        Map<String, Object> response = buildResponse(result, "🟢 RESTORE");

        if (result.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========================================
    // INFORMATION ENDPOINTS
    // ========================================

    /**
     * Get deletion preview - shows what would be affected by deletion
     */
    @GetMapping("/deletion-preview")
    public ResponseEntity<Map<String, Object>> getDeletionPreview() {
        Long currentUserId = AuthenticationUtil.requireCurrentUserId();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", currentUserId);

        // Deletion options
        Map<String, Object> options = new HashMap<>();

        // Soft delete info
        Map<String, Object> softDelete = new HashMap<>();
        softDelete.put("name", "Deactivate Account (Soft Delete)");
        softDelete.put("description", "Marks account as inactive but preserves all data");
        softDelete.put("reversible", true);
        softDelete.put("endpoint", "PUT /api/user/deactivate");
        softDelete.put("safety", "SAFE - Data can be recovered");
        options.put("softDelete", softDelete);

        // Hard delete info
        Map<String, Object> hardDelete = new HashMap<>();
        hardDelete.put("name", "Delete Permanently (Hard Delete)");
        hardDelete.put("description", "Permanently removes all user data from database");
        hardDelete.put("reversible", false);
        hardDelete.put("endpoint", "DELETE /api/user/delete-permanently");
        hardDelete.put("safety", "DANGEROUS - Data cannot be recovered");
        hardDelete.put("warning", "⚠️ THIS ACTION CANNOT BE UNDONE!");
        options.put("hardDelete", hardDelete);

        response.put("deletionOptions", options);

        // What will be affected
        Map<String, String> dataAffected = new HashMap<>();
        dataAffected.put("userRecord", "Your account information");
        dataAffected.put("emails", "All email addresses associated with account");
        dataAffected.put("passwords", "Password history");
        dataAffected.put("loginAttempts", "Login history and security logs");
        dataAffected.put("passwordResets", "Any pending password reset tokens");
        response.put("dataAffected", dataAffected);

        return ResponseEntity.ok(response);
    }

    /**
     * Get account status - check if account is active, inactive, etc.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAccountStatus() {
        Long currentUserId = AuthenticationUtil.requireCurrentUserId();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", currentUserId);
        response.put("message", "Account is currently active");
        response.put("canRestore", false);
        response.put("canDeactivate", true);
        response.put("canHardDelete", true);

        return ResponseEntity.ok(response);
    }

    // ========================================
    // FUTURE: ADMIN ENDPOINTS
    // ========================================

    /**
     * Admin endpoint: Delete any user by ID (HARD DELETE)
     * TODO: Add admin role validation when roles are implemented
     */
    @DeleteMapping("/admin/delete-user/{userId}")
    public ResponseEntity<Map<String, Object>> adminDeleteUser(@PathVariable Long userId) {
        Long currentUserId = AuthenticationUtil.requireCurrentUserId();

        // TODO: Add admin role check here
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Admin functionality not yet implemented");
        response.put("note", "Role-based access control will be added in future version");

        return ResponseEntity.status(403).body(response);
    }

    /**
     * Admin endpoint: Soft delete any user by ID
     * TODO: Add admin role validation when roles are implemented
     */
    @PutMapping("/admin/deactivate-user/{userId}")
    public ResponseEntity<Map<String, Object>> adminDeactivateUser(@PathVariable Long userId) {
        Long currentUserId = AuthenticationUtil.requireCurrentUserId();

        // TODO: Add admin role check here
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Admin functionality not yet implemented");

        return ResponseEntity.status(403).body(response);
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private Map<String, Object> buildResponse(UserDeletionService.UserDeletionResult result, String operation) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        response.put("operation", operation);

        if (result.isSuccess() && result.getSummary() != null) {
            Map<String, Object> summary = new HashMap<>();
            summary.put("type", result.getSummary().getType().name());
            summary.put("description", result.getSummary().getDescription());
            summary.put("usersAffected", result.getSummary().getUsersAffected());
            summary.put("emailsAffected", result.getSummary().getEmailsAffected());
            summary.put("passwordsAffected", result.getSummary().getPasswordsAffected());
            summary.put("loginAttemptsAffected", result.getSummary().getLoginAttemptsAffected());
            summary.put("passwordResetsAffected", result.getSummary().getPasswordResetsAffected());
            response.put("summary", summary);
        }

        return response;
    }
}