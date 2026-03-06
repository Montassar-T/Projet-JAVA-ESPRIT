package tn.esprit.educlass.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.educlass.enums.EvaluationType;
import tn.esprit.educlass.enums.QuestionType;
import tn.esprit.educlass.model.*;
import tn.esprit.educlass.service.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the student evaluation-taking interface.
 * Dynamically builds the question UI and auto-calculates marks on submit.
 */
public class TakeEvaluationController {

    @FXML private Label evaluationTitle;
    @FXML private Label evaluationType;
    @FXML private Label evaluationDuration;
    @FXML private Label evaluationDescription;
    @FXML private Label timerLabel;
    @FXML private Label statusLabel;
    @FXML private VBox questionsContainer;
    @FXML private Button submitBtn;

    private final QuestionService questionService = new QuestionService();
    private final ChoiceService choiceService = new ChoiceService();
    private final StudentResponseService responseService = new StudentResponseService();
    private final MarkService markService = new MarkService();

    private Evaluation evaluation;
    private int studentId;
    private boolean submitted = false;
    private BigDecimal resultMark = null;

    // Track question data for collecting responses
    private final List<QuestionBlock> questionBlocks = new ArrayList<>();

    // Timer
    private Timeline timer;
    private int remainingSeconds;

    /**
     * Called by the parent controller to set up the evaluation for a given student.
     */
    public void setEvaluation(Evaluation eval, int studentId) {
        this.evaluation = eval;
        this.studentId = studentId;

        // Populate header info
        evaluationTitle.setText(eval.getTitle());

        String typeLabel = switch (eval.getType()) {
            case QUIZ -> "Quiz";
            case ASSIGNMENT -> "Devoir";
            case EXAM -> "Examen";
        };
        evaluationType.setText("Type: " + typeLabel);
        evaluationDuration.setText("Durée: " + eval.getDuration() + " min");
        evaluationDescription.setText(eval.getDescription() != null ? eval.getDescription() : "");

        // Load questions
        try {
            List<Question> questions = questionService.findByEvaluation(eval.getId());
            int qNum = 0;
            for (Question q : questions) {
                qNum++;
                List<Choice> choices = choiceService.findByQuestion(q.getId());
                addQuestionUI(qNum, q, choices);
            }

            if (questions.isEmpty()) {
                statusLabel.setText("Aucune question disponible pour cette évaluation.");
                submitBtn.setDisable(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Erreur chargement des questions.");
        }

        // Start timer if duration > 0
        if (eval.getDuration() > 0) {
            startTimer(eval.getDuration());
        }
    }

    /**
     * Builds one question block UI with appropriate input controls.
     */
    private void addQuestionUI(int qNum, Question question, List<Choice> choices) {
        VBox qBox = new VBox(8);
        qBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-padding: 14; " +
                       "-fx-background-color: white; -fx-background-radius: 5;");

        // Question header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label numLabel = new Label("Question " + qNum);
        numLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        String typeStr = switch (question.getQuestionType()) {
            case SINGLE_CHOICE -> "Choix unique";
            case MULTIPLE_CHOICE -> "Choix multiple";
            case OPEN -> "Question ouverte";
        };
        Label typeLabel = new Label("(" + typeStr + " — " + question.getPoints() + " pts)");
        typeLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        header.getChildren().addAll(numLabel, typeLabel);

        // Question text
        Label textLabel = new Label(question.getText());
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");

        qBox.getChildren().addAll(header, textLabel);

        QuestionBlock block = new QuestionBlock();
        block.question = question;

        if (question.getQuestionType() == QuestionType.OPEN) {
            // Open question: TextArea for answer
            TextArea answerArea = new TextArea();
            answerArea.setPromptText("Votre réponse...");
            answerArea.setPrefHeight(80);
            answerArea.setWrapText(true);
            answerArea.setStyle("-fx-border-color: #ddd; -fx-border-radius: 3;");
            qBox.getChildren().add(answerArea);
            block.answerArea = answerArea;

        } else if (question.getQuestionType() == QuestionType.SINGLE_CHOICE) {
            // Single choice: RadioButtons in a ToggleGroup
            ToggleGroup group = new ToggleGroup();
            VBox choicesBox = new VBox(5);
            choicesBox.setPadding(new Insets(0, 0, 0, 15));
            for (Choice c : choices) {
                RadioButton rb = new RadioButton(c.getText());
                rb.setToggleGroup(group);
                rb.setUserData(c);
                rb.setStyle("-fx-font-size: 12px;");
                choicesBox.getChildren().add(rb);
            }
            qBox.getChildren().add(choicesBox);
            block.toggleGroup = group;

        } else if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            // Multiple choice: CheckBoxes
            VBox choicesBox = new VBox(5);
            choicesBox.setPadding(new Insets(0, 0, 0, 15));
            List<CheckBox> checkBoxes = new ArrayList<>();
            for (Choice c : choices) {
                CheckBox cb = new CheckBox(c.getText());
                cb.setUserData(c);
                cb.setStyle("-fx-font-size: 12px;");
                choicesBox.getChildren().add(cb);
                checkBoxes.add(cb);
            }
            qBox.getChildren().add(choicesBox);
            block.checkBoxes = checkBoxes;
        }

        questionsContainer.getChildren().add(qBox);
        questionBlocks.add(block);
    }

    /**
     * Start a countdown timer.
     */
    private void startTimer(int durationMinutes) {
        remainingSeconds = durationMinutes * 60;
        updateTimerLabel();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            updateTimerLabel();
            if (remainingSeconds <= 0) {
                timer.stop();
                timerLabel.setText("⏱ Temps écoulé !");
                timerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                // Auto-submit when time runs out
                handleSubmit();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateTimerLabel() {
        int min = remainingSeconds / 60;
        int sec = remainingSeconds % 60;
        timerLabel.setText(String.format("⏱ %02d:%02d", min, sec));
    }

    @FXML
    private void handleSubmit() {
        if (submitted) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Soumettre l'évaluation");
        confirm.setContentText("Êtes-vous sûr de vouloir soumettre vos réponses ?\nVous ne pourrez plus les modifier.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                doSubmit();
            }
        });
    }

    private void doSubmit() {
        if (submitted) return;
        submitted = true;

        // Stop timer
        if (timer != null) {
            timer.stop();
        }

        try {
            // Delete any previous responses for this student+evaluation (in case of re-take)
            responseService.deleteByStudentAndEvaluation(studentId, evaluation.getId());

            // Save all responses
            for (QuestionBlock block : questionBlocks) {
                saveResponses(block);
            }

            // Auto-calculate mark
            Mark mark = markService.calculateAndSaveMark(studentId, evaluation.getId());
            resultMark = mark != null ? mark.getMark() : BigDecimal.ZERO;

            // Disable submit
            submitBtn.setDisable(true);

            // Show result
            showResult();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la soumission: " + e.getMessage());
            submitted = false; // allow retry
        }
    }

    /**
     * Save student responses for one question block.
     */
    private void saveResponses(QuestionBlock block) throws SQLException {
        Question q = block.question;

        if (q.getQuestionType() == QuestionType.OPEN) {
            // Save the text answer
            String answer = block.answerArea != null ? block.answerArea.getText() : "";
            if (answer != null && !answer.trim().isEmpty()) {
                StudentResponse sr = new StudentResponse(studentId, q.getId(), null, answer.trim());
                responseService.ajouter(sr);
            }

        } else if (q.getQuestionType() == QuestionType.SINGLE_CHOICE) {
            // Save the selected radio button's choice
            if (block.toggleGroup != null && block.toggleGroup.getSelectedToggle() != null) {
                Toggle selected = block.toggleGroup.getSelectedToggle();
                Choice choice = (Choice) ((RadioButton) selected).getUserData();
                StudentResponse sr = new StudentResponse(studentId, q.getId(), choice.getId(), null);
                responseService.ajouter(sr);
            }

        } else if (q.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            // Save each checked checkbox's choice
            if (block.checkBoxes != null) {
                for (CheckBox cb : block.checkBoxes) {
                    if (cb.isSelected()) {
                        Choice choice = (Choice) cb.getUserData();
                        StudentResponse sr = new StudentResponse(studentId, q.getId(), choice.getId(), null);
                        responseService.ajouter(sr);
                    }
                }
            }
        }
    }

    /**
     * Show the auto-calculated result to the student.
     */
    private void showResult() {
        // Check if there are OPEN questions (need manual grading)
        boolean hasOpenQuestions = questionBlocks.stream()
                .anyMatch(b -> b.question.getQuestionType() == QuestionType.OPEN);

        String message;
        if (hasOpenQuestions) {
            message = "Vos réponses ont été enregistrées.\n\n" +
                      "Note automatique (questions à choix): " + resultMark + "/20\n\n" +
                      "⚠ Les questions ouvertes nécessitent une correction manuelle par l'enseignant.\n" +
                      "La note finale pourra être ajustée.";
        } else {
            message = "Vos réponses ont été enregistrées.\n\n" +
                      "🎓 Votre note: " + resultMark + "/20";
        }

        Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
        resultAlert.setTitle("Résultat");
        resultAlert.setHeaderText("Évaluation soumise avec succès !");
        resultAlert.setContentText(message);
        resultAlert.showAndWait();

        statusLabel.setText("✓ Soumis — Note: " + resultMark + "/20");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    @FXML
    private void handleCancel() {
        if (submitted) {
            closeStage();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annuler");
        confirm.setHeaderText("Quitter l'évaluation");
        confirm.setContentText("Vos réponses ne seront pas enregistrées. Continuer ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (timer != null) timer.stop();
                closeStage();
            }
        });
    }

    private void closeStage() {
        if (timer != null) timer.stop();
        Stage stage = (Stage) evaluationTitle.getScene().getWindow();
        stage.close();
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public BigDecimal getResultMark() {
        return resultMark;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Helper class to track each question block's UI components.
     */
    private static class QuestionBlock {
        Question question;
        ToggleGroup toggleGroup;       // for SINGLE_CHOICE
        List<CheckBox> checkBoxes;     // for MULTIPLE_CHOICE
        TextArea answerArea;           // for OPEN
    }
}
