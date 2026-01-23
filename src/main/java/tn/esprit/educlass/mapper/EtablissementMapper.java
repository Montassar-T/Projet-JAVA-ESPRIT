package tn.esprit.educlass.mapper;

import tn.esprit.educlass.enums.StatutEtablissement;
import tn.esprit.educlass.model.Etablissement;
import tn.esprit.educlass.model.StructureAcademique;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EtablissementMapper {

  public static Etablissement map(ResultSet rs) throws SQLException {
    Etablissement etab = new Etablissement();
    etab.setIdEtab(rs.getLong("id_etab"));
    etab.setNomEtab(rs.getString("nom_etab"));
    etab.setCodeEtab(rs.getString("code_etab"));
    etab.setVille(rs.getString("ville"));
    etab.setStatut(StatutEtablissement.valueOf(rs.getString("statut")));
    etab.setCapaciteEtudiants(rs.getInt("capacite_etudiants"));
    etab.setDateOuverture(rs.getDate("date_ouverture"));

    long structureId = rs.getLong("structure_id");
    if (!rs.wasNull()) {
      StructureAcademique structure = new StructureAcademique();
      structure.setIdStructure(structureId);
      etab.setStructure(structure);
    }

    return etab;
  }
}
