package tn.esprit.educlass.controller;

import tn.esprit.educlass.model.Etablissement;
import tn.esprit.educlass.model.StructureAcademique;
import tn.esprit.educlass.model.SystemeConfig;
import tn.esprit.educlass.model.Supervision;
import tn.esprit.educlass.service.AdminService;

import java.sql.SQLException;
import java.util.List;

public class AdminController {

  private final AdminService service;

  public AdminController(AdminService service) {
    this.service = service;
  }

  /*
   * =====================================================
   * SYSTEME CONFIG
   * =====================================================
   */

  public void modifierParametre(SystemeConfig config) throws SQLException {
    service.upsertConfig(config);
  }

  public SystemeConfig obtenirConfig() throws SQLException {
    return service.getConfig();
  }

  /*
   * =====================================================
   * STRUCTURE ACADEMIQUE
   * =====================================================
   */

  public void createStructure(StructureAcademique structure) throws SQLException {
    service.createStructure(structure);
  }

  public List<StructureAcademique> getAllStructures() throws SQLException {
    return service.getAllStructures();
  }

  public void supprimerStructure(Long id) throws SQLException {
    service.deleteStructure(id);
  }

  /*
   * =====================================================
   * ETABLISSEMENT
   * =====================================================
   */

  public void ouvrirEtablissement(Etablissement etab) throws SQLException {
    service.createEtablissement(etab);
  }

  public List<Etablissement> getEtablissementsParStructure(Long structureId) throws SQLException {
    return service.getEtablissementsByStructure(structureId);
  }

  /*
   * =====================================================
   * SUPERVISION
   * =====================================================
   */

  public void enregistrerAction(Supervision supervision) throws SQLException {
    service.registerAction(supervision);
  }

  public List<Supervision> monitorAction() throws SQLException {
    return service.getAllLogs();
  }
}
