package com.example.hatd.model.dto;

public class DriverLocationUpdate {
    // Match ID là bắt buộc để xác định chuyến đi và Khách hàng
    private Long matchId; 
    
    // Tọa độ hiện tại của Driver
    private Double lat;
    private Double lng;
    
    // Hướng di chuyển (Bearing)
    private Double bearing;

    // --- Constructors, Getters và Setters ---
    public DriverLocationUpdate() {}

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }
}