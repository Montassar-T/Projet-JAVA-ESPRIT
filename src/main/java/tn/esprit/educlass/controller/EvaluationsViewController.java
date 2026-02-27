package tn.esprit.educlass.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.educlass.enums.EvaluationType;
import tn.esprit.educlass.model.Evaluation;
import tn.esprit.educlass.service.EvaluationService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class EvaluationsViewController {

    @FXML private TableView<Evaluation> evaluationsTable;
    @FXML private TableColumn<Evaluation, Number> idCol;
    @FXML private TableColumn<Evaluation, String> titleCol;
    @FXML private TableColumn<Evaluation, String> typeCol;
    @FXML private TableColumn<Evaluation, String> statusCol;
    @FXML private TableColumn<Evaluation, Number> durationCol;
    @FXML private TableColumn<Evaluation, String> dueDateCol;
    @FXML private TableColumn<Evaluation, Void> actionsCol;

    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private TextField searchField;

    @FXML private Label totalLabel;
    @FXML private Label draftLabel;
    @FXML private Label publishedLabel;
    @FXML private Label closedLabel;

    private final EvaluationService evaluationService = new EvaluationService();
    private final ObservableList<Evaluation> evaluationsList = FXCollections.observableArrayList();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupFilters();
        setupTable();
        setupActionsColumn();
        loadEvaluations();

        // Listen for filter changes
        filterTypeCombo.setOnAction(e -> applyFilters());
        filterStatusCombo.setOnAction(e -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupFilters() {
        filterTypeCombo.getItems().add("Tous les types");
        for (EvaluationType t : EvaluationType.values()) {
            filterTypeCombo.getItems().add(t.name());
        }
        filterTypeCombo.getSelectionModel().select(0);

        filterStatusCombo.getItems().addAll("Tous les statuts", "DRAFT", "PUBLISHED", "CLOSED");
        filterStatusCombo.getSelectionModel().select(0);
    }

    private void setupTable() {
        idCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        titleCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitle()));
        typeCol.setCellValueFactory(cell -> {
            EvaluationType type = cell.getValue().getType();
            String label = switch (type) {
                case QUIZ -> "Quiz";
                case ASSIGNMENT -> "Devoir";
                case EXAM -> "Examen";
            };
            return new SimpleStringProperty(label);
        });
        statusCol.setCellValueFactory(cell -> {
            String status = cell.getValue().getStatus();
            String label = switch (status) {
                case "DRAFT" -> "Brouillon";
                case "PUBLISHED" -> "Publié";
                case "CLOSED" -> "Fermé";
                default -> status;
            };
            return new SimpleStringProperty(label);
        });
        // Style status column with colors
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Brouillon" -> setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        case "Publié" -> setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        case "Fermé" -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });
        durationCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDuration()));
        dueDateCol.setCellValueFactory(cell -> {
            if (cell.getValue().getDueDate() != null) {
                return new SimpleStringProperty(dateFormat.format(cell.getValue().getDueDate()));
            }
            return new SimpleStringProperty("—");
        });

        evaluationsTable.setItems(evaluationsList);
    }

    private void setupActionsColumn() {
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final Button publishBtn = new Button("Publier");
            private final HBox box = new HBox(5, editBtn, deleteBtn, publishBtn);

            {
                box.setAlignment(Pos.CENTER);
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                publishBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");

                editBtn.setOnAction(e -> {
                    Evaluation eval = getTableView().getItems().get(getIndex());
                    onEdit(eval);
                });
                deleteBtn.setOnAction(e -> {
                    Evaluation eval = getTableView().getItems().get(getIndex());
                    onDelete(eval);
                });
                publishBtn.setOnAction(e -> {
                    Evaluation eval = getTableView().getItems().get(getIndex());
                    onPublish(eval);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Evaluation eval = getTableView().getItems().get(getIndex());
                    // Hide publish button if already published or closed
                    publishBtn.setVisible("DRAFT".equals(eval.getStatus()));
                    publishBtn.setManaged("DRAFT".equals(eval.getStatus()));
                    setGraphic(box);
                }
            }
        });
    }

    private void loadEvaluations() {
        try {
            List<Evaluation> all = evaluationService.afficher();
            evaluationsList.clear();
            evaluationsList.addAll(all);
            applyFilters();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les évaluations: " + e.getMessage());
        }
    }

    private void applyFilters() {
        try {
            List<Evaluation> all = evaluationService.afficher();

            String typeFilter = filterTypeCombo.getValue();
            String statusFilter = filterStatusCombo.getValue();
            String searchQuery = searchField.getText();

            List<Evaluation> filtered = all.stream()
                    .filter(e -> typeFilter == null || "Tous les types".equals(typeFilter) || e.getType().name().equals(typeFilter))
                    .filter(e -> statusFilter == null || "Tous les statuts".equals(statusFilter) || e.getStatus().equals(statusFilter))
                    .filter(e -> searchQuery == null || searchQuery.isEmpty() || e.getTitle().toLowerCase().contains(searchQuery.toLowerCase()))
                    .collect(Collectors.toList());

            evaluationsList.clear();
            evaluationsList.addAll(filtered);
            updateStats(all);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStats(List<Evaluation> all) {
        totalLabel.setText("Total: " + all.size());
        long drafts = all.stream().filter(e -> "DRAFT".equals(e.getStatus())).count();
        long published = all.stream().filter(e -> "PUBLISHED".equals(e.getStatus())).count();
        long closed = all.stream().filter(e -> "CLOSED".equals(e.getStatus())).count();
        draftLabel.setText("Brouillons: " + drafts);
        publishedLabel.setText("Publiés: " + published);
        closedLabel.setText("Fermés: " + closed);
    }

    @FXML
    private void onRefresh() {
        loadEvaluations();
    }

    @FXML
    private void onAdd() {
        openEvaluationForm(null);
    }

    private void onEdit(Evaluation evaluation) {
        openEvaluationForm(evaluation);
    }

    private void openEvaluationForm(Evaluation evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/add_evaluation.fxml"));
            Parent root = loader.load();

            AddEvaluationController controller = loader.getController();
            if (evaluation != null) {
                controller.setEvaluation(evaluation);
            }

            Stage stage = new Stage();
            stage.setTitle(evaluation == null ? "Ajouter une évaluation" : "Modifier l'évaluation");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                loadEvaluations();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors de l'ouverture du formulaire: " + e.getMessage());
        }
    }

    private void onDelete(Evaluation evaluation) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'évaluation");
        confirm.setContentText("Voulez-vous vraiment supprimer \"" + evaluation.getTitle() + "\" ?\nToutes les questions et choix associés seront également supprimés.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (evaluationService.supprimer(evaluation)) {
                        loadEvaluations();
                        showInfo("Évaluation supprimée avec succès.");
                    } else {
                        showError("Erreur", "Impossible de supprimer l'évaluation.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Erreur", "Erreur base de données: " + e.getMessage());
                }
            }
        });
    }

    private void onPublish(Evaluation evaluation) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Publier l'évaluation");
        confirm.setHeaderText("Publier les résultats");
        confirm.setContentText("Voulez-vous publier \"" + evaluation.getTitle() + "\" ?\nL'évaluation sera visible par les étudiants.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (evaluationService.publish(evaluation.getId())) {
                        loadEvaluations();
                        showInfo("Évaluation publiée avec succès.");
                    } else {
                        showError("Erreur", "Impossible de publier l'évaluation.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Erreur", "Erreur base de données: " + e.getMessage());
                }
            }
        });
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
}

