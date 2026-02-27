package tn.esprit.educlass.service;

import tn.esprit.educlass.mapper.ConversationMapper;
import tn.esprit.educlass.mapper.MessageMapper;
import tn.esprit.educlass.model.Conversation;
import tn.esprit.educlass.model.Message;
import tn.esprit.educlass.utlis.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatService {

    private final Connection con;

    public ChatService() {
        this.con = DataSource.getInstance().getCon();
    }

    /* =====================================================
       CONVERSATION OPERATIONS
       ===================================================== */

    /**
     * Get all conversations for a user, enriched with the other
     * user's name, the last message preview, and unread count.
     */
    public List<Conversation> getConversationsForUser(int userId) throws SQLException {
        String sql = """
            SELECT c.*,
                   CONCAT(u.first_name, ' ', u.last_name) AS other_user_name,
                   (SELECT m.content FROM message m WHERE m.conversation_id = c.id ORDER BY m.created_at DESC LIMIT 1) AS last_message,
                   (SELECT COUNT(*) FROM message m WHERE m.conversation_id = c.id AND m.sender_id != ? AND m.is_read = FALSE) AS unread_count
            FROM conversation c
            JOIN users u ON u.id = CASE WHEN c.user1_id = ? THEN c.user2_id ELSE c.user1_id END
            WHERE c.user1_id = ? OR c.user2_id = ?
            ORDER BY c.updated_at DESC
        """;

        List<Conversation> conversations = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Conversation c = ConversationMapper.map(rs);
                    c.setOtherUserName(rs.getString("other_user_name"));
                    c.setLastMessage(rs.getString("last_message"));
                    c.setUnreadCount(rs.getInt("unread_count"));
                    conversations.add(c);
                }
            }
        }
        return conversations;
    }

    /**
     * Find an existing conversation between two users, or create a new one.
     * Always stores the smaller user ID as user1_id for consistency.
     */
    public Conversation getOrCreateConversation(int userAId, int userBId) throws SQLException {
        int u1 = Math.min(userAId, userBId);
        int u2 = Math.max(userAId, userBId);

        // Try to find existing
        String findSql = "SELECT * FROM conversation WHERE user1_id = ? AND user2_id = ?";
        try (PreparedStatement ps = con.prepareStatement(findSql)) {
            ps.setInt(1, u1);
            ps.setInt(2, u2);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return ConversationMapper.map(rs);
                }
            }
        }

        // Create new
        String insertSql = "INSERT INTO conversation (user1_id, user2_id) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, u1);
            ps.setInt(2, u2);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Conversation c = new Conversation();
                    c.setId(keys.getInt(1));
                    c.setUser1Id(u1);
                    c.setUser2Id(u2);
                    return c;
                }
            }
        }
        throw new SQLException("Failed to create conversation");
    }

    /* =====================================================
       MESSAGE OPERATIONS
       ===================================================== */

    /**
     * Get all messages for a conversation, ordered by creation time.
     */
    public List<Message> getMessages(int conversationId) throws SQLException {
        String sql = """
            SELECT m.*, CONCAT(u.first_name, ' ', u.last_name) AS sender_name
            FROM message m
            JOIN users u ON u.id = m.sender_id
            WHERE m.conversation_id = ?
            ORDER BY m.created_at ASC
        """;

        List<Message> messages = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = MessageMapper.map(rs);
                    m.setSenderName(rs.getString("sender_name"));
                    messages.add(m);
                }
            }
        }
        return messages;
    }

    /**
     * Send a new message in a conversation and update the conversation's updated_at.
     */
    public Message sendMessage(int conversationId, int senderId, String content) throws SQLException {
        String sql = "INSERT INTO message (conversation_id, sender_id, content) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, senderId);
            ps.setString(3, content);
            ps.executeUpdate();

            // Update conversation's updated_at so it sorts to the top
            String updateSql = "UPDATE conversation SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement ps2 = con.prepareStatement(updateSql)) {
                ps2.setInt(1, conversationId);
                ps2.executeUpdate();
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Message m = new Message();
                    m.setId(keys.getLong(1));
                    m.setConversationId(conversationId);
                    m.setSenderId(senderId);
                    m.setContent(content);
                    m.setRead(false);
                    m.setCreatedAt(new java.util.Date());
                    return m;
                }
            }
        }
        throw new SQLException("Failed to send message");
    }

    /**
     * Mark all messages in a conversation as read for a specific user
     * (marks messages NOT sent by this user).
     */
    public void markMessagesAsRead(int conversationId, int userId) throws SQLException {
        String sql = "UPDATE message SET is_read = TRUE WHERE conversation_id = ? AND sender_id != ? AND is_read = FALSE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}
