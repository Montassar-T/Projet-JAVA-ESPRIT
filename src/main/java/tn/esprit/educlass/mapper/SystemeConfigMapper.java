package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.SystemConfig;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemeConfigMapper {

  public static SystemConfig map(ResultSet rs) throws SQLException {
    SystemConfig config = new SystemConfig();
    config.setIdConfig(rs.getLong("id_config"));
    config.setNomPlateforme(rs.getString("nom_plateforme"));
    config.setLangueDefault(rs.getString("langue_defaut"));
    config.setFuseauHoraire(rs.getString("fuseau_horaire"));
    config.setModeMaintenance(rs.getBoolean("mode_maintenance"));
    config.setEmailSupport(rs.getString("email_support"));
    config.setDateMaj(rs.getTimestamp("date_maj"));
    return config;
  }
}
