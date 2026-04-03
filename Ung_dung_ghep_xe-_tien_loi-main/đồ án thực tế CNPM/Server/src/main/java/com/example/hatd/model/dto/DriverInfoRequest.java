package com.example.hatd.model.dto;

public class DriverInfoRequest {
    // Tên trường phải khớp với Kotlin Data Class
    private String phoneNumber; 
    private String hangXe;
    private String bienSo;
    private String gioiTinh;

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getHangXe() { return hangXe; }
    public void setHangXe(String hangXe) { this.hangXe = hangXe; }
    public String getBienSo() { return bienSo; }
    public void setBienSo(String bienSo) { this.bienSo = bienSo; }
    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
    
    // Constructors (Thêm nếu cần)
    public DriverInfoRequest() {}
}