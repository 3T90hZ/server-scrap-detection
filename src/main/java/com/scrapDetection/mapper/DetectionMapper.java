package com.scrapDetection.mapper;

import com.scrapDetection.dto.detection.DetectionResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class DetectionMapper {
    public DetectionResponseDTO toReceivedResponse(String className,
                                                   Double confidence,
                                                   Double weightG) {
        return DetectionResponseDTO.received(className, confidence, weightG);
    }

    public DetectionResponseDTO toErrorResponse(String message) {
        return DetectionResponseDTO.error(message);
    }
}
