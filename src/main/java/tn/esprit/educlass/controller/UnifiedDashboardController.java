package tn.esprit.educlass.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.educlass.enums.EvaluationType;
import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.model.*;
import tn.esprit.educlass.service.*;
import tn.esprit.educlass.utlis.DataSource;
import tn.esprit.educlass.utlis.SessionManager;
import javafx.collections.FXCollections;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class UnifiedDashboardController implements Initializable {

    private static String roleToFrench(Role role) {
        if (role == null) return "";
        return switch (role) {
            case TEACHER -> "Enseignant";
            case STUDENT -> "Étudiant";
            case ADMIN -> "Administrateur";
        };
    }

    private static final String SIDEBAR_BTN = "-fx-padding: 10 15; -fx-alignment: CENTER_LEFT; -fx-background-radius: 5; ";

    // Panes (student = BorderPane, admin/teacher = VBox content only, no sidebar/header)
    @FXML private BorderPane studentPane;
    @FXML private javafx.scene.layout.VBox adminPane;
    @FXML private javafx.scene.layout.VBox teacherPane;

    // Student
    @FXML private Label studentDateLabel;
    @FXML private Label studentTotalCoursesLabel;
    @FXML private Label studentCoursesDetailLabel;
    @FXML private Label studentTotalEvaluationsLabel;
    @FXML private Label studentEvaluationsDetailLabel;
    @FXML private Label studentTotalUsersLabel;
    @FXML private Label studentUsersDetailLabel;
    @FXML private ListView<String> studentCoursesListView;
    @FXML private ListView<String> studentEvaluationsListView;
    @FXML private VBox studentPerformanceBox;
    @FXML private ProgressBar studentEvalSuccessBar;
    @FXML private Label studentEvalSuccessLabel;

    // Admin (content only – stats and chart from DB)
    @FXML private StackPane adminContentPane;
    @FXML private VBox adminDashboardView;
    @FXML private Label adminDateLabel;
    @FXML private Label adminTotalUsersLabel;
    @FXML private Label adminUsersDetailLabel;
    @FXML private Label adminTotalCoursesLabel;
    @FXML private Label adminCoursesDetailLabel;
    @FXML private Label adminTotalEvaluationsLabel;
    @FXML private Label adminEvaluationsDetailLabel;
    @FXML private javafx.scene.chart.PieChart adminUsersRolePieChart;
    @FXML private ListView<String> adminSupervisionListView;

    // Teacher (content only)
    @FXML private StackPane teacherContentPane;
    @FXML private VBox teacherDashboardView;
    @FXML private VBox teacherMyCoursesView;
    @FXML private VBox teacherAddCourseView;
    @FXML private VBox teacherMyLessonsView;
    @FXML private VBox teacherAddLessonView;
    @FXML private VBox teacherMyQuestionsView;
    @FXML private VBox teacherAddQuestionView;
    @FXML private VBox teacherMyStudentsView;
    @FXML private VBox teacherAddStudentView;
    @FXML private VBox teacherGradesView;
    @FXML private VBox teacherEvaluationsView;
    @FXML private VBox teacherSettingsView;
    @FXML private Label teacherStatsCoursesLabel;
    @FXML private Label teacherStatsLessonsLabel;
    @FXML private Label teacherStatsQuestionsLabel;
    @FXML private Label teacherStatsStudentsLabel;
    @FXML private ListView<String> teacherRecentCoursesListView;
    @FXML private ListView<String> teacherRecentLessonsListView;
    @FXML private javafx.scene.chart.PieChart teacherCoursesLevelPieChart;
    @FXML private BarChart<String, Number> teacherEvaluationsTypeBarChart;

    private User currentUser;
    private UserService userService;
    private CourseService courseService;
    private EvaluationService evaluationService;
    private NotificationService notificationService;
    private MarkService markService;
    private QuestionService questionService;
    private AdminService adminService;
    private List<Node> teacherAllViews;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();
        courseService = new CourseService();
        evaluationService = new EvaluationService();
        notificationService = new NotificationService();
        try { markService = new MarkService(); } catch (Exception e) { /* ignore */ }
        questionService = new QuestionService();
        try { adminService = new AdminService(DataSource.getInstance().getCon()); } catch (Exception e) { adminService = null; }

        if (adminContentPane != null && adminDashboardView != null) {
            adminContentPane.getChildren().clear();
            adminContentPane.getChildren().add(adminDashboardView);
        }
        teacherAllViews = Arrays.asList(
                teacherDashboardView, teacherMyCoursesView, teacherAddCourseView,
                teacherMyLessonsView, teacherAddLessonView, teacherMyQuestionsView,
                teacherAddQuestionView, teacherMyStudentsView, teacherAddStudentView,
                teacherGradesView, teacherEvaluationsView, teacherSettingsView);

        applyVisibilityByRole();
        initVisiblePane();
    }

    private void applyVisibilityByRole() {
        User user = currentUser != null ? currentUser : SessionManager.getCurrentUser();
        Role role = user != null ? user.getRole() : Role.STUDENT;
        studentPane.setVisible(role == Role.STUDENT);
        adminPane.setVisible(role == Role.ADMIN);
        teacherPane.setVisible(role == Role.TEACHER);
        if (studentPane.isVisible()) studentPane.toFront();
        else if (adminPane.isVisible()) adminPane.toFront();
        else if (teacherPane.isVisible()) teacherPane.toFront();
    }

    private void initVisiblePane() {
        User user = currentUser != null ? currentUser : SessionManager.getCurrentUser();
        Role role = user != null ? user.getRole() : Role.STUDENT;
        if (role == Role.STUDENT) {
            loadStudentDate();
            loadStudentDashboardData();
        } else if (role == Role.ADMIN) {
            loadAdminDashboardData();
        } else if (role == Role.TEACHER) {
            teacherShowView(teacherDashboardView);
            loadTeacherDashboardData();
            loadTeacherNotifications();
            if (user != null) updateTeacherUserInfo(user);
        }
    }

    private void loadStudentDate() {
        if (studentDateLabel == null) return;
        studentDateLabel.setText(new SimpleDateFormat("EEEE, MMMM d, yyyy").format(new Date()));
    }

    private void loadStudentDashboardData() {
        new Thread(() -> {
            try {
                List<User> allUsers = userService.afficher();
                Platform.runLater(() -> {
                    if (studentTotalUsersLabel != null) studentTotalUsersLabel.setText(String.valueOf(allUsers.size()));
                    if (studentUsersDetailLabel != null) studentUsersDetailLabel.setText(allUsers.stream().filter(u -> u.getStatus() != null && "ACTIVE".equals(u.getStatus().name())).count() + " utilisateurs actifs");
                });
                List<Course> allCourses = courseService.getAllCourses();
                Platform.runLater(() -> {
                    if (studentTotalCoursesLabel != null) studentTotalCoursesLabel.setText(String.valueOf(allCourses.size()));
                    if (studentCoursesDetailLabel != null) studentCoursesDetailLabel.setText(allCourses.size() + " matières publiées");
                });
                List<Evaluation> allEvals = evaluationService.afficher();
                Platform.runLater(() -> {
                    if (studentTotalEvaluationsLabel != null) studentTotalEvaluationsLabel.setText(String.valueOf(allEvals.size()));
                    if (studentEvaluationsDetailLabel != null) studentEvaluationsDetailLabel.setText(allEvals.stream().filter(e -> "IN_PROGRESS".equals(e.getStatus())).count() + " en cours");
                });
                List<String> courseItems = courseService.getAllCourses().stream().limit(5)
                        .map(c -> "📚 " + c.getTitle() + "\n  Level: " + c.getLevel()).collect(Collectors.toList());
                Platform.runLater(() -> {
                    if (studentCoursesListView != null) { studentCoursesListView.getItems().clear(); studentCoursesListView.getItems().addAll(courseItems); }
                });
                List<String> evalItems = evaluationService.afficher().stream()
                        .filter(e -> e.getStatus() != null && "PUBLISHED".equalsIgnoreCase(e.getStatus()))
                        .limit(5)
                        .map(e -> "📝 " + e.getTitle() + (e.getDueDate() != null ? " | " + new SimpleDateFormat("dd/MM/yyyy").format(e.getDueDate()) : ""))
                        .collect(Collectors.toList());
                Platform.runLater(() -> {
                    if (studentEvaluationsListView != null) { studentEvaluationsListView.getItems().clear(); studentEvaluationsListView.getItems().addAll(evalItems); }
                });
                // Performance: only show when we have real data (marks for current student)
                if (markService != null && currentUser != null) {
                    List<Mark> studentMarks = markService.findByStudentId(currentUser.getId());
                    final boolean hasMarks = studentMarks != null && !studentMarks.isEmpty();
                    final double successRate = hasMarks
                            ? Math.min(1.0, studentMarks.stream().mapToDouble(m -> m.getMark() != null ? m.getMark().doubleValue() : 0).average().orElse(0) / 20.0)
                            : 0;
                    Platform.runLater(() -> {
                        if (studentPerformanceBox != null) studentPerformanceBox.setVisible(hasMarks);
                        if (hasMarks && studentEvalSuccessBar != null) {
                            studentEvalSuccessBar.setProgress(successRate);
                            if (studentEvalSuccessLabel != null) studentEvalSuccessLabel.setText(String.format("%.0f%%", successRate * 100));
                        }
                    });
                } else {
                    Platform.runLater(() -> { if (studentPerformanceBox != null) studentPerformanceBox.setVisible(false); });
                }
            } catch (SQLException e) {
                System.err.println("Error loading student dashboard: " + e.getMessage());
            }
        }).start();
    }

    private void updateAdminUserInfo(User user) {
        // No sidebar/header in dashboard – user info shown in main.fxml
    }

    private void updateTeacherUserInfo(User user) {
        // No sidebar/header in dashboard – user info shown in main.fxml
    }

    private void loadAdminDashboardData() {
        Platform.runLater(() -> {
            if (adminDateLabel != null) {
                adminDateLabel.setText(new SimpleDateFormat("EEEE, d MMMM yyyy").format(new Date()));
            }
        });
        new Thread(() -> {
            int usersCount = 0;
            long activeCount = 0;
            Map<String, Long> roleCountsForChart = new LinkedHashMap<>();
            int coursesCount = 0;
            int evaluationsCount = 0;
            List<String> logLines = new ArrayList<>();

            try {
                List<User> users = userService.afficher();
                usersCount = users.size();
                activeCount = users.stream()
                        .filter(u -> u.getStatus() != null && "ACTIVE".equals(u.getStatus().name()))
                        .count();
                // Ensure all roles appear (even 0) so the chart always renders
                roleCountsForChart.put("Administrateur", 0L);
                roleCountsForChart.put("Enseignant", 0L);
                roleCountsForChart.put("Étudiant", 0L);
                for (User u : users) {
                    if (u.getRole() != null) {
                        String label = roleToFrench(u.getRole());
                        roleCountsForChart.merge(label, 1L, Long::sum);
                    }
                }
            } catch (Exception e) {
                System.err.println("Admin dashboard (users): " + e.getMessage());
                roleCountsForChart.put("Administrateur", 0L);
                roleCountsForChart.put("Enseignant", 0L);
                roleCountsForChart.put("Étudiant", 0L);
            }
            try {
                List<Course> courses = courseService.getAllCourses();
                coursesCount = courses.size();
            } catch (Exception e) {
                System.err.println("Admin dashboard (courses): " + e.getMessage());
            }
            try {
                List<Evaluation> evaluations = evaluationService.afficher();
                evaluationsCount = evaluations.size();
            } catch (Exception e) {
                System.err.println("Admin dashboard (evaluations): " + e.getMessage());
            }
            try {
                if (adminService != null) {
                    SimpleDateFormat logFmt = new SimpleDateFormat("dd/MM HH:mm");
                    for (tn.esprit.educlass.model.Supervision log : adminService.getAllLogs()) {
                        if (logLines.size() >= 15) break;
                        logLines.add(logFmt.format(log.getTimestamp() != null ? log.getTimestamp() : new Date()) + " | "
                                + (log.getUser() != null ? log.getUser() : "?") + " – "
                                + (log.getAction() != null ? log.getAction() : "")
                                + (log.getResult() != null && !log.getResult().isEmpty() ? " (" + log.getResult() + ")" : ""));
                    }
                }
            } catch (Exception e) {
                System.err.println("Admin dashboard (institutions/structures/logs): " + e.getMessage());
            }

            final int uCount = usersCount;
            final long aCount = activeCount;
            final int cCount = coursesCount;
            final int eCount = evaluationsCount;
            final Map<String, Long> roleCountsSnapshot = new LinkedHashMap<>(roleCountsForChart);
            Platform.runLater(() -> {
                if (adminTotalUsersLabel != null) adminTotalUsersLabel.setText(String.valueOf(uCount));
                if (adminUsersDetailLabel != null) adminUsersDetailLabel.setText(aCount + " actifs");
                if (adminTotalCoursesLabel != null) adminTotalCoursesLabel.setText(String.valueOf(cCount));
                if (adminCoursesDetailLabel != null) adminCoursesDetailLabel.setText(cCount + " cours");
                if (adminTotalEvaluationsLabel != null) adminTotalEvaluationsLabel.setText(String.valueOf(eCount));
                if (adminEvaluationsDetailLabel != null) adminEvaluationsDetailLabel.setText(eCount + " créées");
                if (adminUsersRolePieChart != null) {
                    javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> pieData = FXCollections.observableArrayList();
                    long total = roleCountsSnapshot.values().stream().filter(v -> v != null).mapToLong(Long::longValue).sum();
                    if (total == 0) {
                        pieData.add(new javafx.scene.chart.PieChart.Data("Aucun utilisateur", 1));
                    } else {
                        for (Map.Entry<String, Long> e : roleCountsSnapshot.entrySet()) {
                            if (e.getValue() != null && e.getValue() > 0) {
                                pieData.add(new javafx.scene.chart.PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()));
                            }
                        }
                    }
                    adminUsersRolePieChart.setData(pieData);
                    adminUsersRolePieChart.setLabelsVisible(true);
                    adminUsersRolePieChart.setLegendVisible(true);
                }
                if (adminSupervisionListView != null) {
                    adminSupervisionListView.getItems().clear();
                    adminSupervisionListView.getItems().addAll(logLines);
                }
            });
        }).start();
    }

    private void teacherShowView(Node view) {
        if (view == null) return;
        for (Node v : teacherAllViews) { if (v != null) v.setVisible(false); }
        view.setVisible(true);
    }

    private void loadTeacherDashboardData() {
        new Thread(() -> {
            int coursesCount = 0;
            int usersCount = 0;
            int lessonsCount = 0;
            int questionsCount = 0;
            List<String> courseItems = new ArrayList<>();
            List<String> lessonItems = new ArrayList<>();
            javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> pieData = FXCollections.observableArrayList();

            try {
                List<Course> courses = courseService.getAllCourses();
                coursesCount = courses.size();
                for (Course c : courses) {
                    if (courseItems.size() >= 10) break;
                    courseItems.add(c.getTitle() + " (Level: " + c.getLevel() + ")");
                }
                Map<Integer, Long> levelDist = courses.stream()
                        .collect(Collectors.groupingBy(Course::getLevel, Collectors.counting()));
                levelDist.forEach((l, count) -> pieData.add(new javafx.scene.chart.PieChart.Data("Niveau " + l, count)));
            } catch (Exception e) {
                System.err.println("Teacher dashboard (courses): " + e.getMessage());
            }
            try {
                List<User> users = userService.afficher();
                usersCount = users.size();
            } catch (Exception e) {
                System.err.println("Teacher dashboard (users): " + e.getMessage());
            }
            try {
                List<Lesson> lessons = courseService.getAllLessons();
                lessonsCount = lessons.size();
                for (int i = 0; i < Math.min(10, lessons.size()); i++) {
                    Lesson le = lessons.get(i);
                    lessonItems.add(le.getTitle() != null ? le.getTitle() : ("Leçon " + (i + 1)));
                }
                if (lessonItems.isEmpty() && lessonsCount == 0) {
                    lessonItems.add("Aucune leçon");
                }
            } catch (Exception e) {
                System.err.println("Teacher dashboard (lessons): " + e.getMessage());
                lessonItems.add("Aucune leçon");
            }
            try {
                List<Question> questions = questionService.getAllQuestions();
                questionsCount = questions.size();
            } catch (Exception e) {
                System.err.println("Teacher dashboard (questions): " + e.getMessage());
            }

            // Evaluations by type (for BarChart) – teacher's evaluations only
            Map<String, Integer> evaluationsByType = new LinkedHashMap<>();
            for (EvaluationType et : EvaluationType.values()) {
                evaluationsByType.put(et.name(), 0);
            }
            try {
                int teacherId = currentUser != null ? currentUser.getId() : -1;
                List<Evaluation> allEvals = evaluationService.afficher();
                for (Evaluation ev : allEvals) {
                    if (ev.getTeacherId() == teacherId && ev.getType() != null) {
                        String key = ev.getType().name();
                        evaluationsByType.merge(key, 1, Integer::sum);
                    }
                }
            } catch (Exception e) {
                System.err.println("Teacher dashboard (evaluations chart): " + e.getMessage());
            }
            final Map<String, Integer> evalTypeData = new LinkedHashMap<>(evaluationsByType);

            final int cCount = coursesCount;
            final int uCount = usersCount;
            final int lCount = lessonsCount;
            final int qCount = questionsCount;
            final List<String> courseList = new ArrayList<>(courseItems);
            final List<String> lessonList = new ArrayList<>(lessonItems);
            Platform.runLater(() -> {
                if (teacherStatsCoursesLabel != null) teacherStatsCoursesLabel.setText(String.valueOf(cCount));
                if (teacherStatsLessonsLabel != null) teacherStatsLessonsLabel.setText(String.valueOf(lCount));
                if (teacherStatsQuestionsLabel != null) teacherStatsQuestionsLabel.setText(String.valueOf(qCount));
                if (teacherStatsStudentsLabel != null) teacherStatsStudentsLabel.setText(String.valueOf(uCount));
                if (teacherRecentCoursesListView != null) {
                    teacherRecentCoursesListView.getItems().clear();
                    teacherRecentCoursesListView.getItems().addAll(courseList);
                }
                if (teacherRecentLessonsListView != null) {
                    teacherRecentLessonsListView.getItems().clear();
                    teacherRecentLessonsListView.getItems().addAll(lessonList);
                }
                if (teacherCoursesLevelPieChart != null) {
                    teacherCoursesLevelPieChart.setData(pieData);
                    teacherCoursesLevelPieChart.setLabelsVisible(true);
                }
                if (teacherEvaluationsTypeBarChart != null) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Évaluations");
                    for (Map.Entry<String, Integer> entry : evalTypeData.entrySet()) {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                    teacherEvaluationsTypeBarChart.getData().clear();
                    teacherEvaluationsTypeBarChart.getData().add(series);
                }
            });
        }).start();
    }

    private void loadTeacherNotifications() {
        // No notification dropdown in dashboard – notifications in main header
    }

    private void adminLoadView(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof UsersController) {
                ((UsersController) ctrl).setCurrentUser(currentUser);
                ((UsersController) ctrl).setMainController(null);
            }
            adminContentPane.getChildren().clear();
            adminContentPane.getChildren().add(view);
        } catch (Exception e) {
            System.err.println("Error loading " + path + ": " + e.getMessage());
        }
    }

    private void teacherLoadView(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();
            teacherContentPane.getChildren().clear();
            teacherContentPane.getChildren().add(view);
        } catch (Exception e) {
            System.err.println("Error loading " + path + ": " + e.getMessage());
        }
    }

    @FXML
    public void hoverEnter(MouseEvent e) {
        Button btn = (Button) e.getSource();
        if (btn.getStyle() != null && btn.getStyle().contains("#3498db")) return;
        btn.setStyle(SIDEBAR_BTN + "-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-cursor: hand;");
    }

    @FXML
    public void hoverExit(MouseEvent e) {
        Button btn = (Button) e.getSource();
        if (btn.getStyle() != null && btn.getStyle().contains("#3498db")) return;
        btn.setStyle(SIDEBAR_BTN + "-fx-background-color: transparent; -fx-text-fill: white;");
    }

    @FXML public void adminSwitchToDashboard() { adminContentPane.getChildren().setAll(adminDashboardView); }
    @FXML public void adminSwitchToUsers() { adminLoadView("/view/users.fxml"); }
    @FXML public void adminSwitchToClasses() { adminLoadView("/view/school_classes.fxml"); }
    @FXML public void adminSwitchToSupervision() { adminLoadView("/view/supervision.fxml"); }

    private boolean hasAdminAccess() {
        User user = currentUser != null ? currentUser : SessionManager.getCurrentUser();
        return user != null && user.getRole() == Role.ADMIN;
    }

    @FXML
    public void handleQuickAccessUsers(ActionEvent event) {
        if (!hasAdminAccess()) return;
        adminSwitchToUsers();
    }

    @FXML
    public void handleQuickAccessClasses(ActionEvent event) {
        if (!hasAdminAccess()) return;
        adminSwitchToClasses();
    }

    @FXML
    public void handleQuickAccessSupervision(ActionEvent event) {
        if (!hasAdminAccess()) return;
        adminSwitchToSupervision();
    }


    @FXML
    public void adminHandleLogout(ActionEvent e) {
        try {
            SessionManager.clear();
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(FXMLLoader.load(getClass().getResource("/view/login.fxml"))));
            stage.setTitle("Connexion");
            stage.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    public void teacherSwitchToDashboard(ActionEvent e) {
        teacherContentPane.getChildren().setAll(teacherDashboardView);
        teacherShowView(teacherDashboardView);
        loadTeacherDashboardData();
    }

    @FXML public void teacherSwitchToMyCourses(ActionEvent e) { teacherLoadView("/view/courses.fxml"); }
    @FXML public void teacherSwitchToMyLessons(ActionEvent e) { teacherShowView(teacherMyLessonsView); }
    @FXML public void teacherSwitchToEvaluations(ActionEvent e) { teacherLoadView("/view/evaluations.fxml"); }
    @FXML public void teacherSwitchToMyStudents(ActionEvent e) { teacherShowView(teacherMyStudentsView); }
    @FXML public void teacherSwitchToGrades(ActionEvent e) { teacherLoadView("/view/marks.fxml"); }
    @FXML public void teacherSwitchToSettings(ActionEvent e) { teacherLoadView("/view/settings.fxml"); }

    @FXML
    public void teacherToggleNotifications(ActionEvent e) {
        // No notification dropdown in dashboard
    }

    @FXML
    public void teacherHandleLogout(ActionEvent e) {
        try {
            SessionManager.clear();
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(FXMLLoader.load(getClass().getResource("/view/login.fxml"))));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void setUser(User user) {
        this.currentUser = user;
        SessionManager.setCurrentUser(user);
        applyVisibilityByRole();
        if (user.getRole() == Role.ADMIN) loadAdminDashboardData();
        else if (user.getRole() == Role.TEACHER) { updateTeacherUserInfo(user); loadTeacherDashboardData(); loadTeacherNotifications(); }
        else loadStudentDashboardData();
    }
}
