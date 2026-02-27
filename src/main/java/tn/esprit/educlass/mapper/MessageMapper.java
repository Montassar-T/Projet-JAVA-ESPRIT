package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.Message;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageMapper {

    public static Message map(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getLong("id"));
        m.setConversationId(rs.getInt("conversation_id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setContent(rs.getString("content"));
        m.setRead(rs.getBoolean("is_read"));
        m.setCreatedAt(rs.getTimestamp("created_at"));
        return m;
    }
}
