package com.example.hatd.controller;

import com.example.hatd.model.AccountDriver;
import com.example.hatd.model.dto.DriverInfoRequest;
import com.example.hatd.model.dto.DriverResponse;
import com.example.hatd.service.DriverInforService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    @Autowired
    private DriverInforService driverInforService;

    @PostMapping("/save-info")
    public ResponseEntity<?> saveDriverInfo(@RequestBody DriverInfoRequest request) {
        try {
            AccountDriver saved = driverInforService.saveOrUpdateDriverInfo(request);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Lỗi hệ thống: " + e.getMessage());
        }
    }

     @GetMapping("/me/{soDienThoai}")
    public ResponseEntity<DriverResponse> getCurrentDriver(@PathVariable String soDienThoai) {
        AccountDriver driver = driverInforService.getDriverByPhone(soDienThoai);

        if (driver != null) {
            // Mapping entity -> DTO
            DriverResponse response = new DriverResponse(
                driver.getIdDriver(),
                driver.getPhoneNumber(),
                driver.getGioiTinh(),
                driver.getBienSo(),
                driver.getHangXe(),
                driver.getRole(), // ✅ đảm bảo role có getter hợp lệ trong entity
                "Lấy thông tin tài xế thành công"
            );
            return ResponseEntity.ok(response);
        } else {
            // Nếu không tìm thấy, trả về message trong DTO
            DriverResponse response = new DriverResponse(
                null, null, null, null, null, null,
                "Không tìm thấy tài xế với số điện thoại: " + soDienThoai
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
