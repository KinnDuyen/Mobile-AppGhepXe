package com.example.hatd.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object): Lớp này CHỈ dùng để nhận dữ liệu
 * từ Frontend (Yêu cầu) gửi lên Controller.
 * NÓ KHÔNG PHẢI LÀ ENTITY.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YeuCauChuyenDi {

    // --- Thông tin địa điểm ---
    private String tenDiemDi; 
    private String tenDiemDen;
    private Double viDoDiemDi;
    private Double kinhDoDiemDi;
    private Double viDoDiemDen;
    private Double kinhDoDiemDen;

    // --- Thông tin người dùng/Vai trò ---
    private String soDienThoai;
    
    // Trường quan trọng để phân biệt USER hay DRIVER
    private String vaiTro; 
    
    // Thời gian hẹn của DRIVER (FE gửi lên dạng String, Spring tự parse)
    private LocalDateTime thoiGianDriver; 
}