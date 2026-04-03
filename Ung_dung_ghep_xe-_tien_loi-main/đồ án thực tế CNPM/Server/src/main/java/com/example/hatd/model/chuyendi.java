package com.example.hatd.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import java.time.LocalDateTime; // Thêm dòng này nếu chưa import

@Entity
@Table(name = "chuyendi")
public class chuyendi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idchuyen;

    private Long id_driver;
    private Long id_user;
    private String Loaixe;
    private String DiemDi;
    private String DiemDen;
    private Double GiaTien;
    private Long Phuongthuc;
    private LocalDateTime ThoiGianBatDau;
    private LocalDateTime ThoiGianKetThuc;
    private Long GhiChu;  
  
    // Constructor mặc định
    public chuyendi() {}
    public chuyendi(Long idchuyen, String Loaixe, String DiemDi, String DiemDen, Long id_user, Long id_driver, Double GiaTien, Long Phuongthuc, LocalDateTime ThoiGianBatDau, LocalDateTime  ThoiGianKetThuc, Long GhiChu) {   
        this.idchuyen = idchuyen;       
        this.Loaixe = Loaixe;
        this.DiemDi = DiemDi;       
        this.DiemDen = DiemDen;
        this.id_user = id_user;
        this.id_driver = id_driver;
        this.GiaTien = GiaTien;
        this.Phuongthuc = Phuongthuc;       
        this.ThoiGianBatDau = ThoiGianBatDau;
        this.ThoiGianKetThuc = ThoiGianKetThuc;
        this.GhiChu = GhiChu;
    }
    public Long getIdchuyen() {
        return idchuyen;
    }       
    public void setIdchuyen(Long idchuyen) {
        this.idchuyen = idchuyen;
    }
    public String getLoaixe() {
        return Loaixe;
    }
    public void setLoaixe(String Loaixe) {
        this.Loaixe = Loaixe;
    }

        public String getDiemDi() {
            return DiemDi;
        }
        public void setDiemDi(String DiemDi) {
            this.DiemDi = DiemDi;
        }
    public String getDiemDen() {
        return DiemDen;
    }
    public void setDiemDen(String DiemDen) {
        this.DiemDen = DiemDen;
    }
    public Long getId_user() {
        return id_user;
    }
    public void setId_user(Long id_user) {
        this.id_user = id_user;
    }
    public Long getId_driver() {
        return id_driver;
    }
    public void setId_driver(Long id_driver) {
        this.id_driver = id_driver;
    }
    public Double getGiaTien() {
        return GiaTien;
    }
    public void setGiaTien(Double GiaTien) {
        this.GiaTien = GiaTien;
    }
    public Long getPhuongthuc() {
        return Phuongthuc;
    }
    public void setPhuongthuc(Long Phuongthuc) {
        this.Phuongthuc = Phuongthuc;
    }
    public LocalDateTime  getThoiGianBatDau() {
        return ThoiGianBatDau;
    }
    public void setThoiGianBatDau(LocalDateTime  ThoiGianBatDau) {
        this.ThoiGianBatDau = ThoiGianBatDau;
    }
    public LocalDateTime  getThoiGianKetThuc() {
        return ThoiGianKetThuc;
    }
    public void setThoiGianKetThuc(LocalDateTime  ThoiGianKetThuc) {
        this.ThoiGianKetThuc = ThoiGianKetThuc;
    }
    public Long getGhiChu() {
        return GhiChu;
    }
    public void setGhiChu(Long GhiChu) {
        this.GhiChu = GhiChu;
    }
}