package com.example.hatd.model.dto;

public class UserResponse {
    private Long userId;
    private String name;
    private String phoneNumber;
    private String canCuocCongDan;
    private String role;
    private String message;

    public UserResponse(Long userId, String name, String phoneNumber,
                        String canCuocCongDan, String role, String message) {
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.canCuocCongDan = canCuocCongDan;
        this.role = role;
        this.message = message;
    }

    // Getters và Setters đầy đủ
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCanCuocCongDan() { return canCuocCongDan; }
    public void setCanCuocCongDan(String canCuocCongDan) { this.canCuocCongDan = canCuocCongDan; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
