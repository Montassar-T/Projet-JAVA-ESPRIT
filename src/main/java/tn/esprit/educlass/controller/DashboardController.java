package tn.esprit.educlass.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import tn.esprit.educlass.model.User;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label initialsLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Circle avatarCircle;

    private User user;

    public void setUser(User user) {
        this.user = user;

        // Set name
        nameLabel.setText(user.getFullName());

        // Set role
        roleLabel.setText(user.getRole().name());

        // Generate initials
        String initials =
                user.getFirstName().substring(0,1).toUpperCase() +
                        user.getLastName().substring(0,1).toUpperCase();

        initialsLabel.setText(initials);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();

            // Use event source to get current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
