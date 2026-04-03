package com.example.hatd.model.dto;

import lombok.Data;

@Data
public class CancelRideRequest {
    private Long matchId; // Giống với trường trong Kotlin Client
    private String reason; // Lý do hủy chuyến
}