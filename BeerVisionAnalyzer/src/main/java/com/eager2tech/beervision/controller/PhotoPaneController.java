package com.eager2tech.beervision.controller;

import com.eager2tech.beervision.dto.DownloadResult;
import com.eager2tech.beervision.service.APIService;
import com.eager2tech.beervision.service.DownloadFileService;
import com.eager2tech.beervision.util.AlertDialogUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class PhotoPaneController implements Initializable {

    @FXML
    ImageView ivPhoto;
    @FXML
    TextArea txtResult;

    private final APIService apiService = new APIService();


    private void loadPhoto(String filename) {
        DownloadFileService loadPhotoSrv = new DownloadFileService(apiService, filename);
        loadPhotoSrv.onFailedProperty().setValue(evt -> {
            AlertDialogUtil.showCommonAlert(Alert.AlertType.ERROR,
                    "Error",
                    evt.getSource().getException().getMessage(),
                    "Contact your administrator for this problem.");
        });
        loadPhotoSrv.onSucceededProperty().setValue(evt -> {
            DownloadResult sr = (DownloadResult)evt.getSource().getValue();
            File imageFile = new File(sr.getFullName());
            ivPhoto.setImage(new Image(imageFile.toURI().toString()));
        });
        loadPhotoSrv.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File imageFile = new File("C:\\projects\\beer-vision\\test_data\\test3.jpg");
        ivPhoto.setImage(new Image(imageFile.toURI().toString()));
        String conclusion = "- 2 Beer- BeerViets, 6 Humans, 1 marketing staff,\n" +
                "- 6 humans, 2 beers VIet, 1 marketing staff";
        txtResult.setText(conclusion);
    }
}
