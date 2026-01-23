package tn.esprit.educlass.mapper;

import tn.esprit.educlass.enums.NotificationType;
import tn.esprit.educlass.model.Notification;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class NotificationMapper {

    public static Notification map(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getLong("id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));

        String typeValue = rs.getString("type");
        if (typeValue != null) {
            notification.setType(NotificationType.valueOf(typeValue));
        }

        notification.setRead(rs.getBoolean("is_read"));

        Date createdAt = rs.getTimestamp("created_at");
        notification.setCreatedAt(createdAt);

        Date readAt = rs.getTimestamp("read_at");
        notification.setReadAt(readAt);

        return notification;
    }
}
