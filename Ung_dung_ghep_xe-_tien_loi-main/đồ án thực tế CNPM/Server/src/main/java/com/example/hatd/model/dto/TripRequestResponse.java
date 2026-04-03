package com.example.hatd.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object): Lớp này CHỈ dùng để chứa thông tin
 * trả về cho Frontend sau khi xử lý yêu cầu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripRequestResponse {
    
    private Long requestId;       // ID của chuyến đi vừa tạo
    private String status;        // Trạng thái (VD: PENDING)
    private LocalDateTime createdAt;  // Thời gian tạo
    private String message;       // Thông điệp (VD: Tạo thành công)
    
}