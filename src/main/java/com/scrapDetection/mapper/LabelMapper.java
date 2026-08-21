package com.scrapDetection.mapper;

import com.scrapDetection.dto.label.LabelResponse;
import com.scrapDetection.entity.Label;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LabelMapper {

    public LabelResponse toResponseDTO(Label entity) {
        if (entity == null) return null;

        return LabelResponse.builder()
                .labelId(entity.getLabelId())
                .label(entity.getLabel())
                .materialId(entity.getMaterial().getMaterialId())
                .materialName(entity.getMaterial().getItemName())
                .build();
    }

    public List<LabelResponse> toResponseDTOList(List<Label> labels) {
        return labels.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}
