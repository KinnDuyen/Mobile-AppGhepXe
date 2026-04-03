package com.example.hatd.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChuyenDiInfoRequest {

    private String phoneNumber;
    private String role;
    
    // Vị trí định vị hiện tại (GPS thiết bị)
    private Double viDo;
    private Double kinhDo;
    
    // ✅ VỊ TRÍ ĐIỂM ĐI (BẮT ĐẦU CHUYẾN ĐI)
    private Double viDoDiemDi;
    private Double kinhDoDiemDi;
    
    // ✅ VỊ TRÍ ĐIỂM ĐẾN (KẾT THÚC CHUYẾN ĐI)
    private Double viDoDiemDen;
    private Double kinhDoDiemDen;
    
    private String thoiGian; // ISO string yyyy-MM-dd'T'HH:mm:ss
    private String diemDi; // Tên địa chỉ Điểm Đi
    private String diemDen; // Tên địa chỉ Điểm Đến
    private Long thoiGianDriver;

    public ChuyenDiInfoRequest() {}

    // --- GETTERS & SETTERS HIỆN CÓ ---
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Double getViDo() { return viDo; }
    public void setViDo(Double viDo) { this.viDo = viDo; }

    public Double getKinhDo() { return kinhDo; }
    public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }

    public String getThoiGian() { return thoiGian; }
    public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }

    public String getDiemDi() { return diemDi; }
    public void setDiemDi(String diemDi) { this.diemDi = diemDi; }

    public String getDiemDen() { return diemDen; }
    public void setDiemDen(String diemDen) { this.diemDen = diemDen; }

    public Long getThoiGianDriver() { return thoiGianDriver; }
    public void setThoiGianDriver(Long thoiGianDriver) { this.thoiGianDriver = thoiGianDriver; }
    
    // --- ✅ GETTERS & SETTERS MỚI CHO TỌA ĐỘ ---
    
    // Điểm Đi
    public Double getViDoDiemDi() { return viDoDiemDi; }
    public void setViDoDiemDi(Double viDoDiemDi) { this.viDoDiemDi = viDoDiemDi; }

    public Double getKinhDoDiemDi() { return kinhDoDiemDi; }
    public void setKinhDoDiemDi(Double kinhDoDiemDi) { this.kinhDoDiemDi = kinhDoDiemDi; }

    // Điểm Đến
    public Double getViDoDiemDen() { return viDoDiemDen; }
    public void setViDoDiemDen(Double viDoDiemDen) { this.viDoDiemDen = viDoDiemDen; }

    public Double getKinhDoDiemDen() { return kinhDoDiemDen; }
    public void setKinhDoDiemDen(Double kinhDoDiemDen) { this.kinhDoDiemDen = kinhDoDiemDen; }
}