package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.StudentResponse;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentResponseMapper {

    public static StudentResponse map(ResultSet rs) throws SQLException {
        StudentResponse sr = new StudentResponse();
        sr.setId(rs.getInt("id"));
        sr.setStudentId(rs.getInt("student_id"));
        sr.setQuestionId(rs.getInt("question_id"));
        int choiceId = rs.getInt("choice_id");
        if (!rs.wasNull()) {
            sr.setChoiceId(choiceId);
        }
        sr.setAnswerText(rs.getString("answer_text"));
        return sr;
    }
}