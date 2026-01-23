package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.User;
import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.enums.UserStatus;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper {

    public static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setPassword(rs.getString("password").trim());
        u.setEmail(rs.getString("email"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setStatus(UserStatus.valueOf(rs.getString("status")));
        return u;
    }
}
