package tn.esprit.educlass.model;

import tn.esprit.educlass.enums.StatutEtablissement;
import java.util.Date;

public class Etablissement extends BaseEntity {
  private Long idEtab;
  private String nomEtab;
  private String codeEtab;
  private String ville;
  private StatutEtablissement statut;
  private Integer capaciteEtudiants;
  private Date dateOuverture;

  private StructureAcademique structure;

  public Etablissement() {
  }

  public Long getIdEtab() {
    return idEtab;
  }

  public void setIdEtab(Long idEtab) {
    this.idEtab = idEtab;
  }

  public String getNomEtab() {
    return nomEtab;
  }

  public void setNomEtab(String nomEtab) {
    this.nomEtab = nomEtab;
  }

  public String getCodeEtab() {
    return codeEtab;
  }

  public void setCodeEtab(String codeEtab) {
    this.codeEtab = codeEtab;
  }

  public String getVille() {
    return ville;
  }

  public void setVille(String ville) {
    this.ville = ville;
  }

  public StatutEtablissement getStatut() {
    return statut;
  }

  public void setStatut(StatutEtablissement statut) {
    this.statut = statut;
  }

  public Integer getCapaciteEtudiants() {
    return capaciteEtudiants;
  }

  public void setCapaciteEtudiants(Integer capaciteEtudiants) {
    this.capaciteEtudiants = capaciteEtudiants;
  }

  public Date getDateOuverture() {
    return dateOuverture;
  }

  public void setDateOuverture(Date dateOuverture) {
    this.dateOuverture = dateOuverture;
  }

  public StructureAcademique getStructure() {
    return structure;
  }

  public void setStructure(StructureAcademique structure) {
    this.structure = structure;
  }

  public void ouvrir() {
    // Implementation logic
  }

  public void fermer() {
    // Implementation logic
  }
}
