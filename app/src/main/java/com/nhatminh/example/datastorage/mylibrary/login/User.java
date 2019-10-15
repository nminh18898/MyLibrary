package com.nhatminh.example.datastorage.mylibrary.login;

public class User {
    String userId;
    int role;

    public User(String userId, int role) {
        this.userId = userId;
        this.role = role;
    }

    public User() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
