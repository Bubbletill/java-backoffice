package store.bubbletill.backoffice.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DateTimeStringConverter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import store.bubbletill.backoffice.BOApplication;
import store.bubbletill.commons.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class BOContainerController {

    @FXML private SubScene boSubScene;
    @FXML private AnchorPane containerAnchor;

    // Top status bar
    @FXML private Label dateTimeLabel;
    @FXML private Label statusLabel;
    @FXML private Label registerTextLabel;
    @FXML private Label registerLabel;
    @FXML private Label storeLabel;
    @FXML private Label operatorLabel;
    @FXML private Pane errorPane;
    @FXML private Label errorLabel;

    private BOApplication app;
    private static BOContainerController instance;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    @FXML
    private void initialize() {
        app = BOApplication.getInstance();
        instance = this;

        updateSubScene("mainmenu");
        showError(null);

        if (app.dateTimeTimer != null)
            app.dateTimeTimer.cancel();

        app.dateTimeTimer = new Timer();
        app.dateTimeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    dateTimeLabel.setText(dtf.format(LocalDateTime.now()));
                });
            }
        }, 0, 5000);

        statusLabel.setText((app.workingOnline ? "Online" : "Offline"));
        registerLabel.setText(app.localData.getReg() > -1 ? "" + app.localData.getReg() : "N/A");
        storeLabel.setText("" + app.localData.getStore());
        operatorLabel.setText(app.operator.getId());

        registerLabel.setVisible(app.localData.getReg() > -1);
        registerTextLabel.setVisible(app.localData.getReg() > -1);
    }

    public static BOContainerController getInstance() {
        return instance;
    }

    public void updateSubScene(String fxml) {
        try {
            if (boSubScene != null)
                containerAnchor.getChildren().remove(boSubScene);

            FXMLLoader fxmlLoader = new FXMLLoader(BOApplication.class.getResource(fxml + ".fxml"));
            boSubScene = new SubScene(fxmlLoader.load(), 1920, 1010);
            boSubScene.relocate(0, 70);
            boSubScene.setVisible(true);
            containerAnchor.getChildren().add(boSubScene);
        } catch (Exception e) {
            showError("Failed to load sub-scene: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void showError(String error) {
        if (error == null) {
            errorPane.setVisible(false);
            return;
        }

        errorPane.setVisible(true);
        errorLabel.setText(error);
        BOApplication.buzzer("double");
    }

    public void logout(boolean exit) {
        if (exit) {
            System.exit(0);
            return;
        }

        try {
            app.dateTimeTimer.cancel();
            FXMLLoader fxmlLoader = new FXMLLoader(BOApplication.class.getResource("login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
            Stage stage = (Stage) dateTimeLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
