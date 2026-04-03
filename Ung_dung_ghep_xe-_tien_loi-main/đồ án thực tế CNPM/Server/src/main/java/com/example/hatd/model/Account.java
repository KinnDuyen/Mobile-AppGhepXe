package com.example.hatd.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.PreUpdate;

@Entity
@Table(name = "taikhoan") 
public class Account { 
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SĐT là duy nhất và bắt buộc
    @Column(name = "SoDienThoai", unique = true, nullable = false) 
    private String soDienThoai; 
    
    @Column(name = "Name", nullable = false)
    private String name; 
    
    @Column(name = "CanCuocCongDan") 
    private String canCuocCongDan;

    @Column(name = "Role")
    private String role; // Vai trò mặc định là USER

    // Constructor mặc định
    public Account() {}

    // Constructor đầy đủ tham số
    public Account(String soDienThoai, String name, String canCuocCongDan, String bienSo, String role, Double kinhDo, Double viDo) {
        this.soDienThoai = soDienThoai;
        this.name = name;
        this.canCuocCongDan = canCuocCongDan;
        this.role = role;
    }

    @PrePersist
    public void prePersist() {
        if (role == null) {
            role = "USER";
        }
    }

    // --- Getters và Setters (Đảm bảo camelCase) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCanCuocCongDan() { return canCuocCongDan; }
    public void setCanCuocCongDan(String canCuocCongDan) { this.canCuocCongDan = canCuocCongDan; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}