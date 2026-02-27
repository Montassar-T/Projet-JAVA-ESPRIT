package tn.esprit.educlass.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.model.*;
import tn.esprit.educlass.service.*;
import tn.esprit.educlass.utlis.DataSource;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label dateLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label usersDetailLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label coursesDetailLabel;
    @FXML private Label totalEvaluationsLabel;
    @FXML private Label evaluationsDetailLabel;
    @FXML private Label notificationsLabel;
    @FXML private Label notificationsDetailLabel;
    @FXML private ListView<String> coursesListView;
    @FXML private ListView<String> evaluationsListView;
    @FXML private ListView<String> notificationsListView;
    @FXML private VBox rolesStatisticsBox;
    @FXML private Button notificationButton;
    @FXML private Button profileButton;
    @FXML private Label notificationCountBadge;
    @FXML private javafx.scene.chart.PieChart rolesPieChart;
    @FXML private javafx.scene.control.ListView<String> designsListView;
    @FXML private javafx.scene.control.TextArea designViewerArea;

    // Controllers for managing different entities
    private UserService userService;
    private CourseService courseService;
    private EvaluationService evaluationService;
    private NotificationService notificationService;
    private MarkService markService;
    private AdminService adminService;

    @FXML
    public void initialize() {
        initializeServices();
        loadDate();
        loadDashboardData();
    }

    /**
     * Initialize all services
     */
    private void initializeServices() {
        try {
            userService = new UserService();
            courseService = new CourseService();
            evaluationService = new EvaluationService();
            notificationService = new NotificationService(DataSource.getInstance().getCon());
            markService = new MarkService();
            adminService = new AdminService(DataSource.getInstance().getCon());
        } catch (Exception e) {
            System.err.println("Error initializing services: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Display current date on dashboard header
     */
    private void loadDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        String currentDate = sdf.format(new Date());
        dateLabel.setText(currentDate);
    }

    /**
     * Load all dashboard data asynchronously to avoid blocking UI
     */
    private void loadDashboardData() {
        new Thread(() -> {
            try {
                // Load all statistics and data
                loadUserStatistics();
                loadCourseStatistics();
                loadEvaluationStatistics();
                loadNotificationStatistics();
                loadRecentCourses();
                loadRecentEvaluations();
                loadUserRolesDistribution();
                loadRecentNotifications();
            } catch (SQLException e) {
                System.err.println("Error loading dashboard data: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Load and display user statistics from UserService
     */
    private void loadUserStatistics() throws SQLException {
        List<User> allUsers = userService.afficher();
        int totalUsers = allUsers.size();
        long activeUsers = allUsers.stream()
                .filter(u -> u.getStatus() != null && u.getStatus().name().equals("ACTIVE"))
                .count();

        Platform.runLater(() -> {
            totalUsersLabel.setText(String.valueOf(totalUsers));
            usersDetailLabel.setText(activeUsers + " active users");
        });
    }

    /**
     * Load and display course statistics from CourseService
     */
    private void loadCourseStatistics() throws SQLException {
        List<Course> allCourses = courseService.getAllCourses();
        int totalCourses = allCourses.size();

        Platform.runLater(() -> {
            totalCoursesLabel.setText(String.valueOf(totalCourses));
            coursesDetailLabel.setText(totalCourses + " published courses");
        });
    }

    /**
     * Load and display evaluation statistics from EvaluationService
     */
    private void loadEvaluationStatistics() throws SQLException {
        List<Evaluation> allEvaluations = evaluationService.afficher();
        int totalEvaluations = allEvaluations.size();
        long inProgress = allEvaluations.stream()
                .filter(e -> e.getStatus() != null && e.getStatus().equals("IN_PROGRESS"))
                .count();

        Platform.runLater(() -> {
            totalEvaluationsLabel.setText(String.valueOf(totalEvaluations));
            evaluationsDetailLabel.setText(inProgress + " in progress");
        });
    }

    /**
     * Load notification statistics
     */
    private void loadNotificationStatistics() throws SQLException {
        try {
            // Get all notifications to count unread ones
            List<Notification> allNotifications = notificationService.getAllNotifications();
            int unreadCount = (int) allNotifications.stream()
                    .filter(n -> !n.isRead())
                    .count();

            Platform.runLater(() -> {
                notificationsLabel.setText(String.valueOf(allNotifications.size()));
                notificationsDetailLabel.setText(unreadCount + " unread");
                notificationCountBadge.setText(String.valueOf(unreadCount));
            });
        } catch (Exception e) {
            System.err.println("Error loading notifications: " + e.getMessage());
            Platform.runLater(() -> {
                notificationsLabel.setText("0");
                notificationsDetailLabel.setText("0 unread");
                notificationCountBadge.setText("0");
            });
        }
    }

    /**
     * Load and display recent courses from CourseService
     */
    private void loadRecentCourses() throws SQLException {
        List<Course> allCourses = courseService.getAllCourses();
        List<String> courseItems = allCourses.stream()
                .limit(5)
                .map(course -> {
                    String chapters = course.getChapters() != null ?
                            " (" + course.getChapters().size() + " chapters)" : "";
                    String description = course.getDescription() != null ?
                            course.getDescription().substring(0, Math.min(50, course.getDescription().length())) + "..." : "No description";
                    return "📚 " + course.getTitle() + chapters +
                            "\n  Level: " + course.getLevel() + " | " + description;
                })
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            coursesListView.getItems().clear();
            coursesListView.getItems().addAll(courseItems);
        });
    }

    /**
     * Load and display recent evaluations from EvaluationService
     */
    private void loadRecentEvaluations() throws SQLException {
        List<Evaluation> allEvaluations = evaluationService.afficher();
        List<String> evaluationItems = allEvaluations.stream()
                .limit(5)
                .map(eval -> {
                    String dueDateStr = eval.getDueDate() != null ?
                            " (Due: " + new SimpleDateFormat("MMM dd, yyyy").format(eval.getDueDate()) + ")" : "";
                    return "📝 " + eval.getTitle() + dueDateStr +
                            "\n  Type: " + (eval.getType() != null ? eval.getType().name() : "N/A") +
                            " | Status: " + eval.getStatus() +
                            " | Duration: " + eval.getDuration() + " min";
                })
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            evaluationsListView.getItems().clear();
            evaluationsListView.getItems().addAll(evaluationItems);
        });
    }

    /**
     * Load and display user roles distribution using UserService
     */
    private void loadUserRolesDistribution() throws SQLException {
        List<User> allUsers = userService.afficher();

        // Count users by role using stream and grouping
        Map<Role, Long> roleDistribution = allUsers.stream()
                .filter(u -> u.getRole() != null)
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));

        int totalUsers = allUsers.size();

        Platform.runLater(() -> {
            rolesStatisticsBox.getChildren().clear();

            roleDistribution.forEach((role, count) -> {
                double percentage = totalUsers > 0 ? (count * 100.0) / totalUsers : 0;
                VBox roleItem = createRoleStatisticItem(role.name(), count.intValue(), percentage);
                rolesStatisticsBox.getChildren().add(roleItem);
            });

            // Populate pie chart if present
            if (rolesPieChart != null) {
                javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> pieData = javafx.collections.FXCollections.observableArrayList();
                roleDistribution.forEach((role, count) -> pieData.add(new javafx.scene.chart.PieChart.Data(role.name(), count)));
                rolesPieChart.setData(pieData);
                rolesPieChart.setLabelsVisible(true);
            }
        });

        // Also load available design diagrams
        loadDesignsList();
    }

    /**
     * Load and display recent notifications
     */
    private void loadRecentNotifications() throws SQLException {
        try {
            List<Notification> allNotifications = notificationService.getAllNotifications();
            List<String> notificationItems = allNotifications.stream()
                    .limit(5)
                    .map(notif -> {
                        String readStatus = notif.isRead() ? "✓" : "●";
                        String typeIcon = notif.getType() != null ? getNotificationIcon(notif.getType().name()) : "ℹ️";
                        return typeIcon + " " + readStatus + " " + notif.getTitle() +
                                "\n  " + (notif.getMessage() != null ?
                                notif.getMessage().substring(0, Math.min(40, notif.getMessage().length())) + "..." : "");
                    })
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                notificationsListView.getItems().clear();
                notificationsListView.getItems().addAll(notificationItems);
            });
        } catch (Exception e) {
            System.err.println("Error loading notifications: " + e.getMessage());
            Platform.runLater(() -> {
                notificationsListView.getItems().clear();
                notificationsListView.getItems().add("📢 Welcome to EduClass Dashboard");
            });
        }
    }

    /**
     * Get icon based on notification type
     */
    private String getNotificationIcon(String type) {
        return switch (type.toUpperCase()) {
            case "SYSTEM" -> "⚙️";
            case "COURSE" -> "📚";
            case "EVALUATION" -> "📝";
            case "MARK" -> "📊";
            default -> "ℹ️";
        };
    }

    /**
     * Handle notification button hover effect
     */
    @FXML
    private void onNotificationHoverEnter(MouseEvent event) {
        notificationButton.setStyle("-fx-font-size: 18px; -fx-background-color: rgba(52, 152, 219, 0.2); -fx-cursor: hand; -fx-padding: 10px; -fx-background-radius: 5;");
    }

    /**
     * Handle notification button hover exit
     */
    @FXML
    private void onNotificationHoverExit(MouseEvent event) {
        notificationButton.setStyle("-fx-font-size: 18px; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 10px;");
    }

    /**
     * Handle profile button hover effect
     */
    @FXML
    private void onProfileHoverEnter(MouseEvent event) {
        profileButton.setStyle("-fx-font-size: 18px; -fx-background-color: rgba(52, 152, 219, 0.2); -fx-cursor: hand; -fx-padding: 10px; -fx-background-radius: 5;");
    }

    /**
     * Handle profile button hover exit
     */
    @FXML
    private void onProfileHoverExit(MouseEvent event) {
        profileButton.setStyle("-fx-font-size: 18px; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 10px;");
    }

    // Load available PUML diagrams from resources/puml and populate list
    private void loadDesignsList() {
        try {
            java.nio.file.Path pumlDir = java.nio.file.Paths.get("src/main/resources/puml");
            if (java.nio.file.Files.exists(pumlDir)) {
                java.util.List<String> names = java.nio.file.Files.list(pumlDir)
                        .filter(p -> p.toString().endsWith(".puml"))
                        .map(p -> p.getFileName().toString())
                        .sorted()
                        .toList();
                javafx.application.Platform.runLater(() -> {
                    designsListView.getItems().clear();
                    designsListView.getItems().addAll(names);
                });

                // selection handler
                designsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                    if (newV != null) showPUMLSource(newV);
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading PUML diagrams: " + e.getMessage());
        }
    }

    private void showPUMLSource(String filename) {
        try {
            java.nio.file.Path p = java.nio.file.Paths.get("src/main/resources/puml", filename);
            if (java.nio.file.Files.exists(p)) {
                String content = java.nio.file.Files.readString(p);
                javafx.application.Platform.runLater(() -> designViewerArea.setText(content));
            }
        } catch (Exception e) {
            System.err.println("Error reading PUML file: " + e.getMessage());
        }
    }

    /**
     * Create a visual role statistic item with progress representation
     */
    private VBox createRoleStatisticItem(String roleName, int count, double percentage) {
        VBox container = new VBox(5);
        container.setStyle("-fx-padding: 10px;");

        // Header with role name and count
        HBox labelBox = new HBox(10);
        javafx.scene.control.Label roleLabel = new javafx.scene.control.Label(roleName + ":");
        roleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 12px;");
        javafx.scene.control.Label countLabel = new javafx.scene.control.Label(count + " users (" + String.format("%.1f%%", percentage) + ")");
        countLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
        labelBox.getChildren().addAll(roleLabel, countLabel);

        // Progress bar
        ProgressBar progressBar = new ProgressBar(percentage / 100.0);
        progressBar.setPrefHeight(15);
        progressBar.setStyle("-fx-accent: " + getRoleColor(roleName) + ";");

        container.getChildren().addAll(labelBox, progressBar);
        return container;
    }

    /**
     * Get color based on user role
     */
    private String getRoleColor(String roleName) {
        if (roleName == null) return "#95a5a6";
        return switch (roleName.toUpperCase()) {
            case "ADMIN" -> "#e74c3c";
            case "TEACHER" -> "#3498db";
            case "STUDENT" -> "#2ecc71";
            default -> "#95a5a6";
        };
    }

    /**
     * Set the current user for the dashboard
     */
    public void setUser(User user) {
        // Store in session manager
        tn.esprit.educlass.utlis.SessionManager.getInstance().setCurrentUser(user);
    }
}
