package net.jobdistributor.dashboard.dto;

import jakarta.validation.constraints.*;

public class UpdateProfileRequest {
    @NotBlank(message = "First name is required")
    @Size(max = 32, message = "First name too long")
    private String firstName;

    @Size(max = 32, message = "Last name too long")
    private String lastName;

    @NotBlank(message = "Organization is required")
    @Size(max = 100, message = "Organization name too long")
    private String organization;

    @NotBlank(message = "Job role is required")
    @Size(max = 100, message = "Job role too long")
    private String jobRole;

    private String profileUrl;

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getJobRole() { return jobRole; }
    public void setJobRole(String jobRole) { this.jobRole = jobRole; }

    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }
}