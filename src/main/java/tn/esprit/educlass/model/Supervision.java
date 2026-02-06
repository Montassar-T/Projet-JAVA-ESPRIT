package tn.esprit.educlass.model;

import tn.esprit.educlass.enums.ActionType;
import tn.esprit.educlass.enums.ActionResult;
import java.util.Date;

public class Supervision extends BaseEntity {
  private Long id;
  private String action;
  private String user;
  private ActionType type;
  private ActionResult result;
  private Date timestamp;

  public Supervision() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public ActionType getType() {
    return type;
  }

  public void setType(ActionType type) {
    this.type = type;
  }

  public ActionResult getResult() {
    return result;
  }

  public void setResult(ActionResult result) {
    this.result = result;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public void saveAction() {
    this.timestamp = new Date();
  }

  public void generateReport() {
    // Implementation logic
  }
}
