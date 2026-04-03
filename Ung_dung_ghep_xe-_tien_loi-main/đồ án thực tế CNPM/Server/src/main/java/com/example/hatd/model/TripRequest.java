package com.example.hatd.model; 

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Entity JPA để ánh xạ với bảng 'trip_requests' trong database.
 */
@Entity
@Table(name = "trip_requests")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class TripRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Khóa chính tự tăng

    // --- Thông tin địa điểm ---
    
    @Column(name = "ten_diem_di", nullable = false)
    private String tenDiemDi; 
    
    @Column(name = "ten_diem_den", nullable = false)
    private String tenDiemDen;
    
    @Column(name = "vi_do_di", nullable = false)
    private Double viDoDiemDi;
    
    @Column(name = "kinh_do_di", nullable = false)
    private Double kinhDoDiemDi;
    
    @Column(name = "vi_do_den", nullable = false)
    private Double viDoDiemDen;
    
    @Column(name = "kinh_do_den", nullable = false)
    private Double kinhDoDiemDen;

    // --- Thông tin người dùng/Vai trò ---
    
    @Column(name = "so_dien_thoai", nullable = false) 
    private String soDienThoai;
    
    @Column(name = "vai_tro_request")
    private String vaiTro; // USER hoặc DRIVER
    
    // --- Metadata ---
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "status")
    private String status;

    @Column(name = "thoi_gian_driver")
    private LocalDateTime thoiGianDriver; // Thời gian hẹn của Driver

    /**
     * Tự động thiết lập thời gian và trạng thái PENDING
     * trước khi lưu lần đầu (Persist) vào database.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
           createdAt = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        }
        if (status == null) {
            status = "PENDING";
        }
    }
}