package tn.esprit.educlass.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.educlass.model.Institution;
import tn.esprit.educlass.model.AcademicStructure;
import tn.esprit.educlass.model.SystemConfig;
import tn.esprit.educlass.model.Supervision;
import tn.esprit.educlass.service.AdminService;
import tn.esprit.educlass.utlis.DataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class AdminController {

  private AdminService service;
  private ObservableList<Supervision> supervisionLogs = FXCollections.observableArrayList();
  private ObservableList<AcademicStructure> academicStructures = FXCollections.observableArrayList();

  public AdminController() {
    try {
      this.service = new AdminService(DataSource.getInstance().getCon());
    } catch (Throwable t) {
      System.err.println("Database service initialization skipped: " + t.getMessage());
    }
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
  @FXML private TableColumn<Supervision, String> typeActionCol;
  @FXML private TableColumn<Supervision, String> resultatCol;
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
  @FXML private ComboBox<String> structTypeCombo;
  @FXML private TextField structCodeField;
  @FXML private TextArea structAddressArea;
  @FXML private TextField structManagerField;

  // FXML Fields for Institution
  @FXML private TableView<Institution> institutionTable;
  @FXML private TextField institutionSearchField;
  @FXML private ComboBox<AcademicStructure> filterStructureCombo;
  @FXML private TableColumn<Institution, String> instNameCol;
  @FXML private TableColumn<Institution, String> instCodeCol;
  @FXML private TableColumn<Institution, String> instCityCol;
  @FXML private TableColumn<Institution, String> instStatusCol;
  @FXML private TableColumn<Institution, Integer> instCapacityCol;
  @FXML private TableColumn<Institution, Date> instDateCol;

  // Institution Form Fields
  @FXML private TextField instNameField;
  @FXML private TextField instCodeField;
  @FXML private TextField instCityField;
  @FXML private ComboBox<String> instStatusCombo;
  @FXML private TextField instCapacityField;
  @FXML private DatePicker instDateField;
  @FXML private ComboBox<AcademicStructure> instMainStructureCombo;

  // Button references

  // Button references
  @FXML private Button saveStructureBtn;
  @FXML private Button createNewBtn;
  @FXML private Button deleteStructureBtn;
  @FXML private Button saveConfigBtn;
  @FXML private Button addInstitutionBtn;
  @FXML private Button editInstitutionBtn;
  @FXML private Button deleteInstitutionBtn;
  @FXML private Button refreshLogsBtn;

  @FXML
  public void initialize() {
    try {
      // Setup Supervision Table
      if (supervisionTable != null) {
        if (idActionCol != null) idActionCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (actionCol != null) actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        if (userCol != null) userCol.setCellValueFactory(new PropertyValueFactory<>("user"));
        if (typeActionCol != null) typeActionCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (resultatCol != null) resultatCol.setCellValueFactory(new PropertyValueFactory<>("result"));
        if (timestampCol != null) timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        supervisionTable.setItems(supervisionLogs);
        
        handleRefreshLogs();
      }

      // Setup Academic Structure ListView
      if (structureListView != null) {
        structureListView.setItems(academicStructures);
        
        // Add selection listener
        structureListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
          if (newVal != null) {
            populateStructureFields(newVal);
          }
        });
        
        handleLoadStructures();
      }

      // Setup ComboBoxes
      if (languageCombo != null) {
        languageCombo.setItems(FXCollections.observableArrayList("English", "French", "Arabic"));
      }
      if (structTypeCombo != null) {
        structTypeCombo.setItems(FXCollections.observableArrayList("UNIVERSITE", "FACULTE", "FACULTY", "DEPARTEMENT"));
      }

      // Setup Institution Table
      if (institutionTable != null) {
        if (instNameCol != null) instNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (instCodeCol != null) instCodeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        if (instCityCol != null) instCityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        if (instStatusCol != null) instStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (instCapacityCol != null) instCapacityCol.setCellValueFactory(new PropertyValueFactory<>("studentCapacity"));
        if (instDateCol != null) instDateCol.setCellValueFactory(new PropertyValueFactory<>("openingDate"));
        
        institutionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
          if (newVal != null) {
            populateInstitutionFields(newVal);
          }
        });
        
        handleRefreshInstitutions();
      }
      
      // Setup Institution View ComboBoxes
      if (instStatusCombo != null) {
        instStatusCombo.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE", "PUBLIC", "PRIVATE"));
      }
      if (instMainStructureCombo != null || filterStructureCombo != null) {
        if (service == null) {
            System.err.println("Warning: AdminService is null during initialize.");
            return;
        }
        try {
          List<AcademicStructure> structures = service.getAllStructures();
          ObservableList<AcademicStructure> structList = FXCollections.observableArrayList(structures);
          if (instMainStructureCombo != null) instMainStructureCombo.setItems(structList);
          if (filterStructureCombo != null) {
            filterStructureCombo.setItems(structList);
            filterStructureCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
              handleRefreshInstitutions();
            });
          }
        } catch (SQLException e) {
          System.err.println("Error loading structures in initialize: " + e.getMessage());
        }
      }
    } catch (Throwable t) {
      System.err.println("Non-fatal error in AdminController.initialize(): " + t.getMessage());
    }
  }

  private void populateStructureFields(AcademicStructure structure) {
    structNameField.setText(structure.getName());
    structTypeCombo.setValue(structure.getType());
    structCodeField.setText(structure.getCode());
    structAddressArea.setText(structure.getAddress());
    structManagerField.setText(structure.getManager());
    if (saveStructureBtn != null) {
      saveStructureBtn.setText("Update Structure");
    }
  }

  @FXML
  private void handleLoadStructures() {
    if (service == null) return;
    try {
      List<AcademicStructure> structures = service.getAllStructures();
      academicStructures.setAll(structures);
    } catch (SQLException e) {
      showAlert("Error loading structures", e.getMessage());
    }
  }

  @FXML
  private void handleRefreshLogs() {
    if (service == null) return;
    try {
      List<Supervision> logs = service.getAllLogs();
      supervisionLogs.setAll(logs);
    } catch (SQLException e) {
      showAlert("Error loading logs", e.getMessage());
    }
  }

  @FXML
  private void handleRefreshInstitutions() {
    if (service == null) return;
    try {
      AcademicStructure selected = filterStructureCombo != null ? filterStructureCombo.getValue() : null;
      List<Institution> insts;
      
      if (selected != null) {
        insts = service.getInstitutionsByStructure(selected.getId());
      } else {
        // Default: load all institutions
        insts = service.getAllInstitutions();
      }
      
      institutionTable.setItems(FXCollections.observableArrayList(insts));
      System.out.println("Loaded " + insts.size() + " institutions");
    } catch (Exception e) {
      System.err.println("Error in handleRefreshInstitutions: " + e.getMessage());
      showAlert("Error", "Could not load institutions: " + e.getMessage());
    }
  }

  @FXML
  private void handleSaveConfig() {
    if (service == null) {
      showAlert("Error", "Database service is not initialized.");
      return;
    }
    try {
      SystemConfig config = service.getConfig();
      if (config == null) {
        config = new SystemConfig();
      }
      config.setPlatformName(platformNameField.getText());
      config.setDefaultLanguage(languageCombo.getValue());
      config.setTimezone(timezoneField.getText());
      config.setSupportEmail(supportEmailField.getText());
      config.setMaintenanceMode(maintenanceToggle.isSelected());
      config.setUpdatedAt(new Date());

      service.upsertConfig(config);
      showAlert("Success", "Configuration updated successfully");
    } catch (SQLException e) {
      showAlert("Error saving config", e.getMessage());
    }
  }

  @FXML
  private void handleSaveStructure() {
    try {
      AcademicStructure selected = structureListView.getSelectionModel().getSelectedItem();
      AcademicStructure structure = (selected != null) ? selected : new AcademicStructure();
      
      structure.setName(structNameField.getText());
      structure.setType(structTypeCombo.getValue());
      structure.setCode(structCodeField.getText());
      structure.setAddress(structAddressArea.getText());
      structure.setManager(structManagerField.getText());

      if (selected != null) {
        service.updateStructure(structure);
        showAlert("Success", "Academic structure updated successfully");
      } else {
        structure.setCreatedAt(new Date());
        service.createStructure(structure);
        showAlert("Success", "Academic structure created successfully");
      }
      
      handleLoadStructures();
      handleClearFields();
    } catch (SQLException e) {
      showAlert("Error saving structure", e.getMessage());
    }
  }

  @FXML
  private void handleClearFields() {
    structNameField.clear();
    structTypeCombo.setValue(null);
    structCodeField.clear();
    structAddressArea.clear();
    structManagerField.clear();
    structureListView.getSelectionModel().clearSelection();
    if (saveStructureBtn != null) {
      saveStructureBtn.setText("Add New Structure");
    }
  }

  @FXML
  private void handleDeleteStructure() {
    AcademicStructure selected = structureListView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      try {
        service.deleteStructure(selected.getId());
        handleLoadStructures();
        showAlert("Success", "Structure deleted");
      } catch (SQLException e) {
        showAlert("Error deleting structure", e.getMessage());
      }
    }
  }

  @FXML
  private void handleCreateNew() {
    handleClearFields();
  }

  private void populateInstitutionFields(Institution inst) {
    instNameField.setText(inst.getName());
    instCodeField.setText(inst.getCode());
    instCityField.setText(inst.getCity());
    instStatusCombo.setValue(inst.getStatus());
    instCapacityField.setText(String.valueOf(inst.getStudentCapacity()));
    if (inst.getOpeningDate() != null) {
      instDateField.setValue(new java.sql.Date(inst.getOpeningDate().getTime()).toLocalDate());
    }
    instMainStructureCombo.setValue(inst.getStructure());
    if (addInstitutionBtn != null) {
      addInstitutionBtn.setText("Update Institution");
    }
  }

  @FXML
  private void handleAddInstitution() {
    try {
      Institution selected = institutionTable.getSelectionModel().getSelectedItem();
      Institution inst = (selected != null) ? selected : new Institution();

      inst.setName(instNameField.getText());
      inst.setCode(instCodeField.getText());
      inst.setCity(instCityField.getText());
      inst.setStatus(instStatusCombo.getValue());
      inst.setStudentCapacity(Integer.parseInt(instCapacityField.getText()));
      
      if (instDateField.getValue() != null) {
        inst.setOpeningDate(java.sql.Date.valueOf(instDateField.getValue()));
      }
      inst.setStructure(instMainStructureCombo.getValue());

      if (selected != null) {
        service.updateInstitution(inst);
        showAlert("Success", "Institution updated successfully");
      } else {
        service.createInstitution(inst);
        showAlert("Success", "Institution created successfully");
      }

      handleRefreshInstitutions();
      handleClearInstitutionFields();
    } catch (Exception e) {
      showAlert("Error saving institution", e.getMessage());
    }
  }

  @FXML
  private void handleEditInstitution() {
    // This is handled by selection listener populating fields
  }

  @FXML
  private void handleDeleteInstitution() {
    Institution selected = institutionTable.getSelectionModel().getSelectedItem();
    if (selected != null) {
      try {
        service.deleteInstitution(selected.getId());
        handleRefreshInstitutions();
        handleClearInstitutionFields();
        showAlert("Success", "Institution deleted");
      } catch (SQLException e) {
        showAlert("Error deleting institution", e.getMessage());
      }
    } else {
      showAlert("Warning", "Please select an institution to delete.");
    }
  }

  @FXML
  private void handleClearInstitutionFields() {
    instNameField.clear();
    instCodeField.clear();
    instCityField.clear();
    instStatusCombo.setValue(null);
    instCapacityField.clear();
    instDateField.setValue(null);
    instMainStructureCombo.setValue(null);
    institutionTable.getSelectionModel().clearSelection();
    if (addInstitutionBtn != null) {
      addInstitutionBtn.setText("Add Institution");
    }
  }

  private void showAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
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
