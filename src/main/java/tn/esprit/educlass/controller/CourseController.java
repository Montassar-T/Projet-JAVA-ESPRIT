package tn.esprit.educlass.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import tn.esprit.educlass.model.Chapter;
import tn.esprit.educlass.model.Course;
import tn.esprit.educlass.model.Lesson;
import tn.esprit.educlass.service.CourseService;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CourseController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> levelFilter;
    @FXML
    private GridPane courseGrid;

    private final CourseService service = new CourseService();

    @FXML
    public void initialize() {
        levelFilter.getItems().addAll("Tous les niveaux", "1","2","3","4","5");
        levelFilter.getSelectionModel().select(0);
        
        // Load initial data
        loadCourses();
    }

    private void loadCourses() {
        try {
            List<Course> courses = service.getAllCourses();
            displayCourses(courses);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayCourses(List<Course> courses) {
        courseGrid.getChildren().clear();
        int row = 0;
        int col = 0;
        for (Course course : courses) {
            VBox courseCard = createCourseCard(course);
            courseGrid.add(courseCard, col, row);
            col++;
            if (col > 1) { // Reduced to 2 columns for better visibility of details
                col = 0;
                row++;
            }
        }
    }

    private VBox createCourseCard(Course course) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(350);

        Label titleLabel = new Label(course.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);

        Label descLabel = new Label(course.getDescription());
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        descLabel.setWrapText(true);

        Accordion chaptersAccordion = new Accordion();
        try {
            List<Chapter> chapters = service.getChaptersByCourse(course.getId());
            for (Chapter chapter : chapters) {
                VBox lessonsBox = new VBox(5);
                lessonsBox.setPadding(new Insets(5, 10, 5, 10));
                
                List<Lesson> lessons = service.getLessonsByChapter(chapter.getId());
                if (lessons.isEmpty()) {
                    lessonsBox.getChildren().add(new Label("Aucune leçon pour le moment."));
                } else {
                    for (Lesson lesson : lessons) {
                        Hyperlink lessonLink = new Hyperlink(lesson.getTitle());
                        lessonLink.setStyle("-fx-text-fill: #3498db; -fx-font-size: 13px;");
                        lessonLink.setOnAction(e -> showLessonPopup(lesson));
                        lessonsBox.getChildren().add(lessonLink);
                    }
                }

                TitledPane chapterPane = new TitledPane(chapter.getTitle(), lessonsBox);
                chapterPane.setAnimated(true);
                chaptersAccordion.getPanes().add(chapterPane);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            card.getChildren().add(new Label("Erreur lors du chargement des chapitres."));
        }

        VBox.setVgrow(chaptersAccordion, Priority.ALWAYS);
        card.getChildren().addAll(titleLabel, descLabel, new Separator(), new Label("Chapitres:"), chaptersAccordion);
        
        return card;
    }

    private void showLessonPopup(Lesson lesson) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la leçon");
        alert.setHeaderText(lesson.getTitle());
        
        // Use a ScrollPane for the content in case it's long
        TextArea textArea = new TextArea(lesson.getContent());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(300);
        textArea.setPrefWidth(400);
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    /* =====================================================
       COURSE ENDPOINTS
       ===================================================== */

    public long createCourse(Course course) throws SQLException {
        return service.createCourse(course);
    }

    public Course getCourseById(long id) throws SQLException {
        return service.getCourseById(id);
    }

    public List<Course> getAllCourses() throws SQLException {
        return service.getAllCourses();
    }

    public void updateCourse(Course course) throws SQLException {
        service.updateCourse(course);
    }

    public void deleteCourse(long id) throws SQLException {
        service.deleteCourse(id);
    }

    /* =====================================================
       CHAPTER ENDPOINTS
       ===================================================== */

    public long createChapter(Chapter chapter) throws SQLException {
        return service.createChapter(chapter);
    }

    public Chapter getChapterById(long id) throws SQLException {
        return service.getChapterById(id);
    }

    public List<Chapter> getChaptersByCourse(long courseId) throws SQLException {
        return service.getChaptersByCourse(courseId);
    }

    public void updateChapter(Chapter chapter) throws SQLException {
        service.updateChapter(chapter);
    }

    public void deleteChapter(long id) throws SQLException {
        service.deleteChapter(id);
    }

    /* =====================================================
       LESSON ENDPOINTS
       ===================================================== */

    public void createLesson(Lesson lesson) throws SQLException {
        service.createLesson(lesson);
    }

    public Lesson getLessonById(long id) throws SQLException {
        return service.getLessonById(id);
    }

    public List<Lesson> getLessonsByChapter(long chapterId) throws SQLException {
        return service.getLessonsByChapter(chapterId);
    }

    public void updateLesson(Lesson lesson) throws SQLException {
        service.updateLesson(lesson);
    }

    public void deleteLesson(long id) throws SQLException {
        service.deleteLesson(id);
    }

    @FXML
    public void onAddCourse(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/add_course.fxml"));
            Parent root = loader.load();
            
            AddCourseController controller = loader.getController();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouveau cours");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            if (controller.isSaved()) {
                loadCourses(); // Refresh the list
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ouverture du formulaire");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void onSearch(ActionEvent actionEvent) {
        onApplyFilters(actionEvent);
    }

    @FXML
    public void onApplyFilters(ActionEvent actionEvent) {
        String level = levelFilter.getValue();
        System.out.println("Applying filters: Level=" + level);

        try {
            List<Course> courses;
            if (level == null || level.equals("Tous les niveaux")) {
                courses = service.getAllCourses();
            } else {
                courses = service.getCoursesByLevel(Integer.parseInt(level));
            }

            // Apply search filter if present
            String query = searchField.getText();
            if (query != null && !query.isEmpty()) {
                courses = courses.stream()
                        .filter(c -> c.getTitle().toLowerCase().contains(query.toLowerCase()))
                        .toList();
            }

            displayCourses(courses);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onResetFilters(ActionEvent actionEvent) {
        searchField.clear();
        levelFilter.getSelectionModel().select(0);
        loadCourses();
    }


}
