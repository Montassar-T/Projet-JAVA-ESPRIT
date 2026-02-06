package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.SystemConfig;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemConfigMapper {

  public static SystemConfig map(ResultSet rs) throws SQLException {
    SystemConfig config = new SystemConfig();
    config.setId(rs.getLong("id"));
    config.setPlatformName(rs.getString("platform_name"));
    config.setDefaultLanguage(rs.getString("default_language"));
    config.setTimezone(rs.getString("timezone"));
    config.setMaintenanceMode(rs.getBoolean("maintenance_mode"));
    config.setSupportEmail(rs.getString("support_email"));
    config.setUpdatedAt(rs.getTimestamp("updated_at"));
    return config;
  }
}
