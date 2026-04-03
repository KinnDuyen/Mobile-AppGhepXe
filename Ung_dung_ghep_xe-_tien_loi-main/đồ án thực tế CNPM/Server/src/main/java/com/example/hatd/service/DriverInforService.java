package com.example.hatd.service;

import com.example.hatd.model.AccountDriver;
import com.example.hatd.model.dto.DriverInfoRequest;
import com.example.hatd.repository.AccountDriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.Optional;

@Service
public class DriverInforService {

    @Autowired
    private AccountDriverRepository accountDriverRepository;

    @Transactional
    public AccountDriver saveOrUpdateDriverInfo(DriverInfoRequest request) {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }

        // 1️⃣ Tìm hoặc tạo mới
        Optional<AccountDriver> existingAccount = accountDriverRepository.findByPhoneNumber(request.getPhoneNumber());
        AccountDriver account = existingAccount.orElseGet(() -> {
            AccountDriver newAcc = new AccountDriver();
            newAcc.setPhoneNumber(request.getPhoneNumber());
            return newAcc;
        });

        // 2️⃣ Cập nhật thông tin
        account.setGioiTinh(request.getGioiTinh());
        account.setBienSo(request.getBienSo());
        account.setHangXe(request.getHangXe());

        // 3️⃣ Gán role mặc định nếu null
        if (account.getRole() == null) {
            account.setRole("DRIVER");
        }

        return accountDriverRepository.save(account);
    }

    public AccountDriver getDriverByPhone(String soDienThoai) {
        if (soDienThoai != null && !soDienThoai.startsWith("+")) {
            soDienThoai = "+" + soDienThoai;
        }
        return accountDriverRepository.findByPhoneNumber(soDienThoai).orElse(null);
    }
}
