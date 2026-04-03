package com.example.hatd.model.dto;

public class DriverResponse {
    private Long IdDriver;
    private String phoneNumber;
    private String gioiTinh;
    private String bienSo;
     private String hangXe;
    private String role;
    private String message;

    public DriverResponse(Long IdDriver, String phoneNumber,String gioitinh, String bienSo, String hangXe, String role, String message) {
        this.IdDriver = IdDriver;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.gioiTinh = gioitinh;
        this.bienSo = bienSo;
        this.hangXe = hangXe;
        this.message = message;
    }

    // Getters và Setters đầy đủ
    public Long getIdDriver() { return IdDriver; }
    public void setIdDriver(Long IdDriver) { this.IdDriver = IdDriver; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getBienSo() { return bienSo; }
    public void setBienSo(String bienSo) { this.bienSo = bienSo; }

    public String getHangXe() { return hangXe; }
    public void setHangXe(String hangXe) { this.hangXe = hangXe; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
