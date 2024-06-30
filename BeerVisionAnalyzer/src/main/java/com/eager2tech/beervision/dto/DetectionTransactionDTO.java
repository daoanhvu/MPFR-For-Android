package com.eager2tech.beervision.dto;

import java.util.List;

public class DetectionTransactionDTO {
    private String transactionId;
    private List<DetectionSummaryDTO> detections;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<DetectionSummaryDTO> getDetections() {
        return detections;
    }

    public void setDetections(List<DetectionSummaryDTO> detections) {
        this.detections = detections;
    }
}
