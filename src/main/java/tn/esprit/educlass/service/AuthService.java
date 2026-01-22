package tn.esprit.educlass.service;

import tn.esprit.educlass.mapper.UserMapper;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.utlis.DataSource;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class AuthService {

    private Connection con = DataSource.getInstance().getCon();

    public User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        User user = null;
        if(rs.next()) {
            user = UserMapper.map(rs);
            if(!BCrypt.checkpw(password, user.getPassword())) {
                user = null;
            }
        }

        rs.close();
        ps.close();
        return user;
    }

    public boolean changePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        ps.setInt(2, userId);
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }
}
