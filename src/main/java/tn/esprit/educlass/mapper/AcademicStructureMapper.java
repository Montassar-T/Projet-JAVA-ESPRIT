package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.AcademicStructure;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AcademicStructureMapper {

  public static AcademicStructure map(ResultSet rs) throws SQLException {
    AcademicStructure structure = new AcademicStructure();
    structure.setId(rs.getLong("id"));
    structure.setName(rs.getString("name"));
    structure.setType(rs.getString("type"));
    structure.setCode(rs.getString("code"));
    structure.setAddress(rs.getString("address"));
    structure.setManager(rs.getString("manager"));
    structure.setCreatedAt(rs.getTimestamp("created_at"));
    return structure;
  }
}
