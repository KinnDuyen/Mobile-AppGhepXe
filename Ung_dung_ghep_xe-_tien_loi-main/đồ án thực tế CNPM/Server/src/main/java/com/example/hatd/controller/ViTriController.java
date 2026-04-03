package com.example.hatd.controller;

import com.example.hatd.model.ViTri;
import com.example.hatd.model.dto.ChuyenDiInfoRequest;
import com.example.hatd.service.ViTriService;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chuyen-di")
public class ViTriController {

    private final ViTriService viTriService;

    public ViTriController(ViTriService viTriService) {
        this.viTriService = viTriService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendChuyenDi(@RequestBody ChuyenDiInfoRequest request) {
        try {
            ViTri saved = viTriService.saveChuyenDi(request);
            // Lý tưởng: Chuyển đổi ViTri -> ChuyenDiResponse trước khi trả về
            // Ví dụ: return ResponseEntity.ok(new ChuyenDiResponse(saved));
            return ResponseEntity.ok(saved); 
            
        } catch (IllegalArgumentException e) {
            // Bắt lỗi về định dạng dữ liệu (ví dụ: lỗi thời gian)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            // Bắt lỗi hệ thống, DB (ví dụ: ConstraintViolationException)
            // Lỗi 500 ở đây là đúng cho các lỗi không lường trước được (Server)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi server: Đã xảy ra lỗi hệ thống khi lưu dữ liệu.");
        }
    }
}