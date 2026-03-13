package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.Mark;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MarkMapper {

    public static Mark map(ResultSet rs) throws SQLException {
        Mark m = new Mark();
        m.setId(rs.getInt("id"));
        m.setStudentId(rs.getInt("student_id"));
        m.setExamId(rs.getInt("exam_id"));
        BigDecimal markValue = rs.getBigDecimal("mark");
        if (markValue != null) {
            m.setMark(markValue);
        }
        try {
            boolean requested = rs.getBoolean("review_requested");
            if (!rs.wasNull()) {
                m.setReviewRequested(requested);
            }
        } catch (SQLException ignored) {
            // older schema without column
        }
        try {
            boolean resolved = rs.getBoolean("review_resolved");
            if (!rs.wasNull()) {
                m.setReviewResolved(resolved);
            }
        } catch (SQLException ignored) {
            // older schema without column
        }
        return m;
    }
}
