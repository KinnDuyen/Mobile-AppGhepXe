package com.example.hatd.repository;

import com.example.hatd.model.AccountDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountDriverRepository extends JpaRepository<AccountDriver, Long> {
    Optional<AccountDriver> findByPhoneNumber(String phoneNumber);
}
