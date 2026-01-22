package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.Evaluation;
import tn.esprit.educlass.enums.EvaluationType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EvaluationMapper {

    public static Evaluation map(ResultSet rs) throws SQLException {
        Evaluation e = new Evaluation();
        e.setId(rs.getInt("id"));
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        String t = rs.getString("type");
        if(t != null) e.setType(EvaluationType.valueOf(t));
        e.setTeacherId(rs.getInt("teacher_id"));
        e.setDuration(rs.getInt("duration"));
        java.sql.Timestamp due = rs.getTimestamp("due_date");
        if(due != null) e.setDueDate(new java.util.Date(due.getTime()));
        e.setStatus(rs.getString("status"));
        return e;
    }
}
