package com.example.visiontest;

public class UserProfile {

    // all varaibles to be measured stored here, consistency accuracy etc
    private String email;
    private String username;
    private String profilePictureUrl;

    private String reps;

    public UserProfile() {
        // Default constructor required for calls to DataSnapshot.getValue(UserProfile.class)
    }

    public UserProfile(String email, String username, String profilePictureUrl) {
        this.email = email;
        this.username = username;
        this.profilePictureUrl = profilePictureUrl;
        this.reps= "default reps";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getReps() {
        return reps;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
// Getters and setters
}

