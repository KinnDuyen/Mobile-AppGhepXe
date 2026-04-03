package com.example.hatd.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "taikhoan_driver") 
public class AccountDriver { 
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long IdDriver;

    // SĐT là duy nhất và bắt buộc
    @Column(name = "SoDienThoai", unique = true, nullable = false) 
    private String phoneNumber; 
    
    
    @Column(name = "GioiTinh") 
    private String gioiTinh;
    
    @Column(name = "Bienso")
    private String bienSo; 

    @Column(name = "Hangxe")
    private String hangXe;
    

    @Column(name = "Role")
    private String role; // Vai trò mặc định là USER

    // Constructor mặc định
    public AccountDriver() {}

    // Constructor đầy đủ tham số
    public AccountDriver(Long IdDriver, String phoneNumber, String gioiTinh, String bienSo, String hangXe) {
        this.IdDriver = IdDriver;
        this.phoneNumber = phoneNumber;
        this.gioiTinh = gioiTinh;
        this.bienSo = bienSo;
        this.hangXe = hangXe;
    }

     @PrePersist
    public void prePersist() {
        if (role == null) {
            role = "DRIVER";
        }
    }

    // --- Getters và Setters (Đảm bảo camelCase) ---
    public Long getIdDriver() { return IdDriver; }
    public void setIdDriver(Long IdDriver) { this.IdDriver = IdDriver; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
    public String getBienSo() { return bienSo; }
    public void setBienSo(String bienSo) { this.bienSo = bienSo; }
    public String getHangXe() { return hangXe; }
    public void setHangXe(String hangXe) { this.hangXe = hangXe; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}