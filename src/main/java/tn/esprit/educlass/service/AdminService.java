package tn.esprit.educlass.service;

import tn.esprit.educlass.mapper.EtablissementMapper;
import tn.esprit.educlass.mapper.StructureAcademiqueMapper;
import tn.esprit.educlass.mapper.SystemeConfigMapper;
import tn.esprit.educlass.mapper.SupervisionMapper;
import tn.esprit.educlass.model.Etablissement;
import tn.esprit.educlass.model.StructureAcademique;
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
    if (config.getIdConfig() == null) {
      sql = """
              INSERT INTO systeme_config (nom_plateforme, langue_defaut, fuseau_horaire, mode_maintenance, email_support, date_maj)
              VALUES (?, ?, ?, ?, ?, ?)
          """;
    } else {
      sql = """
              UPDATE systeme_config SET nom_plateforme = ?, langue_defaut = ?, fuseau_horaire = ?, mode_maintenance = ?, email_support = ?, date_maj = ?
              WHERE id_config = ?
          """;
    }

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, config.getNomPlateforme());
      ps.setString(2, config.getLangueDefault());
      ps.setString(3, config.getFuseauHoraire());
      ps.setBoolean(4, config.getModeMaintenance());
      ps.setString(5, config.getEmailSupport());
      ps.setTimestamp(6, new java.sql.Timestamp(
          config.getDateMaj() != null ? config.getDateMaj().getTime() : new java.util.Date().getTime()));
      if (config.getIdConfig() != null) {
        ps.setLong(7, config.getIdConfig());
      }
      ps.executeUpdate();
    }
  }

  public SystemConfig getConfig() throws SQLException {
    String sql = "SELECT * FROM systeme_config LIMIT 1";
    try (PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      return rs.next() ? SystemeConfigMapper.map(rs) : null;
    }
  }

  /*
   * =====================================================
   * STRUCTURE ACADEMIQUE CRUD
   * =====================================================
   */

  public void createStructure(StructureAcademique structure) throws SQLException {
    String sql = "INSERT INTO structure_academique (nom_structure, type_structure, code_structure, adresse, responsable, date_creation) VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, structure.getNomStructure());
      ps.setString(2, structure.getTypeStructure().name());
      ps.setString(3, structure.getCodeStructure());
      ps.setString(4, structure.getAdresse());
      ps.setString(5, structure.getResponsable());
      ps.setTimestamp(6,
          new java.sql.Timestamp(structure.getDateCreation() != null ? structure.getDateCreation().getTime()
              : new java.util.Date().getTime()));
      ps.executeUpdate();
    }
  }

  public List<StructureAcademique> getAllStructures() throws SQLException {
    String sql = "SELECT * FROM structure_academique";
    List<StructureAcademique> list = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(StructureAcademiqueMapper.map(rs));
      }
    }
    return list;
  }

  public void deleteStructure(Long id) throws SQLException {
    String sql = "DELETE FROM structure_academique WHERE id_structure = ?";
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

  public void createEtablissement(Etablissement etab) throws SQLException {
    String sql = "INSERT INTO etablissement (nom_etab, code_etab, ville, statut, capacite_etudiants, date_ouverture, structure_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, etab.getNomEtab());
      ps.setString(2, etab.getCodeEtab());
      ps.setString(3, etab.getVille());
      ps.setString(4, etab.getStatut().name());
      ps.setInt(5, etab.getCapaciteEtudiants());
      ps.setDate(6, new java.sql.Date(etab.getDateOuverture().getTime()));
      ps.setLong(7, etab.getStructure().getIdStructure());
      ps.executeUpdate();
    }
  }

  public List<Etablissement> getEtablissementsByStructure(Long structureId) throws SQLException {
    String sql = "SELECT * FROM etablissement WHERE structure_id = ?";
    List<Etablissement> list = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, structureId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(EtablissementMapper.map(rs));
        }
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
    String sql = "INSERT INTO supervision (action, utilisateur, type_action, resultat, date_action) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, supervision.getAction());
      ps.setString(2, supervision.getUtilisateur());
      ps.setString(3, supervision.getTypeAction().name());
      ps.setString(4, supervision.getResultat().name());
      ps.setTimestamp(5, new java.sql.Timestamp(new java.util.Date().getTime()));
      ps.executeUpdate();
    }
  }

  public List<Supervision> getAllLogs() throws SQLException {
    String sql = "SELECT * FROM supervision ORDER BY date_action DESC";
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
