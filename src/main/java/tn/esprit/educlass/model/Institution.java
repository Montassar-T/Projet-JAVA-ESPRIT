package tn.esprit.educlass.model;

import tn.esprit.educlass.enums.InstitutionStatus;
import java.util.Date;

public class Institution extends BaseEntity {
  private Long id;
  private String name;
  private String code;
  private String city;
  private InstitutionStatus status;
  private Integer studentCapacity;
  private Date openingDate;

  private AcademicStructure structure;

  public Institution() {
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

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public InstitutionStatus getStatus() {
    return status;
  }

  public void setStatus(InstitutionStatus status) {
    this.status = status;
  }

  public Integer getStudentCapacity() {
    return studentCapacity;
  }

  public void setStudentCapacity(Integer studentCapacity) {
    this.studentCapacity = studentCapacity;
  }

  public Date getOpeningDate() {
    return openingDate;
  }

  public void setOpeningDate(Date openingDate) {
    this.openingDate = openingDate;
  }

  public AcademicStructure getStructure() {
    return structure;
  }

  public void setStructure(AcademicStructure structure) {
    this.structure = structure;
  }

  public void open() {
    // Implementation logic
  }

  public void close() {
    // Implementation logic
  }
}
