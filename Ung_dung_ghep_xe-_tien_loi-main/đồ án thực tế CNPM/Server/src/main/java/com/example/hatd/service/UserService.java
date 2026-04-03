package com.example.hatd.service;

import com.example.hatd.model.Account;
import com.example.hatd.model.dto.UserInfoRequest;
import com.example.hatd.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional; // Dùng jakarta.transaction.Transactional
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public Account saveOrUpdateUserInfo(UserInfoRequest request) {
        if (request.getPhoneNumber() == null || request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại và Tên không được để trống.");
        }
        
        // 1. Tìm kiếm User hiện tại theo Số điện thoại
        Optional<Account> existingAccount = accountRepository.findBySoDienThoai(request.getPhoneNumber());
        
        Account account;
        if (existingAccount.isPresent()) {
            account = existingAccount.get();
        } else {
            // Hoặc tạo mới nếu chưa tồn tại (Dù sau xác thực OTP thì phải tồn tại)
            account = new Account();
            account.setSoDienThoai(request.getPhoneNumber()); 
        }
        
        // 2. Cập nhật các trường từ Request
        account.setName(request.getName().trim());
        
        String cccdValue = request.getCccd();
        if (cccdValue != null && !cccdValue.trim().isEmpty()) {
            account.setCanCuocCongDan(cccdValue.trim()); 
        } else if (cccdValue != null && cccdValue.trim().isEmpty()) {
             // Nếu FE gửi chuỗi rỗng, set về null trong DB (tùy thuộc vào yêu cầu của bạn)
             account.setCanCuocCongDan(null); 
        }
        
        if (account.getRole() == null) {
    account.setRole("USER");
}

        return accountRepository.save(account);
    }

    public Account getUserByPhone(String soDienThoai) {
    String normalizedPhone = soDienThoai;
    
    // 1. Loại bỏ các ký tự không phải số (trừ dấu +) 
    //    Nếu bạn muốn hỗ trợ tìm kiếm linh hoạt hơn
    
    // 2. Chuẩn hóa để khớp với DB: Nếu chuỗi chưa có dấu '+' (sau khi URL-decode), 
    //    thì thêm nó vào, vì DB đang lưu có dấu +.
    if (normalizedPhone != null && !normalizedPhone.startsWith("+")) {
        // Đây là bước quan trọng nhất: đảm bảo chuỗi tìm kiếm khớp với DB
        normalizedPhone = "+" + normalizedPhone; 
    }
    
    // 3. Thực hiện tìm kiếm
    return accountRepository.findBySoDienThoai(normalizedPhone).orElse(null);
}
}