package tn.esprit.educlass.service;

import tn.esprit.educlass.mapper.MarkMapper;
import tn.esprit.educlass.model.Mark;
import tn.esprit.educlass.utlis.DataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarkService {

    private Connection con;

    public MarkService() {
        this.con = DataSource.getInstance().getCon();
    }

    // CREATE
    public boolean ajouter(Mark m) throws SQLException {
        String sql = "INSERT INTO marks (student_id, exam_id, mark) VALUES (?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, m.getStudentId());
        ps.setInt(2, m.getExamId());
        ps.setBigDecimal(3, m.getMark());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    // UPDATE
    public boolean modifier(Mark m) throws SQLException {
        String sql = "UPDATE marks SET student_id=?, exam_id=?, mark=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, m.getStudentId());
        ps.setInt(2, m.getExamId());
        ps.setBigDecimal(3, m.getMark());
        ps.setInt(4, m.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    // DELETE
    public boolean supprimer(Mark m) throws SQLException {
        String sql = "DELETE FROM marks WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, m.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    // LIST ALL MARKS
    public List<Mark> afficher() throws SQLException {
        List<Mark> marks = new ArrayList<>();
        String sql = "SELECT * FROM marks";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            marks.add(MarkMapper.map(rs));
        }
        rs.close();
        st.close();
        return marks;
    }

    // FIND BY ID
    public Mark findById(int id) throws SQLException {
        String sql = "SELECT * FROM marks WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Mark m = null;
        if (rs.next()) {
            m = MarkMapper.map(rs);
        }
        rs.close();
        ps.close();
        return m;
    }

    // FIND MARKS BY STUDENT ID
    public List<Mark> findByStudentId(int studentId) throws SQLException {
        List<Mark> marks = new ArrayList<>();
        String sql = "SELECT * FROM marks WHERE student_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, studentId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            marks.add(MarkMapper.map(rs));
        }
        rs.close();
        ps.close();
        return marks;
    }

    // FIND MARKS BY EXAM ID
    public List<Mark> findByExamId(int examId) throws SQLException {
        List<Mark> marks = new ArrayList<>();
        String sql = "SELECT * FROM marks WHERE exam_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, examId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            marks.add(MarkMapper.map(rs));
        }
        rs.close();
        ps.close();
        return marks;
    }

    // FIND MARK BY STUDENT AND EXAM (unique constraint)
    public Mark findByStudentAndExam(int studentId, int examId) throws SQLException {
        String sql = "SELECT * FROM marks WHERE student_id=? AND exam_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, studentId);
        ps.setInt(2, examId);
        ResultSet rs = ps.executeQuery();
        Mark m = null;
        if (rs.next()) {
            m = MarkMapper.map(rs);
        }
        rs.close();
        ps.close();
        return m;
    }
}
