package tn.esprit.educlass.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import tn.esprit.educlass.model.Chapter;
import tn.esprit.educlass.model.Course;
import tn.esprit.educlass.model.Lesson;
import tn.esprit.educlass.service.CourseService;

import java.sql.SQLException;
import java.util.List;

public class CourseController {

    @FXML
    private TextField searchField;
    @FXML
    private Button searchBtn;
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private ComboBox<String> levelFilter;
    @FXML
    private Button applyFilterBtn;
    @FXML
    private Button resetFilterBtn;
    @FXML
    private GridPane courseGrid;

    private CourseService service = new CourseService();

    @FXML
    public void initialize() {
        levelFilter.getItems().addAll("1","2","3","4","5");
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
            // Placeholder for course card creation
            Label label = new Label(course.getTitle());
            courseGrid.add(label, col, row);
            col++;
            if (col > 3) {
                col = 0;
                row++;
            }
        }
    }

    /* =====================================================
       COURSE ENDPOINTS
       ===================================================== */

    public void createCourse(Course course) throws SQLException {
        service.createCourse(course);
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

    public void createChapter(Chapter chapter) throws SQLException {
        service.createChapter(chapter);
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
    public void onSearch(ActionEvent actionEvent) {
        String query = searchField.getText();
        if (query == null || query.isEmpty()) {
            loadCourses();
            return;
        }
        try {
            List<Course> courses = service.getAllCourses();
            List<Course> filtered = courses.stream()
                    .filter(c -> c.getTitle().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            displayCourses(filtered);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onApplyFilters(ActionEvent actionEvent) {
        String category = categoryFilter.getValue();
        String level = levelFilter.getValue();

        // For now, filtering is limited as the model doesn't have these fields.
        // In a real scenario, we would pass these to the service or filter the list here.
        System.out.println("Applying filters: Category=" + category + ", Level=" + level);
        loadCourses(); // Refresh list for now
    }

    @FXML
    public void onResetFilters(ActionEvent actionEvent) {
        searchField.clear();
        categoryFilter.getSelectionModel().select(0);
        levelFilter.getSelectionModel().select(0);
        loadCourses();
    }


}
