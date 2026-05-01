package com.scrapDetection.controller;

import com.scrapDetection.dto.DetectionResult;
import com.scrapDetection.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Waste Detection", description = "API nhận diện rác tái chế (chai nhựa & lon nhôm)")
public class AIController {

    private final AIService service;

    public AIController(AIService service) {
        this.service = service;
    }

    @Operation(
            summary = "Detect waste objects in image",
            description = "Upload ảnh chứa chai nhựa hoặc lon nhôm để nhận diện"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detection successful"),
            @ApiResponse(responseCode = "400", description = "Invalid image"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<DetectionResult>> detect(
            @Parameter(description = "Image file (jpg, png, etc.)")
            @RequestParam("image") MultipartFile image) throws IOException {

        List<DetectionResult> results = service.detect(image);
        return ResponseEntity.ok(results);
    }
}