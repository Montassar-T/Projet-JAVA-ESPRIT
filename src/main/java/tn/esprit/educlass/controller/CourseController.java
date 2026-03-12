package tn.esprit.educlass.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.model.Chapter;
import tn.esprit.educlass.model.Course;
import tn.esprit.educlass.model.Lesson;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.service.CourseService;
import tn.esprit.educlass.utlis.SessionManager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> levelFilter;
    @FXML
    private GridPane courseGrid;

    @FXML
    private Button addCourseBtn;
    @FXML
    private Button deleteSelectedBtn;

    private final CourseService service = new CourseService();
    private final List<Long> selectedCourseIds = new ArrayList<>();
    private User currentUser;

    @FXML
    public void initialize() {
        this.currentUser = SessionManager.getCurrentUser();
        
        levelFilter.getItems().addAll("Tous les niveaux", "1","2","3","4","5");
        levelFilter.getSelectionModel().select(0);
        
        // Role check
        if (currentUser != null && currentUser.getRole() == Role.STUDENT) {
            addCourseBtn.setVisible(false);
            addCourseBtn.setManaged(false);
            deleteSelectedBtn.setVisible(false);
            deleteSelectedBtn.setManaged(false);
        }

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
        selectedCourseIds.clear();
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

    private String toRoman(int number) {
        if (number < 1 || number > 3999) return String.valueOf(number);
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] units = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        return thousands[number / 1000] + hundreds[(number % 1000) / 100] + tens[(number % 100) / 10] + units[number % 10];
    }

    private VBox createCourseCard(Course course) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(350);

        HBox topRow = new HBox(10);
        CheckBox checkBox = new CheckBox();
        checkBox.setOnAction(e -> {
            if (checkBox.isSelected()) {
                selectedCourseIds.add(course.getId());
            } else {
                selectedCourseIds.remove(course.getId());
            }
        });

        Label titleLabel = new Label(course.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Button editBtn = new Button("✎");
        editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> onEditCourse(course));

        if (currentUser != null && currentUser.getRole() == Role.STUDENT) {
            checkBox.setVisible(false);
            checkBox.setManaged(false);
            editBtn.setVisible(false);
            editBtn.setManaged(false);
        }

        topRow.getChildren().addAll(checkBox, titleLabel, editBtn);

        Label levelLabel = new Label("Niveau: " + toRoman(course.getLevel()));
        levelLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-background-color: #fcf3cf; -fx-padding: 2 5 2 5; -fx-background-radius: 3;");

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
        card.getChildren().addAll(topRow, levelLabel, descLabel, new Separator(), new Label("Chapitres:"), chaptersAccordion);
        
        return card;
    }

    private void onEditCourse(Course course) {
        if (currentUser != null && currentUser.getRole() == Role.STUDENT) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/add_course.fxml"));
            Parent root = loader.load();
            
            AddCourseController controller = loader.getController();
            controller.setEditMode(course);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier le cours");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            if (controller.isSaved()) {
                loadCourses();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onDeleteSelected(ActionEvent actionEvent) {
        if (currentUser != null && currentUser.getRole() == Role.STUDENT) {
            return;
        }
        if (selectedCourseIds.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Avertissement");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner au moins un cours à supprimer.");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer " + selectedCourseIds.size() + " cours ?");
        confirm.setContentText("Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    for (Long id : selectedCourseIds) {
                        service.deleteCourse(id);
                    }
                    loadCourses();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Erreur lors de la suppression");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }

    private void showLessonPopup(Lesson lesson) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la leçon");
        alert.setHeaderText(lesson.getTitle());
        
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        
        WebView contentWebView = new WebView();
        contentWebView.getEngine().loadContent(lesson.getContent());
        contentWebView.setPrefWidth(750);
        contentWebView.setPrefHeight(300);
        
        container.getChildren().add(contentWebView);
        
        try {
            byte[] pdfData = service.getLessonPdfData(lesson.getId());
            if (pdfData != null) {
                Label pdfLabel = new Label("Document PDF joint :");
                pdfLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                // Create a temporary file to show in WebView
                File tempFile = File.createTempFile("lesson_" + lesson.getId() + "_", ".pdf");
                tempFile.deleteOnExit();
                java.nio.file.Files.write(tempFile.toPath(), pdfData);

                // Use PDF.js for reliable rendering
                WebView pdfWebView = new WebView();
                String viewerUrl = getClass().getResource("/pdfjs/viewer.html").toExternalForm();
                String pdfUrl = tempFile.toURI().toString();
                pdfWebView.getEngine().load(viewerUrl + "?file=" + pdfUrl);
                pdfWebView.setPrefSize(750, 500);

                Button openExternallyBtn = new Button("Ouvrir dans un lecteur externe");
                openExternallyBtn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 11px;");
                openExternallyBtn.setOnAction(e -> {
                    try {
                        Desktop.getDesktop().open(tempFile);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                container.getChildren().addAll(new Separator(), pdfLabel, pdfWebView, openExternallyBtn);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        
        // Wrap everything in a ScrollPane to prevent oversized popups
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600); // Max visible height before scrolling
        
        alert.getDialogPane().setContent(scrollPane);
        alert.getDialogPane().setPrefWidth(820);
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
        if (currentUser != null && currentUser.getRole() == Role.STUDENT) {
            return;
        }
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
