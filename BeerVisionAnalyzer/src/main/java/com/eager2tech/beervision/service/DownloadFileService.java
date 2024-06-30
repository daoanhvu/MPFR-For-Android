package com.eager2tech.beervision.service;


import com.eager2tech.beervision.dto.DownloadResult;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class DownloadFileService extends Service<DownloadResult> {

    private final APIService apiService;
    private final String imageName;

    public DownloadFileService(APIService apiService, String imageName) {
        this.apiService = apiService;
        this.imageName = imageName;
    }

    @Override
    protected Task<DownloadResult> createTask() {
        return new Task<DownloadResult>() {
            @Override
            protected DownloadResult call() {
                DownloadResult downloadResult;
                downloadResult = apiService.downloadFile(imageName);
                return downloadResult;
            }
        };
    }
}
