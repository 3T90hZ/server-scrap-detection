package com.scrapDetection.mapper;

import com.scrapDetection.dto.detection.DetectionResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class DetectionMapper {
    public DetectionResponseDTO toReceivedResponse(String className,
                                                   Double confidence,
                                                   Double weightG,
                                                   Integer bboxX1,
                                                   Integer bboxY1,
                                                   Integer bboxX2,
                                                   Integer bboxY2) {
        return DetectionResponseDTO.received(className, confidence, weightG,
                bboxX1, bboxY1, bboxX2, bboxY2);
    }

    public DetectionResponseDTO toErrorResponse(String message) {
        return DetectionResponseDTO.error(message);
    }
}
