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
import tn.esprit.educlass.utlis.SessionManager;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML
    private Label dateLabel;
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label usersDetailLabel;
    @FXML
    private Label totalCoursesLabel;
    @FXML
    private Label coursesDetailLabel;
    @FXML
    private Label totalEvaluationsLabel;
    @FXML
    private Label evaluationsDetailLabel;
    @FXML
    private Label notificationsLabel;
    @FXML
    private Label notificationsDetailLabel;
    @FXML
    private ListView<String> coursesListView;
    @FXML
    private ListView<String> evaluationsListView;
    @FXML
    private ListView<String> notificationsListView;
    @FXML
    private VBox rolesStatisticsBox;
    @FXML
    private Button notificationButton;
    @FXML
    private Button profileButton;
    @FXML
    private Label notificationCountBadge;
    @FXML
    private javafx.scene.chart.PieChart rolesPieChart;
    @FXML
    private javafx.scene.control.ListView<String> designsListView;
    @FXML
    private javafx.scene.control.TextArea designViewerArea;

    @FXML
    private ProgressBar courseCompletionBar;
    @FXML
    private Label courseCompletionLabel;
    @FXML
    private ProgressBar evalSuccessBar;
    @FXML
    private Label evalSuccessLabel;
    @FXML
    private ProgressBar attendanceBar;
    @FXML
    private Label attendanceLabel;

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

    private void initializeServices() {
        try {
            userService = new UserService();
            courseService = new CourseService();
            evaluationService = new EvaluationService();
            notificationService = new NotificationService();
            markService = new MarkService();
            adminService = new AdminService(DataSource.getInstance().getCon());
        } catch (Exception e) {
            System.err.println("Error initializing services: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        String currentDate = sdf.format(new Date());
        if (dateLabel != null)
            dateLabel.setText(currentDate);
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                loadUserStatistics();
                loadCourseStatistics();
                loadEvaluationStatistics();
                loadNotificationStatistics();
                loadRecentCourses();
                loadRecentEvaluations();
                loadUserRolesDistribution();
                loadRecentNotifications();
                loadPerformanceStatistics();
            } catch (SQLException e) {
                System.err.println("Error loading dashboard data: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void loadUserStatistics() throws SQLException {
        List<User> allUsers = userService.afficher();
        int totalUsers = allUsers.size();
        long activeUsers = allUsers.stream()
                .filter(u -> u.getStatus() != null && u.getStatus().name().equals("ACTIVE"))
                .count();

        Platform.runLater(() -> {
            if (totalUsersLabel != null)
                totalUsersLabel.setText(String.valueOf(totalUsers));
            if (usersDetailLabel != null)
                usersDetailLabel.setText(activeUsers + " utilisateurs actifs");
        });
    }

    private void loadCourseStatistics() throws SQLException {
        List<Course> allCourses = courseService.getAllCourses();
        int totalCourses = allCourses.size();

        Platform.runLater(() -> {
            if (totalCoursesLabel != null)
                totalCoursesLabel.setText(String.valueOf(totalCourses));
            if (coursesDetailLabel != null)
                coursesDetailLabel.setText(totalCourses + " matières publiées");
        });
    }

    private void loadEvaluationStatistics() throws SQLException {
        List<Evaluation> allEvaluations = evaluationService.afficher();
        int totalEvaluations = allEvaluations.size();
        long inProgress = allEvaluations.stream()
                .filter(e -> e.getStatus() != null && e.getStatus().equals("IN_PROGRESS"))
                .count();

        Platform.runLater(() -> {
            if (totalEvaluationsLabel != null)
                totalEvaluationsLabel.setText(String.valueOf(totalEvaluations));
            if (evaluationsDetailLabel != null)
                evaluationsDetailLabel.setText(inProgress + " en cours");
        });
    }

    private void loadNotificationStatistics() throws SQLException {
        try {
            List<Notification> allNotifications = notificationService.getAllNotifications();
            int unreadCount = (int) allNotifications.stream()
                    .filter(n -> !n.isRead())
                    .count();

            Platform.runLater(() -> {
                if (notificationsLabel != null)
                    notificationsLabel.setText(String.valueOf(allNotifications.size()));
                if (notificationsDetailLabel != null)
                    notificationsDetailLabel.setText(unreadCount + " non lus");
                if (notificationCountBadge != null)
                    notificationCountBadge.setText(String.valueOf(unreadCount));
            });
        } catch (Exception e) {
            System.err.println("Error loading notifications: " + e.getMessage());
            Platform.runLater(() -> {
                if (notificationsLabel != null) notificationsLabel.setText("0");
                if (notificationsDetailLabel != null) notificationsDetailLabel.setText("0 non lus");
                if (notificationCountBadge != null) notificationCountBadge.setText("0");
            });
        }
    }

    private void loadRecentCourses() throws SQLException {
        List<Course> allCourses = courseService.getAllCourses();
        List<String> courseItems = allCourses.stream()
                .limit(5)
                .map(course -> {
                    String chapters = course.getChapters() != null ? " (" + course.getChapters().size() + " chapters)" : "";
                    String description = course.getDescription() != null
                            ? course.getDescription().substring(0, Math.min(50, course.getDescription().length())) + "..."
                            : "No description";
                    return "📚 " + course.getTitle() + chapters +
                            "\n  Level: " + course.getLevel() + " | " + description;
                })
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            if (coursesListView != null) {
                coursesListView.getItems().clear();
                coursesListView.getItems().addAll(courseItems);
            }
        });
    }

    private void loadRecentEvaluations() throws SQLException {
        List<Evaluation> allEvaluations = evaluationService.afficher();
        List<String> evaluationItems = allEvaluations.stream()
                .limit(5)
                .map(eval -> {
                    String dueDateStr = eval.getDueDate() != null
                            ? " (Due: " + new SimpleDateFormat("MMM dd, yyyy").format(eval.getDueDate()) + ")"
                            : "";
                    return "📝 " + eval.getTitle() + dueDateStr +
                            "\n  Type: " + (eval.getType() != null ? eval.getType().name() : "N/A") +
                            " | Status: " + eval.getStatus() +
                            " | Duration: " + eval.getDuration() + " min";
                })
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            if (evaluationsListView != null) {
                evaluationsListView.getItems().clear();
                evaluationsListView.getItems().addAll(evaluationItems);
            }
        });
    }

    private void loadUserRolesDistribution() throws SQLException {
        List<User> allUsers = userService.afficher();
        Map<Role, Long> roleDistribution = allUsers.stream()
                .filter(u -> u.getRole() != null)
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));

        int totalUsers = allUsers.size();

        Platform.runLater(() -> {
            if (rolesStatisticsBox != null) {
                rolesStatisticsBox.getChildren().clear();
                roleDistribution.forEach((role, count) -> {
                    double percentage = totalUsers > 0 ? (count * 100.0) / totalUsers : 0;
                    VBox roleItem = createRoleStatisticItem(role.name(), count.intValue(), percentage);
                    rolesStatisticsBox.getChildren().add(roleItem);
                });
            }
            if (rolesPieChart != null) {
                javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> pieData =
                        javafx.collections.FXCollections.observableArrayList();
                roleDistribution.forEach((role, count) ->
                        pieData.add(new javafx.scene.chart.PieChart.Data(role.name(), count)));
                rolesPieChart.setData(pieData);
                rolesPieChart.setLabelsVisible(true);
            }
            loadDesignsList();
        });
    }

    private void loadRecentNotifications() throws SQLException {
        try {
            List<Notification> allNotifications = notificationService.getAllNotifications();
            List<String> notificationItems = allNotifications.stream()
                    .limit(5)
                    .map(notif -> {
                        String readStatus = notif.isRead() ? "✓" : "●";
                        String typeIcon = notif.getType() != null ? getNotificationIcon(notif.getType().name()) : "ℹ️";
                        return typeIcon + " " + readStatus + " " + notif.getTitle() +
                                "\n  " + (notif.getMessage() != null
                                        ? notif.getMessage().substring(0, Math.min(40, notif.getMessage().length())) + "..."
                                        : "");
                    })
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                if (notificationsListView != null) {
                    notificationsListView.getItems().clear();
                    notificationsListView.getItems().addAll(notificationItems);
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading notifications: " + e.getMessage());
            Platform.runLater(() -> {
                if (notificationsListView != null) {
                    notificationsListView.getItems().clear();
                    notificationsListView.getItems().add("📢 Bienvenue sur EduClass");
                }
            });
        }
    }

    private String getNotificationIcon(String type) {
        return switch (type.toUpperCase()) {
            case "SYSTEM" -> "⚙️";
            case "COURSE" -> "📚";
            case "EVALUATION" -> "📝";
            case "MARK" -> "📊";
            default -> "ℹ️";
        };
    }

    @FXML
    private void onNotificationHoverEnter(MouseEvent event) {
        if (notificationButton != null)
            notificationButton.setStyle("-fx-font-size: 18px; -fx-background-color: rgba(52, 152, 219, 0.2); -fx-cursor: hand; -fx-padding: 10px; -fx-background-radius: 5;");
    }

    @FXML
    private void onNotificationHoverExit(MouseEvent event) {
        if (notificationButton != null)
            notificationButton.setStyle("-fx-font-size: 18px; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 10px;");
    }

    @FXML
    private void onProfileHoverEnter(MouseEvent event) {
        if (profileButton != null)
            profileButton.setStyle("-fx-font-size: 18px; -fx-background-color: rgba(52, 152, 219, 0.2); -fx-cursor: hand; -fx-padding: 10px; -fx-background-radius: 5;");
    }

    @FXML
    private void onProfileHoverExit(MouseEvent event) {
        if (profileButton != null)
            profileButton.setStyle("-fx-font-size: 18px; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 10px;");
    }

    private void loadDesignsList() {
        if (designsListView == null) return;
        try {
            java.nio.file.Path pumlDir = java.nio.file.Paths.get("src/main/resources/puml");
            if (java.nio.file.Files.exists(pumlDir)) {
                java.util.List<String> names = java.nio.file.Files.list(pumlDir)
                        .filter(p -> p.toString().endsWith(".puml"))
                        .map(p -> p.getFileName().toString())
                        .sorted()
                        .toList();
                Platform.runLater(() -> {
                    designsListView.getItems().clear();
                    designsListView.getItems().addAll(names);
                });
                designsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                    if (newV != null) showPUMLSource(newV);
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading PUML diagrams: " + e.getMessage());
        }
    }

    private void showPUMLSource(String filename) {
        if (designViewerArea == null) return;
        try {
            java.nio.file.Path p = java.nio.file.Paths.get("src/main/resources/puml", filename);
            if (java.nio.file.Files.exists(p)) {
                String content = java.nio.file.Files.readString(p);
                Platform.runLater(() -> designViewerArea.setText(content));
            }
        } catch (Exception e) {
            System.err.println("Error reading PUML file: " + e.getMessage());
        }
    }

    private VBox createRoleStatisticItem(String roleName, int count, double percentage) {
        VBox container = new VBox(5);
        container.setStyle("-fx-padding: 10px;");
        HBox labelBox = new HBox(10);
        Label roleLabel = new Label(roleName + ":");
        roleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 12px;");
        Label countLabel = new Label(count + " users (" + String.format("%.1f%%", percentage) + ")");
        countLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
        labelBox.getChildren().addAll(roleLabel, countLabel);
        ProgressBar progressBar = new ProgressBar(percentage / 100.0);
        progressBar.setPrefHeight(15);
        progressBar.setStyle("-fx-accent: " + getRoleColor(roleName) + ";");
        container.getChildren().addAll(labelBox, progressBar);
        return container;
    }

    private String getRoleColor(String roleName) {
        if (roleName == null) return "#95a5a6";
        return switch (roleName.toUpperCase()) {
            case "ADMIN" -> "#e74c3c";
            case "TEACHER" -> "#3498db";
            case "STUDENT" -> "#2ecc71";
            default -> "#95a5a6";
        };
    }

    private void loadPerformanceStatistics() {
        try {
            List<Mark> allMarks = markService.afficher();
            double avgMark = 0;
            if (!allMarks.isEmpty()) {
                avgMark = allMarks.stream()
                        .mapToDouble(m -> m.getMark().doubleValue())
                        .average()
                        .orElse(0.0);
            }
            double successRate = Math.min(1.0, avgMark / 20.0);

            List<Evaluation> allEvals = evaluationService.afficher();
            double completion = 0.85;
            if (!allEvals.isEmpty()) {
                long finished = allEvals.stream().filter(e -> "FINISHED".equals(e.getStatus())).count();
                completion = (double) finished / allEvals.size();
                if (completion == 0) completion = 0.5;
            }

            final double finalSuccess = successRate;
            final double finalCompletion = completion;

            Platform.runLater(() -> {
                if (evalSuccessBar != null) {
                    evalSuccessBar.setProgress(finalSuccess);
                    if (evalSuccessLabel != null) evalSuccessLabel.setText(String.format("%.0f%%", finalSuccess * 100));
                }
                if (courseCompletionBar != null) {
                    courseCompletionBar.setProgress(finalCompletion);
                    if (courseCompletionLabel != null) courseCompletionLabel.setText(String.format("%.0f%%", finalCompletion * 100));
                }
                if (attendanceBar != null) {
                    attendanceBar.setProgress(0.92);
                    if (attendanceLabel != null) attendanceLabel.setText("92%");
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading performance stats: " + e.getMessage());
        }
    }

    public void setUser(User user) {
        SessionManager.setCurrentUser(user);
    }
}
