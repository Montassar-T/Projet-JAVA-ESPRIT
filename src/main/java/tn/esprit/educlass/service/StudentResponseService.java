package tn.esprit.educlass.service;

import tn.esprit.educlass.mapper.StudentResponseMapper;
import tn.esprit.educlass.model.StudentResponse;
import tn.esprit.educlass.utlis.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentResponseService {

    private Connection con;

    public StudentResponseService() {
        this.con = DataSource.getInstance().getCon();
    }

    // CREATE
    public boolean ajouter(StudentResponse sr) throws SQLException {
        String sql = "INSERT INTO student_responses (student_id, question_id, choice_id, answer_text) VALUES (?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, sr.getStudentId());
        ps.setInt(2, sr.getQuestionId());
        if (sr.getChoiceId() != null) {
            ps.setInt(3, sr.getChoiceId());
        } else {
            ps.setNull(3, Types.INTEGER);
        }
        ps.setString(4, sr.getAnswerText());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    // DELETE
    public boolean supprimer(StudentResponse sr) throws SQLException {
        String sql = "DELETE FROM student_responses WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, sr.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    // FIND BY STUDENT AND QUESTION
    public List<StudentResponse> findByStudentAndQuestion(int studentId, int questionId) throws SQLException {
        List<StudentResponse> responses = new ArrayList<>();
        String sql = "SELECT * FROM student_responses WHERE student_id=? AND question_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, studentId);
        ps.setInt(2, questionId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            responses.add(StudentResponseMapper.map(rs));
        }
        rs.close();
        ps.close();
        return responses;
    }

    // FIND ALL RESPONSES BY STUDENT FOR AN EVALUATION (via question's evaluation_id)
    public List<StudentResponse> findByStudentAndEvaluation(int studentId, int evaluationId) throws SQLException {
        List<StudentResponse> responses = new ArrayList<>();
        String sql = "SELECT sr.* FROM student_responses sr " +
                     "JOIN questions q ON sr.question_id = q.id " +
                     "WHERE sr.student_id=? AND q.evaluation_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, studentId);
        ps.setInt(2, evaluationId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            responses.add(StudentResponseMapper.map(rs));
        }
        rs.close();
        ps.close();
        return responses;
    }

    // DELETE ALL RESPONSES BY STUDENT FOR AN EVALUATION (for re-taking)
    public boolean deleteByStudentAndEvaluation(int studentId, int evaluationId) throws SQLException {
        String sql = "DELETE sr FROM student_responses sr " +
                     "JOIN questions q ON sr.question_id = q.id " +
                     "WHERE sr.student_id=? AND q.evaluation_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, studentId);
        ps.setInt(2, evaluationId);
        boolean success = ps.executeUpdate() >= 0;
        ps.close();
        return success;
    }

    // CHECK IF STUDENT ALREADY RESPONDED TO AN EVALUATION
    public boolean hasStudentResponded(int studentId, int evaluationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM student_responses sr " +
                     "JOIN questions q ON sr.question_id = q.id " +
                     "WHERE sr.student_id=? AND q.evaluation_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, studentId);
        ps.setInt(2, evaluationId);
        ResultSet rs = ps.executeQuery();
        boolean exists = false;
        if (rs.next()) {
            exists = rs.getInt(1) > 0;
        }
        rs.close();
        ps.close();
        return exists;
    }
}