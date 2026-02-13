package tn.esprit.educlass.model;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class AcademicStructure extends BaseEntity {
  private Long id;
  private String name;
  private String type;
  private String code;
  private String address;
  private String manager;
  private Date createdAt;

  private List<Institution> institutions = new ArrayList<>();

  public AcademicStructure() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getManager() {
    return manager;
  }

  public void setManager(String manager) {
    this.manager = manager;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public List<Institution> getInstitutions() {
    return institutions;
  }

  public void setInstitutions(List<Institution> institutions) {
    this.institutions = institutions;
  }

  public void updateStructure() {
    // Implementation logic
  }

  public void deleteStructure() {
    // Implementation logic
  }

  @Override
  public String toString() {
    return name != null ? name : "Unnamed Structure";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AcademicStructure that = (AcademicStructure) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
