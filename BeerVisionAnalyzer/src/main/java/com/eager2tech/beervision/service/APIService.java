package com.eager2tech.beervision.service;

import com.eager2tech.beervision.dto.DetectionSummaryDTO;
import com.eager2tech.beervision.dto.DetectionTransactionDTO;
import com.eager2tech.beervision.dto.DownloadResult;
import com.eager2tech.beervision.util.MyJsonProvider;
import com.fasterxml.jackson.core.util.JacksonFeature;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.List;


public class APIService {
    protected final Client apiClient;
    protected final String contextPath;

    public APIService() {
        // initialize the api client
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(JacksonFeature.class);
        apiClient = ClientBuilder.newClient(clientConfig);
        apiClient.register(MyJsonProvider.class);
        contextPath = "http://localhost:9099/";
    }

    protected Response callGet(WebTarget webResource) {
        return webResource.request()
                .get(Response.class);
    }

    protected Response makePost(WebTarget webTarget, Entity<?> entity) {
        return webTarget.request(MediaType.APPLICATION_JSON)
                .post(entity);
    }

    public DownloadResult downloadFile(String fileName) {
        WebTarget webResource = apiClient.target(contextPath + "/download_image/" + fileName);
        Response response = webResource.request().get();

        DownloadResult result = new DownloadResult();

        if(response.getStatus() == 500) {
            throw new RuntimeException("Server error code: " + response.getStatus());
        }

        File folderTmp = new File("C://projects//data//");
        if(!folderTmp.exists()) {
            folderTmp.mkdirs();
        }

        result.setFullName(folderTmp.getAbsolutePath() + "/" + fileName);
        File targetFile = new File(folderTmp, fileName);
        InputStream is = response.readEntity(InputStream.class);
        try {
            java.nio.file.Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public DetectionTransactionDTO getProcessedResults(String transactionId) {
        WebTarget webTarget = apiClient.target(contextPath + "/load_results/" + transactionId);
        Response response = webTarget.request(MediaType.APPLICATION_JSON)
                .get();
        if(response.getStatus() == 500) {
            throw new RuntimeException("Server error code: " + response.getStatus());
        }
        return response.readEntity(DetectionTransactionDTO.class);
    }

    public int triggerProcessing(String folderName, String transactionId) {
        TriggerRequest requestData = new TriggerRequest(folderName, transactionId);
        WebTarget webResource = apiClient.target(contextPath + "/trigger_processing");
        try (Response response = webResource.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(requestData, MediaType.APPLICATION_JSON_TYPE))) {
            return response.getStatus();
        }
    }

    static class TriggerRequest {
        String folderName;
        String transactionId;

        TriggerRequest(String fn, String tr) {
            this.folderName = fn;
            transactionId = tr;
        }

        public String getFolderName() {
            return folderName;
        }

        public void setFolderName(String folderName) {
            this.folderName = folderName;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }
    }
}
