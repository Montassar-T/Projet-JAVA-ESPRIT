package tn.esprit.educlass.service;

import tn.esprit.educlass.enums.UserStatus;
import tn.esprit.educlass.mapper.UserMapper;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.utlis.DataSource;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class AuthService {

    private final Connection con;

    // Login result constants
    public static final int LOGIN_SUCCESS = 0;
    public static final int LOGIN_INVALID_CREDENTIALS = 1;
    public static final int LOGIN_ACCOUNT_INACTIVE = 2;
    public static final int LOGIN_ACCOUNT_SUSPENDED = 3;

    private User lastLoggedUser;

    public AuthService() {
        this.con = DataSource.getInstance().getCon();
    }

    public int authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        lastLoggedUser = null;

        if (rs.next()) {
            User user = UserMapper.map(rs);
            rs.close();
            ps.close();

            // Check password
            if (!BCrypt.checkpw(password, user.getPassword())) {
                return LOGIN_INVALID_CREDENTIALS;
            }

            // Check status
            if (user.getStatus() == UserStatus.INACTIVE) {
                return LOGIN_ACCOUNT_INACTIVE;
            }
            if (user.getStatus() == UserStatus.SUSPENDED) {
                return LOGIN_ACCOUNT_SUSPENDED;
            }

            lastLoggedUser = user;
            return LOGIN_SUCCESS;
        }

        rs.close();
        ps.close();
        return LOGIN_INVALID_CREDENTIALS;
    }

    public User getLastLoggedUser() {
        return lastLoggedUser;
    }

    // Keep legacy method for password verification in settings
    public User login(String email, String password) throws SQLException {
        int result = authenticate(email, password);
        return result == LOGIN_SUCCESS ? lastLoggedUser : null;
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

