package com.example.hatd.service;

import com.example.hatd.model.dto.YeuCauChuyenDi;
import com.example.hatd.model.dto.TripRequestResponse;
import com.example.hatd.model.MatchedTrip;
import com.example.hatd.model.TripRequest;
import com.example.hatd.repository.MatchedTripRepository;
import com.example.hatd.repository.TripRequestRepository;
import com.example.hatd.model.dto.MatchNotificationDTO; 
import org.springframework.messaging.simp.SimpMessagingTemplate; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// =======================================================
// ✅ IMPORT CÁC REPO VÀ MODEL MÀ BẠN CUNG CẤP
// =======================================================
import com.example.hatd.repository.AccountRepository;
import com.example.hatd.repository.AccountDriverRepository;
import com.example.hatd.model.Account;
import com.example.hatd.model.AccountDriver;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TripService {

    private final TripRequestRepository tripRequestRepository;
    private final MatchedTripRepository matchedTripRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ✅ 1. "TIÊM" 2 REPO TÀI KHOẢN MỚI
    private final AccountRepository accountRepository;
    private final AccountDriverRepository accountDriverRepository;

    private static final double TIEN_XE_MOI_KM = 2500.0;

    // ✅ 2. CẬP NHẬT CONSTRUCTOR
    @Autowired
    public TripService(TripRequestRepository tripRequestRepository,
                       MatchedTripRepository matchedTripRepository,
                       SimpMessagingTemplate messagingTemplate,
                       AccountRepository accountRepository, // Thêm
                       AccountDriverRepository accountDriverRepository) { // Thêm
        this.tripRequestRepository = tripRequestRepository;
        this.matchedTripRepository = matchedTripRepository;
        this.messagingTemplate = messagingTemplate;
        this.accountRepository = accountRepository; // Gán
        this.accountDriverRepository = accountDriverRepository; // Gán
    }

    /**
     * Xử lý chính: Lưu request và kích hoạt tìm kiếm 2 chiều.
     * (Hàm này không đổi)
     */
    @Transactional
    public TripRequestResponse saveNewTripRequest(YeuCauChuyenDi dto) {
        
        TripRequest entity = new TripRequest();
        entity.setTenDiemDi(dto.getTenDiemDi());
        entity.setTenDiemDen(dto.getTenDiemDen());
        entity.setViDoDiemDi(dto.getViDoDiemDi());
        entity.setKinhDoDiemDi(dto.getKinhDoDiemDi());
        entity.setViDoDiemDen(dto.getViDoDiemDen());
        entity.setKinhDoDiemDen(dto.getKinhDoDiemDen());
        entity.setSoDienThoai(dto.getSoDienThoai());
        entity.setThoiGianDriver(dto.getThoiGianDriver());
        entity.setVaiTro(dto.getVaiTro());
        
        TripRequest savedEntity = tripRequestRepository.save(entity);

        if ("USER".equalsIgnoreCase(savedEntity.getVaiTro())) {
            this.findAndProcessMatches_UserFindsDriver(savedEntity);
        } else if ("DRIVER".equalsIgnoreCase(savedEntity.getVaiTro())) {
            this.findAndProcessMatches_DriverFindsUser(savedEntity);
        }

        TripRequestResponse response = new TripRequestResponse();
        response.setRequestId(savedEntity.getId());
        response.setStatus(tripRequestRepository.findById(savedEntity.getId()).get().getStatus()); 
        response.setCreatedAt(savedEntity.getCreatedAt()); 
        response.setMessage("Yêu cầu chuyến đi đã được tạo thành công.");

        return response;
    }

    // ... (Hàm findAndProcessMatches_UserFindsDriver không đổi) ...
    // ... (Hàm findAndProcessMatches_DriverFindsUser không đổi) ...
     /**
     * Kích hoạt khi USER tạo chuyến.
     */
    private void findAndProcessMatches_UserFindsDriver(TripRequest userRequest) {
        System.out.println("USER (ID " + userRequest.getId() + ") đang tìm DRIVER...");
        
        List<TripRequest> matchingDrivers = tripRequestRepository.findMatchingDrivers(
            userRequest.getViDoDiemDi(),
            userRequest.getKinhDoDiemDi(),
            userRequest.getViDoDiemDen(),
            userRequest.getKinhDoDiemDen(),
            userRequest.getCreatedAt()
        );

        Optional<TripRequest> bestDriverOpt = matchingDrivers.stream()
            .filter(driver -> !driver.getSoDienThoai().equals(userRequest.getSoDienThoai()))
            .findFirst();

        if (bestDriverOpt.isEmpty()) {
            System.out.println("Không tìm thấy DRIVER phù hợp (hoặc tất cả đều trùng SĐT).");
        } else {
            saveMatch(userRequest, bestDriverOpt.get());
        }
    }

    /**
     * Kích hoạt khi DRIVER tạo chuyến.
     */
    private void findAndProcessMatches_DriverFindsUser(TripRequest driverRequest) {
        System.out.println("DRIVER (ID " + driverRequest.getId() + ") đang tìm USER...");
        
        List<TripRequest> matchingUsers = tripRequestRepository.findMatchingUsers(
            driverRequest.getViDoDiemDi(),
            driverRequest.getKinhDoDiemDi(),
            driverRequest.getViDoDiemDen(),
            driverRequest.getKinhDoDiemDen(),
            driverRequest.getThoiGianDriver()
        );

        Optional<TripRequest> bestUserOpt = matchingUsers.stream()
            .filter(user -> !user.getSoDienThoai().equals(driverRequest.getSoDienThoai()))
            .findFirst();

        if (bestUserOpt.isEmpty()) {
            System.out.println("Không tìm thấy USER phù hợp (hoặc tất cả đều trùng SĐT).");
        } else {
            saveMatch(bestUserOpt.get(), driverRequest);
        }
    }


    /**
     * HÀM TIỆN ÍCH (ĐÃ CẬP NHẬT HOÀN CHỈNH)
     * Lấy thông tin từ Repo tài khoản VÀ GỬI THÔNG BÁO
     */
    private void saveMatch(TripRequest userRequest, TripRequest driverRequest) {
        System.out.println("MATCH FOUND! UserID: " + userRequest.getId() + ", DriverID: " + driverRequest.getId());

        // 1. Cập nhật trạng thái...
        userRequest.setStatus("MATCHED");
        driverRequest.setStatus("MATCHED");
        tripRequestRepository.save(userRequest);
        tripRequestRepository.save(driverRequest);

        // 2. Tính toán...
        double userTripDistKm = calculateHaversineDistance(
            userRequest.getViDoDiemDi(), userRequest.getKinhDoDiemDi(),
            userRequest.getViDoDiemDen(), userRequest.getKinhDoDiemDen()
        );
        double tienXe = userTripDistKm * TIEN_XE_MOI_KM;
        LocalDateTime thoiGianDriverDenUser = userRequest.getCreatedAt().plusMinutes(7);

        // 3. Tạo bản ghi cho bảng Lịch sử
        MatchedTrip matchRecord = new MatchedTrip();

        // --- Thông tin chuyến đi ---
        matchRecord.setUserOriginName(userRequest.getTenDiemDi());
        matchRecord.setUserDestinationName(userRequest.getTenDiemDen());
        matchRecord.setDriverOriginName(driverRequest.getTenDiemDi());
        matchRecord.setDriverDestinationName(driverRequest.getTenDiemDen());
        matchRecord.setViDoDiemDi(userRequest.getViDoDiemDi());
        matchRecord.setKinhDoDiemDi(userRequest.getKinhDoDiemDi());
        matchRecord.setViDoDiemDen(userRequest.getViDoDiemDen());
        matchRecord.setKinhDoDiemDen(userRequest.getKinhDoDiemDen());
        matchRecord.setUserTripDistanceKm(userTripDistKm);
        matchRecord.setGiaTien(tienXe);
        matchRecord.setHinhThucThanhToan("Tiền mặt"); 

        // --- Thông tin User/Driver ---
        matchRecord.setUserPhone(userRequest.getSoDienThoai());
        matchRecord.setDriverPhone(driverRequest.getSoDienThoai());

        // --- Thông tin thời gian ---
        matchRecord.setThoiGianDriverDenUser(thoiGianDriverDenUser);
        
        // =================================================================
        // ✅ 3B. LẤY THÔNG TIN TỪ BẢNG TÀI KHOẢN (ĐÃ SỬA)
        // =================================================================
        
        // Lấy thông tin User (Từ bảng taikhoan)
        Optional<Account> userAccountOpt = accountRepository.findBySoDienThoai(userRequest.getSoDienThoai());
        if (userAccountOpt.isPresent()) {
            matchRecord.setTenUser(userAccountOpt.get().getName()); // Dùng getName()
        }

        // Lấy thông tin Driver (Truy vấn 2 bảng)
        
        // Lấy Tên Driver từ bảng taikhoan
        Optional<Account> driverMainAccountOpt = accountRepository.findBySoDienThoai(driverRequest.getSoDienThoai());
        if (driverMainAccountOpt.isPresent()) {
            matchRecord.setTenDriver(driverMainAccountOpt.get().getName()); // Dùng getName()
        }

        // Lấy Biển số/Hãng xe từ bảng taikhoan_driver
        Optional<AccountDriver> driverInfoOpt = accountDriverRepository.findByPhoneNumber(driverRequest.getSoDienThoai()); // Dùng findByPhoneNumber
        if (driverInfoOpt.isPresent()) {
            AccountDriver driverInfo = driverInfoOpt.get();
            matchRecord.setBienSoXe(driverInfo.getBienSo()); // Dùng getBienSo()
            matchRecord.setHangXe(driverInfo.getHangXe()); // Dùng getHangXe()
        }

        // 4. Lưu vào database (Lấy về bản ghi đã lưu để có ID)
        MatchedTrip savedMatch = matchedTripRepository.save(matchRecord);
        System.out.println("Đã lưu bản ghi match (bảng lich_su_chuyen_di) thành công. ID: " + savedMatch.getId());
        
        // =================================================================
        // 5. GỬI THÔNG BÁO QUA WEBSOCKET (Đã cập nhật)
        // =================================================================
        
        MatchNotificationDTO notification = new MatchNotificationDTO();
        notification.setMatchId(savedMatch.getId());
        notification.setMessage("Đã tìm thấy chuyến đi!");
        
        // Gửi thông tin đầy đủ (đã lấy từ savedMatch)
        notification.setTenDriver(savedMatch.getTenDriver()); 
        notification.setBienSoXe(savedMatch.getBienSoXe());
        notification.setHangXe(savedMatch.getHangXe());
        notification.setTenUser(savedMatch.getTenUser());
        
        notification.setSdtUser(savedMatch.getUserPhone());
        notification.setSdtDriver(savedMatch.getDriverPhone());
        notification.setTenDiemDiUser(savedMatch.getUserOriginName());
        notification.setTenDiemDenUser(savedMatch.getUserDestinationName());
        notification.setGiaTien(savedMatch.getGiaTien());
        notification.setThoiGianDriverDenUser(savedMatch.getThoiGianDriverDenUser());
        notification.setHinhThucThanhToan(savedMatch.getHinhThucThanhToan());

        notification.setViDoDiemDi(savedMatch.getViDoDiemDi());
        notification.setKinhDoDiemDi(savedMatch.getKinhDoDiemDi());
        notification.setViDoDiemDen(savedMatch.getViDoDiemDen());
        notification.setKinhDoDiemDen(savedMatch.getKinhDoDiemDen());

        // 5.2. Gửi thông báo đến 2 kênh cá nhân
        String userDestination = "/topic/match/" + userRequest.getSoDienThoai();
        String driverDestination = "/topic/match/" + driverRequest.getSoDienThoai();

        messagingTemplate.convertAndSend(userDestination, notification);
        messagingTemplate.convertAndSend(driverDestination, notification);
        
        System.out.println("Đã gửi thông báo match đến kênh: " + userDestination);
        System.out.println("Đã gửi thông báo match đến kênh: " + driverDestination);
    }

    /**
     * Tính khoảng cách Haversine...
     * (Hàm này không đổi)
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // km
        return distance;
    }
}