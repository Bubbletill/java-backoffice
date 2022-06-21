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
import java.util.Map;
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
    @FXML private TableView<TransactionListData> historyTable;
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

    // Transaction View
    @FXML private Pane transViewPane;
    @FXML private TableView<StockData> transViewTable;
    @FXML private Label transViewStoreLabel;
    @FXML private Label transViewRegisterLabel;
    @FXML private Label transViewTransLabel;
    @FXML private Label transViewDateLabel;
    @FXML private Label transViewTimeLabel;
    @FXML private Label transViewTypeLabel;
    @FXML private Label transViewPrimMethodLabel;
    @FXML private Label transViewTotalLabel;
    @FXML private Label transViewVoidedLabel;
    @FXML private Label transViewOperatorLabel;
    @FXML private Label transViewManAuthLabel;
    @FXML private Button transViewPostVoidButton;

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
    DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

    @FXML
    private void initialize() {
        app = BOApplication.getInstance();

        errorPane.setVisible(false);
        homePane.setVisible(true);
        userManPane.setVisible(false);
        transHistoryPane.setVisible(false);
        transViewPane.setVisible(false);

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

        historyTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("store"));
        historyTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        historyTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("type"));
        historyTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("register"));
        historyTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("trans"));
        historyTable.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("oper"));
        historyTable.getColumns().get(6).setCellValueFactory(new PropertyValueFactory<>("total"));
        historyTable.getColumns().get(7).setCellValueFactory(new PropertyValueFactory<>("primary_method"));

        transViewTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("category"));
        transViewTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        transViewTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("description"));
        transViewTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("price"));
        transViewTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("priceWithReduction"));
        transViewTable.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("refunded"));
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
        historyStoreInput.setDisable(true);

        historyStartDateInput.setValue(LocalDate.now());
        historyEndDateInput.setValue(LocalDate.now());

        historyStartTimeInput.setText(LocalTime.MIN.toString());
        historyEndTimeInput.setText(LocalTime.MAX.toString());

        historyRegisterInput.setText("" + (app.register == -1 ? "" : app.register));

        historyOperatorInput.setText("");

        historyStartTotalInput.setText("");
        historyEndTotalInput.setText("");

        historyTable.getItems().clear();

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

            HttpPost postMethod = new HttpPost(BOApplication.backendUrl + "/bo/listoperators");
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

    @FXML private void onHistorySubmitButtonPress() {
        historyTable.getItems().clear();
        showError(null);
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();

            StringEntity requestEntity = new StringEntity(
                    "{"
                            + "\"store\": \"" + historyStoreInput.getText()
                            + "\", \"startDate\": \"" + dbDateFormatter.format(historyStartDateInput.getValue())
                            + "\", \"endDate\": \"" + dbDateFormatter.format(historyEndDateInput.getValue())
                            + "\", \"startTime\": \"" + historyStartTimeInput.getText()
                            + "\", \"endTime\": \"" + historyEndTimeInput.getText()
                            + "\", \"register\": \"" + (historyRegisterInput.getText() == null || historyRegisterInput.getText().isEmpty() ? "" : historyRegisterInput.getText())
                            + "\", \"operator\": \"" + (historyOperatorInput.getText() == null || historyOperatorInput.getText().isEmpty() ? "" : historyOperatorInput.getText())
                            + "\", \"startTotal\": \"" + (historyStartTotalInput.getText() == null || historyStartTotalInput.getText().isEmpty() ? Double.MIN_VALUE : Double.parseDouble(historyStartTotalInput.getText()))
                            + "\", \"endTotal\": \"" + (historyEndTotalInput.getText() == null || historyEndTotalInput.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(historyEndTotalInput.getText()))
                            + "\", \"token\" :\"" + app.accessToken
                            + "\"}",
                    ContentType.APPLICATION_JSON);

            HttpPost postMethod = new HttpPost(BOApplication.backendUrl + "/bo/listtransactions");
            postMethod.setEntity(requestEntity);

            HttpResponse rawResponse = httpClient.execute(postMethod);
            String out = EntityUtils.toString(rawResponse.getEntity());

            TransactionListData[] listData = BOApplication.gson.fromJson(out, TransactionListData[].class);

            for (TransactionListData t : listData) {
                historyTable.getItems().add(t);
            }

            if (listData.length == 0)
                showError("Search returned no results.");
        } catch (Exception e) {
            e.printStackTrace();
            showError(e.getMessage());
        }
    }

    @FXML private void onHistoryViewSelectedButtonPress() {
        showError(null);
        transViewTable.getItems().clear();
        if (historyTable.getSelectionModel().getSelectedItem() == null) {
            showError("Please select a transaction to view.");
            return;
        }

        TransactionListData selected = historyTable.getSelectionModel().getSelectedItem();

        Transaction trans = BOApplication.gson.fromJson(selected.getItems(), Transaction.class);

        transViewStoreLabel.setText("Store: " + selected.getStore());
        transViewRegisterLabel.setText("Register: " + selected.getRegister());
        transViewTransLabel.setText("Transaction: " + selected.getTrans());
        transViewDateLabel.setText("Date: " + selected.getDate());
        transViewTimeLabel.setText("Time: " + selected.getTime());

        transViewTypeLabel.setText("Transaction Type: " + selected.getType().getLocalName());
        transViewPrimMethodLabel.setText("Primary Payment Method: " + selected.getPrimary_method().getLocalName());
        transViewTotalLabel.setText("Total: £" + Formatters.decimalFormatter.format(selected.getTotal()));
        transViewVoidedLabel.setText("Voided: " + ((trans.isVoided()) ? "Yes" : "No"));
        transViewOperatorLabel.setText("Operator: " + selected.getOper());

        StringBuilder mngrActions = new StringBuilder("");
        if (!trans.getManagerActions().isEmpty()) {
            for (Map.Entry<String, String> e : trans.getManagerActions().entrySet()) {
                String addon = "";
                if (!mngrActions.isEmpty())
                    addon = ", ";
                mngrActions.append(e.getKey()).append(" (").append(e.getValue()).append(")");
            }

            transViewManAuthLabel.setText(mngrActions.toString());
        } else {
            transViewManAuthLabel.setText("N/A");
        }

        for (StockData item : trans.getBasket()) {
            transViewTable.getItems().add(item);
        }

        transViewPostVoidButton.setDisable(trans.isVoided());

        transHistoryPane.setVisible(false);
        transViewPane.setVisible(true);
    }

    @FXML private void onTransViewPrintReceiptButtonPress() {
        try {
            TransactionListData selected = historyTable.getSelectionModel().getSelectedItem();
            String items = selected.getItems().replaceAll("\"", "\\\\\"");

            HttpClient httpClient = HttpClientBuilder.create().build();
            StringEntity requestEntity = new StringEntity(
                    "{"
                            + "\"store\": \"" + selected.getStore()
                            + "\", \"reg\": \"" + selected.getRegister()
                            + "\", \"trans\": \"" + selected.getTrans()
                            + "\", \"oper\": \"" + selected.getOper()
                            + "\", \"datetime\": \"" + selected.getDate() + " " + selected.getTime()
                            + "\", \"items\": \"" + items
                            + "\", \"paydata\": \"" + "NA"
                            + "\", \"copy\": true"
                            + "}",
                    ContentType.APPLICATION_JSON);

            HttpPost postMethod = new HttpPost("http://localhost:5001/print/receipt");
            postMethod.setEntity(requestEntity);

            HttpResponse rawResponse = httpClient.execute(postMethod);
        } catch (Exception e) {
            e.printStackTrace();
            showError(e.getMessage());
        }
    }

    @FXML private void onTransViewPostVoidButtonPress() {
        Alert receiptQuestion = new Alert(Alert.AlertType.CONFIRMATION);
        receiptQuestion.setTitle("Post Void");
        receiptQuestion.setHeaderText("Are you sure you want to void this transaction?");
        receiptQuestion.setContentText("Please select an option.");
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        receiptQuestion.getButtonTypes().setAll(yesButton, new ButtonType("No", ButtonBar.ButtonData.NO));
        boolean run = false;
        receiptQuestion.showAndWait().ifPresent(buttonType -> {
            if (buttonType == yesButton)
                transViewVoid();
        });
    }

    private void transViewVoid() {
        TransactionListData selected = historyTable.getSelectionModel().getSelectedItem();

        Transaction trans = BOApplication.gson.fromJson(selected.getItems(), Transaction.class);
        trans.setVoided(true);
        trans.addManagerAction("Post Void", app.operator.getOperatorId());

        selected.setItems(BOApplication.gson.toJson(trans));
        String items = selected.getItems().replaceAll("\"", "\\\\\"");

        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            StringEntity requestEntity = new StringEntity(
                    "{"
                            + "\"utid\": \"" + selected.getUtid()
                            + "\", \"items\": \"" + items
                            + "\", \"token\": \"" + app.accessToken
                            + "\"}",
                    ContentType.APPLICATION_JSON);

            HttpPost postMethod = new HttpPost(BOApplication.backendUrl + "/bo/postvoid");
            postMethod.setEntity(requestEntity);

            HttpResponse rawResponse = httpClient.execute(postMethod);
            String out = EntityUtils.toString(rawResponse.getEntity());
            if (!out.equals("Success")) {
                showError("Unknown error. Please try again later.");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Transaction " + selected.getUtid() + " voided successfully.", ButtonType.OK);
            alert.setTitle("Success");
            alert.setHeaderText("Success");
            alert.showAndWait();

            onTransViewBackButtonPress();
            onHistorySubmitButtonPress();
        } catch (Exception e) {
            e.printStackTrace();
            showError(e.getMessage());
        }
    }

    @FXML private void onTransViewBackButtonPress() {
        showError(null);
        transHistoryPane.setVisible(true);
        transViewPane.setVisible(false);
    }


    @FXML private void onHistoryBackButtonPress() {
        showError(null);
        homePane.setVisible(true);
        transHistoryPane.setVisible(false);
    }

}
