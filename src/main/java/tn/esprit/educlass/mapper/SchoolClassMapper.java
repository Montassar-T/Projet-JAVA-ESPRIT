package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.SchoolClass;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SchoolClassMapper {

    public static SchoolClass map(ResultSet rs) throws SQLException {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(rs.getLong("id"));
        schoolClass.setName(rs.getString("name"));
        schoolClass.setCode(rs.getString("code"));
        schoolClass.setLevel(rs.getString("level"));
        Object capacity = rs.getObject("capacity");
        schoolClass.setCapacity(capacity == null ? null : rs.getInt("capacity"));
        schoolClass.setCreatedAt(rs.getTimestamp("created_at"));
        return schoolClass;
    }
}
