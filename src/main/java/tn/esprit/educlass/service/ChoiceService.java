package tn.esprit.educlass.service;

import tn.esprit.educlass.mapper.ChoiceMapper;
import tn.esprit.educlass.model.Choice;
import tn.esprit.educlass.utlis.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChoiceService {

    private Connection con;

    public ChoiceService() {
        this.con = DataSource.getInstance().getCon();
    }

    public boolean ajouter(Choice c) throws SQLException {
        String sql = "INSERT INTO choices (question_id, text, is_correct) VALUES (?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, c.getQuestionId());
        ps.setString(2, c.getText());
        ps.setBoolean(3, c.isCorrect());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    public boolean modifier(Choice c) throws SQLException {
        String sql = "UPDATE choices SET question_id=?, text=?, is_correct=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, c.getQuestionId());
        ps.setString(2, c.getText());
        ps.setBoolean(3, c.isCorrect());
        ps.setInt(4, c.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    public boolean supprimer(Choice c) throws SQLException {
        String sql = "DELETE FROM choices WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, c.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    public List<Choice> findByQuestion(int questionId) throws SQLException {
        List<Choice> choices = new ArrayList<>();
        String sql = "SELECT * FROM choices WHERE question_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, questionId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            choices.add(ChoiceMapper.map(rs));
        }
        rs.close();
        ps.close();
        return choices;
    }
}

