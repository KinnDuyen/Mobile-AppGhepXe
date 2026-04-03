package com.example.hatd.repository;

import com.example.hatd.model.MatchedTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional; // ✅ Cần import

@Repository
public interface MatchedTripRepository extends JpaRepository<MatchedTrip, Long> {
    
    /**
     * Tìm bản ghi MatchedTrip mới nhất (đầu tiên) dựa trên:
     * 1. SĐT của User (userPhone)
     * 2. Trạng thái (status) - Phải là "MATCHED" hoặc tương tự
     * Sắp xếp theo thời gian tạo (matchedAt) giảm dần để lấy bản ghi mới nhất.
     */
   Optional<MatchedTrip> findFirstByUserPhoneAndStatusAndMatchedAtAfterOrderByMatchedAtDesc(
    String userPhone, 
    String status,
    LocalDateTime cutOffTime // Match phải được tạo SAU thời điểm này
);

    Optional<MatchedTrip> findByIdAndUserPhone(Long id, String userPhone);

    Optional<MatchedTrip> findByIdAndDriverPhone(Long id, String driverPhone);
}