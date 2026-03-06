package tn.esprit.educlass.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.educlass.enums.EvaluationType;
import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.model.Evaluation;
import tn.esprit.educlass.model.Mark;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.service.EvaluationService;
import tn.esprit.educlass.service.MarkService;
import tn.esprit.educlass.service.UserService;
import tn.esprit.educlass.utlis.SessionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class MarksViewController {

    @FXML private TableView<Mark> marksTable;
    @FXML private TableColumn<Mark, Number> idCol;
    @FXML private TableColumn<Mark, String> studentCol;
    @FXML private TableColumn<Mark, String> examCol;
    @FXML private TableColumn<Mark, Number> markCol;
    @FXML private ComboBox<User> studentCombo;
    @FXML private ComboBox<Evaluation> examCombo;
    @FXML private TextField markField;
    @FXML private ComboBox<User> filterStudentCombo;
    @FXML private ComboBox<Evaluation> filterExamCombo;
    @FXML private Button deleteMarkBtn;
    @FXML private Button saveMarkBtn;
    @FXML private TextField newExamField;

    private final MarkService markService = new MarkService();
    private final UserService userService = new UserService();
    private final EvaluationService evaluationService = new EvaluationService();

    private final ObservableList<Mark> marksList = FXCollections.observableArrayList();
    private List<User> students;
    private List<Evaluation> exams;
    private Mark selectedMark;

    @FXML
    public void initialize() {
        try {
            loadStudentsAndExams();
            setupTable();
            setupCombos();
            loadMarks();
            marksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                selectedMark = newVal;
                if (newVal != null) {
                    fillForm(newVal);
                } else {
                    clearForm();
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Chargement initial: " + e.getMessage());
        }
    }

    private void loadStudentsAndExams() throws SQLException {
        students = userService.findByRole(Role.STUDENT);
        List<Evaluation> allEvals = evaluationService.afficher();
        exams = allEvals; // Show marks for all evaluation types (auto-calculated)
    }

    private void setupTable() {
        idCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        studentCol.setCellValueFactory(cell -> {
            int sid = cell.getValue().getStudentId();
            String name = students.stream()
                    .filter(u -> u.getId() == sid)
                    .findFirst()
                    .map(User::getFullName)
                    .orElse("id=" + sid);
            return new SimpleStringProperty(name);
        });
        examCol.setCellValueFactory(cell -> {
            int eid = cell.getValue().getExamId();
            String title = exams.stream()
                    .filter(e -> e.getId() == eid)
                    .findFirst()
                    .map(Evaluation::getTitle)
                    .orElse("id=" + eid);
            return new SimpleStringProperty(title);
        });
        markCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getMark()));
        marksTable.setItems(marksList);
    }

    private void setupCombos() {
        studentCombo.setItems(FXCollections.observableArrayList(students));
        examCombo.setItems(FXCollections.observableArrayList(exams));
        studentCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(User u) { return u == null ? "" : u.getFullName() + " (" + u.getEmail() + ")"; }
            @Override
            public User fromString(String s) { return null; }
        });
        examCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Evaluation e) { return e == null ? "" : e.getTitle(); }
            @Override
            public Evaluation fromString(String s) { return null; }
        });
        filterStudentCombo.getItems().add(null);
        filterStudentCombo.getItems().addAll(students);
        filterStudentCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(User u) { return u == null ? "Tous les étudiants" : u.getFullName(); }
            @Override
            public User fromString(String s) { return null; }
        });
        filterExamCombo.getItems().add(null);
        filterExamCombo.getItems().addAll(exams);
        filterExamCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Evaluation e) { return e == null ? "Tous les examens" : e.getTitle(); }
            @Override
            public Evaluation fromString(String s) { return null; }
        });
    }

    private void loadMarks() throws SQLException {
        List<Mark> all = markService.afficher();
        User fs = filterStudentCombo.getValue();
        Evaluation fe = filterExamCombo.getValue();
        List<Mark> filtered = all.stream()
                .filter(m -> fs == null || m.getStudentId() == fs.getId())
                .filter(m -> fe == null || m.getExamId() == fe.getId())
                .collect(Collectors.toList());
        marksList.clear();
        marksList.addAll(filtered);
    }

    private void fillForm(Mark m) {
        studentCombo.setValue(students.stream().filter(u -> u.getId() == m.getStudentId()).findFirst().orElse(null));
        examCombo.setValue(exams.stream().filter(e -> e.getId() == m.getExamId()).findFirst().orElse(null));
        markField.setText(m.getMark() != null ? m.getMark().toString() : "");
    }

    @FXML
    private void onClear() {
        selectedMark = null;
        marksTable.getSelectionModel().clearSelection();
        clearForm();
    }

    private void clearForm() {
        studentCombo.setValue(null);
        examCombo.setValue(null);
        markField.clear();
    }

    @FXML
    private void onRefresh() {
        try {
            loadMarks();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void onSave() {
        User u = studentCombo.getValue();
        Evaluation e = examCombo.getValue();
        String raw = markField.getText() == null ? "" : markField.getText().trim();
        if (u == null || e == null || raw.isEmpty()) {
            showError("Champs manquants", "Veuillez choisir un étudiant, un examen et saisir une note.");
            return;
        }
        BigDecimal value;
        try {
            value = new BigDecimal(raw);
            if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("20")) > 0) {
                showError("Note invalide", "La note doit être entre 0 et 20.");
                return;
            }
        } catch (NumberFormatException ex) {
            showError("Note invalide", "Saisissez un nombre (ex: 14.5).");
            return;
        }
        try {
            if (selectedMark != null) {
                selectedMark.setStudentId(u.getId());
                selectedMark.setExamId(e.getId());
                selectedMark.setMark(value);
                if (markService.modifier(selectedMark)) {
                    loadMarks();
                    onClear();
                    showInfo("Note mise à jour.");
                } else {
                    showError("Erreur", "Impossible de modifier la note.");
                }
            } else {
                Mark m = new Mark(u.getId(), e.getId(), value);
                if (markService.ajouter(m)) {
                    loadMarks();
                    onClear();
                    showInfo("Note ajoutée.");
                } else {
                    showError("Erreur", "Impossible d'ajouter (ex: une note existe déjà pour cet étudiant et cet examen).");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Erreur base de données", ex.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        Mark m = marksTable.getSelectionModel().getSelectedItem();
        if (m == null) {
            showError("Aucune sélection", "Sélectionnez une note à supprimer.");
            return;
        }
        try {
            if (markService.supprimer(m)) {
                loadMarks();
                onClear();
                showInfo("Note supprimée.");
            } else {
                showError("Erreur", "Impossible de supprimer.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private void showInfo(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information");
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    @FXML
    private void onAddExam() {
        String title = newExamField.getText() == null ? "" : newExamField.getText().trim();
        if (title.isEmpty()) {
            showError("Champ manquant", "Saisissez le nom de l'examen.");
            return;
        }
        User currentUser = SessionManager.getCurrentUser();
        int teacherId = (currentUser != null && currentUser.getId() > 0) ? currentUser.getId() : 1;
        Evaluation e = new Evaluation();
        e.setTitle(title);
        e.setDescription("");
        e.setType(EvaluationType.EXAM);
        e.setTeacherId(teacherId);
        e.setDuration(60);
        e.setDueDate(null);
        e.setStatus("DRAFT");
        try {
            int id = evaluationService.ajouter(e);
            if (id > 0) {
                exams = evaluationService.afficher();
                examCombo.setItems(FXCollections.observableArrayList(exams));
                filterExamCombo.getItems().clear();
                filterExamCombo.getItems().add(null);
                filterExamCombo.getItems().addAll(exams);
                newExamField.clear();
                showInfo("Examen ajouté. Vous pouvez le sélectionner dans la liste.");
            } else {
                showError("Erreur", "Impossible d'ajouter l'examen.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Erreur base de données", ex.getMessage());
        }
    }
}
