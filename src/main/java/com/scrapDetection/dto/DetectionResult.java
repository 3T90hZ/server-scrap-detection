package com.scrapDetection.dto;

public class DetectionResult {

    private String className;     // or "label" — match exactly what Python returns
    private double confidence;
    private int[] bbox;           // [x1, y1, x2, y2]

    // Default constructor
    public DetectionResult() {}

    // Getters and Setters
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public int[] getBbox() { return bbox; }
    public void setBbox(int[] bbox) { this.bbox = bbox; }
}