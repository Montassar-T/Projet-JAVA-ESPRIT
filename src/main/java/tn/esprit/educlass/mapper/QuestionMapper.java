package tn.esprit.educlass.mapper;

import tn.esprit.educlass.enums.QuestionType;
import tn.esprit.educlass.model.Question;

import java.sql.ResultSet;
import java.sql.SQLException;

public class QuestionMapper {

    public static Question map(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getInt("id"));
        q.setEvaluationId(rs.getInt("evaluation_id"));
        q.setText(rs.getString("text"));
        String t = rs.getString("question_type");
        if (t != null) q.setQuestionType(QuestionType.valueOf(t));
        q.setPoints(rs.getDouble("points"));
        return q;
    }
}

