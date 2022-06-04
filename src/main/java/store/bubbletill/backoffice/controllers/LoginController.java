package store.bubbletill.backoffice.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
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
import store.bubbletill.backoffice.data.ApiRequestData;
import store.bubbletill.backoffice.data.OperatorData;

public class LoginController {

    @FXML
    private TextField userIdForm;

    @FXML
    private PasswordField passwordForm;

    @FXML
    private Pane errorPane;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        System.out.println("LoginController Initialized");
        userIdForm.requestFocus();
        errorPane.setVisible(false);
    }

    private void showError(String error) {
        if (error == null) {
            errorPane.setVisible(false);
            return;
        }

        errorPane.setVisible(true);
        errorLabel.setText(error);
    }

    @FXML
    protected void onLoginButtonClick() {
        showError(null);
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();

            StringEntity requestEntity = new StringEntity(
                    "{\"user\":\"" + userIdForm.getText() + "\",\"password\":\"" + passwordForm.getText() + "\", \"token\":\"" + BOApplication.getInstance().accessToken + "\"}",
                    ContentType.APPLICATION_JSON);

            HttpPost postMethod = new HttpPost("http://localhost:5000/pos/login");
            postMethod.setEntity(requestEntity);

            HttpResponse rawResponse = httpClient.execute(postMethod);
            String out = EntityUtils.toString(rawResponse.getEntity());

            ApiRequestData data = BOApplication.gson.fromJson(out, ApiRequestData.class);

            if (!data.isSuccess()) {
                showError("Error: " + data.getMessage());
                BOApplication.buzzer("double");
                return;
            }

            BOApplication.getInstance().operator = BOApplication.gson.fromJson(out, OperatorData.class);

            if (!BOApplication.getInstance().operator.isManager()) {
                showError("You are not authorized to use Backoffice.");
                BOApplication.buzzer("double");
                BOApplication.getInstance().operator = null;
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(BOApplication.class.getResource("bohome.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
            Stage stage = (Stage) userIdForm.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Bubbletill Backoffice 22.0.1");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        } catch(Exception e) {
            e.printStackTrace();
            showError("Internal error: " + e.getMessage());
        }
    }

    @FXML
    private void onExitButtonPress() {
        System.exit(0);
    }

    @FXML
    protected void onUIDKeyPress(KeyEvent e) {
        if (e.getCode().toString().equals("ENTER")) {
            passwordForm.requestFocus();
        }
    }

    @FXML
    protected void onPasswordKeyPress(KeyEvent e) {
        if (e.getCode().toString().equals("ENTER")) {
            onLoginButtonClick();
        }
    }
}