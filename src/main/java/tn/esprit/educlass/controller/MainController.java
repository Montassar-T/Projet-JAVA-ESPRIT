package tn.esprit.educlass.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.utlis.SessionManager;

public class MainController {

    @FXML private Label initialsLabel;
    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private StackPane contentPane;
    @FXML private Button usersButton;

    private User user;

    // Refresh user from database and update sidebar
    public void refreshUserFromDb() {
        try {
            tn.esprit.educlass.service.UserService userService = new tn.esprit.educlass.service.UserService();
            User refreshed = userService.findById(user.getId());
            if (refreshed != null) {
                updateSidebar(refreshed);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update sidebar display with given user
    private void updateSidebar(User user) {
        this.user = user;
        SessionManager.setCurrentUser(user);
        nameLabel.setText(user.getFullName());
        roleLabel.setText(user.getRole().name());
        String initials = user.getFirstName().substring(0, 1).toUpperCase() +
                user.getLastName().substring(0, 1).toUpperCase();
        initialsLabel.setText(initials);

        // Show/hide admin-only buttons
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (usersButton != null) {
            usersButton.setVisible(isAdmin);
            usersButton.setManaged(isAdmin);
        }
    }

    // Called after login to set user and load default section
    public void setUser(User user) {
        updateSidebar(user);
        showDashboard(null);
    }

    @FXML
    private void hoverEnter(javafx.scene.input.MouseEvent event) {
        ((Button) event.getSource()).setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-cursor: hand;");
    }

    @FXML
    private void hoverExit(javafx.scene.input.MouseEvent event) {
        ((Button) event.getSource()).setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        loadSection("/view/dashboard.fxml");
    }

    @FXML
    private void showCours(ActionEvent event) {
        loadSection("/view/courses.fxml");
    }

    @FXML
    private void showSettings(ActionEvent event) {
        loadSection("/view/settings.fxml");
    }

    @FXML
    private void showMarks(ActionEvent event) {
        loadSection("/view/marks.fxml");
    }

    @FXML
    private void showUsers(ActionEvent event) {
        loadSection("/view/users.fxml");
    }

    @FXML
    private void showEvaluations(ActionEvent event) {
        loadSection("/view/evaluations.fxml");
    }

    @FXML
    private void showChat(ActionEvent event) {
        loadSection("/view/chat.fxml");
    }

    private void loadSection(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            Object controller = loader.getController();
            // Pass user and MainController reference to SettingsController
            if (controller instanceof SettingsController) {
                ((SettingsController) controller).setUser(user);
                ((SettingsController) controller).setMainController(this);
            }
            // Pass current user and MainController reference to UsersController
            if (controller instanceof UsersController) {
                ((UsersController) controller).setCurrentUser(user);
                ((UsersController) controller).setMainController(this);
            }
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            SessionManager.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
