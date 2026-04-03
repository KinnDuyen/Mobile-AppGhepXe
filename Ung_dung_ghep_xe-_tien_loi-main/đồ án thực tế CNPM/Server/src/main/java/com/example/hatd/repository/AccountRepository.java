package com.example.hatd.repository;

import com.example.hatd.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Sử dụng Account và kiểu dữ liệu khóa chính Long
public interface AccountRepository extends JpaRepository<Account, Long> {
    // ✅ Phương thức tìm kiếm theo Số điện thoại
    Optional<Account> findBySoDienThoai(String soDienThoai);
}