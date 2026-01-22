package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.Choice;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChoiceMapper {

    public static Choice map(ResultSet rs) throws SQLException {
        Choice c = new Choice();
        c.setId(rs.getInt("id"));
        c.setQuestionId(rs.getInt("question_id"));
        c.setText(rs.getString("text"));
        c.setCorrect(rs.getBoolean("is_correct"));
        return c;
    }
}

