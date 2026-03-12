package tn.esprit.educlass.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.educlass.model.Conversation;
import tn.esprit.educlass.model.Message;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.service.ChatService;
import tn.esprit.educlass.service.UserService;
import tn.esprit.educlass.utlis.SessionManager;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ChatController {

    @FXML private ListView<Conversation> conversationListView;
    @FXML private TextField searchField;
    @FXML private VBox messagesContainer;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private TextField messageField;
    @FXML private HBox inputArea;
    @FXML private HBox chatHeader;
    @FXML private Label chatPartnerLabel;
    @FXML private VBox emptyState;
    @FXML private Button newConversationBtn;
    @FXML private Button sendBtn;

    private ChatService chatService;
    private UserService userService;
    private Conversation selectedConversation;
    private ObservableList<Conversation> allConversations;

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        chatService = new ChatService();
        userService = new UserService();
        allConversations = FXCollections.observableArrayList();

        setupConversationList();
        loadConversations();
        setupSearch();
    }

    /* =====================================================
       CONVERSATION LIST
       ===================================================== */

    private void setupConversationList() {
        conversationListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation conv, boolean empty) {
                super.updateItem(conv, empty);
                if (empty || conv == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: #2c3e50;");
                } else {
                    VBox cell = new VBox(2);
                    cell.setPadding(new Insets(8, 12, 8, 12));

                    // Name row
                    HBox nameRow = new HBox(8);
                    nameRow.setAlignment(Pos.CENTER_LEFT);

                    // Avatar circle
                    String initials = getInitials(conv.getOtherUserName());
                    Label avatar = new Label(initials);
                    avatar.setStyle(
                        "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-min-width: 36; -fx-min-height: 36; -fx-max-width: 36; -fx-max-height: 36; " +
                        "-fx-alignment: center; -fx-background-radius: 18;"
                    );

                    VBox textBox = new VBox(2);
                    HBox.setHgrow(textBox, Priority.ALWAYS);

                    Label nameLabel = new Label(conv.getOtherUserName());
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

                    Label previewLabel = new Label(
                        conv.getLastMessage() != null ?
                            (conv.getLastMessage().length() > 35 ? conv.getLastMessage().substring(0, 35) + "..." : conv.getLastMessage())
                            : "Aucun message"
                    );
                    previewLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");

                    textBox.getChildren().addAll(nameLabel, previewLabel);
                    nameRow.getChildren().addAll(avatar, textBox);

                    // Unread badge
                    if (conv.getUnreadCount() > 0) {
                        Label badge = new Label(String.valueOf(conv.getUnreadCount()));
                        badge.setStyle(
                            "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; " +
                            "-fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 10;"
                        );
                        nameRow.getChildren().add(badge);
                    }

                    cell.getChildren().add(nameRow);
                    setGraphic(cell);
                    setText(null);

                    // Hover and selection styles
                    boolean isSelected = getListView().getSelectionModel().getSelectedItem() == conv;
                    setStyle(isSelected
                        ? "-fx-background-color: #3d566e; -fx-cursor: hand;"
                        : "-fx-background-color: #2c3e50; -fx-cursor: hand;"
                    );
                    setOnMouseEntered(e -> {
                        if (getListView().getSelectionModel().getSelectedItem() != conv) {
                            setStyle("-fx-background-color: #34495e; -fx-cursor: hand;");
                        }
                    });
                    setOnMouseExited(e -> {
                        if (getListView().getSelectionModel().getSelectedItem() != conv) {
                            setStyle("-fx-background-color: #2c3e50; -fx-cursor: hand;");
                        }
                    });
                }
            }
        });

        conversationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                openConversation(newVal);
            }
        });
    }

    private void loadConversations() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        try {
            List<Conversation> conversations = chatService.getConversationsForUser(currentUser.getId());
            allConversations.setAll(conversations);
            conversationListView.setItems(allConversations);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                conversationListView.setItems(allConversations);
            } else {
                String lower = newVal.toLowerCase();
                ObservableList<Conversation> filtered = allConversations.filtered(
                    c -> c.getOtherUserName() != null && c.getOtherUserName().toLowerCase().contains(lower)
                );
                conversationListView.setItems(filtered);
            }
        });
    }

    /* =====================================================
       OPEN CONVERSATION & MESSAGES
       ===================================================== */

    private void openConversation(Conversation conv) {
        selectedConversation = conv;
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        // Show chat UI
        chatHeader.setVisible(true);
        chatHeader.setManaged(true);
        inputArea.setVisible(true);
        inputArea.setManaged(true);
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        chatPartnerLabel.setText(conv.getOtherUserName());

        // Mark messages as read
        try {
            chatService.markMessagesAsRead(conv.getId(), currentUser.getId());
            conv.setUnreadCount(0);
            conversationListView.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Load messages
        loadMessages(conv.getId());
        messageField.requestFocus();
    }

    private void loadMessages(int conversationId) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        messagesContainer.getChildren().clear();

        try {
            List<Message> messages = chatService.getMessages(conversationId);

            if (messages.isEmpty()) {
                Label empty = new Label("Aucun message. Dites bonjour ! 👋");
                empty.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 13px;");
                messagesContainer.setAlignment(Pos.CENTER);
                messagesContainer.getChildren().add(empty);
            } else {
                messagesContainer.setAlignment(Pos.TOP_LEFT);
                for (Message msg : messages) {
                    messagesContainer.getChildren().add(createMessageBubble(msg, currentUser.getId()));
                }
            }

            // Scroll to bottom
            Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createMessageBubble(Message msg, int currentUserId) {
        boolean isMine = msg.getSenderId() == currentUserId;

        VBox bubble = new VBox(2);
        bubble.setMaxWidth(350);
        bubble.setPadding(new Insets(8, 12, 8, 12));

        Label contentLabel = new Label(msg.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (isMine ? "white" : "#2c3e50") + ";");

        String timeStr = msg.getCreatedAt() != null ? TIME_FORMAT.format(msg.getCreatedAt()) : "";
        Label timeLabel = new Label(timeStr);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (isMine ? "#d5e8f0" : "#95a5a6") + ";");

        bubble.getChildren().addAll(contentLabel, timeLabel);

        if (isMine) {
            bubble.setStyle(
                "-fx-background-color: #3498db; -fx-background-radius: 15 15 3 15;"
            );
        } else {
            bubble.setStyle(
                "-fx-background-color: white; -fx-background-radius: 15 15 15 3; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 1);"
            );
        }

        HBox row = new HBox();
        row.setPadding(new Insets(2, 0, 2, 0));
        row.getChildren().add(bubble);
        row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        return row;
    }

    /* =====================================================
       SEND MESSAGE
       ===================================================== */

    @FXML
    private void onSendMessage() {
        if (selectedConversation == null) return;
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        String content = messageField.getText();
        if (content == null || content.isBlank()) return;

        try {
            Message sent = chatService.sendMessage(selectedConversation.getId(), currentUser.getId(), content);
            sent.setSenderName(currentUser.getFullName());

            // Add bubble
            messagesContainer.setAlignment(Pos.TOP_LEFT);
            messagesContainer.getChildren().add(createMessageBubble(sent, currentUser.getId()));
            messageField.clear();
            Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));

            // Update conversation list preview
            selectedConversation.setLastMessage(content);
            conversationListView.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* =====================================================
       NEW CONVERSATION
       ===================================================== */

    @FXML
    private void onNewConversation() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        try {
            List<User> allUsers = userService.afficher();

            // Remove current user and users with existing conversations
            allUsers.removeIf(u -> u.getId() == currentUser.getId());

            if (allUsers.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText(null);
                alert.setContentText("Aucun utilisateur disponible pour démarrer une conversation.");
                alert.showAndWait();
                return;
            }

            // Show choice dialog
            ChoiceDialog<User> dialog = new ChoiceDialog<>(allUsers.get(0), allUsers);
            dialog.setTitle("Nouvelle Conversation");
            dialog.setHeaderText("Choisir un utilisateur");
            dialog.setContentText("Utilisateur:");

            // Custom display for User objects
            dialog.getDialogPane().getScene().getWindow().setOnShown(e -> {
                // Set the converter for the ComboBox inside the dialog
                @SuppressWarnings("unchecked")
                ComboBox<User> comboBox = (ComboBox<User>) dialog.getDialogPane().lookup(".combo-box");
                if (comboBox != null) {
                    comboBox.setConverter(new javafx.util.StringConverter<>() {
                        @Override
                        public String toString(User user) {
                            return user != null ? user.getFullName() + " (" + user.getRole().name() + ")" : "";
                        }
                        @Override
                        public User fromString(String s) { return null; }
                    });
                    comboBox.setButtonCell(new ListCell<>() {
                        @Override
                        protected void updateItem(User item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(item != null ? item.getFullName() + " (" + item.getRole().name() + ")" : "");
                        }
                    });
                }
            });

            dialog.showAndWait().ifPresent(selectedUser -> {
                try {
                    Conversation conv = chatService.getOrCreateConversation(currentUser.getId(), selectedUser.getId());
                    conv.setOtherUserName(selectedUser.getFullName());

                    // Reload conversation list and select the new one
                    loadConversations();

                    // Find and select
                    for (Conversation c : allConversations) {
                        if (c.getId() == conv.getId()) {
                            conversationListView.getSelectionModel().select(c);
                            break;
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* =====================================================
       HELPERS
       ===================================================== */

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return parts[0].substring(0, 1).toUpperCase();
    }

    public boolean focusConversationByOtherUserName(String otherUserName) {
        if (otherUserName == null || otherUserName.isBlank()) return false;
        String needle = otherUserName.trim();

        for (Conversation c : allConversations) {
            if (c.getOtherUserName() != null && c.getOtherUserName().trim().equalsIgnoreCase(needle)) {
                conversationListView.getSelectionModel().select(c);
                conversationListView.scrollTo(c);
                return true;
            }
        }
        return false;
    }
}
