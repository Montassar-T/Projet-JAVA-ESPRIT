package tn.esprit.educlass.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.service.AuthService;
import tn.esprit.educlass.service.UserService;
import tn.esprit.educlass.utlis.ValidationUtils;

import java.sql.SQLException;

public class SettingsController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private Label roleLabel;
    @FXML private Label statusLabel;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private User user;
    private final UserService userService = new UserService();
    private final AuthService authService = new AuthService();
    private MainController mainController;

    public void setUser(User user) {
        this.user = user;
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        roleLabel.setText(user.getRole().name());
        statusLabel.setText(user.getStatus().name());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleSaveProfile() {
        // Validate name
        String nameErr = ValidationUtils.validateName(firstNameField.getText(), "Le prénom");
        if (nameErr != null) { showError(nameErr); return; }
        nameErr = ValidationUtils.validateName(lastNameField.getText(), "Le nom");
        if (nameErr != null) { showError(nameErr); return; }

        // Validate email
        if (!ValidationUtils.isValidEmail(emailField.getText())) {
            showError("Format d'email invalide.");
            return;
        }

        // Check email uniqueness (if changed)
        if (!emailField.getText().equals(user.getEmail())) {
            try {
                User existing = userService.findByEmail(emailField.getText());
                if (existing != null) {
                    showError("Cet email est déjà utilisé par un autre compte.");
                    return;
                }
            } catch (SQLException e) {
                showError("Erreur SQL : " + e.getMessage());
                return;
            }
        }

        user.setFirstName(firstNameField.getText().trim());
        user.setLastName(lastNameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        try {
            boolean ok = userService.modifier(user);
            if (ok) {
                showSuccess("Profil mis à jour avec succès.");
                if (mainController != null) {
                    mainController.refreshUserFromDb();
                }
            } else {
                showError("Échec de la mise à jour du profil.");
            }
        } catch (SQLException e) {
            showError("Erreur SQL : " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePassword() {
        String oldPwd = oldPasswordField.getText();
        String newPwd = newPasswordField.getText();
        String confirmPwd = confirmPasswordField.getText();
        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            showError("Veuillez remplir tous les champs de mot de passe.");
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        // Validate password strength
        String pwdErr = ValidationUtils.validatePassword(newPwd);
        if (pwdErr != null) { showError(pwdErr); return; }

        try {
            User check = authService.login(user.getEmail(), oldPwd);
            if (check == null) {
                showError("Ancien mot de passe incorrect.");
                return;
            }
            boolean ok = authService.changePassword(user.getId(), newPwd);
            if (ok) {
                showSuccess("Mot de passe changé avec succès.");
                oldPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            } else {
                showError("Échec du changement de mot de passe.");
            }
        } catch (SQLException e) {
            showError("Erreur SQL : " + e.getMessage());
        }
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(msg);
    }

    private void showSuccess(String msg) {
        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setText(msg);
    }
}

