package com.example.hatd.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vi_tri")
public class ViTri {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idViTri;

    @Column(name = "so_dien_thoai", nullable = false, unique = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String role;

    // Tọa độ Định vị hiện tại (GPS thiết bị)
    @Column(nullable = false)
    private Double viDo;

    @Column(nullable = false)
    private Double kinhDo;
    
    // ✅ TỌA ĐỘ ĐIỂM ĐI
    @Column(name = "vi_do_diem_di")
    private Double viDoDiemDi;

    @Column(name = "kinh_do_diem_di")
    private Double kinhDoDiemDi;

    // ✅ TỌA ĐỘ ĐIỂM ĐẾN
    @Column(name = "vi_do_diem_den")
    private Double viDoDiemDen;

    @Column(name = "kinh_do_diem_den")
    private Double kinhDoDiemDen;

    @Column(nullable = false)
    private LocalDateTime thoiGian;

    @Column(name = "thoi_gian_driver") 
    private Long thoiGianDriver;

    @Column(name = "diem_di")
    private String diemDi;
    
    @Column(name = "diem_den")
    private String diemDen;

    // --- Constructors ---
    public ViTri() {}
    
    // ✅ CẬP NHẬT CONSTRUCTOR VỚI TỌA ĐỘ MỚI
    public ViTri(String phoneNumber, String role, Double viDo, Double kinhDo, 
                 Double viDoDiemDi, Double kinhDoDiemDi, Double viDoDiemDen, Double kinhDoDiemDen,
                 LocalDateTime thoiGian, String diemDi, String diemDen, Long thoiGianDriver) {
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.viDo = viDo;
        this.kinhDo = kinhDo;
        
        this.viDoDiemDi = viDoDiemDi;
        this.kinhDoDiemDi = kinhDoDiemDi;
        this.viDoDiemDen = viDoDiemDen;
        this.kinhDoDiemDen = kinhDoDiemDen;
        
        this.thoiGian = thoiGian;
        this.diemDi = diemDi;
        this.diemDen = diemDen;
        this.thoiGianDriver = thoiGianDriver;
    }

    // --- Getters & Setters Hiện tại ---
    public Long getIdViTri() { return idViTri; }
    public void setIdViTri(Long idViTri) { this.idViTri = idViTri; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Double getViDo() { return viDo; }
    public void setViDo(Double viDo) { this.viDo = viDo; }

    public Double getKinhDo() { return kinhDo; }
    public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }

    public String getDiemDi() { return diemDi; }
    public void setDiemDi(String diemDi) { this.diemDi = diemDi; }

    public String getDiemDen() { return diemDen; }
    public void setDiemDen(String diemDen) { this.diemDen = diemDen; }

    public Long getThoiGianDriver() { return thoiGianDriver; }
    public void setThoiGianDriver(Long thoiGianDriver) { this.thoiGianDriver = thoiGianDriver; }
    
    // --- ✅ GETTERS & SETTERS MỚI CHO TỌA ĐỘ ĐIỂM ĐI/ĐẾN ---

    public Double getViDoDiemDi() { return viDoDiemDi; }
    public void setViDoDiemDi(Double viDoDiemDi) { this.viDoDiemDi = viDoDiemDi; }

    public Double getKinhDoDiemDi() { return kinhDoDiemDi; }
    public void setKinhDoDiemDi(Double kinhDoDiemDi) { this.kinhDoDiemDi = kinhDoDiemDi; }

    public Double getViDoDiemDen() { return viDoDiemDen; }
    public void setViDoDiemDen(Double viDoDiemDen) { this.viDoDiemDen = viDoDiemDen; }

    public Double getKinhDoDiemDen() { return kinhDoDiemDen; }
    public void setKinhDoDiemDen(Double kinhDoDiemDen) { this.kinhDoDiemDen = kinhDoDiemDen; }
}