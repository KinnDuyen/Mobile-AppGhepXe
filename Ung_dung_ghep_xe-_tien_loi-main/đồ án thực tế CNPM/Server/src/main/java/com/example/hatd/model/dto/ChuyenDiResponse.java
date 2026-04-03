package com.example.hatd.model.dto;

import java.time.LocalDateTime;

// DTO này được sử dụng để trả về cho Client (ứng dụng Android)
// Nó ánh xạ chính xác với Kotlin Data Class ChuyenDiResponse.kt
public class ChuyenDiResponse {

    private Long idViTri;
    private String phoneNumber;
    private String role;
    
    // Vị trí Định vị hiện tại
    private Double viDo;
    private Double kinhDo;
    
    // ✅ TỌA ĐỘ ĐIỂM ĐI MỚI
    private Double viDoDiemDi;
    private Double kinhDoDiemDi;
    
    // ✅ TỌA ĐỘ ĐIỂM ĐẾN MỚI
    private Double viDoDiemDen;
    private Double kinhDoDiemDen;
    
    private LocalDateTime thoiGian;
    private String diemDi;
    private String diemDen;
    private String trangThai; // Trạng thái của chuyến đi

    // Constructors
    public ChuyenDiResponse() {}

    // ✅ CẬP NHẬT CONSTRUCTOR ĐẦY ĐỦ THAM SỐ
    public ChuyenDiResponse(Long idViTri, String phoneNumber, String role, 
                            Double viDo, Double kinhDo, 
                            Double viDoDiemDi, Double kinhDoDiemDi, 
                            Double viDoDiemDen, Double kinhDoDiemDen, 
                            LocalDateTime thoiGian, String diemDi, String diemDen, String trangThai) {
        this.idViTri = idViTri;
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
        this.trangThai = trangThai;
    }

    // --- Getters and Setters Hiện tại (Không thay đổi) ---

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

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    
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