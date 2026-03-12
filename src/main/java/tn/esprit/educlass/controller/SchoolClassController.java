package tn.esprit.educlass.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.educlass.model.SchoolClass;
import tn.esprit.educlass.service.SchoolClassService;

import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

public class SchoolClassController {

    @FXML private TableView<SchoolClass> classesTable;
    @FXML private TableColumn<SchoolClass, String> codeCol;
    @FXML private TableColumn<SchoolClass, String> nameCol;
    @FXML private TableColumn<SchoolClass, String> levelCol;
    @FXML private TableColumn<SchoolClass, Integer> capacityCol;

    @FXML private TextField searchField;
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> levelCombo;
    @FXML private Spinner<Integer> capacitySpinner;

    @FXML private Label formTitleLabel;
    @FXML private Label infoLabel;
    @FXML private Label messageLabel;
    @FXML private Label studentCountLabel;
    @FXML private Label capacityInfoLabel;

    @FXML private Button saveBtn;
    @FXML private Button deleteBtn;

    private final SchoolClassService service = new SchoolClassService();
    private final ObservableList<SchoolClass> classList = FXCollections.observableArrayList();
    private FilteredList<SchoolClass> filteredList;

    private SchoolClass selectedClass;

    @FXML
    public void initialize() {
        // Setup table columns
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        levelCol.setCellValueFactory(new PropertyValueFactory<>("level"));
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        // Setup level combo
        levelCombo.setItems(FXCollections.observableArrayList(
                "1ère année",
                "2ème année",
                "3ème année",
                "Master 1",
                "Master 2"
        ));

        // Setup capacity spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 30);
        capacitySpinner.setValueFactory(valueFactory);

        // Setup filtered list
        filteredList = new FilteredList<>(classList, p -> true);
        classesTable.setItems(filteredList);

        // Load classes
        loadClasses();

        // Search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        // Table selection listener
        classesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectClass(newVal);
            }
        });
    }

    private void applyFilter() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        filteredList.setPredicate(cls -> {
            if (search.isEmpty()) return true;
            return cls.getCode().toLowerCase().contains(search)
                    || cls.getName().toLowerCase().contains(search)
                    || (cls.getLevel() != null && cls.getLevel().toLowerCase().contains(search));
        });
    }

    private void loadClasses() {
        try {
            classList.setAll(service.getAllClasses());
            messageLabel.setText("");
        } catch (SQLException e) {
            showError("Erreur lors du chargement des classes : " + e.getMessage());
        }
    }

    private void selectClass(SchoolClass schoolClass) {
        selectedClass = schoolClass;
        codeField.setText(schoolClass.getCode());
        nameField.setText(schoolClass.getName());
        levelCombo.setValue(schoolClass.getLevel());
        capacitySpinner.getValueFactory().setValue(schoolClass.getCapacity() != null ? schoolClass.getCapacity() : 30);

        formTitleLabel.setText("Modifier la classe");
        saveBtn.setText("Mettre à jour");

        // Load student count
        try {
            int studentCount = service.getStudentCount(schoolClass.getId());
            int capacity = schoolClass.getCapacity() != null ? schoolClass.getCapacity() : 30;
            studentCountLabel.setText("Étudiants assignés : " + studentCount);
            capacityInfoLabel.setText("Utilisation : " + studentCount + "/" + capacity);
        } catch (SQLException e) {
            studentCountLabel.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        // Validate inputs
        if (codeField.getText().trim().isEmpty()) {
            showError("Le code est requis.");
            return;
        }
        if (nameField.getText().trim().isEmpty()) {
            showError("Le nom est requis.");
            return;
        }
        if (levelCombo.getValue() == null) {
            showError("Veuillez sélectionner un niveau.");
            return;
        }

        try {
            if (selectedClass == null) {
                // Create new class
                SchoolClass newClass = new SchoolClass();
                newClass.setCode(codeField.getText().trim());
                newClass.setName(nameField.getText().trim());
                newClass.setLevel(levelCombo.getValue());
                newClass.setCapacity(capacitySpinner.getValue());
                newClass.setCreatedAt(new Date());

                boolean success = service.createClass(newClass);
                if (success) {
                    showSuccess("Classe créée avec succès.");
                    handleClear();
                    loadClasses();
                } else {
                    showError("Erreur lors de la création de la classe.");
                }
            } else {
                // Update existing class
                selectedClass.setCode(codeField.getText().trim());
                selectedClass.setName(nameField.getText().trim());
                selectedClass.setLevel(levelCombo.getValue());
                selectedClass.setCapacity(capacitySpinner.getValue());

                boolean success = service.updateClass(selectedClass);
                if (success) {
                    showSuccess("Classe mise à jour avec succès.");
                    handleClear();
                    loadClasses();
                } else {
                    showError("Erreur lors de la mise à jour de la classe.");
                }
            }
        } catch (SQLException e) {
            showError("Erreur SQL : " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedClass == null) {
            showError("Sélectionnez une classe à supprimer.");
            return;
        }

        // Confirm deletion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer la classe " + selectedClass.getCode() + " ?");
        confirm.setContentText("Cette action supprimera la classe. Les étudiants assignés perdront leur classe.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean success = service.deleteClass(selectedClass.getId());
            if (success) {
                showSuccess("Classe supprimée avec succès.");
                handleClear();
                loadClasses();
            } else {
                showError("Erreur lors de la suppression de la classe.");
            }
        } catch (SQLException e) {
            showError("Erreur SQL : " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        selectedClass = null;
        codeField.clear();
        nameField.clear();
        levelCombo.setValue(null);
        capacitySpinner.getValueFactory().setValue(30);
        formTitleLabel.setText("Nouvelle Classe");
        saveBtn.setText("Ajouter");
        studentCountLabel.setText("Étudiants assignés : 0");
        capacityInfoLabel.setText("Utilisation : 0/30");
        classesTable.getSelectionModel().clearSelection();
        messageLabel.setText("");
    }

    @FXML
    private void handleRefresh() {
        loadClasses();
        showSuccess("Classes actualisées.");
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        messageLabel.setText(msg);
    }

    private void showSuccess(String msg) {
        messageLabel.setStyle("-fx-text-fill: #2ecc71;");
        messageLabel.setText(msg);
    }
}

