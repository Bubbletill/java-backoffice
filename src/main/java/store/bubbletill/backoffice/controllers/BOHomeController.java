package store.bubbletill.backoffice.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import store.bubbletill.backoffice.BOApplication;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class BOHomeController {

    @FXML private Pane homePane;
    @FXML private Label homeNameLabel;
    @FXML private Button homeExitButton;

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
        registerLabel.setText("" + app.register);
        storeLabel.setText("" + app.store);
        operatorLabel.setText(app.operator.getOperatorId());

        homeNameLabel.setText("Welcome, " + app.operator.getName() + ".");

        homeExitButton.requestFocus();
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

    @FXML
    private void onHomeExitButtonPress() {
        System.exit(0);
    }

    @FXML private void onHomeTransactionHistoryButtonPress() { }
    @FXML private void onHomeManageUsersButtonPress() { }
    @FXML private void onHomeStoreOperationsButtonPress() { }

}
