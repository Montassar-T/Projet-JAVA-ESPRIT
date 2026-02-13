package tn.esprit.educlass.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import tn.esprit.educlass.service.AuthService;
import tn.esprit.educlass.model.User;

public class AuthController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill all fields");
            return;
        }

        try {
            User user = authService.login(email, password);

            if (user != null) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Welcome " + user.getFullName());

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
                Parent root = loader.load();

                // Pass the user to MainController
                MainController controller = loader.getController();
                controller.setUser(user);

                // Set scene
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("EduClass - Dashboard");
                stage.show();

            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Invalid email or password");
            }

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
