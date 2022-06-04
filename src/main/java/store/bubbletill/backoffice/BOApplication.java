package store.bubbletill.backoffice;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import store.bubbletill.backoffice.controllers.StartupErrorController;
import store.bubbletill.backoffice.data.OperatorData;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;

public class BOApplication extends Application {

    public static BOApplication instance;

    public static Gson gson = new Gson();
    public static final DecimalFormat df = new DecimalFormat("0.00");
    private Stage stage;
    public Timer dateTimeTimer;

    // General Data
    public OperatorData operator;
    public boolean workingOnline = true;
    public int store;
    public int register;
    public String accessToken;

    @Override
    public void start(Stage stage) throws IOException {
        instance = this;
        this.stage = stage;

        try {
            // Reg number
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet method = new HttpGet("http://localhost:5001/info/regno");
            HttpResponse rawResponse = httpClient.execute(method);
            String out = EntityUtils.toString(rawResponse.getEntity());
            register = Integer.parseInt(out);
            System.out.println("Loaded regno");

            // Store number
            method = new HttpGet("http://localhost:5001/info/storeno");
            rawResponse = httpClient.execute(method);
            out = EntityUtils.toString(rawResponse.getEntity());
            store = Integer.parseInt(out);
            System.out.println("Loaded storeno");

            // Access token
            method = new HttpGet("http://localhost:5001/info/accesstoken");
            rawResponse = httpClient.execute(method);
            out = EntityUtils.toString(rawResponse.getEntity());
            accessToken = out;
            System.out.println("Loaded access token");
        } catch (Exception e) {
            System.out.println("Reg get failed: " + e.getMessage());
            launchError(stage, "Failed to launch Backoffice: " + e.getMessage());
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(BOApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
        stage.setTitle("Bubbletill Backoffice 22.0.1");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.show();
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

    public static void buzzer(String type) {
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet method = new HttpGet("http://localhost:5001/buzzer/" + type);
            httpClient.execute(method);
        } catch (Exception e) {
            System.out.println("Buzzer failed: " + e.getMessage());
        }
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
}