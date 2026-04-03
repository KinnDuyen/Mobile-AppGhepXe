package com.example.hatd.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;

@Entity
@Table(name = "otp")
public class otp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idotp;

    private String SoDienThoai;
    private String Otp_code;
    private LocalDateTime ThoiGianHetHan;
    private Boolean used;
    // Constructor mặc định
    public otp() {}
    public otp(Long idotp, String SoDienThoai, String Otp_code, LocalDateTime ThoiGianHetHan, Boolean used) {   
        this.idotp = idotp;       
        this.SoDienThoai = SoDienThoai;
        this.Otp_code = Otp_code;       
        this.ThoiGianHetHan = ThoiGianHetHan;
        this.used = used;
    }   
    public Long getIdotp() {
        return idotp;
    }
    public void setIdotp(Long idotp) {
        this.idotp = idotp;
    }
    public String getSoDienThoai() {
        return SoDienThoai;
    }
    public void setSoDienThoai(String SoDienThoai) {
        this.SoDienThoai = SoDienThoai;
    }
    public String getOtp_code() {
        return Otp_code;
    }
    public void setOtp_code(String Otp_code) {
        this.Otp_code = Otp_code;
    }
    public LocalDateTime getThoiGianHetHan() {
        return ThoiGianHetHan;
    }
    public void setThoiGianHetHan(LocalDateTime ThoiGianHetHan) {
        this.ThoiGianHetHan = ThoiGianHetHan;
    }
    public Boolean getUsed() {
        return used;
    }
    public void setUsed(Boolean used) {
        this.used = used;
    }

}