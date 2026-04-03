package com.example.hatd.controller;

import com.example.hatd.service.RideService; // Service mới để xử lý logic xác nhận
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.hatd.model.dto.ReviewRequest;
import com.example.hatd.model.dto.CancelRideRequest;
import com.example.hatd.model.dto.DriverLocationUpdate;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    @Autowired
    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    /**
     * Endpoint: POST /api/rides/confirm/{matchId}
     * Mục đích: Xử lý yêu cầu "Đặt xe" (Confirm Booking) từ User.
     * Trả về: 204 No Content nếu thành công, 400/404 nếu thất bại.
     */
    @PostMapping("/confirm/{matchId}")
    public ResponseEntity<Void> confirmBooking(@PathVariable Long matchId) {
        
        try {
            // Gọi service để xác nhận Match và cập nhật trạng thái trong DB
            boolean success = rideService.confirmUserBooking(matchId);

            if (success) {
                // ✅ Trả về 204 No Content (Thành công và không có nội dung trả về)
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); 
            } else {
                // 400 Bad Request nếu Match không hợp lệ (đã bị hủy/hết hạn)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); 
            }
        } catch (Exception e) {
            // Xử lý các lỗi nội bộ khác (ví dụ: Database Error)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/location-update")
    public ResponseEntity<Void> updateDriverLocation(@RequestBody DriverLocationUpdate request) {
        try {
            // Service xử lý: 1. Lưu DB/Cache. 2. Gửi Socket cho User.
            // Chú ý: Hàm updateDriverLocation trong Service đang nhận DriverLocationDTO, cần sửa lại
            // Tạm thời truyền request vào (vì request có đủ các trường DTO)
            boolean success = rideService.updateDriverLocation(request); 

            if (success) {
                // Trả về 204 No Content (Thành công)
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); 
            } else {
                // 400 Bad Request nếu matchId không hợp lệ
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); 
            }
        } catch (Exception e) {
            // Log lỗi và trả về lỗi Server
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

     @PostMapping("/complete/{matchId}")
    public ResponseEntity<Void> completeRide(@PathVariable("matchId") Long matchId) {

        boolean success = rideService.completeRide(matchId);

        if (success) {
            // Trả về 200 OK (Thành công)
            return ResponseEntity.ok().build();
        } else {
            // Trả về 404 NOT FOUND nếu chuyến đi không tồn tại
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/review/{matchId}")
    public ResponseEntity<Void> reviewRide(
        @PathVariable Long matchId,
        @RequestBody ReviewRequest request) { // ✅ Nhận ReviewRequest từ body
        
        // Đặt Match ID vào request để Service dễ dàng xử lý
        // (Tuy nhiên, chúng ta đã lấy matchId từ @PathVariable)

        try {
            boolean success = rideService.saveReview(matchId, request);

            if (success) {
                // Trả về 204 No Content (Thành công và không cần nội dung)
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                // 400 Bad Request nếu Match ID không hợp lệ hoặc chuyến đi chưa hoàn thành
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            // Log lỗi (nếu cần) và trả về lỗi Server
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelRide(@RequestBody CancelRideRequest request) { // ✅ Không cần @PathVariable, nhận request body
        
        try {
            // Gọi service để hủy chuyến
            boolean success = rideService.cancelRide(request.getMatchId(), request.getReason());

            if (success) {
                // ✅ Trả về 204 No Content (Thành công và không có nội dung trả về)
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); 
            } else {
                // 400 Bad Request nếu Match ID không hợp lệ hoặc trạng thái không thể hủy
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); 
            }
        } catch (Exception e) {
            // Xử lý các lỗi nội bộ khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/picked-up/{matchId}")
public ResponseEntity<Void> pickedUpRide(@PathVariable Long matchId) {
    try {
        boolean success = rideService.pickedUpRide(matchId);

        if (success) {
            // ✅ Trả về 204 No Content
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            // 🛑 Trả về 400 Bad Request cho lỗi logic (như trạng thái không hợp lệ)
            // Nếu bạn muốn phân biệt giữa "Không tìm thấy chuyến" và "Trạng thái không hợp lệ",
            // bạn cần ném Exception chi tiết hơn từ Service và bắt ở đây.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    } catch (Exception e) {
        // Xử lý lỗi nội bộ
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

    @PostMapping("/reject/{matchId}") // Sửa từ /api/matches/reject/{matchId} thành /api/rides/reject/{matchId} cho đồng bộ
    public ResponseEntity<Void> rejectMatch(@PathVariable Long matchId) {
        try {
            // Gọi service để từ chối Match và cập nhật trạng thái trong DB
            boolean success = rideService.rejectDriverRide(matchId);

            if (success) {
                // ✅ Trả về 204 No Content
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                // 400 Bad Request nếu Match không tồn tại hoặc không ở trạng thái hợp lệ để từ chối
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            // Xử lý các lỗi nội bộ
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}