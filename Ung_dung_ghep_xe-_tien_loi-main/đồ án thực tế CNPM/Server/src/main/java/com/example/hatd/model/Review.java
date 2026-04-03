package com.example.hatd.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "danh_gia_chuyen_di")
@Data
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Khóa ngoại liên kết với chuyến đi được đánh giá
    @Column(name = "match_id", nullable = false)
    private Long matchId; 
    
    // Đánh giá sao (từ 1 đến 5)
    @Column(name = "rating", nullable = false)
    private Integer rating; 

    // Các lời khen được chọn, lưu dưới dạng JSON hoặc String được phân tách
    @Column(name = "compliments")
    private String compliments; // Lưu dưới dạng chuỗi JSON hoặc CSV

    // Ghi chú/Bình luận của User
    @Column(name = "note", length = 500)
    private String note;

    // Thông tin người đánh giá (User)
    @Column(name = "sdt_user")
    private String userPhone;
    
    // Thông tin người được đánh giá (Driver)
    @Column(name = "sdt_driver")
    private String driverPhone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}