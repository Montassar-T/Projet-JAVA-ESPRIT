package tn.esprit.educlass.service;

import tn.esprit.educlass.enums.EvaluationType;
import tn.esprit.educlass.mapper.EvaluationMapper;
import tn.esprit.educlass.model.Evaluation;
import tn.esprit.educlass.utlis.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EvaluationService {

    private Connection con;

    public EvaluationService() {
        this.con = DataSource.getInstance().getCon();
    }

    // CREATE
    public boolean ajouter(Evaluation e) throws SQLException {
        String sql = "INSERT INTO evaluations (title, description, type, teacher_id, duration, due_date, status) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, e.getTitle());
        ps.setString(2, e.getDescription());
        ps.setString(3, e.getType() != null ? e.getType().name() : EvaluationType.QUIZ.name());
        ps.setInt(4, e.getTeacherId());
        ps.setInt(5, e.getDuration());
        if (e.getDueDate() != null) {
            ps.setTimestamp(6, new Timestamp(e.getDueDate().getTime()));
        } else {
            ps.setNull(6, Types.TIMESTAMP);
        }
        ps.setString(7, e.getStatus() != null ? e.getStatus() : "DRAFT");
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    // UPDATE
    public boolean modifier(Evaluation e) throws SQLException {
        String sql = "UPDATE evaluations SET title=?, description=?, type=?, teacher_id=?, duration=?, due_date=?, status=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, e.getTitle());
        ps.setString(2, e.getDescription());
        ps.setString(3, e.getType() != null ? e.getType().name() : null);
        ps.setInt(4, e.getTeacherId());
        ps.setInt(5, e.getDuration());
        if (e.getDueDate() != null) {
            ps.setTimestamp(6, new Timestamp(e.getDueDate().getTime()));
        } else {
            ps.setNull(6, Types.TIMESTAMP);
        }
        ps.setString(7, e.getStatus());
        ps.setInt(8, e.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    // DELETE
    public boolean supprimer(Evaluation e) throws SQLException {
        String sql = "DELETE FROM evaluations WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, e.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    // LIST
    public List<Evaluation> afficher() throws SQLException {
        List<Evaluation> evaluations = new ArrayList<>();
        String sql = "SELECT * FROM evaluations";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            evaluations.add(EvaluationMapper.map(rs));
        }
        rs.close();
        st.close();
        return evaluations;
    }

    // FIND BY ID
    public Evaluation findById(int id) throws SQLException {
        String sql = "SELECT * FROM evaluations WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Evaluation e = null;
        if (rs.next()) {
            e = EvaluationMapper.map(rs);
        }
        rs.close();
        ps.close();
        return e;
    }

    // PUBLISH RESULTS (simple state change)
    public boolean publish(int evaluationId) throws SQLException {
        String sql = "UPDATE evaluations SET status=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, "PUBLISHED");
        ps.setInt(2, evaluationId);
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }
}

