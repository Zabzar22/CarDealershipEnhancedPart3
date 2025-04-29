package org.example.view;

import javafx.scene.control.Alert;

public class DialogUtils {
    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showSuccess(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Success", null, message);
    }

    public static void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", null, message);
    }

    public static void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Information", null, message);
    }
}