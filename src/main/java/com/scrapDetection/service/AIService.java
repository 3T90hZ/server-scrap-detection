package com.scrapDetection.service;

import com.scrapDetection.dto.DetectionResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AIService {
    public List<DetectionResult> detect(MultipartFile file) throws IOException;
}
