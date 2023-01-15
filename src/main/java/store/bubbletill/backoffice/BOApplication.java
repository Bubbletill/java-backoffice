package store.bubbletill.backoffice;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import store.bubbletill.backoffice.controllers.StartupErrorController;
import store.bubbletill.commons.*;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Timer;

public class BOApplication extends Application {

    public static BOApplication instance;

    public static Gson gson = new Gson();
    private Stage stage;
    public Timer dateTimeTimer;
    public DatabaseManager databaseManager;

    // General Data
    public OperatorData operator;
    public boolean workingOnline = true;
    public LocalData localData;

    // Cache info
    public HashMap<String, OperatorData> operators = new HashMap<>();
    public HashMap<Integer, OperatorGroup> operatorGroups = new HashMap<>();

    @Override
    public void start(Stage initStage) throws IOException {
        Stage splashStage = launchSplash(initStage);
        instance = this;

        new Thread(() -> {
            try {
                Reader dataReader = Files.newBufferedReader(Paths.get("C:\\bubbletill\\data.json"));
                localData = gson.fromJson(dataReader, LocalData.class);
                databaseManager = new DatabaseManager(localData.getDbUsername(), localData.getDbPassword(), localData);

                ArrayList<OperatorData> operatorData = databaseManager.getOperators();
                HashMap<Integer, OperatorGroup> operatorGroups = databaseManager.getOperatorGroups();
                if (operatorData == null || operatorGroups == null) {
                    throw new Exception("Database failed to sync.");
                }

                this.operatorGroups = operatorGroups;

                for (OperatorData od : databaseManager.getOperators())
                    operators.put(od.getId(), od);

                Platform.runLater(this::postInit);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> launchError(new Stage(), "Failed to launch Back Office: " + e.getMessage()));
            } finally {
                if (splashStage != null) { Platform.runLater(splashStage::close); }
            }
        }).start();
    }

    @Override
    public void stop() throws Exception {
        if (dateTimeTimer != null)
            dateTimeTimer.cancel();
        System.out.println("BO shutting down");
    }

    public static BOApplication getInstance() { return instance;}

    public static void main(String[] args) {
        launch();
    }

    private Stage launchSplash(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(BOApplication.class.getResource("splash.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 200);
            stage.setScene(scene);
            stage.setAlwaysOnTop(true);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();

            return stage;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void postInit() {
        try {
            Stage primaryStage = new Stage(StageStyle.DECORATED);
            this.stage = primaryStage;
            FXMLLoader fxmlLoader = new FXMLLoader(BOApplication.class.getResource("login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
            primaryStage.setTitle("Bubbletill Back Office 22.0.1");
            primaryStage.setScene(scene);
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
            primaryStage.setX(0);
            primaryStage.setY(0);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void buzzer(String type) {

    }

    public static void launchError(Stage stage, String message) {
        buzzer("double");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(BOApplication.class.getResource("startuperror.fxml"));
            StartupErrorController sec = new StartupErrorController(message);
            fxmlLoader.setController(sec);

            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("Bubbletill Backoffice 22.0.1 - LAUNCH ERROR");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
            stage.setAlwaysOnTop(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean canPerformAction(BOAction actionId) {
        if (operator.canPerformAction(operatorGroups, actionId))
            return true;

        if (!actionId.isLoginIfNoPermission())
            return false;

        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Authentication");
        dialog.setHeaderText("Manager permission is required.\nPlease sign-in to authenticate " + actionId.getDisplayName() + ".");


        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("User ID");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("User ID"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password"), 0, 1);
        grid.add(password, 1, 1);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        username.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    password.requestFocus();
                }
            }
        });

        // Do some validation (using the Java 8 lambda syntax).
        password.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(username::requestFocus);

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        if (!result.isPresent())
            return false;

        OperatorData od = getOperatorData(result.get().getKey(), result.get().getValue());
        if (od == null)
            return false;

        return od.canPerformAction(operatorGroups, actionId);
    }

    private OperatorData getOperatorData(String username, String password) {
        try {
            if (operators.containsKey(username)) {
                OperatorData od = operators.get(username);
                if (!od.getPassword().equals(password)) {
                    return null;
                }

                return od;
            }

            HttpClient httpClient = HttpClientBuilder.create().build();

            StringEntity requestEntity = new StringEntity(
                    "{\"user\":\"" + username + "\",\"password\":\"" + password + "\", \"token\":\"" + localData.getToken() + "\"}",
                    ContentType.APPLICATION_JSON);

            HttpPost postMethod = new HttpPost(localData.getBackend() + "/pos/login");
            postMethod.setEntity(requestEntity);

            HttpResponse rawResponse = httpClient.execute(postMethod);
            String out = EntityUtils.toString(rawResponse.getEntity());

            ApiRequestData data = BOApplication.gson.fromJson(out, ApiRequestData.class);

            if (!data.isSuccess()) {
                return null;
            }

            OperatorData od = BOApplication.gson.fromJson(out, OperatorData.class);

            return od;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Transaction getTransaction(int store, int register, int trans, String date) {
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();

            StringEntity requestEntity = new StringEntity(
                    "{"
                            + "\"store\": \"" + store
                            + "\", \"register\": \"" + register
                            + "\", \"trans\": \"" + trans
                            + "\", \"date\": \"" + date
                            + "\", \"token\" :\"" + localData.getToken()
                            + "\"}",
                    ContentType.APPLICATION_JSON);

            HttpPost postMethod = new HttpPost(localData.getBackend() + "/bo/gettrans");
            postMethod.setEntity(requestEntity);

            HttpResponse rawResponse = httpClient.execute(postMethod);
            String out = EntityUtils.toString(rawResponse.getEntity());

            Transaction transObj;
            try {
               transObj = gson.fromJson(out, Transaction.class);
            } catch (Exception e) {
                return null; // Assuming the API returned a no trans found
            }

            return transObj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}