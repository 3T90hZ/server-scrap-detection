package com.scrapDetection.dto;

import java.util.List;

public class DetectionResponse {

    private List<DetectionResult> detections;

    // Default constructor (important for Jackson)
    public DetectionResponse() {}

    public List<DetectionResult> getDetections() {
        return detections;
    }

    public void setDetections(List<DetectionResult> detections) {
        this.detections = detections;
    }
}