package com.example.hatd.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MatchNotificationDTO {

    private Long matchId;
    private String message; // Ví dụ: "Đã tìm thấy tài xế!"

    // Thông tin tài xế (gửi cho User)
    private String tenDriver;
    private String sdtDriver;
    private String bienSoXe;
    private String hangXe;

    private Double viDoDiemDi;
    private Double kinhDoDiemDi;
    private Double viDoDiemDen;
    private Double kinhDoDiemDen;

    // Thông tin khách (gửi cho Driver)
    private String tenUser;
    private String sdtUser;

    // Thông tin chuyến đi
    private String tenDiemDiUser;
    private String tenDiemDenUser;
    private Double giaTien;
    private LocalDateTime thoiGianDriverDenUser;
    private String hinhThucThanhToan;

    // Bạn có thể thêm bất cứ trường nào từ MatchedTrip mà UI cần
}