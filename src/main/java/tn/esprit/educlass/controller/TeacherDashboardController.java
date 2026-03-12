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
import tn.esprit.educlass.model.Notification;
import tn.esprit.educlass.model.Lesson;
import tn.esprit.educlass.model.Question;
import tn.esprit.educlass.service.CourseService;
import tn.esprit.educlass.service.UserService;
import tn.esprit.educlass.service.EvaluationService;
import tn.esprit.educlass.service.QuestionService;
import tn.esprit.educlass.service.NotificationService;
import tn.esprit.educlass.utlis.SessionManager;
import tn.esprit.educlass.utlis.DataSource;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.application.Platform;

import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import tn.esprit.educlass.enums.Role;

public class TeacherDashboardController implements Initializable {

    private static String roleToFrench(Role role) {
        if (role == null) return "";
        return switch (role) {
            case TEACHER -> "Enseignant";
            case STUDENT -> "Étudiant";
            case ADMIN -> "Administrateur";
        };
    }

    @FXML
    private Label nameLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Circle avatarCircle;
    @FXML
    private Label initialsLabel;

    @FXML
    private VBox sidebar;

    @FXML
    private StackPane contentPane;
    @FXML
    private VBox dashboardView;
    @FXML
    private VBox myCoursesView;
    @FXML
    private VBox addCourseView;
    @FXML
    private VBox myLessonsView;
    @FXML
    private VBox addLessonView;
    @FXML
    private VBox myQuestionsView;
    @FXML
    private VBox addQuestionView;
    @FXML
    private VBox myStudentsView;
    @FXML
    private VBox addStudentView;
    @FXML
    private VBox gradesView;
    @FXML
    private VBox evaluationsView;
    @FXML
    private VBox settingsView;

    @FXML
    private Label statsCoursesLabel;
    @FXML
    private Label statsLessonsLabel;
    @FXML
    private Label statsQuestionsLabel;
    @FXML
    private Label statsStudentsLabel;

    @FXML
    private VBox notificationDropdown;
    @FXML
    private ListView<String> notificationListView;
    @FXML
    private Label notificationBadge;

    @FXML
    private ListView<?> recentCoursesListView;
    @FXML
    private ListView<?> recentLessonsListView;

    @FXML
    private ListView<String> designsListView;
    @FXML
    private TextArea designViewerArea;

    private CourseService courseService;
    private UserService userService;
    private EvaluationService evaluationService;
    private QuestionService questionService;
    private NotificationService notificationService;

    private User currentUser;

    private List<Node> allViews;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        courseService = new CourseService();
        userService = new UserService();
        evaluationService = new EvaluationService();
        questionService = new QuestionService();
        notificationService = new NotificationService();

        if (notificationDropdown != null) {
            notificationDropdown.setVisible(false);
        }

        allViews = Arrays.asList(
                dashboardView, myCoursesView, addCourseView,
                myLessonsView, addLessonView, myQuestionsView,
                addQuestionView, myStudentsView, addStudentView,
                gradesView, evaluationsView, settingsView);

        if (dashboardView != null) {
            showView(dashboardView);
            loadDashboardData();
        }

        loadDesignsListForTeacher();
        loadNotifications();
    }

    @SuppressWarnings("unchecked")
    private void loadDashboardData() {
        try {
            List<Course> allCourses = courseService.getAllCourses();
            List<User> allUsers = userService.afficher();
            List<Lesson> allLessons = courseService.getAllLessons();
            List<Question> allQuestions = questionService.getAllQuestions();

            if (statsCoursesLabel != null)
                statsCoursesLabel.setText(String.valueOf(allCourses.size()));
            if (statsLessonsLabel != null)
                statsLessonsLabel.setText(String.valueOf(allLessons.size()));
            if (statsQuestionsLabel != null)
                statsQuestionsLabel.setText(String.valueOf(allQuestions.size()));
            if (statsStudentsLabel != null)
                statsStudentsLabel.setText(String.valueOf(allUsers.size()));

            if (recentCoursesListView != null && !allCourses.isEmpty()) {
                ObservableList<Object> courseNames = FXCollections.observableArrayList();
                allCourses.stream().limit(10)
                        .forEach(c -> courseNames.add(c.getTitle() + " (Level: " + c.getLevel() + ")"));
                ((ListView<Object>) recentCoursesListView).setItems(courseNames);
            }

            if (recentLessonsListView != null) {
                ObservableList<Object> lessonsData = FXCollections.observableArrayList(
                        "Sample Lesson 1", "Sample Lesson 2");
                ((ListView<Object>) recentLessonsListView).setItems(lessonsData);
            }

            loadCourseLevelDistribution(allCourses);
        } catch (SQLException e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
        }
    }

    private void loadCourseLevelDistribution(List<Course> courses) {
        try {
            java.util.Map<Integer, Long> levelDistribution = courses.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Course::getLevel,
                            java.util.stream.Collectors.counting()));

            javafx.scene.chart.PieChart chart = findPieChart();
            if (chart != null) {
                javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> pieData =
                        javafx.collections.FXCollections.observableArrayList();
                levelDistribution.forEach(
                        (level, count) -> pieData.add(new javafx.scene.chart.PieChart.Data("Level " + level, count)));
                chart.setData(pieData);
                chart.setLabelsVisible(true);
            }
        } catch (Exception e) {
            System.err.println("Error loading course level distribution: " + e.getMessage());
        }
    }

    private javafx.scene.chart.PieChart findPieChart() {
        return findNodeByType(dashboardView, javafx.scene.chart.PieChart.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T findNodeByType(Node root, Class<T> targetClass) {
        if (root == null) return null;
        if (targetClass.isInstance(root)) return (T) root;
        if (root instanceof javafx.scene.layout.Pane) {
            for (Node child : ((javafx.scene.layout.Pane) root).getChildren()) {
                T result = findNodeByType(child, targetClass);
                if (result != null) return result;
            }
        }
        return null;
    }

    private void loadDesignsListForTeacher() {
        try {
            URL resource = getClass().getResource("/puml");
            if (resource == null) return;

            java.io.File folder = new java.io.File(resource.toURI());
            if (folder.exists() && folder.isDirectory()) {
                java.io.File[] files = folder.listFiles((dir, name) -> name.endsWith(".puml"));
                if (files != null) {
                    java.util.List<String> names = java.util.Arrays.stream(files)
                            .map(java.io.File::getName)
                            .sorted()
                            .toList();

                    Platform.runLater(() -> {
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
            }
        } catch (Exception e) {
            System.err.println("Error loading PUML diagrams for teacher: " + e.getMessage());
        }
    }

    private void showPUMLSourceTeacher(String filename) {
        try {
            URL resource = getClass().getResource("/puml/" + filename);
            if (resource != null) {
                java.nio.file.Path p = java.nio.file.Paths.get(resource.toURI());
                String content = java.nio.file.Files.readString(p);
                Platform.runLater(() -> {
                    if (designViewerArea != null) designViewerArea.setText(content);
                });
            }
        } catch (Exception e) {
            System.err.println("Error reading PUML file for teacher: " + e.getMessage());
        }
    }

    private void showView(Node view) {
        if (view == null) return;
        for (Node v : allViews) {
            if (v != null) v.setVisible(false);
        }
        view.setVisible(true);
        if (notificationDropdown != null) notificationDropdown.setVisible(false);
    }

    private void loadView(String fxmlPath) {
        try {
            if (notificationDropdown != null) {
                notificationDropdown.setVisible(false);
                notificationDropdown.setManaged(false);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("Error loading view " + fxmlPath + ": " + e.getMessage());
        }
    }

    @FXML
    public void toggleNotifications(ActionEvent e) {
        if (notificationDropdown != null) {
            boolean isVisible = notificationDropdown.isVisible();
            notificationDropdown.setVisible(!isVisible);
            notificationDropdown.setManaged(!isVisible);
            if (!isVisible) loadNotifications();
        }
    }

    private void loadNotifications() {
        if (notificationService == null) return;
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        Platform.runLater(() -> {
            try {
                List<Notification> notifications = notificationService.getNotificationsByUser(user.getId());
                if (notificationListView != null) {
                    notificationListView.getItems().clear();
                    for (Notification n : notifications) {
                        notificationListView.getItems().add(n.getTitle() + ": " + n.getMessage());
                    }
                }
                if (notificationBadge != null) {
                    long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
                    notificationBadge.setText(String.valueOf(unreadCount));
                    notificationBadge.setVisible(unreadCount > 0);
                }
            } catch (SQLException ex) {
                System.err.println("Error loading notifications: " + ex.getMessage());
            }
        });
    }

    @FXML
    public void switchToDashboard(ActionEvent e) {
        contentPane.getChildren().setAll(dashboardView);
        showView(dashboardView);
        loadDashboardData();
    }

    @FXML
    public void switchToMyCourses(ActionEvent e) {
        loadView("/view/courses.fxml");
    }

    @FXML
    public void switchToMyLessons(ActionEvent e) {
        showView(myLessonsView);
    }

    @FXML
    public void switchToEvaluations(ActionEvent e) {
        loadView("/view/evaluations.fxml");
    }

    @FXML
    public void switchToMyStudents(ActionEvent e) {
        showView(myStudentsView);
    }

    @FXML
    public void switchToGrades(ActionEvent e) {
        loadView("/view/marks.fxml");
    }

    @FXML
    public void switchToSettings(ActionEvent e) {
        loadView("/view/settings.fxml");
    }

    private static final String SIDEBAR_BTN_PADDING = "-fx-padding: 10 15; -fx-alignment: CENTER_LEFT; -fx-background-radius: 5; ";

    @FXML
    public void hoverEnter(MouseEvent event) {
        Button btn = (Button) event.getSource();
        if (btn.getStyle().contains("#3498db")) return;
        btn.setStyle(SIDEBAR_BTN_PADDING + "-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-cursor: hand;");
    }

    @FXML
    public void hoverExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        if (btn.getStyle().contains("#3498db")) return;
        btn.setStyle(SIDEBAR_BTN_PADDING + "-fx-background-color: transparent; -fx-text-fill: white;");
    }

    @FXML
    public void handleLogout(ActionEvent e) {
        try {
            SessionManager.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        SessionManager.setCurrentUser(user);

        if (nameLabel != null) nameLabel.setText(user.getFullName());
        if (roleLabel != null) roleLabel.setText(roleToFrench(user.getRole()));

        loadDashboardData();
        loadNotifications();

        if (initialsLabel != null && user.getFirstName() != null && user.getLastName() != null) {
            String initials = user.getFirstName().substring(0, 1).toUpperCase() +
                    user.getLastName().substring(0, 1).toUpperCase();
            initialsLabel.setText(initials);
        }
    }
}
