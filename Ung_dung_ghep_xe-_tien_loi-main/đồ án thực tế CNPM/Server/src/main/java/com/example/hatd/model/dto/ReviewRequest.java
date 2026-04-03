// File: com.example.hatd.repository/ReviewRequest.java (Dùng trong Repository layer)

package com.example.hatd.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class ReviewRequest {
    private Integer rating;
    private List<String> compliments;
    private String note;
}