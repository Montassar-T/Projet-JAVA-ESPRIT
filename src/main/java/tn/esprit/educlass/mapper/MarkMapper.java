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
        return m;
    }
}
