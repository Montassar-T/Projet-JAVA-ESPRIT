package tn.esprit.educlass.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import tn.esprit.educlass.model.Course;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.service.CourseService;
import tn.esprit.educlass.service.UserService;
import tn.esprit.educlass.service.EvaluationService;
import tn.esprit.educlass.utlis.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class TeacherDashboardController implements Initializable {

    // Header fields
    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private Circle avatarCircle;
    @FXML private Label initialsLabel;

    // Sidebar
    @FXML private VBox sidebar;

    // View fields
    @FXML private StackPane contentPane;
    @FXML private VBox dashboardView;
    @FXML private VBox myCoursesView;
    @FXML private VBox addCourseView;
    @FXML private VBox myLessonsView;
    @FXML private VBox addLessonView;
    @FXML private VBox myQuestionsView;
    @FXML private VBox addQuestionView;
    @FXML private VBox myStudentsView;
    @FXML private VBox addStudentView;
    @FXML private VBox gradesView;
    @FXML private VBox evaluationsView;
    @FXML private VBox settingsView;

    // Dashboard stats labels
    @FXML private Label statsCoursesLabel;
    @FXML private Label statsLessonsLabel;
    @FXML private Label statsQuestionsLabel;
    @FXML private Label statsStudentsLabel;

    // Recent items
    @FXML private ListView<?> recentCoursesListView;
    @FXML private ListView<?> recentLessonsListView;

    // Designs
    @FXML private ListView<String> designsListView;
    @FXML private TextArea designViewerArea;

    // Service instances
    private CourseService courseService;
    private UserService userService;
    private EvaluationService evaluationService;

    // Store current user
    private User currentUser;

    private List<Node> allViews;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        courseService = new CourseService();
        userService = new UserService();
        evaluationService = new EvaluationService();

        // Collect all views
        allViews = Arrays.asList(
                dashboardView, myCoursesView, addCourseView,
                myLessonsView, addLessonView, myQuestionsView,
                addQuestionView, myStudentsView, addStudentView,
                gradesView, evaluationsView, settingsView
        );

        // Initially show dashboard
        if (dashboardView != null) {
            showView(dashboardView);
            loadDashboardData();
        }

        // Load designs/diagrams
        loadDesignsListForTeacher();
    }

    // ============ DATA LOADING METHODS ============

    @SuppressWarnings("unchecked")
    private void loadDashboardData() {
        try {
            // Load and display statistics
            List<Course> allCourses = courseService.getAllCourses();
            List<User> allUsers = userService.afficher();

            // Update stats labels
            if (statsCoursesLabel != null) statsCoursesLabel.setText(String.valueOf(allCourses.size()));
            if (statsLessonsLabel != null) statsLessonsLabel.setText("0");
            if (statsQuestionsLabel != null) statsQuestionsLabel.setText("0");
            if (statsStudentsLabel != null) statsStudentsLabel.setText(String.valueOf(allUsers.size()));

            // Load recent courses
            if (recentCoursesListView != null && !allCourses.isEmpty()) {
                ObservableList<Object> courseNames = FXCollections.observableArrayList();
                allCourses.stream().limit(10)
                    .forEach(c -> courseNames.add(c.getTitle() + " (Level: " + c.getLevel() + ")"));
                ((ListView<Object>) recentCoursesListView).setItems(courseNames);
            }

            // Load recent lessons
            if (recentLessonsListView != null) {
                ObservableList<Object> lessonsData = FXCollections.observableArrayList(
                    "Sample Lesson 1", "Sample Lesson 2"
                );
                ((ListView<Object>) recentLessonsListView).setItems(lessonsData);
            }

            // Load course level distribution
            loadCourseLevelDistribution(allCourses);
        } catch (SQLException e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
        }
    }

    private void loadCourseLevelDistribution(List<Course> courses) {
        try {
            // Count by level
            java.util.Map<Integer, Long> levelDistribution = courses.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Course::getLevel, java.util.stream.Collectors.counting()));

            // Find and populate pie chart
            javafx.scene.chart.PieChart chart = findPieChart();
            if (chart != null) {
                javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> pieData =
                    javafx.collections.FXCollections.observableArrayList();
                levelDistribution.forEach((level, count) ->
                    pieData.add(new javafx.scene.chart.PieChart.Data("Level " + level, count)));
                chart.setData(pieData);
                chart.setLabelsVisible(true);
            }
        } catch (Exception e) {
            System.err.println("Error loading course level distribution: " + e.getMessage());
        }
    }

    private javafx.scene.chart.PieChart findPieChart() {
        // Find coursesLevelPieChart in dashboardView recursively
        return findNodeByType(dashboardView, javafx.scene.chart.PieChart.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T findNodeByType(Node root, Class<T> targetClass) {
        if (root == null) return null;
        if (targetClass.isInstance(root)) {
            return (T) root;
        }
        if (root instanceof javafx.scene.layout.Pane) {
            for (Node child : ((javafx.scene.layout.Pane) root).getChildren()) {
                T result = findNodeByType(child, targetClass);
                if (result != null) return result;
            }
        }
        return null;
    }

    // Load PUML diagrams
    private void loadDesignsListForTeacher() {
        try {
            java.nio.file.Path pumlDir = java.nio.file.Paths.get("src/main/resources/puml");
            if (java.nio.file.Files.exists(pumlDir)) {
                java.util.List<String> names = java.nio.file.Files.list(pumlDir)
                        .filter(p -> p.toString().endsWith(".puml"))
                        .map(p -> p.getFileName().toString())
                        .sorted()
                        .toList();
                javafx.application.Platform.runLater(() -> {
                    if (designsListView != null) {
                        designsListView.getItems().clear();
                        designsListView.getItems().addAll(names);
                    }
                });

                if (designsListView != null) {
                    designsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                        if (newV != null) showPUMLSourceTeacher(newV);
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading PUML diagrams for teacher: " + e.getMessage());
        }
    }

    private void showPUMLSourceTeacher(String filename) {
        try {
            java.nio.file.Path p = java.nio.file.Paths.get("src/main/resources/puml", filename);
            if (java.nio.file.Files.exists(p)) {
                String content = java.nio.file.Files.readString(p);
                javafx.application.Platform.runLater(() -> {
                    if (designViewerArea != null) {
                        designViewerArea.setText(content);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error reading PUML file for teacher: " + e.getMessage());
        }
    }

    private void showView(Node view) {
        if (view == null) return;
        // Hide all views
        for (Node v : allViews) {
            if (v != null) v.setVisible(false);
        }
        // Show requested view
        view.setVisible(true);
    }

    // Navigation handlers
    @FXML public void switchToDashboard(ActionEvent e) {
        showView(dashboardView);
        loadDashboardData();
    }

    @FXML public void switchToMyCourses(ActionEvent e) {
        showView(myCoursesView);
    }

    @FXML public void switchToMyLessons(ActionEvent e) {
        showView(myLessonsView);
    }

    @FXML public void switchToEvaluations(ActionEvent e) {
        showView(evaluationsView);
    }

    @FXML public void switchToMyStudents(ActionEvent e) {
        showView(myStudentsView);
    }

    @FXML public void switchToGrades(ActionEvent e) {
        showView(gradesView);
    }

    @FXML public void switchToSettings(ActionEvent e) {
        showView(settingsView);
    }

    // Hover effects
    @FXML
    public void hoverEnter(MouseEvent event) {
        ((Button) event.getSource()).setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-cursor: hand;");
    }

    @FXML
    public void hoverExit(MouseEvent event) {
        ((Button) event.getSource()).setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
    }

    @FXML
    public void handleLogout(ActionEvent e) {
        try {
            // Clear session
            SessionManager.getInstance().clearSession();

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/login.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Set the current user for the teacher dashboard
     */
    public void setUser(User user) {
        this.currentUser = user;
        // Store in session manager
        SessionManager.getInstance().setCurrentUser(user);

        // Display user info in sidebar
        if (nameLabel != null) nameLabel.setText(user.getFullName());
        if (roleLabel != null) roleLabel.setText(user.getRole().name());

        // Set initials
        if (initialsLabel != null) {
            String initials = user.getFirstName().substring(0, 1).toUpperCase() +
                    user.getLastName().substring(0, 1).toUpperCase();
            initialsLabel.setText(initials);
        }
    }
}

