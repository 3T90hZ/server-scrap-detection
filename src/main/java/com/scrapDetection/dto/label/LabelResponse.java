package com.scrapDetection.dto.label;

import com.scrapDetection.entity.Material;
import com.scrapDetection.entity.ScrapYard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelResponse {
    private Long labelId;
    private String label;
    private Long materialId;
    private String materialName;
}
