package com.example.hatd.repository;

import com.example.hatd.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Không cần thêm hàm nào phức tạp ở đây lúc đầu
}