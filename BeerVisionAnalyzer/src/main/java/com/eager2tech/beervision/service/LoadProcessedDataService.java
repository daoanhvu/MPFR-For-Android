package com.eager2tech.beervision.service;

import com.eager2tech.beervision.dto.DetectionSummaryDTO;
import com.eager2tech.beervision.dto.DetectionTransactionDTO;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.List;

public class LoadProcessedDataService extends Service<DetectionTransactionDTO> {

    private final APIService apiService;
    private final String transactionId;

    public LoadProcessedDataService(APIService apiService, String transactionId) {
        this.apiService = apiService;
        this.transactionId = transactionId;
    }

    @Override
    protected Task<DetectionTransactionDTO> createTask() {
        return new Task<DetectionTransactionDTO>() {
            @Override
            protected DetectionTransactionDTO call() throws Exception {
                return apiService.getProcessedResults(transactionId);
            }
        };
    }
}
