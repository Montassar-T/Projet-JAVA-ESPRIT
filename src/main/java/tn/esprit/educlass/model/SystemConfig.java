package tn.esprit.educlass.model;

import java.util.Date;

public class SystemConfig extends BaseEntity {
  private Long idConfig;
  private String nomPlateforme;
  private String langueDefault;
  private String fuseauHoraire;
  private Boolean modeMaintenance;
  private String emailSupport;
  private Date dateMaj;

  public SystemConfig() {
  }

  public Long getIdConfig() {
    return idConfig;
  }

  public void setIdConfig(Long idConfig) {
    this.idConfig = idConfig;
  }

  public String getNomPlateforme() {
    return nomPlateforme;
  }

  public void setNomPlateforme(String nomPlateforme) {
    this.nomPlateforme = nomPlateforme;
  }

  public String getLangueDefault() {
    return langueDefault;
  }

  public void setLangueDefault(String langueDefault) {
    this.langueDefault = langueDefault;
  }

  public String getFuseauHoraire() {
    return fuseauHoraire;
  }

  public void setFuseauHoraire(String fuseauHoraire) {
    this.fuseauHoraire = fuseauHoraire;
  }

  public Boolean getModeMaintenance() {
    return modeMaintenance;
  }

  public void setModeMaintenance(Boolean modeMaintenance) {
    this.modeMaintenance = modeMaintenance;
  }

  public String getEmailSupport() {
    return emailSupport;
  }

  public void setEmailSupport(String emailSupport) {
    this.emailSupport = emailSupport;
  }

  public Date getDateMaj() {
    return dateMaj;
  }

  public void setDateMaj(Date dateMaj) {
    this.dateMaj = dateMaj;
  }

  public void activerMaintenance() {
    this.modeMaintenance = true;
  }

  public void modifierParametre() {
    this.dateMaj = new Date();
  }
}
