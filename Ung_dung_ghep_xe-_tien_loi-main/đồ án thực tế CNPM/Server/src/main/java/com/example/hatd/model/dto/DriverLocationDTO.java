package com.example.hatd.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DriverLocationDTO {
    private Double lat;
    private Double lng;
    private Double bearing;

    public DriverLocationDTO(Double lat, Double lng, Double bearing) {
        this.lat = lat;
        this.lng = lng;
        this.bearing = bearing;
    }
}