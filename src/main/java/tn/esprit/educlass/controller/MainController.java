package tn.esprit.educlass.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.util.Duration;
import tn.esprit.educlass.enums.NotificationType;
import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.model.Notification;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.service.NotificationService;
import tn.esprit.educlass.utlis.SessionManager;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {

    private static String roleToFrench(tn.esprit.educlass.enums.Role role) {
        if (role == null) return "";
        return switch (role) {
            case TEACHER -> "Enseignant";
            case STUDENT -> "Étudiant";
            case ADMIN -> "Administrateur";
        };
    }

    @FXML private Label initialsLabel;
    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private StackPane contentPane;
    @FXML private Button usersButton;
    @FXML private Button classesButton;
    @FXML private Button notificationButton;
    @FXML private Label notificationBadge;

    private User user;
    private NotificationService notificationService;
    private Popup notificationPopup;
    private Timeline pollTimeline;

    // Refresh user from database and update sidebar
    public void refreshUserFromDb() {
        try {
            tn.esprit.educlass.service.UserService userService = new tn.esprit.educlass.service.UserService();
            User refreshed = userService.findById(user.getId());
            if (refreshed != null) {
                updateSidebar(refreshed);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update sidebar display with given user
    private void updateSidebar(User user) {
        this.user = user;
        SessionManager.setCurrentUser(user);
        nameLabel.setText(user.getFullName());
        roleLabel.setText(roleToFrench(user.getRole()));
        String initials = user.getFirstName().substring(0, 1).toUpperCase() +
                user.getLastName().substring(0, 1).toUpperCase();
        initialsLabel.setText(initials);

        // Show/hide admin-only buttons
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (usersButton != null) {
            usersButton.setVisible(isAdmin);
            usersButton.setManaged(isAdmin);
        }
        if (classesButton != null) {
            classesButton.setVisible(isAdmin);
            classesButton.setManaged(isAdmin);
        }
    }

    // Called after login to set user and load default section
    public void setUser(User user) {
        updateSidebar(user);
        initNotifications();
        showDashboard(null);
    }

    /* =====================================================
       NOTIFICATIONS
       ===================================================== */

    private void initNotifications() {
        notificationService = new NotificationService();
        updateBadge();
        startPolling();
    }

    private void startPolling() {
        if (pollTimeline != null) {
            pollTimeline.stop();
        }
        pollTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> updateBadge()));
        pollTimeline.setCycleCount(Timeline.INDEFINITE);
        pollTimeline.play();
    }

    private void updateBadge() {
        if (user == null) return;
        try {
            int count = notificationService.getUnreadCountByUser(user.getId());
            Platform.runLater(() -> {
                if (count > 0) {
                    notificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                    notificationBadge.setVisible(true);
                    notificationBadge.setManaged(true);
                } else {
                    notificationBadge.setVisible(false);
                    notificationBadge.setManaged(false);
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleNotifications(ActionEvent event) {
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.hide();
            return;
        }
        showNotificationPanel();
    }

    private void showNotificationPanel() {
        if (user == null) return;

        notificationPopup = new Popup();
        notificationPopup.setAutoHide(true);

        // Main container
        VBox panel = new VBox(0);
        panel.setPrefWidth(360);
        panel.setMaxHeight(480);
        panel.setStyle(
            "-fx-background-color: white; -fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 16, 0, 0, 4);" +
            "-fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-border-width: 1;"
        );

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle("-fx-background-color: #34495e; -fx-background-radius: 10 10 0 0;");

        Label headerLabel = new Label("🔔 Notifications");
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        HBox.setHgrow(headerLabel, Priority.ALWAYS);

        Button markAllBtn = new Button("Mark all read");
        markAllBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-font-size: 11px;" +
            "-fx-cursor: hand; -fx-padding: 2 8; -fx-border-color: #3498db; -fx-border-radius: 4;" +
            "-fx-background-radius: 4;"
        );
        markAllBtn.setOnAction(e -> {
            try {
                notificationService.markAllAsReadByUser(user.getId());
                updateBadge();
                notificationPopup.hide();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        header.getChildren().addAll(headerLabel, markAllBtn);

        // Notification list
        VBox listContainer = new VBox(0);
        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(380);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-width: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        try {
            List<Notification> notifications = notificationService.getNotificationsByUser(user.getId());

            if (notifications.isEmpty()) {
                Label emptyLabel = new Label("No notifications yet");
                emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 13px; -fx-padding: 40 0;");
                emptyLabel.setAlignment(Pos.CENTER);
                emptyLabel.setMaxWidth(Double.MAX_VALUE);
                listContainer.getChildren().add(emptyLabel);
            } else {
                for (Notification n : notifications) {
                    listContainer.getChildren().add(createNotificationRow(n));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        panel.getChildren().addAll(header, scrollPane);
        notificationPopup.getContent().add(panel);

        // Position below the bell button
        javafx.geometry.Bounds bounds = notificationButton.localToScreen(notificationButton.getBoundsInLocal());
        if (bounds != null) {
            notificationPopup.show(notificationButton,
                bounds.getMaxX() - 360,
                bounds.getMaxY() + 6);
        }
    }

    private HBox createNotificationRow(Notification notification) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(javafx.scene.Cursor.HAND);

        boolean unread = !notification.isRead();
        row.setStyle(unread
            ? "-fx-background-color: #eaf2fb; -fx-border-color: transparent transparent #ecf0f1 transparent; -fx-border-width: 0 0 1 0;"
            : "-fx-background-color: white; -fx-border-color: transparent transparent #ecf0f1 transparent; -fx-border-width: 0 0 1 0;"
        );

        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle(
            "-fx-background-color: #f0f4f8; -fx-border-color: transparent transparent #ecf0f1 transparent; -fx-border-width: 0 0 1 0;"
        ));
        row.setOnMouseExited(e -> row.setStyle(unread
            ? "-fx-background-color: #eaf2fb; -fx-border-color: transparent transparent #ecf0f1 transparent; -fx-border-width: 0 0 1 0;"
            : "-fx-background-color: white; -fx-border-color: transparent transparent #ecf0f1 transparent; -fx-border-width: 0 0 1 0;"
        ));

        // Type icon
        Label icon = new Label(getTypeIcon(notification.getType()));
        icon.setStyle("-fx-font-size: 22px; -fx-min-width: 32; -fx-alignment: center;");

        // Text content
        VBox textBox = new VBox(2);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label titleLabel = new Label(notification.getTitle());
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: " + (unread ? "bold" : "normal") +
                           "; -fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);

        String msgText = notification.getMessage();
        if (msgText != null && msgText.length() > 80) {
            msgText = msgText.substring(0, 80) + "…";
        }
        Label messageLabel = new Label(msgText);
        messageLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        messageLabel.setWrapText(true);

        Label timeLabel = new Label(formatRelativeTime(notification.getCreatedAt()));
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6;");

        textBox.getChildren().addAll(titleLabel, messageLabel, timeLabel);

        // Unread dot
        if (unread) {
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: #3498db; -fx-font-size: 10px; -fx-padding: 0 0 0 4;");
            row.getChildren().addAll(icon, textBox, dot);
        } else {
            row.getChildren().addAll(icon, textBox);
        }

        // Click to mark as read and navigate to the related area
        row.setOnMouseClicked(e -> {
            if (unread) {
                try {
                    notificationService.markAsRead(notification.getId());
                    notification.setRead(true);
                    updateBadge();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            if (notificationPopup != null) {
                notificationPopup.hide();
            }
            navigateFromNotification(notification);
        });

        return row;
    }

    private void navigateFromNotification(Notification notification) {
        if (notification == null) return;
        NotificationType type = notification.getType();

        if (type == NotificationType.CHAT) {
            Object controller = loadSectionInternal("/view/chat.fxml");
            if (controller instanceof ChatController chatController) {
                String senderName = extractChatSenderName(notification);
                if (senderName != null) {
                    chatController.focusConversationByOtherUserName(senderName);
                }
            }
            return;
        }

        if (type == NotificationType.EVALUATION || type == NotificationType.MARK) {
            Object controller = loadSectionInternal("/view/evaluations.fxml");
            if (controller instanceof EvaluationsViewController evaluationsController) {
                String evalTitle = extractEvaluationTitle(notification);
                if (evalTitle != null) {
                    evaluationsController.focusEvaluationByTitle(evalTitle);
                }
            }
            return;
        }

        if (type == NotificationType.COURSE) {
            loadSection("/view/courses.fxml");
            return;
        }

        loadSection("/view/dashboard.fxml");
    }

    private String extractChatSenderName(Notification notification) {
        if (notification == null || notification.getTitle() == null) return null;
        String title = notification.getTitle().trim();
        String prefix = "Message from ";
        if (title.startsWith(prefix) && title.length() > prefix.length()) {
            return title.substring(prefix.length()).trim();
        }
        return null;
    }

    private String extractEvaluationTitle(Notification notification) {
        if (notification == null || notification.getMessage() == null) return null;
        String message = notification.getMessage().trim();
        if (message.isEmpty()) return null;

        Matcher quoted = Pattern.compile("\"([^\"]+)\"").matcher(message);
        if (quoted.find()) {
            return quoted.group(1).trim();
        }

        String publishedMarker = " is now available";
        int idx = message.indexOf(publishedMarker);
        if (idx > 0) {
            return message.substring(0, idx).trim();
        }
        return null;
    }

    private String getTypeIcon(NotificationType type) {
        if (type == null) return "🔔";
        return switch (type) {
            case SYSTEM -> "⚙️";
            case COURSE -> "📚";
            case EVALUATION -> "📝";
            case CHAT -> "💬";
            case MARK -> "📊";
        };
    }

    private String formatRelativeTime(Date date) {
        if (date == null) return "";
        long diff = System.currentTimeMillis() - date.getTime();
        if (diff < 0) diff = 0;

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        if (hours < 24) return hours + "h ago";
        if (days < 7) return days + "d ago";
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    @FXML
    private void hoverEnter(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        if (btn.getStyle().contains("#3498db")) return; // keep active (dashboard) button as-is
        btn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-cursor: hand;");
    }

    @FXML
    private void hoverExit(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        if (btn.getStyle().contains("#3498db")) return; // keep active (dashboard) button blue
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        loadSection("/view/dashboard.fxml");
    }

    @FXML
    private void showCours(ActionEvent event) {
        loadSection("/view/courses.fxml");
    }

    @FXML
    private void showSettings(ActionEvent event) {
        loadSection("/view/settings.fxml");
    }

    @FXML
    private void showMarks(ActionEvent event) {
        loadSection("/view/marks.fxml");
    }

    @FXML
    private void showUsers(ActionEvent event) {
        if (user == null || user.getRole() != Role.ADMIN) {
            return;
        }
        loadSection("/view/users.fxml");
    }

    @FXML
    private void showSupervision(ActionEvent event) {
        if (user == null || user.getRole() != Role.ADMIN) {
            return;
        }
        loadSection("/view/supervision.fxml");
    }

    @FXML
    private void showEvaluations(ActionEvent event) {
        loadSection("/view/evaluations.fxml");
    }

    @FXML
    private void showChat(ActionEvent event) {
        loadSection("/view/chat.fxml");
    }

    @FXML
    private void showSchoolClasses(ActionEvent event) {
        if (user == null || user.getRole() != Role.ADMIN) {
            return;
        }
        loadSection("/view/school_classes.fxml");
    }

    private void loadSection(String fxmlPath) {
        loadSectionInternal(fxmlPath);
    }

    private Object loadSectionInternal(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            Object controller = loader.getController();
            // Pass user and MainController reference to SettingsController
            if (controller instanceof SettingsController) {
                ((SettingsController) controller).setUser(user);
                ((SettingsController) controller).setMainController(this);
            }
            // Pass current user and MainController reference to UsersController
            if (controller instanceof UsersController) {
                ((UsersController) controller).setCurrentUser(user);
                ((UsersController) controller).setMainController(this);
            }
            // Pass current user to dashboard controller (unified: student / admin / teacher)
            if (controller instanceof tn.esprit.educlass.controller.UnifiedDashboardController && user != null) {
                ((tn.esprit.educlass.controller.UnifiedDashboardController) controller).setUser(user);
            }
            // Legacy: pass user to standalone DashboardController if ever used
            if (controller instanceof DashboardController && user != null) {
                ((DashboardController) controller).setUser(user);
            }
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Stop polling
            if (pollTimeline != null) {
                pollTimeline.stop();
                pollTimeline = null;
            }
            if (notificationPopup != null) {
                notificationPopup.hide();
                notificationPopup = null;
            }
            SessionManager.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
