package tn.esprit.educlass.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.educlass.enums.EvaluationType;
import tn.esprit.educlass.enums.QuestionType;
import tn.esprit.educlass.model.Choice;
import tn.esprit.educlass.model.Evaluation;
import tn.esprit.educlass.model.Question;
import tn.esprit.educlass.service.ChoiceService;
import tn.esprit.educlass.service.EvaluationService;
import tn.esprit.educlass.service.QuestionService;

import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class AddEvaluationController {

    @FXML private Label formTitle;
    @FXML private TextField titleField;
    @FXML private ComboBox<EvaluationType> typeCombo;
    @FXML private TextField durationField;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea descriptionArea;
    @FXML private VBox questionsContainer;

    private final EvaluationService evaluationService = new EvaluationService();
    private final QuestionService questionService = new QuestionService();
    private final ChoiceService choiceService = new ChoiceService();

    private Evaluation editingEvaluation = null;
    private boolean saved = false;
    private int questionCounter = 0;

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll(EvaluationType.values());
        typeCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(EvaluationType t) {
                if (t == null) return "";
                return switch (t) {
                    case QUIZ -> "Quiz";
                    case ASSIGNMENT -> "Devoir";
                    case EXAM -> "Examen";
                };
            }
            @Override
            public EvaluationType fromString(String s) { return null; }
        });
        typeCombo.getSelectionModel().select(EvaluationType.QUIZ);

        statusCombo.getItems().addAll("DRAFT", "PUBLISHED", "CLOSED");
        statusCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(String s) {
                if (s == null) return "";
                return switch (s) {
                    case "DRAFT" -> "Brouillon";
                    case "PUBLISHED" -> "Publié";
                    case "CLOSED" -> "Fermé";
                    default -> s;
                };
            }
            @Override
            public String fromString(String s) { return s; }
        });
        statusCombo.getSelectionModel().select("DRAFT");
    }

    public boolean isSaved() {
        return saved;
    }

    /**
     * Called when editing an existing evaluation — pre-fills all form fields.
     */
    public void setEvaluation(Evaluation eval) {
        this.editingEvaluation = eval;
        formTitle.setText("Modifier l'évaluation");

        titleField.setText(eval.getTitle());
        typeCombo.setValue(eval.getType());
        durationField.setText(String.valueOf(eval.getDuration()));
        statusCombo.setValue(eval.getStatus());
        descriptionArea.setText(eval.getDescription());

        if (eval.getDueDate() != null) {
            dueDatePicker.setValue(eval.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        // Load existing questions & choices
        try {
            List<Question> questions = questionService.findByEvaluation(eval.getId());
            for (Question q : questions) {
                List<Choice> choices = choiceService.findByQuestion(q.getId());
                addQuestionBlock(q, choices);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddQuestion() {
        addQuestionBlock(null, null);
    }

    /**
     * Dynamically creates a question block with choices sub-section.
     */
    private void addQuestionBlock(Question existingQuestion, List<Choice> existingChoices) {
        questionCounter++;
        VBox questionBox = new VBox(8);
        questionBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-padding: 12; -fx-background-color: #f9f9f9; -fx-background-radius: 5;");

        // --- Header row: Question number + type + points + remove ---
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label qLabel = new Label("Question " + questionCounter);
        qLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        ComboBox<QuestionType> questionTypeCombo = new ComboBox<>();
        questionTypeCombo.getItems().addAll(QuestionType.values());
        questionTypeCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(QuestionType qt) {
                if (qt == null) return "";
                return switch (qt) {
                    case MULTIPLE_CHOICE -> "Choix multiple";
                    case SINGLE_CHOICE -> "Choix unique";
                    case OPEN -> "Question ouverte";
                };
            }
            @Override
            public QuestionType fromString(String s) { return null; }
        });
        questionTypeCombo.getSelectionModel().select(existingQuestion != null ? existingQuestion.getQuestionType() : QuestionType.SINGLE_CHOICE);

        TextField pointsField = new TextField();
        pointsField.setPromptText("Points");
        pointsField.setPrefWidth(70);
        if (existingQuestion != null) {
            pointsField.setText(String.valueOf(existingQuestion.getPoints()));
        }

        Button removeBtn = new Button("Supprimer");
        removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px;");
        removeBtn.setOnAction(e -> questionsContainer.getChildren().remove(questionBox));

        header.getChildren().addAll(qLabel, new Label("Type:"), questionTypeCombo, new Label("Points:"), pointsField, removeBtn);

        // --- Question text ---
        TextArea questionText = new TextArea();
        questionText.setPromptText("Texte de la question...");
        questionText.setPrefHeight(50);
        questionText.setWrapText(true);
        if (existingQuestion != null) {
            questionText.setText(existingQuestion.getText());
        }

        // --- Choices container ---
        VBox choicesContainer = new VBox(5);
        choicesContainer.setPadding(new Insets(0, 0, 0, 20));

        HBox choicesHeader = new HBox(10);
        choicesHeader.setAlignment(Pos.CENTER_LEFT);
        Label choicesLabel = new Label("Choix:");
        choicesLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        Button addChoiceBtn = new Button("+ Ajouter Choix");
        addChoiceBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 11px;");
        addChoiceBtn.setOnAction(e -> addChoiceRow(choicesContainer, null));
        choicesHeader.getChildren().addAll(choicesLabel, addChoiceBtn);

        // Show/hide choices based on question type
        boolean isChoiceType = questionTypeCombo.getValue() != QuestionType.OPEN;
        choicesHeader.setVisible(isChoiceType);
        choicesHeader.setManaged(isChoiceType);
        choicesContainer.setVisible(isChoiceType);
        choicesContainer.setManaged(isChoiceType);

        questionTypeCombo.setOnAction(e -> {
            boolean show = questionTypeCombo.getValue() != QuestionType.OPEN;
            choicesHeader.setVisible(show);
            choicesHeader.setManaged(show);
            choicesContainer.setVisible(show);
            choicesContainer.setManaged(show);
        });

        // Pre-fill existing choices
        if (existingChoices != null) {
            for (Choice c : existingChoices) {
                addChoiceRow(choicesContainer, c);
            }
        }

        questionBox.getChildren().addAll(header, questionText, choicesHeader, choicesContainer);
        questionsContainer.getChildren().add(questionBox);
    }

    /**
     * Adds a single choice row (text + isCorrect checkbox + remove button).
     */
    private void addChoiceRow(VBox choicesContainer, Choice existingChoice) {
        HBox choiceRow = new HBox(8);
        choiceRow.setAlignment(Pos.CENTER_LEFT);
        choiceRow.setStyle("-fx-border-color: #ecf0f1; -fx-border-radius: 3; -fx-padding: 5; -fx-background-color: white; -fx-background-radius: 3;");

        TextField choiceText = new TextField();
        choiceText.setPromptText("Texte du choix");
        HBox.setHgrow(choiceText, Priority.ALWAYS);

        CheckBox isCorrectCheck = new CheckBox("Correct");
        isCorrectCheck.setStyle("-fx-text-fill: #27ae60;");

        Button removeChoiceBtn = new Button("X");
        removeChoiceBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px;");
        removeChoiceBtn.setOnAction(e -> choicesContainer.getChildren().remove(choiceRow));

        if (existingChoice != null) {
            choiceText.setText(existingChoice.getText());
            isCorrectCheck.setSelected(existingChoice.isCorrect());
        }

        choiceRow.getChildren().addAll(choiceText, isCorrectCheck, removeChoiceBtn);
        choicesContainer.getChildren().add(choiceRow);
    }

    @FXML
    private void handleSave() {
        // Validate required fields
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le titre de l'évaluation est obligatoire.");
            return;
        }
        if (typeCombo.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un type d'évaluation.");
            return;
        }

        int duration = 0;
        if (durationField.getText() != null && !durationField.getText().trim().isEmpty()) {
            try {
                duration = Integer.parseInt(durationField.getText().trim());
                if (duration < 0) {
                    showAlert("Erreur", "La durée doit être un nombre positif.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Durée invalide. Saisissez un nombre entier.");
                return;
            }
        }

        try {
            Evaluation evaluation;
            if (editingEvaluation != null) {
                evaluation = editingEvaluation;
            } else {
                evaluation = new Evaluation();
                evaluation.setTeacherId(1); // Default teacher ID
            }

            evaluation.setTitle(titleField.getText().trim());
            evaluation.setDescription(descriptionArea.getText());
            evaluation.setType(typeCombo.getValue());
            evaluation.setDuration(duration);
            evaluation.setStatus(statusCombo.getValue() != null ? statusCombo.getValue() : "DRAFT");

            if (dueDatePicker.getValue() != null) {
                evaluation.setDueDate(Date.from(dueDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            } else {
                evaluation.setDueDate(null);
            }

            int evaluationId;
            if (editingEvaluation != null) {
                // UPDATE existing evaluation
                if (!evaluationService.modifier(evaluation)) {
                    showAlert("Erreur", "Impossible de modifier l'évaluation.");
                    return;
                }
                evaluationId = evaluation.getId();

                // Delete old questions (cascade deletes choices too via FK)
                questionService.deleteByEvaluation(evaluationId);
            } else {
                // CREATE new evaluation
                evaluationId = evaluationService.ajouter(evaluation);
                if (evaluationId < 0) {
                    showAlert("Erreur", "Impossible de créer l'évaluation.");
                    return;
                }
            }

            // Save questions & choices
            for (Node qNode : questionsContainer.getChildren()) {
                if (qNode instanceof VBox questionBox) {
                    saveQuestion(questionBox, evaluationId);
                }
            }

            saved = true;
            closeStage();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur base de données: " + e.getMessage());
        }
    }

    private void saveQuestion(VBox questionBox, int evaluationId) throws SQLException {
        // Header row: [Label, Label("Type:"), ComboBox, Label("Points:"), TextField, Button]
        HBox header = (HBox) questionBox.getChildren().get(0);
        ComboBox<QuestionType> qTypeCombo = (ComboBox<QuestionType>) header.getChildren().get(2);
        TextField pointsField = (TextField) header.getChildren().get(4);

        // Question text
        TextArea questionText = (TextArea) questionBox.getChildren().get(1);

        String text = questionText.getText();
        if (text == null || text.trim().isEmpty()) return;

        double points = 0;
        if (pointsField.getText() != null && !pointsField.getText().trim().isEmpty()) {
            try {
                points = Double.parseDouble(pointsField.getText().trim());
            } catch (NumberFormatException e) {
                // Ignore bad points, default 0
            }
        }

        Question question = new Question();
        question.setEvaluationId(evaluationId);
        question.setText(text.trim());
        question.setQuestionType(qTypeCombo.getValue());
        question.setPoints(points);

        int questionId = questionService.ajouter(question);
        if (questionId < 0) return;

        // Save choices if applicable
        if (qTypeCombo.getValue() != QuestionType.OPEN) {
            // Choices container is at index 3
            VBox choicesContainer = (VBox) questionBox.getChildren().get(3);
            for (Node cNode : choicesContainer.getChildren()) {
                if (cNode instanceof HBox choiceRow) {
                    TextField choiceTextField = (TextField) choiceRow.getChildren().get(0);
                    CheckBox isCorrectCheck = (CheckBox) choiceRow.getChildren().get(1);

                    String choiceText = choiceTextField.getText();
                    if (choiceText == null || choiceText.trim().isEmpty()) continue;

                    Choice choice = new Choice();
                    choice.setQuestionId(questionId);
                    choice.setText(choiceText.trim());
                    choice.setCorrect(isCorrectCheck.isSelected());
                    choiceService.ajouter(choice);
                }
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

