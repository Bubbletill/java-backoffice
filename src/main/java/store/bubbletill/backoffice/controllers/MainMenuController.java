package store.bubbletill.backoffice.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import store.bubbletill.backoffice.BOApplication;

import java.time.LocalDate;
import java.time.LocalTime;

public class MainMenuController {

    private final BOApplication app = BOApplication.getInstance();
    private final BOContainerController controller = BOContainerController.getInstance();

    @FXML private Pane homePane;
    @FXML private Label homeNameLabel;
    @FXML private Button homeExitButton;

    @FXML
    public void initialize() {
        homeNameLabel.setText("Welcome, " + app.operator.getName() + ".");

        homeExitButton.requestFocus();


        homeExitButton.setText(app.localData.getReg() == -1 ? "Logout" : "Exit");
    }

    @FXML
    private void onHomeExitButtonPress() {
        controller.logout(homeExitButton.getText().equals("Exit"));
    }

    @FXML private void onHomeTransactionHistoryButtonPress() {
        controller.updateSubScene("transhist");
    }

    @FXML private void onHomeStoreOperationsButtonPress() { }

    @FXML private void onHomeManageUsersButtonPress() {
        controller.updateSubScene("userman");
    }

}
