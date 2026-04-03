package com.example.hatd.controller;

import com.example.hatd.model.dto.MatchNotificationDTO;
import com.example.hatd.service.MatchQueryService; // Service mới để truy vấn Match
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchQueryService matchQueryService;

    @Autowired
    public MatchController(MatchQueryService matchQueryService) {
        this.matchQueryService = matchQueryService;
    }

    /**
     * Endpoint: GET /api/matches/latest/{soDienThoai}
     * Dùng để kiểm tra Match bị bỏ lỡ (missed match)
     */
    @GetMapping("/latest/{soDienThoai}")
    public ResponseEntity<MatchNotificationDTO> getLatestPendingMatch(
            @PathVariable("soDienThoai") String soDienThoai) {

        // Gọi service để tìm Match mới nhất (status = MATCHED/PENDING)
        MatchNotificationDTO latestMatch = matchQueryService.findLatestPendingMatch(soDienThoai);

        if (latestMatch != null) {
            // Trả về 200 OK và dữ liệu Match đầy đủ
            return ResponseEntity.ok(latestMatch); 
        } else {
            // Trả về 204 No Content nếu không tìm thấy Match nào đang chờ xác nhận
            // (204 tốt hơn 404 vì API là hợp lệ, chỉ dữ liệu là không có)
            return ResponseEntity.noContent().build(); 
        }
    }
}