package com.eager2tech.beervision.controller;

import com.eager2tech.beervision.dto.DetectionSummaryDTO;
import com.eager2tech.beervision.dto.DetectionTransactionDTO;
import com.eager2tech.beervision.service.APIService;
import com.eager2tech.beervision.service.LoadProcessedDataService;
import com.eager2tech.beervision.util.AlertDialogUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainAppController implements Initializable {

    @FXML
    TableView<DetectionSummaryDTO> tvProcessedImages;
    @FXML
    Button btnProcess;

    private final APIService apiService = new APIService();


    private void loadResultCSV() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnProcess.setOnAction(this::onProcessClick);

        tvProcessedImages.setRowFactory(tv -> {
            TableRow<DetectionSummaryDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if ( (event.getClickCount() == 2) && (event.getButton() == MouseButton.PRIMARY) && (! row.isEmpty()) ) {
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(getClass().getResource("/photo_view.fxml"));
                        Parent root = fxmlLoader.load();
                        Scene dlgScene = new Scene(root);
                        Stage newStage = new Stage();
                        newStage.setTitle("Image View");
                        newStage.setScene(dlgScene);
                        newStage.initModality(Modality.WINDOW_MODAL);
                        newStage.showAndWait();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            return row ;
        });

        DetectionTransactionDTO trans1 = new DetectionTransactionDTO();
        trans1.setDetections(new ArrayList<>());

        DetectionSummaryDTO sum1 = new DetectionSummaryDTO();
        sum1.setNumPeople(9);
        sum1.setConclusion("Boi canh: Ban tiec, khong khi vui nhon");
        sum1.setImageName("data_image1.jpg");
        DetectionSummaryDTO sum2 = new DetectionSummaryDTO();
        sum2.setNumPeople(5);
        sum2.setConclusion("Boi canh: Tiem tap hoa, nhiem thung bia thuong hieu Heniken va Tiger");
        sum2.setImageName("data_image1.jpg");
        DetectionSummaryDTO sum3 = new DetectionSummaryDTO();
        sum3.setNumPeople(4);
        sum3.setConclusion("Boi canh: Ban tiec, khong khi vui nhon");
        sum3.setImageName("data_image1.jpg");
        trans1.getDetections().add(sum1);
        trans1.getDetections().add(sum2);
        trans1.getDetections().add(sum3);

        tvProcessedImages.setItems(FXCollections.observableList(trans1.getDetections()));

    }

    @FXML
    public void onProcessClick(ActionEvent mevt) {
        final String transactionId = UUID.randomUUID().toString();
        LoadProcessedDataService loadProcessedDataService =
                new LoadProcessedDataService(apiService, transactionId);
        loadProcessedDataService.onFailedProperty().setValue(evt -> {
            AlertDialogUtil.showCommonAlert(Alert.AlertType.ERROR, "Error",
                    "Could not load data.",
                    evt.getSource().getException().getMessage());
        });
        loadProcessedDataService.onSucceededProperty().setValue(evt -> {
            DetectionTransactionDTO response = (DetectionTransactionDTO)evt.getSource().getValue();
            Platform.runLater(() -> {
                tvProcessedImages.setItems(FXCollections.observableList(response.getDetections()));
                tvProcessedImages.refresh();
            });
        });

        loadProcessedDataService.start();

    }
}
