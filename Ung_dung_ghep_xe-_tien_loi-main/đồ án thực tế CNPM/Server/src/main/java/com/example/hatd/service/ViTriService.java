package com.example.hatd.service;

import com.example.hatd.model.ViTri;
import com.example.hatd.model.dto.ChuyenDiInfoRequest;
import com.example.hatd.repository.ViTriRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ViTriService {

    private static final Logger logger = LoggerFactory.getLogger(ViTriService.class);

    private final ViTriRepository viTriRepository;

    public ViTriService(ViTriRepository viTriRepository) {
        this.viTriRepository = viTriRepository;
    }

    public ViTri saveChuyenDi(ChuyenDiInfoRequest request) {
        
        ViTri viTri = new ViTri(); // Khởi tạo Entity rỗng

        // --- 1. Xử lý Thời gian ---
        LocalDateTime thoiGian;
        try {
            // Sử dụng ISO_LOCAL_DATE_TIME như đã thỏa thuận với FE
            thoiGian = LocalDateTime.parse(request.getThoiGian(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            logger.error("Lỗi parse thoiGian từ FE: {}", request.getThoiGian(), e);
            throw new IllegalArgumentException("Định dạng thời gian không hợp lệ: " + request.getThoiGian());
        }

        // --- 2. Ánh xạ và gán giá trị bằng setters ---
        viTri.setPhoneNumber(request.getPhoneNumber());
        viTri.setRole(request.getRole());
        
        // Vị trí Định vị hiện tại
        viTri.setViDo(request.getViDo());
        viTri.setKinhDo(request.getKinhDo());
        
        // ✅ ÁNH XẠ VỊ TRÍ ĐIỂM ĐI MỚI
        if (request.getViDoDiemDi() != null) {
            viTri.setViDoDiemDi(request.getViDoDiemDi());
        }
        if (request.getKinhDoDiemDi() != null) {
            viTri.setKinhDoDiemDi(request.getKinhDoDiemDi());
        }

        // ✅ ÁNH XẠ VỊ TRÍ ĐIỂM ĐẾN MỚI
        if (request.getViDoDiemDen() != null) {
            viTri.setViDoDiemDen(request.getViDoDiemDen());
        }
        if (request.getKinhDoDiemDen() != null) {
            viTri.setKinhDoDiemDen(request.getKinhDoDiemDen());
        }
        
        viTri.setThoiGian(thoiGian);
        viTri.setDiemDi(request.getDiemDi());
        viTri.setDiemDen(request.getDiemDen());
        viTri.setThoiGianDriver(request.getThoiGianDriver());

        // --- 3. Lưu vào DB ---
        return viTriRepository.save(viTri);
    }
}