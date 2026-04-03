package com.example.hatd.controller;

import com.example.hatd.model.dto.YeuCauChuyenDi;
import com.example.hatd.model.dto.TripRequestResponse;
import com.example.hatd.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    @Autowired
    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    /**
     * Endpoint: POST /api/trips/request
     * Nhận yêu cầu chuyến đi (từ cả USER và DRIVER) và xử lý.
     */
    @PostMapping("/request")
    public ResponseEntity<TripRequestResponse> createTripRequest(@RequestBody YeuCauChuyenDi yeuCau) {
        
        // Gọi Service để xử lý, lưu dữ liệu, và chạy logic matching
        TripRequestResponse response = tripService.saveNewTripRequest(yeuCau);
        
        // Trả về response thành công (HTTP 201 Created)
        return new ResponseEntity<>(response, HttpStatus.CREATED); 
    }
}