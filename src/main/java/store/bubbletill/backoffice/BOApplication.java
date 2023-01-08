package store.bubbletill.backoffice;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import store.bubbletill.backoffice.controllers.StartupErrorController;
import store.bubbletill.commons.LocalData;
import store.bubbletill.commons.OperatorData;
import store.bubbletill.commons.Transaction;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Timer;

public class BOApplication extends Application {

    public static BOApplication instance;

    public static Gson gson = new Gson();
    private Stage stage;
    public Timer dateTimeTimer;

    // General Data
    public OperatorData operator;
    public boolean workingOnline = true;
    public LocalData localData;

    // Cache info
    public HashMap<String, OperatorData> operators = new HashMap<>();

    @Override
    public void start(Stage initStage) throws IOException {
        Stage splashStage = launchSplash(initStage);
        instance = this;

        new Thread(() -> {
            try {
                Reader dataReader = Files.newBufferedReader(Paths.get("C:\\bubbletill\\data.json"));
                localData = gson.fromJson(dataReader, LocalData.class);

                // Load operators
                HttpClient httpClient = HttpClientBuilder.create().build();
                StringEntity requestEntity = new StringEntity(
                        "{\"store\": \"" + localData.getStore() + "\", \"token\":\"" + localData.getToken() + "\"}",
                        ContentType.APPLICATION_JSON);

                HttpPost methodPost = new HttpPost(localData.getBackend() + "/bo/listoperators");
                methodPost.setEntity(requestEntity);
                HttpResponse rawResponse = httpClient.execute(methodPost);
                String out = EntityUtils.toString(rawResponse.getEntity());
                OperatorData[] operatorData = gson.fromJson(out, OperatorData[].class);
                for (OperatorData o : operatorData) {
                    operators.put(o.getOperatorId(), o);
                }
                System.out.println("Loaded operators");

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