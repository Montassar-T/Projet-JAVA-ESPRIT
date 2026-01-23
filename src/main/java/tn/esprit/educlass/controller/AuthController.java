package tn.esprit.educlass.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.service.AuthService;

import java.awt.*;

public class AuthController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private AuthService authService = new AuthService();

    @FXML
    public void handleLogin() {
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
                // TODO: redirect to dashboard
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Invalid email or password");
            }

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Error: " + e.getMessage());
        }
    }
}
