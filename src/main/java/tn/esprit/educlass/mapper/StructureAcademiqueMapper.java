package tn.esprit.educlass.mapper;

import tn.esprit.educlass.enums.StructureType;
import tn.esprit.educlass.model.StructureAcademique;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StructureAcademiqueMapper {

  public static StructureAcademique map(ResultSet rs) throws SQLException {
    StructureAcademique structure = new StructureAcademique();
    structure.setIdStructure(rs.getLong("id_structure"));
    structure.setNomStructure(rs.getString("nom_structure"));
    structure.setTypeStructure(StructureType.valueOf(rs.getString("type_structure")));
    structure.setCodeStructure(rs.getString("code_structure"));
    structure.setAdresse(rs.getString("adresse"));
    structure.setResponsable(rs.getString("responsable"));
    structure.setDateCreation(rs.getTimestamp("date_creation"));
    return structure;
  }
}
