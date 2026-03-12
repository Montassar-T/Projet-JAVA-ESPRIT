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
import tn.esprit.educlass.utlis.ValidationUtils;

public class AuthController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            showError("Format d'email invalide.");
            return;
        }

        try {
            int result = authService.authenticate(email, password);

            switch (result) {
                case AuthService.LOGIN_SUCCESS:
                    User user = authService.getLastLoggedUser();
                    messageLabel.setStyle("-fx-text-fill: green;");
                    messageLabel.setText("Bienvenue " + user.getFullName());

                    String fxmlPath;
                    fxmlPath = "/view/main.fxml";
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent root = loader.load();
                    MainController controller = loader.getController();
                    controller.setUser(user);
                    Stage stage = (Stage) emailField.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("EduClass");
                    stage.show();
                    break;
                case AuthService.LOGIN_INVALID_CREDENTIALS:
                    showError("Email ou mot de passe incorrect.");
                    break;
                case AuthService.LOGIN_ACCOUNT_INACTIVE:
                    showError("Votre compte est inactif. Contactez l'administrateur.");
                    break;
                case AuthService.LOGIN_ACCOUNT_SUSPENDED:
                    showError("Votre compte est suspendu. Contactez l'administrateur.");
                    break;
                default:
                    showError("Erreur inconnue.");
            }
        } catch (Exception e) {
            showError("Erreur de connexion. Veuillez réessayer.");
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(msg);
    }
}

