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
import tn.esprit.educlass.model.Notification;
import tn.esprit.educlass.service.NotificationService;
import tn.esprit.educlass.utlis.SessionManager;
import tn.esprit.educlass.utlis.DataSource;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

public class MainController {

    @FXML
    private Label initialsLabel;
    @FXML
    private Label headerInitialsLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label welcomeNameLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label notificationBadge;
    @FXML
    private javafx.scene.layout.VBox notificationDropdown;
    @FXML
    private ListView<String> notificationListView;
    @FXML
    private StackPane contentPane;

    private User user;
    private NotificationService notificationService;

    // Call this after login to set user
    public void setUser(User user) {
        this.user = user;
        SessionManager.getInstance().setCurrentUser(user);

        // Initialize service here if not done, or in instance init
        if (notificationService == null) {
            notificationService = new NotificationService(DataSource.getInstance().getCon());
        }

        nameLabel.setText(user.getFullName());
        if (welcomeNameLabel != null)
            welcomeNameLabel.setText(user.getFirstName());
        roleLabel.setText(user.getRole().name());

        String initials = user.getFirstName().substring(0, 1).toUpperCase() +
                user.getLastName().substring(0, 1).toUpperCase();
        initialsLabel.setText(initials);
        if (headerInitialsLabel != null)
            headerInitialsLabel.setText(initials);

        // Load notifications
        loadNotifications();

        // Load default section
        showDashboard(null);
    }

    private void loadNotifications() {
        if (user == null || notificationService == null)
            return;
        try {
            List<Notification> notifications = notificationService.getNotificationsByUser((int) user.getId());
            long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();

            if (notificationBadge != null) {
                notificationBadge.setText(String.valueOf(unreadCount));
                notificationBadge.setVisible(unreadCount > 0);
            }

            if (notificationListView != null) {
                ObservableList<String> items = FXCollections.observableArrayList();
                notifications.stream().limit(10).forEach(n -> {
                    String status = n.isRead() ? "✓" : "●";
                    items.add(status + " " + n.getTitle() + "\n  " + n.getMessage());
                });
                notificationListView.setItems(items);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void toggleNotifications(ActionEvent e) {
        if (notificationDropdown != null) {
            boolean isVisible = notificationDropdown.isVisible();
            notificationDropdown.setVisible(!isVisible);
            notificationDropdown.setManaged(!isVisible);
            if (!isVisible) {
                loadNotifications();
            }
        }
    }

    @FXML
    private void hoverEnter(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        if (btn.getStyle().contains("#3498db"))
            return;
        btn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: white; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15;");
    }

    @FXML
    private void hoverExit(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        if (btn.getStyle().contains("#3498db"))
            return;
        btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15;");
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
    private void showEvaluations(ActionEvent event) {
        loadSection("/view/evaluations.fxml");
    }

    private void loadSection(String fxmlPath) {
        try {
            // Hide notification dropdown
            if (notificationDropdown != null) {
                notificationDropdown.setVisible(false);
                notificationDropdown.setManaged(false);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Pass user to the loaded controller if it supports it
            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setUser(user);
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
            // Clear session
            SessionManager.getInstance().clearSession();

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
