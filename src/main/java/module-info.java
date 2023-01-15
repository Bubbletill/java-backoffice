module store.bubbletill.backoffice {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires com.google.gson;
    requires commons;
    requires java.sql;

    opens store.bubbletill.backoffice to javafx.fxml;
    opens store.bubbletill.backoffice.controllers to javafx.fxml;
    exports store.bubbletill.backoffice;
    exports store.bubbletill.backoffice.controllers;
}