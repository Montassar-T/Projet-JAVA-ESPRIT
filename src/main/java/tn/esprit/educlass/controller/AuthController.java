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
import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.utlis.SessionManager;

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
                // Store user in session manager
                SessionManager.getInstance().setCurrentUser(user);

                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Welcome " + user.getFullName());

                // Redirect based on user role
                String fxmlPath;
                String controllerClass;

                if (user.getRole() == Role.TEACHER) {
                    // Teacher redirects to teacher dashboard
                    fxmlPath = "/view/teacherDashboard.fxml";
                    controllerClass = "tn.esprit.educlass.controller.TeacherDashboardController";
                } else {
                    // Student redirects to main dashboard
                    fxmlPath = "/view/main.fxml";
                    controllerClass = "tn.esprit.educlass.controller.MainController";
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();

                // Get controller and set user data
                if (controllerClass.contains("Teacher")) {
                    TeacherDashboardController controller = loader.getController();
                    controller.setUser(user);
                } else {
                    MainController controller = loader.getController();
                    controller.setUser(user);
                }

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
