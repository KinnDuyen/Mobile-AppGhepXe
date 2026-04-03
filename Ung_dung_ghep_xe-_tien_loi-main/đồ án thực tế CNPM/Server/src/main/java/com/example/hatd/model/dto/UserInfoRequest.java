package com.example.hatd.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoRequest {
    // Tên trường phải khớp với Kotlin Data Class
    private String phoneNumber; 
    private String name; 
    private String cccd; // Số căn cước công dân
    // private String role;

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    // public String getRole() { return role; }
    // public void setRole(String role) { this.role = role; }
    // Constructors (Thêm nếu cần)
    public UserInfoRequest() {}
}