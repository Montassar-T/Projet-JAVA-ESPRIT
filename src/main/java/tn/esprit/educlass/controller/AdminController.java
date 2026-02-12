package tn.esprit.educlass.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.educlass.enums.ActionResult;
import tn.esprit.educlass.enums.ActionType;
import tn.esprit.educlass.enums.InstitutionStatus;
import tn.esprit.educlass.enums.StructureType;
import tn.esprit.educlass.model.Institution;
import tn.esprit.educlass.model.AcademicStructure;
import tn.esprit.educlass.model.SystemConfig;
import tn.esprit.educlass.model.Supervision;
import tn.esprit.educlass.service.AdminService;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class AdminController {

  private AdminService service;

  public AdminController() {
  }

  public void setService(AdminService service) {
    this.service = service;
  }

  // FXML Fields for Supervision
  @FXML private TextField supervisionSearchField;
  @FXML private TableView<Supervision> supervisionTable;
  @FXML private TableColumn<Supervision, Long> idActionCol;
  @FXML private TableColumn<Supervision, String> actionCol;
  @FXML private TableColumn<Supervision, String> userCol;
  @FXML private TableColumn<Supervision, ActionType> typeActionCol;
  @FXML private TableColumn<Supervision, ActionResult> resultatCol;
  @FXML private TableColumn<Supervision, Date> timestampCol;

  // FXML Fields for SystemConfig
  @FXML private TextField platformNameField;
  @FXML private ComboBox<String> languageCombo;
  @FXML private TextField timezoneField;
  @FXML private TextField supportEmailField;
  @FXML private ToggleButton maintenanceToggle;

  // FXML Fields for AcademicStructure
  @FXML private ListView<AcademicStructure> structureListView;
  @FXML private TextField structNameField;
  @FXML private ComboBox<StructureType> structTypeCombo;
  @FXML private TextField structCodeField;
  @FXML private TextArea structAddressArea;
  @FXML private TextField structManagerField;

  // FXML Fields for Institution
  @FXML private TableView<Institution> institutionTable;
  @FXML private TextField institutionSearchField;
  @FXML private ComboBox<AcademicStructure> filterStructureCombo;

  @FXML
  public void initialize() {
    // Basic initialization logic could go here
    // e.g., setting up cell value factories
  }

  /*
   * =====================================================
   * SYSTEME CONFIG
   * =====================================================
   */

  public void modifyConfig(SystemConfig config) throws SQLException {
    service.upsertConfig(config);
  }

  public SystemConfig getConfig() throws SQLException {
    return service.getConfig();
  }

  /*
   * =====================================================
   * STRUCTURE ACADEMIQUE
   * =====================================================
   */

  public void createStructure(AcademicStructure structure) throws SQLException {
    service.createStructure(structure);
  }

  public List<AcademicStructure> getAllStructures() throws SQLException {
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

  public void openInstitution(Institution inst) throws SQLException {
    service.createInstitution(inst);
  }

  public List<Institution> getInstitutionsByStructure(Long structureId) throws SQLException {
    return service.getInstitutionsByStructure(structureId);
  }

  /*
   * =====================================================
   * SUPERVISION
   * =====================================================
   */

  public void saveAction(Supervision supervision) throws SQLException {
    service.registerAction(supervision);
  }

  public List<Supervision> monitorActions() throws SQLException {
    return service.getAllLogs();
  }
}
