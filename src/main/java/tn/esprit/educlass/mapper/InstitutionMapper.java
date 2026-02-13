package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.Institution;
import tn.esprit.educlass.model.AcademicStructure;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InstitutionMapper {

  public static Institution map(ResultSet rs) throws SQLException {
    Institution inst = new Institution();
    inst.setId(rs.getLong("id"));
    inst.setName(rs.getString("name"));
    inst.setCode(rs.getString("code"));
    inst.setCity(rs.getString("city"));
    inst.setStatus(rs.getString("status"));
    inst.setStudentCapacity(rs.getInt("student_capacity"));
    inst.setOpeningDate(rs.getDate("opening_date"));

    long structureId = rs.getLong("structure_id");
    if (!rs.wasNull()) {
      AcademicStructure structure = new AcademicStructure();
      structure.setId(structureId);
      inst.setStructure(structure);
    }

    return inst;
  }
}
