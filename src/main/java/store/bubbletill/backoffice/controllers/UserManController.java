package store.bubbletill.backoffice.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import store.bubbletill.backoffice.BOApplication;
import store.bubbletill.commons.OperatorData;

import java.util.ArrayList;

public class UserManController {

    private final BOApplication app = BOApplication.getInstance();
    private final BOContainerController controller = BOContainerController.getInstance();

    @FXML private Pane userManPane;
    @FXML private Pane userManListPane;
    @FXML private TableView<OperatorData> userManListTable;

    @FXML
    public void initialize() {
        userManListTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        userManListTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        userManListTable.getItems().clear();

        try {
            ArrayList<OperatorData> listData = app.databaseManager.getOperators();

            if (listData == null) {
                throw new Exception("Failed to retrieve operators from database.");
            }

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
