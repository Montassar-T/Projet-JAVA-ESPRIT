package tn.esprit.educlass.mapper;

import tn.esprit.educlass.enums.ActionResult;
import tn.esprit.educlass.enums.ActionType;
import tn.esprit.educlass.model.Supervision;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SupervisionMapper {

  public static Supervision map(ResultSet rs) throws SQLException {
    Supervision supervision = new Supervision();
    supervision.setIdAction(rs.getLong("id_action"));
    supervision.setAction(rs.getString("action"));
    supervision.setUtilisateur(rs.getString("utilisateur"));
    supervision.setTypeAction(ActionType.valueOf(rs.getString("type_action")));
    supervision.setResultat(ActionResult.valueOf(rs.getString("resultat")));
    supervision.setDateAction(rs.getTimestamp("date_action"));
    return supervision;
  }
}
