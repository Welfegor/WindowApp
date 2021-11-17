module com.example.finalwindowapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires javafx.graphics;
    requires java.desktop;
    requires javafx.swing;


    opens com.example.finalwindowapp to javafx.fxml;
    exports com.example.finalwindowapp;
}