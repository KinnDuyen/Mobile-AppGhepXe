package com.example.hatd.service;

import com.example.hatd.model.MatchedTrip;
import com.example.hatd.model.dto.MatchNotificationDTO;
import com.example.hatd.repository.MatchedTripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MatchQueryService {

    private final MatchedTripRepository matchedTripRepository;

    // Trạng thái Match đang chờ xác nhận
    private static final String STATUS_MATCHED = "MATCHED";
    
    // ✅ THỜI GIAN HỢP LỆ: Match chỉ được coi là hợp lệ nếu được tạo trong 15 phút qua
    private static final long MATCH_TIMEOUT_MINUTES = 15; 

    @Autowired
    public MatchQueryService(MatchedTripRepository matchedTripRepository) {
        this.matchedTripRepository = matchedTripRepository;
    }

    /**
     * Tìm Match mới nhất, CHƯA HẾT HẠN, và đang chờ xác nhận cho User qua SĐT.
     * (Dùng cho logic dự phòng HTTP để bắt Match bị bỏ lỡ).
     */
    public MatchNotificationDTO findLatestPendingMatch(String soDienThoai) {

        // 1. Tính toán thời điểm cắt: Lấy thời điểm 15 phút trước
        LocalDateTime cutOffTime = LocalDateTime.now().minusMinutes(MATCH_TIMEOUT_MINUTES);

        // 2. ✅ SỬ DỤNG PHƯƠNG THỨC REPOSITORY MỚI (Lọc theo Status VÀ Thời gian)
        // Phương thức này chỉ tồn tại nếu bạn đã thêm nó vào MatchedTripRepository
        Optional<MatchedTrip> latestMatchOpt = matchedTripRepository.findFirstByUserPhoneAndStatusAndMatchedAtAfterOrderByMatchedAtDesc(
             soDienThoai, 
             STATUS_MATCHED,
             cutOffTime 
        );

        if (latestMatchOpt.isPresent()) {
            return convertToDTO(latestMatchOpt.get()); 
        }
        return null;
    }

    // ----------------------------------------------------------------------
    
    /**
     * Hàm chuyển đổi Entity sang DTO (Đã ánh xạ đầy đủ và an toàn).
     */
    private MatchNotificationDTO convertToDTO(MatchedTrip trip) {
        MatchNotificationDTO dto = new MatchNotificationDTO();

        // 1. Ánh xạ ID và Message
        dto.setMatchId(trip.getId());
        dto.setMessage("Match bị bỏ lỡ, vui lòng xác nhận!"); 

        // 2. Thông tin User/Driver
        dto.setTenUser(trip.getTenUser());
        dto.setSdtUser(trip.getUserPhone());
        dto.setTenDriver(trip.getTenDriver());
        dto.setSdtDriver(trip.getDriverPhone());

        // 3. Thông tin Chuyến đi
        dto.setTenDiemDiUser(trip.getUserOriginName());
        dto.setTenDiemDenUser(trip.getUserDestinationName());
        dto.setGiaTien(trip.getGiaTien());
        dto.setHinhThucThanhToan(trip.getHinhThucThanhToan());

        dto.setViDoDiemDi(trip.getViDoDiemDi());
        dto.setKinhDoDiemDi(trip.getKinhDoDiemDi());
        dto.setViDoDiemDen(trip.getViDoDiemDen());
        dto.setKinhDoDiemDen(trip.getKinhDoDiemDen());

        // 4. Thông tin Xe
        dto.setBienSoXe(trip.getBienSoXe());
        dto.setHangXe(trip.getHangXe());

        // 5. Ánh xạ thời gian an toàn (Không NullPointerException)
        dto.setThoiGianDriverDenUser(trip.getThoiGianDriverDenUser());

        return dto;
    }
}