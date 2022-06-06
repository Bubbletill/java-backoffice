package store.bubbletill.backoffice.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DateTimeStringConverter;
import javafx.util.converter.TimeStringConverter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import store.bubbletill.backoffice.BOApplication;
import store.bubbletill.backoffice.data.OperatorData;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    // Transaction History
    @FXML private Pane transHistoryPane;
    @FXML private TextField historyStoreInput;
    @FXML private DatePicker historyStartDateInput;
    @FXML private DatePicker historyEndDateInput;
    @FXML private TextField historyStartTimeInput;
    @FXML private TextField historyEndTimeInput;
    @FXML private TextField historyRegisterInput;
    @FXML private TextField historyOperatorInput;
    @FXML private TextField historyStartTotalInput;
    @FXML private TextField historyEndTotalInput;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

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
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    @FXML
    private void initialize() {
        app = BOApplication.getInstance();

        errorPane.setVisible(false);
        homePane.setVisible(true);
        userManPane.setVisible(false);
        transHistoryPane.setVisible(false);

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

        registerLabel.setVisible(app.register != -1);
        registerTextLabel.setVisible(app.register != -1);

        homeNameLabel.setText("Welcome, " + app.operator.getName() + ".");

        homeExitButton.requestFocus();

        userManListTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("operatorId"));
        userManListTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));

        homeExitButton.setText(app.register == -1 ? "Logout" : "Exit");

        StringConverter<LocalDate> dateStringConverter = new StringConverter<>() {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, formatter);
                } else {
                    return null;
                }
            }
        };

        historyStartDateInput.setConverter(dateStringConverter);
        historyEndDateInput.setConverter(dateStringConverter);

        try {
            historyStartTimeInput.setTextFormatter(new TextFormatter<>(new DateTimeStringConverter(timeFormat), timeFormat.parse("00:00")));
            historyEndTimeInput.setTextFormatter(new TextFormatter<>(new DateTimeStringConverter(timeFormat), timeFormat.parse("00:00")));
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    @FXML private void onHomeTransactionHistoryButtonPress() {
        historyStoreInput.setText("" + app.store);

        historyStartDateInput.setValue(LocalDate.now());
        historyEndDateInput.setValue(LocalDate.now());

        historyStartTimeInput.setText(LocalTime.MIN.toString());
        historyEndTimeInput.setText(LocalTime.MAX.toString());

        historyRegisterInput.setText("" + (app.register == -1 ? "" : app.register));

        historyOperatorInput.setText("");

        historyStartTotalInput.setText("");
        historyEndTotalInput.setText("");

        showError(null);
        homePane.setVisible(false);
        transHistoryPane.setVisible(true);
    }

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

        showError(null);
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

    // Transaction History

    @FXML private void onHistorySubmitButtonPress() { }

    @FXML private void onHistoryBackButtonPress() {
        showError(null);
        homePane.setVisible(true);
        transHistoryPane.setVisible(false);
    }


}
