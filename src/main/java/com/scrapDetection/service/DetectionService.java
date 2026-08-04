package com.scrapDetection.service;

import com.scrapDetection.dto.detection.DetectionRequestDTO;
import com.scrapDetection.dto.detection.DetectionResponseDTO;

public interface DetectionService {

    DetectionResponseDTO processDetection(DetectionRequestDTO requestDTO);

}
