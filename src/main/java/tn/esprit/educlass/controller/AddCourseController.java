package tn.esprit.educlass.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.educlass.model.Chapter;
import tn.esprit.educlass.model.Course;
import tn.esprit.educlass.model.Lesson;
import tn.esprit.educlass.service.CourseService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddCourseController {

    @FXML private TextField titleField;
    @FXML private ComboBox<Integer> levelCombo;
    @FXML private TextArea descriptionArea;
    @FXML private VBox chaptersContainer;

    private final CourseService service = new CourseService();
    private boolean saved = false;

    @FXML
    public void initialize() {
        levelCombo.getItems().addAll(1, 2, 3, 4, 5);
        levelCombo.getSelectionModel().select(0);
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleAddChapter() {
        VBox chapterBox = new VBox(10);
        chapterBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #f9f9f9;");
        
        HBox header = new HBox(10);
        TextField chapterTitle = new TextField();
        chapterTitle.setPromptText("Titre du chapitre");
        chapterTitle.setPrefWidth(300);
        
        Button removeChapterBtn = new Button("Supprimer");
        removeChapterBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        removeChapterBtn.setOnAction(e -> chaptersContainer.getChildren().remove(chapterBox));
        
        header.getChildren().addAll(new Label("Chapitre:"), chapterTitle, removeChapterBtn);
        
        VBox lessonsContainer = new VBox(5);
        lessonsContainer.setPadding(new Insets(0, 0, 0, 20));
        
        Button addLessonBtn = new Button("+ Ajouter Leçon");
        addLessonBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 11px;");
        addLessonBtn.setOnAction(e -> handleAddLesson(lessonsContainer));
        
        chapterBox.getChildren().addAll(header, new Label("Leçons:"), lessonsContainer, addLessonBtn);
        chaptersContainer.getChildren().add(chapterBox);
    }

    private void handleAddLesson(VBox lessonsContainer) {
        VBox lessonBox = new VBox(5);
        lessonBox.setStyle("-fx-border-color: #ecf0f1; -fx-border-radius: 3; -fx-padding: 5;");
        
        HBox top = new HBox(10);
        TextField lessonTitle = new TextField();
        lessonTitle.setPromptText("Titre de la leçon");
        lessonTitle.setPrefWidth(250);
        
        Button removeLessonBtn = new Button("X");
        removeLessonBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        removeLessonBtn.setOnAction(e -> lessonsContainer.getChildren().remove(lessonBox));
        
        top.getChildren().addAll(new Label("Leçon:"), lessonTitle, removeLessonBtn);
        
        TextArea lessonContent = new TextArea();
        lessonContent.setPromptText("Contenu de la leçon...");
        lessonContent.setPrefHeight(60);
        lessonContent.setWrapText(true);
        
        lessonBox.getChildren().addAll(top, lessonContent);
        lessonsContainer.getChildren().add(lessonBox);
    }

    @FXML
    private void handleSave() {
        if (titleField.getText().isEmpty()) {
            showAlert("Erreur", "Le titre du cours est obligatoire.");
            return;
        }

        try {
            Course course = new Course();
            course.setTitle(titleField.getText());
            course.setLevel(levelCombo.getValue());
            course.setDescription(descriptionArea.getText());

            long courseId = service.createCourse(course);
            course.setId(courseId);

            int chapterOrder = 1;
            for (Node node : chaptersContainer.getChildren()) {
                if (node instanceof VBox chapterBox) {
                    HBox header = (HBox) chapterBox.getChildren().get(0);
                    TextField chapterTitleField = (TextField) header.getChildren().get(1);
                    String title = chapterTitleField.getText();
                    
                    if (title.isEmpty()) continue;

                    Chapter chapter = new Chapter();
                    chapter.setTitle(title);
                    chapter.setOrderIndex(chapterOrder++);
                    chapter.setCourse(course);
                    
                    long chapterId = service.createChapter(chapter);
                    chapter.setId(chapterId);

                    VBox lessonsContainer = (VBox) chapterBox.getChildren().get(2);
                    for (Node lNode : lessonsContainer.getChildren()) {
                        if (lNode instanceof VBox lessonBox) {
                            HBox lTop = (HBox) lessonBox.getChildren().get(0);
                            TextField lTitleField = (TextField) lTop.getChildren().get(1);
                            TextArea lContentArea = (TextArea) lessonBox.getChildren().get(1);
                            
                            String lTitle = lTitleField.getText();
                            if (lTitle.isEmpty()) continue;

                            Lesson lesson = new Lesson();
                            lesson.setTitle(lTitle);
                            lesson.setContent(lContentArea.getText());
                            lesson.setDurationMinutes(15); // Default
                            lesson.setChapter(chapter);
                            
                            service.createLesson(lesson);
                        }
                    }
                }
            }

            saved = true;
            closeStage();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de l'enregistrement: " + e.getMessage());
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
