package com.eager2tech.beervision.dto;

import java.util.List;

public class DownloadResult {

    private String fullName;
    private List<BoundingBoxDTO> detections;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<BoundingBoxDTO> getDetections() {
        return detections;
    }

    public void setDetections(List<BoundingBoxDTO> detections) {
        this.detections = detections;
    }
}
