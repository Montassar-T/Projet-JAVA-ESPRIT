package tn.esprit.educlass.model;

import tn.esprit.educlass.enums.StructureType;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class StructureAcademique extends BaseEntity {
  private Long idStructure;
  private String nomStructure;
  private StructureType typeStructure;
  private String codeStructure;
  private String adresse;
  private String responsable;
  private Date dateCreation;

  private List<Etablissement> etablissements = new ArrayList<>();

  public StructureAcademique() {
  }

  public Long getIdStructure() {
    return idStructure;
  }

  public void setIdStructure(Long idStructure) {
    this.idStructure = idStructure;
  }

  public String getNomStructure() {
    return nomStructure;
  }

  public void setNomStructure(String nomStructure) {
    this.nomStructure = nomStructure;
  }

  public StructureType getTypeStructure() {
    return typeStructure;
  }

  public void setTypeStructure(StructureType typeStructure) {
    this.typeStructure = typeStructure;
  }

  public String getCodeStructure() {
    return codeStructure;
  }

  public void setCodeStructure(String codeStructure) {
    this.codeStructure = codeStructure;
  }

  public String getAdresse() {
    return adresse;
  }

  public void setAdresse(String adresse) {
    this.adresse = adresse;
  }

  public String getResponsable() {
    return responsable;
  }

  public void setResponsable(String responsable) {
    this.responsable = responsable;
  }

  public Date getDateCreation() {
    return dateCreation;
  }

  public void setDateCreation(Date dateCreation) {
    this.dateCreation = dateCreation;
  }

  public List<Etablissement> getEtablissements() {
    return etablissements;
  }

  public void setEtablissements(List<Etablissement> etablissements) {
    this.etablissements = etablissements;
  }

  public void majStructure() {
    // Implementation logic
  }

  public void supprimerStructure() {
    // Implementation logic
  }
}
