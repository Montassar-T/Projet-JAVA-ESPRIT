package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.Conversation;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConversationMapper {

    public static Conversation map(ResultSet rs) throws SQLException {
        Conversation c = new Conversation();
        c.setId(rs.getInt("id"));
        c.setUser1Id(rs.getInt("user1_id"));
        c.setUser2Id(rs.getInt("user2_id"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));
        return c;
    }
}
