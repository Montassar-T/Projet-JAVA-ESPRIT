package tn.esprit.educlass.service;

import tn.esprit.educlass.mapper.InstitutionMapper;
import tn.esprit.educlass.mapper.AcademicStructureMapper;
import tn.esprit.educlass.mapper.SystemConfigMapper;
import tn.esprit.educlass.mapper.SupervisionMapper;
import tn.esprit.educlass.model.Institution;
import tn.esprit.educlass.model.AcademicStructure;
import tn.esprit.educlass.model.SystemConfig;
import tn.esprit.educlass.model.Supervision;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

  private final Connection connection;

  public AdminService(Connection connection) {
    this.connection = connection;
  }

  /*
   * =====================================================
   * SYSTEME CONFIG CRUD
   * =====================================================
   */

  public void upsertConfig(SystemConfig config) throws SQLException {
    String sql;
    if (config.getId() == null) {
      sql = """
              INSERT INTO system_config (platform_name, default_language, timezone, maintenance_mode, support_email, updated_at)
              VALUES (?, ?, ?, ?, ?, ?)
          """;
    } else {
      sql = """
              UPDATE system_config SET platform_name = ?, default_language = ?, timezone = ?, maintenance_mode = ?, support_email = ?, updated_at = ?
              WHERE id = ?
          """;
    }

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, config.getPlatformName());
      ps.setString(2, config.getDefaultLanguage());
      ps.setString(3, config.getTimezone());
      ps.setBoolean(4, config.getMaintenanceMode());
      ps.setString(5, config.getSupportEmail());
      ps.setTimestamp(6, new java.sql.Timestamp(
          config.getUpdatedAt() != null ? config.getUpdatedAt().getTime() : new java.util.Date().getTime()));
      if (config.getId() != null) {
        ps.setLong(7, config.getId());
      }
      ps.executeUpdate();
    }
  }

  public SystemConfig getConfig() throws SQLException {
    String sql = "SELECT id, platform_name, default_language, timezone, maintenance_mode, support_email, updated_at FROM system_config LIMIT 1";
    try (PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      return rs.next() ? SystemConfigMapper.map(rs) : null;
    }
  }

  /*
   * =====================================================
   * STRUCTURE ACADEMIQUE CRUD
   * =====================================================
   */

  public void createStructure(AcademicStructure structure) throws SQLException {
    String sql = "INSERT INTO academic_structure (name, type, code, address, manager, created_at) VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, structure.getName());
      ps.setString(2, structure.getType());
      ps.setString(3, structure.getCode());
      ps.setString(4, structure.getAddress());
      ps.setString(5, structure.getManager());
      ps.setTimestamp(6,
          new java.sql.Timestamp(structure.getCreatedAt() != null ? structure.getCreatedAt().getTime()
              : new java.util.Date().getTime()));
      ps.executeUpdate();
    }
  }

  public void updateStructure(AcademicStructure structure) throws SQLException {
    String sql = "UPDATE academic_structure SET name = ?, type = ?, code = ?, address = ?, manager = ? WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, structure.getName());
      ps.setString(2, structure.getType());
      ps.setString(3, structure.getCode());
      ps.setString(4, structure.getAddress());
      ps.setString(5, structure.getManager());
      ps.setLong(6, structure.getId());
      ps.executeUpdate();
    }
  }

  public List<AcademicStructure> getAllStructures() throws SQLException {
    String sql = "SELECT id, name, type, code, address, manager, created_at FROM academic_structure";
    List<AcademicStructure> list = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(AcademicStructureMapper.map(rs));
      }
    }
    return list;
  }

  public void deleteStructure(Long id) throws SQLException {
    String sql = "DELETE FROM academic_structure WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    }
  }

  /*
   * =====================================================
   * ETABLISSEMENT CRUD
   * =====================================================
   */

  public void createInstitution(Institution inst) throws SQLException {
    String sql = "INSERT INTO institution (name, code, city, status, student_capacity, opening_date, structure_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, inst.getName());
      ps.setString(2, inst.getCode());
      ps.setString(3, inst.getCity());
      ps.setString(4, inst.getStatus());
      ps.setInt(5, inst.getStudentCapacity());
      ps.setDate(6, new java.sql.Date(inst.getOpeningDate().getTime()));
      ps.setLong(7, inst.getStructure().getId());
      ps.executeUpdate();
    }
  }

  public void updateInstitution(Institution inst) throws SQLException {
    String sql = "UPDATE institution SET name = ?, code = ?, city = ?, status = ?, student_capacity = ?, opening_date = ?, structure_id = ? WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, inst.getName());
      ps.setString(2, inst.getCode());
      ps.setString(3, inst.getCity());
      ps.setString(4, inst.getStatus());
      ps.setInt(5, inst.getStudentCapacity());
      ps.setDate(6, new java.sql.Date(inst.getOpeningDate().getTime()));
      ps.setLong(7, inst.getStructure().getId());
      ps.setLong(8, inst.getId());
      ps.executeUpdate();
    }
  }

  public void deleteInstitution(Long id) throws SQLException {
    String sql = "DELETE FROM institution WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    }
  }

  public List<Institution> getInstitutionsByStructure(Long structureId) throws SQLException {
    String sql = "SELECT id, name, code, city, status, student_capacity, opening_date, structure_id FROM institution WHERE structure_id = ?";
    List<Institution> list = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, structureId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(InstitutionMapper.map(rs));
        }
      }
    }
    return list;
  }

  public List<Institution> getAllInstitutions() throws SQLException {
    String sql = "SELECT id, name, code, city, status, student_capacity, opening_date, structure_id FROM institution";
    List<Institution> list = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(InstitutionMapper.map(rs));
      }
    }
    return list;
  }

  /*
   * =====================================================
   * SUPERVISION
   * =====================================================
   */

  public void registerAction(Supervision supervision) throws SQLException {
    String sql = "INSERT INTO supervision (action, user, type, result, timestamp) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, supervision.getAction());
      ps.setString(2, supervision.getUser());
      ps.setString(3, supervision.getType());
      ps.setString(4, supervision.getResult());
      ps.setTimestamp(5, new java.sql.Timestamp(new java.util.Date().getTime()));
      ps.executeUpdate();
    }
  }

  public List<Supervision> getAllLogs() throws SQLException {
    String sql = "SELECT id, action, user, type, result, timestamp FROM supervision ORDER BY timestamp DESC";
    List<Supervision> list = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(SupervisionMapper.map(rs));
      }
    }
    return list;
  }
}
