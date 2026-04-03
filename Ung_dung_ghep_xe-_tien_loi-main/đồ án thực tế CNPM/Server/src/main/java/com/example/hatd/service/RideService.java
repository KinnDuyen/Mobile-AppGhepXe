// package com.example.hatd.service;

// import com.example.hatd.model.MatchedTrip;
// import com.example.hatd.model.dto.MatchNotificationDTO; // Import DTO đã tạo
// import com.example.hatd.repository.MatchedTripRepository;
// import com.example.hatd.repository.ReviewRepository;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.example.hatd.model.Review;
// import com.example.hatd.repository.ReviewRepository;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.example.hatd.model.dto.ReviewRequest;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.messaging.simp.SimpMessagingTemplate; // Để gửi Socket thông báo

// import java.time.LocalDateTime;
// import java.util.Optional;

// @Service
// public class RideService {

//     private final MatchedTripRepository matchedTripRepository;
//     private final SimpMessagingTemplate messagingTemplate; // ✅ ĐÃ THÊM messagingTemplate

//     private final ReviewRepository reviewRepository;
//     private final ObjectMapper objectMapper = new ObjectMapper();

//     // ✅ Các hằng số trạng thái
//     private static final String STATUS_MATCHED = "MATCHED";
//     private static final String STATUS_USER_CONFIRMED = "USER_CONFIRMED";
//     private static final String STATUS_DRIVER_ACCEPTED = "DRIVER_ACCEPTED";
//     private static final String STATUS_DRIVER_REJECTED = "DRIVER_REJECTED";
//     private static final String STATUS_PICKED_UP = "PICKED_UP";
//     private static final String STATUS_COMPLETED = "COMPLETED";
//     private static final String STATUS_USER_CANCELLED = "USER_CANCELLED";

//     // Hằng số Topic cho WebSocket (Ví dụ: /topic/user/status/+84xxxx)
//     private static final String USER_STATUS_TOPIC = "/topic/user/status/";
//     private static final String DRIVER_STATUS_TOPIC = "/topic/driver/status/";

//     @Autowired
//     public RideService(MatchedTripRepository matchedTripRepository, SimpMessagingTemplate messagingTemplate, ReviewRepository reviewRepository) {
//         this.matchedTripRepository = matchedTripRepository;
//         this.messagingTemplate = messagingTemplate; // ✅ KHỞI TẠO SimpMessagingTemplate
//         this.reviewRepository = reviewRepository;
//     }

//     /**
//      * Helper để tạo MatchNotificationDTO từ MatchedTrip
//      */
//     private MatchNotificationDTO createNotificationDTO(MatchedTrip trip, String message) {
//         MatchNotificationDTO dto = new MatchNotificationDTO();
//         dto.setMatchId(trip.getId());
//         dto.setMessage(message);
        
//         // Thông tin Driver (Gửi cho User)
//         dto.setTenDriver(trip.getTenDriver());
//         dto.setSdtDriver(trip.getDriverPhone());
//         dto.setBienSoXe(trip.getBienSoXe());
//         dto.setHangXe(trip.getHangXe());

//         // Thông tin User (Gửi cho Driver)
//         dto.setTenUser(trip.getTenUser());
//         dto.setSdtUser(trip.getUserPhone());
        
//         // Thông tin Chuyến đi
//         dto.setTenDiemDiUser(trip.getUserOriginName());
//         dto.setTenDiemDenUser(trip.getUserDestinationName());
//         dto.setGiaTien(trip.getGiaTien());
//         dto.setThoiGianDriverDenUser(trip.getThoiGianDriverDenUser());
//         dto.setHinhThucThanhToan(trip.getHinhThucThanhToan());
        
//         return dto;
//     }

//     private static final Logger logger = LoggerFactory.getLogger(RideService.class);

//     // --- Xử lý User Confirm ---
//     @Transactional
//     public boolean confirmUserBooking(Long matchId) {
//         Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);
//         if (tripOpt.isEmpty()) return false;
//         MatchedTrip trip = tripOpt.get();

//         if (STATUS_MATCHED.equals(trip.getStatus())) {
//             trip.setStatus(STATUS_USER_CONFIRMED); 
//             trip.setConfirmedAt(LocalDateTime.now()); 
//             matchedTripRepository.save(trip); 

//             // 3. ✅ Gửi Socket thông báo cho Driver
//             MatchNotificationDTO notification = createNotificationDTO(trip, "USER_CONFIRMED: Người dùng đã xác nhận chuyến đi.");
//             String driverTopic = DRIVER_STATUS_TOPIC + trip.getDriverPhone();
//             messagingTemplate.convertAndSend(driverTopic, notification);
            
//             return true;
//         } 
//         return false;
//     }

//     // --- Xử lý Driver Accept ---
//     @Transactional
//     public boolean acceptDriverRide(Long matchId) {
//         Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);
//         if (tripOpt.isEmpty()) return false;
//         MatchedTrip trip = tripOpt.get();

//         if (STATUS_USER_CONFIRMED.equals(trip.getStatus())) {
//             trip.setStatus(STATUS_DRIVER_ACCEPTED);
//             trip.setAcceptedAt(LocalDateTime.now()); 
//             matchedTripRepository.save(trip);

//             // 3. ✅ Gửi Socket thông báo cho User (Driver đã chấp nhận)
//             MatchNotificationDTO notification = createNotificationDTO(trip, "DRIVER_ACCEPTED: Tài xế đã chấp nhận chuyến đi.");
//             String userTopic = USER_STATUS_TOPIC + trip.getUserPhone();
//             messagingTemplate.convertAndSend(userTopic, notification);
            
//             return true;
//         }
//         return false; 
//     }
    
//     // --- Xử lý Driver Reject ---
//     @Transactional
//     public boolean rejectDriverRide(Long matchId) {
//         Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);
//         if (tripOpt.isEmpty()) return false;
//         MatchedTrip trip = tripOpt.get();

//         // 1. Kiểm tra trạng thái: Chỉ cho phép từ chối nếu đang ở trạng thái MATCHED hoặc USER_CONFIRMED
//         if (STATUS_MATCHED.equals(trip.getStatus()) || STATUS_USER_CONFIRMED.equals(trip.getStatus())) {
            
//             // 2. Cập nhật trạng thái và lưu DB
//             trip.setStatus(STATUS_DRIVER_REJECTED);
//             matchedTripRepository.save(trip);

//             // 3. ✅ Gửi Socket thông báo cho User (Driver đã từ chối)
//             MatchNotificationDTO notification = createNotificationDTO(trip, "DRIVER_REJECTED: Tài xế đã từ chối chuyến đi. Vui lòng thử tìm tài xế khác.");
            
//             String userTopic = USER_STATUS_TOPIC + trip.getUserPhone();
//             // KHÔNG cần gửi cho Driver vì Driver tự biết họ đã từ chối
//             messagingTemplate.convertAndSend(userTopic, notification);
            
//             return true;
//         } 
        
//         // Trạng thái không hợp lệ
//         return false; 
//     }

//     // --- Xử lý Complete Ride ---
//     @Transactional 
//     public boolean completeRide(Long matchId) {
        
//         Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);

//         if (tripOpt.isPresent()) {
//             MatchedTrip trip = tripOpt.get();
            
//             // 1. Cập nhật trạng thái và thời gian
//             trip.setStatus(STATUS_COMPLETED);
//             trip.setCompletedAt(LocalDateTime.now());
            
//             // 2. Lưu vào Database
//             matchedTripRepository.save(trip);
            
//             // 3. ✅ Gửi Socket thông báo đến User
//             // Sử dụng tiền tố để client dễ dàng nhận diện và điều hướng.
//             MatchNotificationDTO notification = createNotificationDTO(trip, "COMPLETED_RIDE: Chuyến đi đã hoàn thành. Vui lòng đánh giá tài xế."); 
            
//             String userTopic = USER_STATUS_TOPIC + trip.getUserPhone(); 
//             messagingTemplate.convertAndSend(userTopic, notification);
            
//             // TODO: Logic phức tạp hơn: Kích hoạt quy trình thanh toán, tạo hóa đơn, v.v.
            
//             return true;
//         }
//         return false;
//     }


//     @Transactional
//     public boolean saveReview(Long matchId, ReviewRequest request) {
//         // 1. Tìm chuyến đi để lấy thông tin User/Driver
//         Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);

//         if (tripOpt.isEmpty()) {
//             return false;
//         }

//         MatchedTrip trip = tripOpt.get();

//         // 2. Kiểm tra trạng thái: Chỉ cho phép đánh giá khi chuyến đi đã COMPLETED
//         if (!"COMPLETED".equals(trip.getStatus())) {
//              // Tùy chọn: Log lỗi hoặc ném ngoại lệ
//              return false;
//         }

//         Review review = new Review();
//         review.setMatchId(matchId);
//         review.setRating(request.getRating());
//         review.setNote(request.getNote());
        
//         // Lấy thông tin User và Driver từ MatchedTrip
//         review.setUserPhone(trip.getUserPhone());
//         review.setDriverPhone(trip.getDriverPhone());

//         // Chuyển danh sách lời khen thành chuỗi JSON để lưu trữ (vì cột là String)
//         try {
//             String complimentsJson = objectMapper.writeValueAsString(request.getCompliments());
//             review.setCompliments(complimentsJson);
//         } catch (JsonProcessingException e) {
//             // Log lỗi nếu chuyển đổi JSON thất bại
//             review.setCompliments(String.join(",", request.getCompliments())); // Dùng CSV nếu lỗi
//         }

//         // 3. Lưu đánh giá vào DB
//         reviewRepository.save(review);
        
//         // TODO: Có thể thêm logic tính toán lại điểm trung bình của Driver ở đây
        
//         return true;
//     }

//     @Transactional
// public boolean pickedUpRide(Long matchId) {
//     Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);
//     if (tripOpt.isEmpty()) return false;
//     MatchedTrip trip = tripOpt.get();

//     // 1. ✅ SỬA ĐIỀU KIỆN TRẠNG THÁI:
//     // Chuyến đi có thể ở trạng thái USER_CONFIRMED (nếu Driver Accept được bỏ qua)
//     // hoặc DRIVER_ACCEPTED.
//     if (STATUS_USER_CONFIRMED.equals(trip.getStatus()) || 
//         STATUS_DRIVER_ACCEPTED.equals(trip.getStatus())) {
        
//         // 2. Cập nhật trạng thái và thời gian
//         trip.setStatus(STATUS_PICKED_UP); 
//         trip.setPickedUpAt(LocalDateTime.now());
//         matchedTripRepository.save(trip); 

//         // 3. Gửi Socket thông báo cho User
//         MatchNotificationDTO notification = createNotificationDTO(
//             trip, 
//             "RIDE_PICKED_UP: Tài xế đã đón bạn. Chuyến đi đã bắt đầu!"
//         );
//         String userTopic = USER_STATUS_TOPIC + trip.getUserPhone();
//         messagingTemplate.convertAndSend(userTopic, notification);
        
//         return true;
//     } 
    
//     // Nếu trạng thái không hợp lệ
//     return false;
// }

//    @Transactional
// public boolean cancelRide(Long matchId, String reason) {
//     Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);
//     if (tripOpt.isEmpty()) return false;
//     MatchedTrip trip = tripOpt.get();

//     // 1. Kiểm tra trạng thái: Không cho phép hủy nếu đã hoàn thành/hủy/từ chối.
//     if (STATUS_COMPLETED.equals(trip.getStatus()) 
//         || STATUS_DRIVER_REJECTED.equals(trip.getStatus()) 
//         || STATUS_USER_CANCELLED.equals(trip.getStatus())) {
//         return false;
//     }

//     // 2. Cập nhật trạng thái và lưu DB (Chỉ gọi save 1 lần)
//     trip.setStatus(STATUS_USER_CANCELLED); 
    
//     // ✅ Gán lý do hủy (Nếu cột lyDoHuy đã được thêm vào MatchedTrip)
//     // Nếu chưa thêm, hãy bỏ dòng này và chỉ dùng `setStatus`
//     // trip.setLyDoHuy(reason); 
    
//     matchedTripRepository.save(trip); // ✅ LƯU VÀO DB

//     // 3. ✅ Gửi Socket thông báo cho Driver
//     MatchNotificationDTO driverNotification = createNotificationDTO(
//         trip, 
//         "USER_CANCELLED: Người dùng đã hủy chuyến đi. Lý do: " + reason
//     );
//     String driverTopic = DRIVER_STATUS_TOPIC + trip.getDriverPhone();
//     messagingTemplate.convertAndSend(driverTopic, driverNotification);
    
//     // 4. ✅ Gửi Socket thông báo cho User (Xác nhận hủy thành công)
//     MatchNotificationDTO userNotification = createNotificationDTO(
//         trip, 
//         "CANCEL_SUCCESS: Bạn đã hủy chuyến đi thành công."
//     );
//     String userTopic = USER_STATUS_TOPIC + trip.getUserPhone();
//     messagingTemplate.convertAndSend(userTopic, userNotification);
    
//     return true;
// }
// }

package com.example.hatd.service;

import com.example.hatd.model.MatchedTrip;
import com.example.hatd.model.dto.MatchNotificationDTO;
import com.example.hatd.repository.MatchedTripRepository;
import com.example.hatd.repository.ReviewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.hatd.model.Review;
import com.example.hatd.model.dto.ReviewRequest;
import com.example.hatd.model.dto.CancelRideRequest; // Cần import nếu dùng trong Service
import com.example.hatd.model.dto.DriverLocationDTO;
import com.example.hatd.model.dto.DriverLocationUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RideService {

    // ✅ Sử dụng SLF4J Logger chuẩn
    private static final Logger logger = LoggerFactory.getLogger(RideService.class);

    private final MatchedTripRepository matchedTripRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ReviewRepository reviewRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- Hằng số Trạng thái ---
    private static final String STATUS_MATCHED = "MATCHED";
    private static final String STATUS_USER_CONFIRMED = "USER_CONFIRMED";
    private static final String STATUS_DRIVER_ACCEPTED = "DRIVER_ACCEPTED";
    private static final String STATUS_DRIVER_REJECTED = "DRIVER_REJECTED";
    private static final String STATUS_PICKED_UP = "PICKED_UP";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_USER_CANCELLED = "USER_CANCELLED";

    // --- Hằng số Topic WebSocket ---
    private static final String USER_STATUS_TOPIC = "/topic/user/status/";
    private static final String DRIVER_STATUS_TOPIC = "/topic/driver/status/";

    @Autowired
    public RideService(
            MatchedTripRepository matchedTripRepository, 
            SimpMessagingTemplate messagingTemplate, 
            ReviewRepository reviewRepository) {
        this.matchedTripRepository = matchedTripRepository;
        this.messagingTemplate = messagingTemplate;
        this.reviewRepository = reviewRepository;
    }

    /**
     * Helper để tạo MatchNotificationDTO từ MatchedTrip
     */
    private MatchNotificationDTO createNotificationDTO(MatchedTrip trip, String message) {
        MatchNotificationDTO dto = new MatchNotificationDTO();
        dto.setMatchId(trip.getId());
        dto.setMessage(message);
        
        // Thông tin Driver (Gửi cho User)
        dto.setTenDriver(trip.getTenDriver());
        dto.setSdtDriver(trip.getDriverPhone());
        dto.setBienSoXe(trip.getBienSoXe());
        dto.setHangXe(trip.getHangXe());

        // Thông tin User (Gửi cho Driver)
        dto.setTenUser(trip.getTenUser());
        dto.setSdtUser(trip.getUserPhone());
        
        // Thông tin Chuyến đi
        dto.setTenDiemDiUser(trip.getUserOriginName());
        dto.setTenDiemDenUser(trip.getUserDestinationName());
        dto.setGiaTien(trip.getGiaTien());
        dto.setThoiGianDriverDenUser(trip.getThoiGianDriverDenUser());
        dto.setHinhThucThanhToan(trip.getHinhThucThanhToan());
        
        return dto;
    }

    // =======================================================
    // 1. CHUYỂN TRẠNG THÁI (User, Driver)
    // =======================================================
   @Transactional
    // SỬA TỪ DriverLocationDTO sang DriverLocationUpdate
    public boolean updateDriverLocation(DriverLocationUpdate request) { 
        Long matchId = request.getMatchId();
        
        Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);
        if (tripOpt.isEmpty()) {
            logger.warn("Location update failed: Trip {} not found.", matchId);
            return false;
        }
        
        MatchedTrip trip = tripOpt.get();
        
        // 1. Cập nhật vị trí vào Entity MatchedTrip (Lưu vào DB)
        // Dùng request.getLat() thay vì request.getLat() (vì kiểu DTO đã đúng)
        trip.setDriverCurrentLat(request.getLat());
        trip.setDriverCurrentLng(request.getLng());
        trip.setDriverCurrentBearing(request.getBearing());
        matchedTripRepository.save(trip); 

        // 2. Gửi Socket thông báo vị trí MỚI đến Khách hàng (sử dụng DriverLocationDTO)
        DriverLocationDTO locationDto = new DriverLocationDTO(
            request.getLat(),
            request.getLng(),
            request.getBearing()
        );
        
        String locationTopic = "/topic/ride/location/" + matchId; 
        
        messagingTemplate.convertAndSend(locationTopic, locationDto);
        
        logger.debug("Location updated for Trip {} and sent to topic {}", matchId, locationTopic);
        
        return true;
    }
    @Transactional
    public boolean confirmUserBooking(Long matchId) {
        Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);
        if (tripOpt.isEmpty()) return false;
        MatchedTrip trip = tripOpt.get();

        if (STATUS_MATCHED.equals(trip.getStatus())) {
            trip.setStatus(STATUS_USER_CONFIRMED); 
            trip.setConfirmedAt(LocalDateTime.now()); 
            matchedTripRepository.save(trip); 

            MatchNotificationDTO notification = createNotificationDTO(trip, "USER_CONFIRMED: Người dùng đã xác nhận chuyến đi.");
            String driverTopic = DRIVER_STATUS_TOPIC + trip.getDriverPhone();
            messagingTemplate.convertAndSend(driverTopic, notification);
            
            logger.info("Trip {} confirmed by User. Status: {}", matchId, STATUS_USER_CONFIRMED);
            return true;
        } 
        logger.warn("Confirm failed for Trip {}. Current status: {}", matchId, trip.getStatus());
        return false;
    }

    @Transactional
    public boolean acceptDriverRide(Long matchId) {
        Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);
        if (tripOpt.isEmpty()) return false;
        MatchedTrip trip = tripOpt.get();

        if (STATUS_USER_CONFIRMED.equals(trip.getStatus())) {
            trip.setStatus(STATUS_DRIVER_ACCEPTED);
            trip.setAcceptedAt(LocalDateTime.now()); 
            matchedTripRepository.save(trip);

            MatchNotificationDTO notification = createNotificationDTO(trip, "DRIVER_ACCEPTED: Tài xế đã chấp nhận chuyến đi.");
            String userTopic = USER_STATUS_TOPIC + trip.getUserPhone();
            messagingTemplate.convertAndSend(userTopic, notification);
            
            logger.info("Trip {} accepted by Driver. Status: {}", matchId, STATUS_DRIVER_ACCEPTED);
            return true;
        }
        logger.warn("Accept failed for Trip {}. Current status: {}", matchId, trip.getStatus());
        return false; 
    }
    
    @Transactional
    public boolean rejectDriverRide(Long matchId) {
        Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);
        if (tripOpt.isEmpty()) return false;
        MatchedTrip trip = tripOpt.get();

        if (STATUS_MATCHED.equals(trip.getStatus()) || STATUS_USER_CONFIRMED.equals(trip.getStatus())) {
            
            trip.setStatus(STATUS_DRIVER_REJECTED);
            matchedTripRepository.save(trip);

            MatchNotificationDTO notification = createNotificationDTO(trip, "DRIVER_REJECTED: Tài xế đã từ chối chuyến đi. Vui lòng thử tìm tài xế khác.");
            String userTopic = USER_STATUS_TOPIC + trip.getUserPhone();
            messagingTemplate.convertAndSend(userTopic, notification);
            
            logger.info("Trip {} rejected by Driver. Status: {}", matchId, STATUS_DRIVER_REJECTED);
            return true;
        } 
        logger.warn("Reject failed for Trip {}. Current status: {}", matchId, trip.getStatus());
        return false; 
    }
    
    @Transactional
    public boolean pickedUpRide(Long matchId) {
        Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);
        if (tripOpt.isEmpty()) {
            logger.warn("PICKUP FAILED: Trip {} not found.", matchId);
            return false;
        }
        MatchedTrip trip = tripOpt.get();
        String currentStatus = trip.getStatus();

        // 🛑 ĐIỀU KIỆN ĐÃ ĐƯỢC MỞ RỘNG ĐỂ KHẮC PHỤC LỖI 400 TRONG MÔI TRƯỜNG TEST/SẢN XUẤT
        if (STATUS_MATCHED.equals(currentStatus) || 
            STATUS_USER_CONFIRMED.equals(currentStatus) || 
            STATUS_DRIVER_ACCEPTED.equals(currentStatus)) {
            
            trip.setStatus(STATUS_PICKED_UP); 
            trip.setPickedUpAt(LocalDateTime.now());
            matchedTripRepository.save(trip); 

            MatchNotificationDTO notification = createNotificationDTO(
                trip, 
                "RIDE_PICKED_UP: Tài xế đã đón bạn. Chuyến đi đã bắt đầu!"
            );
            String userTopic = USER_STATUS_TOPIC + trip.getUserPhone();
            messagingTemplate.convertAndSend(userTopic, notification);
            
            logger.info("PICKUP SUCCESS: Trip {} status updated to PICKED_UP from {}", matchId, currentStatus);
            return true;
        } 
        
        logger.warn("PICKUP FAILED: Trip {} status is {}, expected {}, {} or {}.", 
                    matchId, currentStatus, STATUS_MATCHED, STATUS_USER_CONFIRMED, STATUS_DRIVER_ACCEPTED);
        return false;
    }
    
    @Transactional 
    public boolean completeRide(Long matchId) {
        Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);

        if (tripOpt.isPresent()) {
            MatchedTrip trip = tripOpt.get();
            
            // Chỉ cho phép hoàn thành nếu đang ở trạng thái PICKED_UP
            if (!STATUS_PICKED_UP.equals(trip.getStatus())) {
                 logger.warn("Complete failed for Trip {}. Current status: {}", matchId, trip.getStatus());
                 return false;
            }
            
            trip.setStatus(STATUS_COMPLETED);
            trip.setCompletedAt(LocalDateTime.now());
            matchedTripRepository.save(trip);
            
            MatchNotificationDTO notification = createNotificationDTO(trip, "COMPLETED_RIDE: Chuyến đi đã hoàn thành. Vui lòng đánh giá tài xế."); 
            String userTopic = USER_STATUS_TOPIC + trip.getUserPhone(); 
            messagingTemplate.convertAndSend(userTopic, notification);
            
            logger.info("Trip {} completed successfully. Status: {}", matchId, STATUS_COMPLETED);
            return true;
        }
        logger.warn("Complete failed: Trip {} not found.", matchId);
        return false;
    }


    // =======================================================
    // 2. HỦY CHUYẾN
    // =======================================================

    @Transactional
    public boolean cancelRide(Long matchId, String reason) {
        Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);
        if (tripOpt.isEmpty()) return false;
        MatchedTrip trip = tripOpt.get();

        // 1. Kiểm tra trạng thái: Không cho phép hủy nếu đã hoàn thành/hủy/từ chối.
        if (STATUS_COMPLETED.equals(trip.getStatus()) 
            || STATUS_DRIVER_REJECTED.equals(trip.getStatus()) 
            || STATUS_USER_CANCELLED.equals(trip.getStatus())) {
            logger.warn("Cancel failed for Trip {}. Current status: {}", matchId, trip.getStatus());
            return false;
        }

        trip.setStatus(STATUS_USER_CANCELLED); 
        // trip.setLyDoHuy(reason); // Thêm nếu bạn có cột này
        matchedTripRepository.save(trip); 

        // Gửi Socket thông báo cho Driver
        MatchNotificationDTO driverNotification = createNotificationDTO(
            trip, 
            "USER_CANCELLED: Người dùng đã hủy chuyến đi. Lý do: " + reason
        );
        String driverTopic = DRIVER_STATUS_TOPIC + trip.getDriverPhone();
        messagingTemplate.convertAndSend(driverTopic, driverNotification);
        
        // Gửi Socket thông báo cho User (Xác nhận hủy thành công)
        MatchNotificationDTO userNotification = createNotificationDTO(
            trip, 
            "CANCEL_SUCCESS: Bạn đã hủy chuyến đi thành công."
        );
        String userTopic = USER_STATUS_TOPIC + trip.getUserPhone();
        messagingTemplate.convertAndSend(userTopic, userNotification);
        
        logger.info("Trip {} cancelled by User. Status: {}", matchId, STATUS_USER_CANCELLED);
        return true;
    }

    // =======================================================
    // 3. ĐÁNH GIÁ
    // =======================================================
    
    @Transactional
    public boolean saveReview(Long matchId, ReviewRequest request) {
        Optional<MatchedTrip> tripOpt = matchedTripRepository.findById(matchId);

        if (tripOpt.isEmpty()) {
            logger.warn("Review failed: Trip {} not found.", matchId);
            return false;
        }

        MatchedTrip trip = tripOpt.get();

        if (!STATUS_COMPLETED.equals(trip.getStatus())) {
            logger.warn("Review failed for Trip {}. Current status: {}", matchId, trip.getStatus());
            return false;
        }

        Review review = new Review();
        review.setMatchId(matchId);
        review.setRating(request.getRating());
        review.setNote(request.getNote());
        review.setUserPhone(trip.getUserPhone());
        review.setDriverPhone(trip.getDriverPhone());

        try {
            String complimentsJson = objectMapper.writeValueAsString(request.getCompliments());
            review.setCompliments(complimentsJson);
        } catch (JsonProcessingException e) {
            logger.error("Error converting compliments to JSON for Trip {}", matchId, e);
            review.setCompliments(String.join(",", request.getCompliments()));
        }

        reviewRepository.save(review);
        logger.info("Review submitted for Trip {}", matchId);
        
        return true;
    }
}