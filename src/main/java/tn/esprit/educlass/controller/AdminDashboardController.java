package tn.esprit.educlass.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.service.UserService;
import tn.esprit.educlass.utlis.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    private static String roleToFrench(tn.esprit.educlass.enums.Role role) {
        if (role == null) return "";
        return switch (role) {
            case TEACHER -> "Enseignant";
            case STUDENT -> "Étudiant";
            case ADMIN -> "Administrateur";
        };
    }

    private static final String SIDEBAR_BTN_PADDING = "-fx-padding: 10 15; -fx-alignment: CENTER_LEFT; -fx-background-radius: 5; ";

    @FXML private VBox sidebar;
    @FXML private Circle avatarCircle;
    @FXML private Label initialsLabel;
    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private StackPane contentPane;

    @FXML private VBox dashboardView;

    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (dashboardView != null) {
            contentPane.getChildren().clear();
            contentPane.getChildren().add(dashboardView);
        }
    }

    public void refreshUserFromDb() {
        if (currentUser == null) return;
        try {
            UserService userService = new UserService();
            User refreshed = userService.findById(currentUser.getId());
            if (refreshed != null) {
                setUser(refreshed);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            Object controller = loader.getController();

            if (controller instanceof UsersController) {
                ((UsersController) controller).setCurrentUser(currentUser);
                ((UsersController) controller).setMainController(null);
                ((UsersController) controller).setAdminDashboardController(this);
            }

            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (Exception e) {
            System.err.println("Error loading view " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void switchToDashboard() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(dashboardView);
    }

    @FXML
    public void switchToUsers() {
        loadView("/view/users.fxml");
    }

    @FXML
    public void switchToAcademicStructure() {
        loadView("/view/academic_structure.fxml");
    }

    @FXML
    public void switchToInstitutions() {
        loadView("/view/institution.fxml");
    }

    @FXML
    public void switchToSupervision() {
        loadView("/view/supervision.fxml");
    }

    @FXML
    public void switchToSystemConfig() {
        loadView("/view/system_config.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent e) {
        try {
            SessionManager.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void hoverEnter(MouseEvent event) {
        Button btn = (Button) event.getSource();
        if (btn.getStyle().contains("#3498db")) return;
        btn.setStyle(SIDEBAR_BTN_PADDING + "-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-cursor: hand;");
    }

    @FXML
    public void hoverExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        if (btn.getStyle().contains("#3498db")) return;
        btn.setStyle(SIDEBAR_BTN_PADDING + "-fx-background-color: transparent; -fx-text-fill: white;");
    }

    public void setUser(User user) {
        this.currentUser = user;
        SessionManager.setCurrentUser(user);

        if (nameLabel != null) nameLabel.setText(user.getFullName());
        if (roleLabel != null) roleLabel.setText(roleToFrench(user.getRole()));

        if (initialsLabel != null && user.getFirstName() != null && user.getLastName() != null) {
            String initials = user.getFirstName().substring(0, 1).toUpperCase() +
                    user.getLastName().substring(0, 1).toUpperCase();
            initialsLabel.setText(initials);
        }
    }
}
