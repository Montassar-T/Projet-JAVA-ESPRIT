package tn.esprit.educlass.model;

import java.util.Date;

public class Supervision extends BaseEntity {
  private Long id;
  private String action;
  private String user;
  private String type;
  private String result;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
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
