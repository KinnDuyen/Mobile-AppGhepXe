package com.example.hatd.repository;

import com.example.hatd.model.TripRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface Repository để giao tiếp với bảng trip_requests.
 * Chứa logic truy vấn tìm kiếm 2 CHIỀU.
 */
@Repository
public interface TripRequestRepository extends JpaRepository<TripRequest, Long> {

    /**
     * TRƯỜNG HỢP 1: USER tìm DRIVER
     * Tìm các chuyến đi của DRIVER khớp với yêu cầu của USER.
     * (Giả sử bạn dùng MySQL 8+)
     */
    @Query(value = 
        "SELECT * FROM trip_requests driver " +
        "WHERE " +
        "   driver.vai_tro_request = 'DRIVER' AND driver.status = 'PENDING' AND " +
        "   ST_Distance_Sphere(" +
        "       POINT(driver.kinh_do_di, driver.vi_do_di), " +
        "       POINT(:kinhDoDiUser, :viDoDiUser)" +
        "   ) <= 500 AND " +
        "   ST_Distance_Sphere(" +
        "       POINT(driver.kinh_do_den, driver.vi_do_den), " +
        "       POINT(:kinhDoDenUser, :viDoDenUser)" +
        "   ) <= 2500 AND " +
        "   driver.thoi_gian_driver > :createdAtUser AND " +
        "   TIMESTAMPDIFF(MINUTE, :createdAtUser, driver.thoi_gian_driver) <= 15 "
        , 
        nativeQuery = true)
    List<TripRequest> findMatchingDrivers(
        @Param("viDoDiUser") Double viDoDiUser,
        @Param("kinhDoDiUser") Double kinhDoDiUser,
        @Param("viDoDenUser") Double viDoDenUser,
        @Param("kinhDoDenUser") Double kinhDoDenUser,
        @Param("createdAtUser") LocalDateTime createdAtUser
    );

    /**
     * TRƯỜNG HỢP 2: DRIVER tìm USER
     * Tìm các yêu cầu của USER khớp với chuyến đi của DRIVER.
     * (Logic tương tự nhưng đảo ngược)
     */
    @Query(value = 
        "SELECT * FROM trip_requests user " +
        "WHERE " +
        "   user.vai_tro_request = 'USER' AND user.status = 'PENDING' AND " +
        "   ST_Distance_Sphere(" +
        "       POINT(user.kinh_do_di, user.vi_do_di), " +
        "       POINT(:kinhDoDiDriver, :viDoDiDriver)" +
        "   ) <= 500 AND " +
        "   ST_Distance_Sphere(" +
        "       POINT(user.kinh_do_den, user.vi_do_den), " +
        "       POINT(:kinhDoDenDriver, :viDoDenDriver)" +
        "   ) <= 2500 AND " +
        "   :thoiGianDriver > user.created_at AND " +
        "   TIMESTAMPDIFF(MINUTE, user.created_at, :thoiGianDriver) <= 15 "
        , 
        nativeQuery = true)
    List<TripRequest> findMatchingUsers(
        @Param("viDoDiDriver") Double viDoDiDriver,
        @Param("kinhDoDiDriver") Double kinhDoDiDriver,
        @Param("viDoDenDriver") Double viDoDenDriver,
        @Param("kinhDoDenDriver") Double kinhDoDenDriver,
        @Param("thoiGianDriver") LocalDateTime thoiGianDriver
    );
}