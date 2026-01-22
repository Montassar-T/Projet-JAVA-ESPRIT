package tn.esprit.educlass.service;

import tn.esprit.educlass.mapper.QuestionMapper;
import tn.esprit.educlass.model.Question;
import tn.esprit.educlass.utlis.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {

    private Connection con;

    public QuestionService() {
        this.con = DataSource.getInstance().getCon();
    }

    public boolean ajouter(Question q) throws SQLException {
        String sql = "INSERT INTO questions (evaluation_id, text, question_type, points) VALUES (?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, q.getEvaluationId());
        ps.setString(2, q.getText());
        ps.setString(3, q.getQuestionType() != null ? q.getQuestionType().name() : null);
        ps.setDouble(4, q.getPoints());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    public boolean modifier(Question q) throws SQLException {
        String sql = "UPDATE questions SET evaluation_id=?, text=?, question_type=?, points=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, q.getEvaluationId());
        ps.setString(2, q.getText());
        ps.setString(3, q.getQuestionType() != null ? q.getQuestionType().name() : null);
        ps.setDouble(4, q.getPoints());
        ps.setInt(5, q.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    public boolean supprimer(Question q) throws SQLException {
        String sql = "DELETE FROM questions WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, q.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    public List<Question> findByEvaluation(int evaluationId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE evaluation_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, evaluationId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            questions.add(QuestionMapper.map(rs));
        }
        rs.close();
        ps.close();
        return questions;
    }
}

