package tn.esprit.educlass.service;

import tn.esprit.educlass.model.User;
import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.enums.UserStatus;
import tn.esprit.educlass.mapper.UserMapper;
import tn.esprit.educlass.utlis.DataSource;
import tn.esprit.educlass.utlis.SupervisionLogger;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static final String USER_SELECT = """
            SELECT u.*, sc.name AS school_class_name, sc.code AS school_class_code, sc.level AS school_class_level
            FROM users u
            LEFT JOIN school_class sc ON u.school_class_id = sc.id
            """;

    private final Connection con;

    public UserService() {
        this.con = DataSource.getInstance().getCon();
    }

    // CREATE
    public boolean ajouter(User user) throws SQLException {
        String sql = "INSERT INTO users (first_name,last_name,email,password,role,status,school_class_id) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, user.getFirstName());
        ps.setString(2, user.getLastName());
        ps.setString(3, user.getEmail());
        ps.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        ps.setString(5, user.getRole().name());
        ps.setString(6, user.getStatus().name());
        bindSchoolClass(ps, 7, user);
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        if (success) SupervisionLogger.logSuccess("Create user: " + user.getEmail());
        return success;
    }

    // DELETE
    public boolean supprimer(User user) throws SQLException {
        String sql = "DELETE FROM users WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, user.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        if (success) SupervisionLogger.logSuccess("Delete user id=" + user.getId());
        return success;
    }

    // UPDATE (without password)
    public boolean modifier(User user) throws SQLException {
        String sql = "UPDATE users SET first_name=?, last_name=?, email=?, role=?, status=?, school_class_id=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, user.getFirstName());
        ps.setString(2, user.getLastName());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getRole().name());
        ps.setString(5, user.getStatus().name());
        bindSchoolClass(ps, 6, user);
        ps.setInt(7, user.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        if (success) SupervisionLogger.logSuccess("Update user: " + user.getEmail());
        return success;
    }

    // LIST ALL
    public List<User> afficher() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = USER_SELECT + " ORDER BY u.id";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            users.add(UserMapper.map(rs));
        }
        rs.close();
        st.close();
        return users;
    }

    // FIND BY ID
    public User findById(int id) throws SQLException {
        String sql = USER_SELECT + " WHERE u.id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        User u = null;
        if (rs.next()) {
            u = UserMapper.map(rs);
        }
        rs.close();
        ps.close();
        return u;
    }

    // FIND BY EMAIL (for uniqueness check)
    public User findByEmail(String email) throws SQLException {
        String sql = USER_SELECT + " WHERE u.email=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        User u = null;
        if (rs.next()) {
            u = UserMapper.map(rs);
        }
        rs.close();
        ps.close();
        return u;
    }

    // CHANGE STATUS
    public boolean changeStatus(int id, UserStatus status) throws SQLException {
        String sql = "UPDATE users SET status=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, status.name());
        ps.setInt(2, id);
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        if (success) SupervisionLogger.logSuccess("Change user status id=" + id + " to " + status.name());
        return success;
    }

    // FIND BY ROLE
    public List<User> findByRole(Role role) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = USER_SELECT + " WHERE u.role=? ORDER BY u.id";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, role.name());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            users.add(UserMapper.map(rs));
        }
        rs.close();
        ps.close();
        return users;
    }

    private void bindSchoolClass(PreparedStatement ps, int index, User user) throws SQLException {
        if (user.getRole() == Role.STUDENT && user.getSchoolClass() != null && user.getSchoolClass().getId() != null) {
            ps.setLong(index, user.getSchoolClass().getId());
        } else {
            ps.setNull(index, Types.BIGINT);
        }
    }
}

