package com.scrapDetection.service.Impl;

import com.scrapDetection.dto.DetectionResponse;
import com.scrapDetection.dto.DetectionResult;
import com.scrapDetection.service.AIService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Service
public class AIServiceImplement implements AIService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String pythonUrl = "http://localhost:8000/detect"; // or Docker service name

    public List<DetectionResult> detect(MultipartFile imageFile) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(imageFile.getBytes()) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(pythonUrl, request, String.class);

        // Parse JSON (use Jackson ObjectMapper)
        ObjectMapper mapper = new ObjectMapper();
        //
        DetectionResponse responseObj = mapper.readValue(response.getBody(), DetectionResponse.class);
        return responseObj.getDetections();
    }
}
