package tn.esprit.educlass.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.enums.UserStatus;
import tn.esprit.educlass.model.SchoolClass;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.service.AuthService;
import tn.esprit.educlass.service.SchoolClassService;
import tn.esprit.educlass.service.UserService;
import tn.esprit.educlass.utlis.ValidationUtils;

import java.sql.SQLException;
import java.util.Optional;

public class UsersController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> firstNameCol;
    @FXML private TableColumn<User, String> lastNameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, Role> roleCol;
    @FXML private TableColumn<User, String> classCol;
    @FXML private TableColumn<User, UserStatus> statusCol;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private HBox passwordRow;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private HBox classRow;
    @FXML private ComboBox<SchoolClass> classCombo;
    @FXML private ComboBox<UserStatus> statusCombo;
    @FXML private Label messageLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterRoleCombo;
    @FXML private ComboBox<String> filterStatusCombo;

    private final UserService userService = new UserService();
    private final AuthService authService = new AuthService();
    private final SchoolClassService schoolClassService = new SchoolClassService();
    private final ObservableList<User> usersList = FXCollections.observableArrayList();
    private FilteredList<User> filteredUsers;
    private User selectedUser;
    private User currentUser;
    private MainController mainController;
    private AdminDashboardController adminDashboardController;

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setAdminDashboardController(AdminDashboardController adminDashboardController) {
        this.adminDashboardController = adminDashboardController;
    }

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList(Role.values()));
        statusCombo.setItems(FXCollections.observableArrayList(UserStatus.values()));
        loadSchoolClasses();
        updateClassRowVisibility(null);

        // Filter combos
        ObservableList<String> roleFilters = FXCollections.observableArrayList("Tous");
        for (Role r : Role.values()) roleFilters.add(r.name());
        filterRoleCombo.setItems(roleFilters);
        filterRoleCombo.setValue("Tous");

        ObservableList<String> statusFilters = FXCollections.observableArrayList("Tous");
        for (UserStatus s : UserStatus.values()) statusFilters.add(s.name());
        filterStatusCombo.setItems(statusFilters);
        filterStatusCombo.setValue("Tous");

        // Filtered list
        filteredUsers = new FilteredList<>(usersList, p -> true);
        usersTable.setItems(filteredUsers);
        loadUsers();

        // Search & filter listeners
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterRoleCombo.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterStatusCombo.valueProperty().addListener((obs, o, n) -> applyFilters());
        roleCombo.valueProperty().addListener((obs, oldRole, newRole) -> updateClassRowVisibility(newRole));

        // Table selection
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedUser = newVal;
                firstNameField.setText(newVal.getFirstName());
                lastNameField.setText(newVal.getLastName());
                emailField.setText(newVal.getEmail());
                roleCombo.setValue(newVal.getRole());
                classCombo.setValue(newVal.getSchoolClass());
                statusCombo.setValue(newVal.getStatus());
                passwordField.clear();
                passwordRow.setVisible(false);
                passwordRow.setManaged(false);
                updateClassRowVisibility(newVal.getRole());

                boolean isSelf = currentUser != null && newVal.getId() == currentUser.getId();
                roleCombo.setDisable(isSelf);
                statusCombo.setDisable(isSelf);
            }
        });
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String roleFilter = filterRoleCombo.getValue();
        String statusFilter = filterStatusCombo.getValue();

        filteredUsers.setPredicate(user -> {
            boolean matchSearch = search.isEmpty()
                    || user.getFirstName().toLowerCase().contains(search)
                    || user.getLastName().toLowerCase().contains(search)
                    || user.getEmail().toLowerCase().contains(search);
            boolean matchRole = "Tous".equals(roleFilter) || user.getRole().name().equals(roleFilter);
            boolean matchStatus = "Tous".equals(statusFilter) || user.getStatus().name().equals(statusFilter);
            return matchSearch && matchRole && matchStatus;
        });
    }

    private void loadUsers() {
        try {
            usersList.setAll(userService.afficher());
        } catch (SQLException e) {
            showError("Échec du chargement des utilisateurs : " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        // Validate names
        String err = ValidationUtils.validateName(firstNameField.getText(), "Le prénom");
        if (err != null) { showError(err); return; }
        err = ValidationUtils.validateName(lastNameField.getText(), "Le nom");
        if (err != null) { showError(err); return; }

        // Validate email
        if (!ValidationUtils.isValidEmail(emailField.getText())) {
            showError("Format d'email invalide.");
            return;
        }
        if (roleCombo.getValue() == null || statusCombo.getValue() == null) {
            showError("Veuillez sélectionner un rôle et un statut.");
            return;
        }
        if (roleCombo.getValue() == Role.STUDENT && classCombo.getValue() == null) {
            showError("Veuillez sélectionner une classe pour l'étudiant.");
            return;
        }

        if (selectedUser == null) {
            // ADD new user
            if (passwordField.getText().isEmpty()) {
                showError("Le mot de passe est requis pour un nouvel utilisateur.");
                return;
            }
            String pwdErr = ValidationUtils.validatePassword(passwordField.getText());
            if (pwdErr != null) { showError(pwdErr); return; }

            // Check email uniqueness
            try {
                User existing = userService.findByEmail(emailField.getText());
                if (existing != null) {
                    showError("Cet email est déjà utilisé.");
                    return;
                }
            } catch (SQLException e) {
                showError("Erreur SQL : " + e.getMessage());
                return;
            }

            User newUser = new User();
            newUser.setFirstName(firstNameField.getText().trim());
            newUser.setLastName(lastNameField.getText().trim());
            newUser.setEmail(emailField.getText().trim());
            newUser.setPassword(passwordField.getText());
            newUser.setRole(roleCombo.getValue());
            newUser.setStatus(statusCombo.getValue());
            newUser.setSchoolClass(roleCombo.getValue() == Role.STUDENT ? classCombo.getValue() : null);
            try {
                boolean ok = userService.ajouter(newUser);
                if (ok) {
                    showSuccess("Utilisateur créé avec succès.");
                    handleClear();
                    loadUsers();
                } else {
                    showError("Échec de la création de l'utilisateur.");
                }
            } catch (SQLException e) {
                showError("Erreur SQL : " + e.getMessage());
            }
        } else {
            // UPDATE existing user
            if (currentUser != null && selectedUser.getId() == currentUser.getId()) {
                if (roleCombo.getValue() != currentUser.getRole() || statusCombo.getValue() != currentUser.getStatus()) {
                    showError("Vous ne pouvez pas modifier votre propre rôle ou statut.");
                    return;
                }
            }

            // Check email uniqueness (if changed)
            if (!emailField.getText().equals(selectedUser.getEmail())) {
                try {
                    User existing = userService.findByEmail(emailField.getText());
                    if (existing != null) {
                        showError("Cet email est déjà utilisé.");
                        return;
                    }
                } catch (SQLException e) {
                    showError("Erreur SQL : " + e.getMessage());
                    return;
                }
            }

            selectedUser.setFirstName(firstNameField.getText().trim());
            selectedUser.setLastName(lastNameField.getText().trim());
            selectedUser.setEmail(emailField.getText().trim());
            selectedUser.setRole(roleCombo.getValue());
            selectedUser.setStatus(statusCombo.getValue());
            selectedUser.setSchoolClass(roleCombo.getValue() == Role.STUDENT ? classCombo.getValue() : null);
            try {
                boolean ok = userService.modifier(selectedUser);
                if (ok) {
                    showSuccess("Utilisateur mis à jour avec succès.");
                    loadUsers();
                    if (currentUser != null && selectedUser.getId() == currentUser.getId()) {
                        if (mainController != null) mainController.refreshUserFromDb();
                        else if (adminDashboardController != null) adminDashboardController.refreshUserFromDb();
                    }
                } else {
                    showError("Échec de la mise à jour de l'utilisateur.");
                }
            } catch (SQLException e) {
                showError("Erreur SQL : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedUser == null) {
            showError("Sélectionnez un utilisateur à supprimer.");
            return;
        }
        if (currentUser != null && selectedUser.getId() == currentUser.getId()) {
            showError("Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }

        // Confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer l'utilisateur " + selectedUser.getFullName() + " ?");
        confirm.setContentText("Cette action désactivera le compte. L'utilisateur ne pourra plus se connecter.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean ok = userService.supprimer(selectedUser);
            if (ok) {
                showSuccess("Utilisateur supprimé avec succès.");
                handleClear();
                loadUsers();
            } else {
                showError("Échec de la suppression de l'utilisateur.");
            }
        } catch (SQLException e) {
            showError("Erreur SQL : " + e.getMessage());
        }
    }

    @FXML
    private void handleResetPassword() {
        if (selectedUser == null) {
            showError("Sélectionnez un utilisateur.");
            return;
        }

        // Prompt for new password
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Réinitialiser le mot de passe");
        dialog.setHeaderText("Nouveau mot de passe pour " + selectedUser.getFullName());
        dialog.setContentText("Mot de passe :");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && !result.get().isEmpty()) {
            String newPwd = result.get();
            String pwdErr = ValidationUtils.validatePassword(newPwd);
            if (pwdErr != null) {
                showError(pwdErr);
                return;
            }
            try {
                boolean ok = authService.changePassword(selectedUser.getId(), newPwd);
                if (ok) {
                    showSuccess("Mot de passe réinitialisé pour " + selectedUser.getFullName() + ".");
                } else {
                    showError("Échec de la réinitialisation du mot de passe.");
                }
            } catch (SQLException e) {
                showError("Erreur SQL : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        selectedUser = null;
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        passwordField.clear();
        roleCombo.setValue(null);
        classCombo.setValue(null);
        statusCombo.setValue(null);
        roleCombo.setDisable(false);
        statusCombo.setDisable(false);
        usersTable.getSelectionModel().clearSelection();
        messageLabel.setText("");
        passwordRow.setVisible(true);
        passwordRow.setManaged(true);
        updateClassRowVisibility(null);
    }

    private void loadSchoolClasses() {
        try {
            classCombo.setItems(FXCollections.observableArrayList(schoolClassService.getAllClasses()));
        } catch (SQLException e) {
            showError("Échec du chargement des classes : " + e.getMessage());
        }
    }

    private void updateClassRowVisibility(Role role) {
        boolean isStudent = role == Role.STUDENT;
        classRow.setVisible(isStudent);
        classRow.setManaged(isStudent);
        if (!isStudent) {
            classCombo.setValue(null);
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

