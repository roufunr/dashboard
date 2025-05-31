// Enhanced UserDto.java - Add to your existing UserDto or replace it

package net.jobdistributor.dashboard.dto;

import net.jobdistributor.dashboard.entity.User;
import net.jobdistributor.dashboard.entity.UserStatus;
import java.time.LocalDateTime;

public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String organization;
    private String jobRole;
    private String profileUrl;
    private UserStatus status;
    private String primaryEmail;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserDto fromUser(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : ""));
        dto.setOrganization(user.getOrganization());
        dto.setJobRole(user.getJobRole());
        dto.setProfileUrl(user.getProfileUrl());
        dto.setStatus(user.getStatus());
        dto.setEmailVerifiedAt(user.getEmailVerifiedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setLastLoginIp(user.getLastLoginIp());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Get primary email
        dto.setPrimaryEmail(user.getEmails().stream()
                .filter(email -> email.getIsPrimary() && !email.getIsDeleted())
                .map(email -> email.getEmailAddress())
                .findFirst()
                .orElse("no-email"));

        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getJobRole() { return jobRole; }
    public void setJobRole(String jobRole) { this.jobRole = jobRole; }

    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public String getPrimaryEmail() { return primaryEmail; }
    public void setPrimaryEmail(String primaryEmail) { this.primaryEmail = primaryEmail; }

    public LocalDateTime getEmailVerifiedAt() { return emailVerifiedAt; }
    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public String getLastLoginIp() { return lastLoginIp; }
    public void setLastLoginIp(String lastLoginIp) { this.lastLoginIp = lastLoginIp; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}