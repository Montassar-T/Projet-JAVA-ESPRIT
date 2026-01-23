package tn.esprit.educlass.service;

import tn.esprit.educlass.enums.NotificationType;
import tn.esprit.educlass.mapper.NotificationMapper;
import tn.esprit.educlass.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationService {

    private final Connection connection;

    public NotificationService(Connection connection) {
        this.connection = connection;
    }

    /* =====================================================
       NOTIFICATION CRUD
       ===================================================== */

    public void createNotification(Notification notification) throws SQLException {
        String sql = """
            INSERT INTO notification (user_id, title, message, type, is_read, read_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getTitle());
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getType() != null
                    ? notification.getType().name()
                    : NotificationType.SYSTEM.name());
            ps.setBoolean(5, notification.isRead());
            if (notification.getReadAt() != null) {
                ps.setTimestamp(6, new Timestamp(notification.getReadAt().getTime()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            ps.executeUpdate();
        }
    }

    public Notification getNotificationById(long id) throws SQLException {
        String sql = "SELECT * FROM notification WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? NotificationMapper.map(rs) : null;
            }
        }
    }

    public List<Notification> getAllNotifications() throws SQLException {
        String sql = "SELECT * FROM notification";
        List<Notification> notifications = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                notifications.add(NotificationMapper.map(rs));
            }
        }
        return notifications;
    }

    public List<Notification> getNotificationsByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM notification WHERE user_id = ? ORDER BY created_at DESC";
        List<Notification> notifications = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(NotificationMapper.map(rs));
                }
            }
        }
        return notifications;
    }

    public void updateNotification(Notification notification) throws SQLException {
        String sql = """
            UPDATE notification
            SET user_id = ?, title = ?, message = ?, type = ?, is_read = ?, read_at = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getTitle());
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getType() != null ? notification.getType().name() : null);
            ps.setBoolean(5, notification.isRead());
            if (notification.getReadAt() != null) {
                ps.setTimestamp(6, new Timestamp(notification.getReadAt().getTime()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            ps.setLong(7, notification.getId());
            ps.executeUpdate();
        }
    }

    public void deleteNotification(long id) throws SQLException {
        String sql = "DELETE FROM notification WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void markAsRead(long id) throws SQLException {
        String sql = "UPDATE notification SET is_read = ?, read_at = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, true);
            ps.setTimestamp(2, new Timestamp(new Date().getTime()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }
}
