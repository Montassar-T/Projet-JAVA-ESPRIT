package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.User;
import tn.esprit.educlass.model.SchoolClass;
import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.enums.UserStatus;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class UserMapper {

    public static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setPassword(rs.getString("password"));
        u.setEmail(rs.getString("email"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setStatus(UserStatus.valueOf(rs.getString("status")));

        if (hasColumn(rs, "school_class_id") && rs.getObject("school_class_id") != null) {
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setId(rs.getLong("school_class_id"));
            if (hasColumn(rs, "school_class_name")) {
                schoolClass.setName(rs.getString("school_class_name"));
            }
            if (hasColumn(rs, "school_class_code")) {
                schoolClass.setCode(rs.getString("school_class_code"));
            }
            if (hasColumn(rs, "school_class_level")) {
                schoolClass.setLevel(rs.getString("school_class_level"));
            }
            u.setSchoolClass(schoolClass);
        }

        return u;
    }

    private static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }
}

