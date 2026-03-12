package tn.esprit.educlass.service;

import tn.esprit.educlass.mapper.SchoolClassMapper;
import tn.esprit.educlass.model.SchoolClass;
import tn.esprit.educlass.utlis.DataSource;
import tn.esprit.educlass.utlis.SupervisionLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SchoolClassService {

    private final Connection con;

    public SchoolClassService() {
        this.con = DataSource.getInstance().getCon();
    }

    // ===== READ =====
    public List<SchoolClass> getAllClasses() throws SQLException {
        List<SchoolClass> classes = new ArrayList<>();
        String sql = "SELECT id, name, code, level, capacity, created_at FROM school_class ORDER BY level, name";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                classes.add(SchoolClassMapper.map(rs));
            }
        }
        return classes;
    }

    public SchoolClass getClassById(Long id) throws SQLException {
        String sql = "SELECT id, name, code, level, capacity, created_at FROM school_class WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return SchoolClassMapper.map(rs);
                }
            }
        }
        return null;
    }

    // ===== CREATE =====
    public boolean createClass(SchoolClass schoolClass) throws SQLException {
        String sql = "INSERT INTO school_class (name, code, level, capacity, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, schoolClass.getName());
            ps.setString(2, schoolClass.getCode());
            ps.setString(3, schoolClass.getLevel());
            ps.setObject(4, schoolClass.getCapacity());
            ps.setTimestamp(5, new java.sql.Timestamp(
                    schoolClass.getCreatedAt() != null ? schoolClass.getCreatedAt().getTime() : new Date().getTime()
            ));
            boolean success = ps.executeUpdate() > 0;
            if (success) SupervisionLogger.logSuccess("Create school class: " + schoolClass.getCode());
            return success;
        }
    }

    // ===== UPDATE =====
    public boolean updateClass(SchoolClass schoolClass) throws SQLException {
        String sql = "UPDATE school_class SET name = ?, code = ?, level = ?, capacity = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, schoolClass.getName());
            ps.setString(2, schoolClass.getCode());
            ps.setString(3, schoolClass.getLevel());
            ps.setObject(4, schoolClass.getCapacity());
            ps.setLong(5, schoolClass.getId());
            boolean success = ps.executeUpdate() > 0;
            if (success) SupervisionLogger.logSuccess("Update school class: " + schoolClass.getCode());
            return success;
        }
    }

    // ===== DELETE =====
    public boolean deleteClass(Long id) throws SQLException {
        String sql = "DELETE FROM school_class WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            boolean success = ps.executeUpdate() > 0;
            if (success) SupervisionLogger.logSuccess("Delete school class id=" + id);
            return success;
        }
    }

    // ===== COUNT STUDENTS =====
    public int getStudentCount(Long classId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE school_class_id = ? AND role = 'STUDENT'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
