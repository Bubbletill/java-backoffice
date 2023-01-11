package store.bubbletill.backoffice.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
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
import store.bubbletill.commons.Formatters;
import store.bubbletill.commons.StockData;
import store.bubbletill.commons.TransactionListData;
import store.bubbletill.commons.TransactionType;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TransHistoryController {

    private final BOApplication app = BOApplication.getInstance();
    private final BOContainerController controller = BOContainerController.getInstance();

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
    private DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

    // Transaction View
    @FXML private Pane transViewPane;
    @FXML private TableView<StockData> transViewTable;
    @FXML private ListView<String> transViewLogs;
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

    @FXML
    public void initialize() {
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

        transViewTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("category"));
        transViewTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        transViewTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("description"));
        transViewTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("price"));
        transViewTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("priceWithReduction"));
        //transViewTable.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("refunded"));

        historyStoreInput.setText("" + app.localData.getStore());
        historyStoreInput.setDisable(true);

        historyStartDateInput.setValue(LocalDate.now());
        historyEndDateInput.setValue(LocalDate.now());

        historyStartTimeInput.setText(LocalTime.MIN.toString());
        historyEndTimeInput.setText(LocalTime.MAX.toString());

        historyRegisterInput.setText("" + (app.localData.getReg() > -1 ? app.localData.getReg() : ""));

        historyOperatorInput.setText("");

        historyStartTotalInput.setText("");
        historyEndTotalInput.setText("");

        historyTable.getItems().clear();

        transHistoryPane.setVisible(true);
        transViewPane.setVisible(false);
    }



    // Transaction History

    @FXML private void onHistorySubmitButtonPress() {
        historyTable.getItems().clear();
        controller.showError(null);
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
                            + "\", \"token\" :\"" + app.localData.getToken()
                            + "\"}",
                    ContentType.APPLICATION_JSON);

            HttpPost postMethod = new HttpPost(app.localData.getBackend() + "/bo/listtransactions");
            postMethod.setEntity(requestEntity);

            HttpResponse rawResponse = httpClient.execute(postMethod);
            String out = EntityUtils.toString(rawResponse.getEntity());

            out = out.replaceAll("\"\\[", "[");
            out = out.replaceAll("]\"", "]");

            out = out.replaceAll("\"\\{", "{");
            out = out.replaceAll("}\"", "}");

            TransactionListData[] listData = BOApplication.gson.fromJson(out, TransactionListData[].class);

            for (TransactionListData t : listData) {
                historyTable.getItems().add(t);
            }

            if (listData.length == 0)
                controller.showError("Search returned no results.");
        } catch (Exception e) {
            e.printStackTrace();
            controller.showError(e.getMessage());
        }
    }

    @FXML private void onHistoryViewSelectedButtonPress() {
        controller.showError(null);
        transViewTable.getItems().clear();
        transViewLogs.getItems().clear();
        if (historyTable.getSelectionModel().getSelectedItem() == null) {
            controller.showError("Please select a transaction to view.");
            return;
        }

        TransactionListData selected = historyTable.getSelectionModel().getSelectedItem();

        transViewStoreLabel.setText("Store: " + selected.getStore());
        transViewRegisterLabel.setText("Register: " + selected.getRegister());
        transViewTransLabel.setText("Transaction: " + selected.getTrans());
        transViewDateLabel.setText("Date: " + selected.getDate());
        transViewTimeLabel.setText("Time: " + selected.getTime());

        transViewTypeLabel.setText("Transaction Type: " + selected.getType().getLocalName());
        transViewPrimMethodLabel.setText("Payment Method(s): " + selected.getMethods().keySet());
        transViewTotalLabel.setText("Total: £" + Formatters.decimalFormatter.format(selected.getTotal()));
        transViewVoidedLabel.setText("Voided: " + ((selected.getType() == TransactionType.VOID) ? "Yes" : "No"));
        transViewOperatorLabel.setText("Operator: " + selected.getOper());

        transViewManAuthLabel.setText("N/A");

        for (StockData item : selected.getBasket()) {
            transViewTable.getItems().add(item);
        }

        for (String log : selected.getData()) {
            transViewLogs.getItems().add(log);
        }

        transViewPostVoidButton.setDisable(selected.getType() == TransactionType.VOID);

        transHistoryPane.setVisible(false);
        transViewPane.setVisible(true);
    }

    @FXML private void onTransViewPrintReceiptButtonPress() {

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

        selected.getData().add("Transaction post-voided on " + Formatters.dateTimeFormatter.format(LocalDateTime.now()) + " by " + app.operator.getId());
        String data = BOApplication.gson.toJson(selected.getData()).replaceAll("\"", "\\\\\"");

        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            StringEntity requestEntity = new StringEntity(
                    "{"
                            + "\"utid\": \"" + selected.getUtid()
                            + "\", \"logs\": \"" + data
                            + "\", \"token\": \"" + app.localData.getToken()
                            + "\"}",
                    ContentType.APPLICATION_JSON);

            HttpPost postMethod = new HttpPost(app.localData.getBackend() + "/bo/postvoid");
            postMethod.setEntity(requestEntity);

            HttpResponse rawResponse = httpClient.execute(postMethod);
            String out = EntityUtils.toString(rawResponse.getEntity());
            System.out.println(out);
            if (!out.equals("Success")) {
                controller.showError("Unknown error. Please try again later.");
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
            controller.showError(e.getMessage());
        }
    }

    @FXML private void onTransViewBackButtonPress() {
        controller.showError(null);
        transHistoryPane.setVisible(true);
        transViewPane.setVisible(false);
    }


    @FXML private void onHistoryBackButtonPress() {
        controller.updateSubScene("mainmenu");
    }

}
