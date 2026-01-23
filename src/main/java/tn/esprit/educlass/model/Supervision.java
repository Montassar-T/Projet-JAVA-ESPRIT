package tn.esprit.educlass.model;

import tn.esprit.educlass.enums.ActionType;
import tn.esprit.educlass.enums.ActionResult;
import java.util.Date;

public class Supervision extends BaseEntity {
  private Long idAction;
  private String action;
  private String utilisateur;
  private ActionType typeAction;
  private ActionResult resultat;
  private Date dateAction;

  public Supervision() {
  }

  public Long getIdAction() {
    return idAction;
  }

  public void setIdAction(Long idAction) {
    this.idAction = idAction;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getUtilisateur() {
    return utilisateur;
  }

  public void setUtilisateur(String utilisateur) {
    this.utilisateur = utilisateur;
  }

  public ActionType getTypeAction() {
    return typeAction;
  }

  public void setTypeAction(ActionType typeAction) {
    this.typeAction = typeAction;
  }

  public ActionResult getResultat() {
    return resultat;
  }

  public void setResultat(ActionResult resultat) {
    this.resultat = resultat;
  }

  public Date getDateAction() {
    return dateAction;
  }

  public void setDateAction(Date dateAction) {
    this.dateAction = dateAction;
  }

  public void enregistrerAction() {
    this.dateAction = new Date();
  }

  public void genererRapport() {
    // Implementation logic
  }
}
