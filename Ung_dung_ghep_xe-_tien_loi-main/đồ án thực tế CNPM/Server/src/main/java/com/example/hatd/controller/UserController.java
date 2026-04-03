package com.example.hatd.controller;

import com.example.hatd.model.Account;
import com.example.hatd.model.dto.UserInfoRequest;
import com.example.hatd.model.dto.UserResponse;
import com.example.hatd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/save-info") // Endpoint Android gọi đến
    public ResponseEntity<?> saveUserInfo(@RequestBody UserInfoRequest request) {
        try {
            Account savedAccount = userService.saveOrUpdateUserInfo(request);

           UserResponse response = new UserResponse(
    savedAccount.getId(),
    savedAccount.getName(),
    savedAccount.getSoDienThoai(),        // phoneNumber
    savedAccount.getCanCuocCongDan(),     // canCuocCongDan
    savedAccount.getRole(),   
    "Thông tin người dùng đã được lưu/cập nhật thành công."
);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi hệ thống: Không thể lưu thông tin. Chi tiết: " + e.getMessage());
        }
    }

    @GetMapping("/me/{soDienThoai}")
public ResponseEntity<UserResponse> getCurrentUser(@PathVariable String soDienThoai) {
    Account account = userService.getUserByPhone(soDienThoai);
    if (account != null) {
        UserResponse response = new UserResponse(
            account.getId(),
            account.getName(),
            account.getSoDienThoai(),
            account.getCanCuocCongDan(),    // SĐT
            account.getRole(),
            "Lấy thông tin thành công"
        );
        return ResponseEntity.ok(response);
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new UserResponse(
                null,
                null,
                null,
                null,
                null,
                "Không tìm thấy người dùng"
            ));
    }
}
}
