package store.bubbletill.backoffice.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import store.bubbletill.backoffice.BOApplication;
import store.bubbletill.backoffice.data.OperatorData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class BOHomeController {

    @FXML private Pane homePane;
    @FXML private Label homeNameLabel;
    @FXML private Button homeExitButton;

    @FXML private Pane userManPane;
    @FXML private Pane userManListPane;
    @FXML private TableView<OperatorData> userManListTable;

    // Top status bar
    @FXML private Label dateTimeLabel;
    @FXML private Label statusLabel;
    @FXML private Label registerLabel;
    @FXML private Label storeLabel;
    @FXML private Label operatorLabel;
    @FXML private Pane errorPane;
    @FXML private Label errorLabel;

    private BOApplication app;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    @FXML
    private void initialize() {
        app = BOApplication.getInstance();

        errorPane.setVisible(false);
        homePane.setVisible(true);
        userManPane.setVisible(false);

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
        registerLabel.setText(app.register == -1 ? "N/A" : "" + app.register);
        storeLabel.setText("" + app.store);
        operatorLabel.setText(app.operator.getOperatorId());

        homeNameLabel.setText("Welcome, " + app.operator.getName() + ".");

        homeExitButton.requestFocus();

        userManListTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("operatorId"));
        userManListTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));

        homeExitButton.setText(app.register == -1 ? "Logout" : "Exit");
    }

    private void showError(String error) {
        if (error == null) {
            errorPane.setVisible(false);
            return;
        }

        errorPane.setVisible(true);
        errorLabel.setText(error);
        BOApplication.buzzer("double");
    }

    // Home

    @FXML
    private void onHomeExitButtonPress() {
        if (homeExitButton.getText().equals("Exit")) {
            System.exit(0);
            return;
        }

        try {
            app.dateTimeTimer.cancel();
            FXMLLoader fxmlLoader = new FXMLLoader(BOApplication.class.getResource("login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
            Stage stage = (Stage) dateTimeLabel.getScene().getWindow();
            stage.setTitle("Bubbletill Backoffice 22.0.1");
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void onHomeTransactionHistoryButtonPress() { }
    @FXML private void onHomeStoreOperationsButtonPress() { }

    @FXML private void onHomeManageUsersButtonPress() {
        userManListTable.getItems().clear();
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();

            StringEntity requestEntity = new StringEntity(
                    "{\"store\":\"" + app.store + "\", \"token\":\"" + BOApplication.getInstance().accessToken + "\"}",
                    ContentType.APPLICATION_JSON);

            HttpPost postMethod = new HttpPost("http://localhost:5000/bo/listoperators");
            postMethod.setEntity(requestEntity);

            HttpResponse rawResponse = httpClient.execute(postMethod);
            String out = EntityUtils.toString(rawResponse.getEntity());

            OperatorData[] listData = BOApplication.gson.fromJson(out, OperatorData[].class);

            for (OperatorData od : listData) {
                userManListTable.getItems().add(od);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError(e.getMessage());
            return;
        }

        homePane.setVisible(false);
        userManPane.setVisible(true);
        userManListPane.setVisible(true);
    }

    // User Management

    @FXML private void onUMMenuCreateButtonPress() { }

    @FXML private void onUMMenuEditButtonPress() {
        showError(null);
        OperatorData selected = userManListTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an operator to edit.");
            return;
        }
    }

    @FXML private void onUMMenuBackButtonPress() {
        showError(null);
        homePane.setVisible(true);
        userManPane.setVisible(false);
    }


}
