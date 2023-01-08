package store.bubbletill.backoffice.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import store.bubbletill.backoffice.BOApplication;
import store.bubbletill.commons.OperatorData;

public class UserManController {

    private final BOApplication app = BOApplication.getInstance();
    private final BOContainerController controller = BOContainerController.getInstance();

    @FXML private Pane userManPane;
    @FXML private Pane userManListPane;
    @FXML private TableView<OperatorData> userManListTable;

    @FXML
    public void initialize() {
        userManListTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("operatorId"));
        userManListTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        userManListTable.getItems().clear();

        try {
            HttpClient httpClient = HttpClientBuilder.create().build();

            StringEntity requestEntity = new StringEntity(
                    "{\"store\":\"" + app.localData.getStore() + "\", \"token\":\"" + app.localData.getToken() + "\"}",
                    ContentType.APPLICATION_JSON);

            HttpPost postMethod = new HttpPost(app.localData.getBackend() + "/bo/listoperators");
            postMethod.setEntity(requestEntity);

            HttpResponse rawResponse = httpClient.execute(postMethod);
            String out = EntityUtils.toString(rawResponse.getEntity());

            OperatorData[] listData = BOApplication.gson.fromJson(out, OperatorData[].class);

            for (OperatorData od : listData) {
                userManListTable.getItems().add(od);
            }
        } catch (Exception e) {
            e.printStackTrace();
            controller.showError(e.getMessage());
            return;
        }

        controller.showError(null);
        userManPane.setVisible(true);
        userManListPane.setVisible(true);
    }

    @FXML private void onUMMenuCreateButtonPress() { }

    @FXML private void onUMMenuEditButtonPress() {
        controller.showError(null);
        OperatorData selected = userManListTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            controller.showError("Please select an operator to edit.");
            return;
        }
    }

    @FXML private void onUMMenuBackButtonPress() {
        controller.updateSubScene("mainmenu");
    }

}
