package tn.esprit.educlass.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import tn.esprit.educlass.model.User;

public class MainController {

    @FXML private Label initialsLabel;
    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private Circle avatarCircle;
    @FXML private StackPane contentPane;

    private User user;

    // Call this after login to set user
    public void setUser(User user) {
        this.user = user;
        nameLabel.setText(user.getFullName());
        roleLabel.setText(user.getRole().name());

        String initials = user.getFirstName().substring(0,1).toUpperCase() +
                user.getLastName().substring(0,1).toUpperCase();
        initialsLabel.setText(initials);

        // Load default section
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
    private void showNotes(ActionEvent event) {
        loadSection("/view/notes.fxml");
    }

    private void loadSection(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
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
