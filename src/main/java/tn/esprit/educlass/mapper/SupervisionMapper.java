package tn.esprit.educlass.mapper;

import tn.esprit.educlass.enums.ActionResult;
import tn.esprit.educlass.enums.ActionType;
import tn.esprit.educlass.model.Supervision;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SupervisionMapper {

  public static Supervision map(ResultSet rs) throws SQLException {
    Supervision supervision = new Supervision();
    supervision.setId(rs.getLong("id"));
    supervision.setAction(rs.getString("action"));
    supervision.setUser(rs.getString("user"));
    supervision.setType(ActionType.valueOf(rs.getString("type")));
    supervision.setResult(ActionResult.valueOf(rs.getString("result")));
    supervision.setTimestamp(rs.getTimestamp("timestamp"));
    return supervision;
  }
}
