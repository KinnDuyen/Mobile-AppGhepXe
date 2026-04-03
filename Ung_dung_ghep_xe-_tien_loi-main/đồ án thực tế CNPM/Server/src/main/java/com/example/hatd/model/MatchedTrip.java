package com.example.hatd.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_chuyen_di") // Đổi tên bảng cho phù hợp với các cột mới
@Data
@NoArgsConstructor
public class MatchedTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Thông tin chuyến đi ---
    @Column(name = "ten_diem_di_user")
    private String userOriginName;

    @Column(name = "ten_diem_den_user")
    private String userDestinationName;

    @Column(name = "ten_diem_di_driver")
    private String driverOriginName;

    @Column(name = "ten_diem_den_driver")
    private String driverDestinationName;

    // Khoảng cách chuyến đi của User (km)
    @Column(name = "khoang_cach_km")
    private Double userTripDistanceKm;

    @Column(name = "gia_tien")
    private Double giaTien;

    @Column(name = "hinh_thuc_thanh_toan")
    private String hinhThucThanhToan;

    // --- Thông tin User ---
    @Column(name = "sdt_user", nullable = false)
    private String userPhone;

    @Column(name = "ten_user")
    private String tenUser;

    // --- Thông tin Driver ---
    @Column(name = "sdt_driver", nullable = false)
    private String driverPhone;

    @Column(name = "ten_driver")
    private String tenDriver;

    @Column(name = "bien_so_xe")
    private String bienSoXe;

    @Column(name = "hang_xe")
    private String hangXe;

    @Column(name = "status")
    private String status = "MATCHED";

    // --- Thông tin thời gian ---
    @Column(name = "thoi_gian_driver_den_user")
    private LocalDateTime thoiGianDriverDenUser;

    @Column(name = "thoi_gian_tao_chuyen", nullable = false, updatable = false)
    private LocalDateTime matchedAt;

    @Column(name = "confirmed_at") 
    private LocalDateTime confirmedAt; 
    
    @Column(name = "accepted_at") 
    private LocalDateTime acceptedAt;

    @Column(name = "vi_do_di")
    private Double viDoDiemDi;

    @Column(name = "kinh_do_di")
    private Double kinhDoDiemDi;

    @Column(name = "vi_do_den")
    private Double viDoDiemDen;

    @Column(name = "kinh_do_den")
    private Double kinhDoDiemDen;

     @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "picked_up_at") 
    private LocalDateTime pickedUpAt;

    @Column(name = "ly_do_huy")
    private String lyDoHuy;

    @Column(name = "driver_current_lat")
    private Double driverCurrentLat;

    @Column(name = "driver_current_lng")
    private Double driverCurrentLng;
    
    @Column(name = "driver_current_bearing")
    private Double driverCurrentBearing;

    @PrePersist
    protected void onCreate() {
        matchedAt = LocalDateTime.now();
    }
}