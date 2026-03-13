package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.Supervision;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class SupervisionMapper {

  public static Supervision map(ResultSet rs) throws SQLException {
    Supervision supervision = new Supervision();
    try { supervision.setId(rs.getLong("id")); } catch (Exception ignored) {}
    try { supervision.setAction(rs.getString("action")); } catch (Exception ignored) {}
    try { supervision.setUser(rs.getString("user")); } catch (Exception ignored) {}
    try { supervision.setType(rs.getString("type")); } catch (Exception ignored) {}
    try { supervision.setResult(rs.getString("result")); } catch (Exception ignored) {}
    try {
      Timestamp ts = rs.getTimestamp("timestamp");
      supervision.setTimestamp(ts != null ? new Date(ts.getTime()) : null);
    } catch (Exception e) {
      try {
        String raw = rs.getString("timestamp");
        if (raw != null && !raw.isBlank()) {
          supervision.setTimestamp(Timestamp.valueOf(raw));
        }
      } catch (Exception ignored) {
        supervision.setTimestamp(new Date());
      }
    }
    return supervision;
  }
}
