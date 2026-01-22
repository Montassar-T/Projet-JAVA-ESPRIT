package tn.esprit.educlass.service;

import tn.esprit.educlass.model.User;
import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.enums.UserStatus;
import tn.esprit.educlass.mapper.UserMapper;
import tn.esprit.educlass.utlis.DataSource;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private Connection con;

    public UserService() {
        this.con = DataSource.getInstance().getCon();
    }

    // CREATE USER
    public boolean ajouter(User user) throws SQLException {
        String sql = "INSERT INTO users (first_name,last_name,email,password,role,UserStatus) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, user.getFirstName());
        ps.setString(2, user.getLastName());
        ps.setString(3, user.getEmail());
        ps.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt())); // hash password
        ps.setString(5, user.getRole().name());
        ps.setString(6, user.getStatus().name());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    // DELETE USER
    public boolean supprimer(User user) throws SQLException {
        String sql = "DELETE FROM users WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, user.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    // UPDATE USER (all fields including password)
    public boolean modifier(User user) throws SQLException {
        String sql = "UPDATE users SET first_name=?, last_name=?, email=?, password=?, role=?, UserStatus=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, user.getFirstName());
        ps.setString(2, user.getLastName());
        ps.setString(3, user.getEmail());
        ps.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt())); // hash updated password
        ps.setString(5, user.getRole().name());
        ps.setString(6, user.getStatus().name());
        ps.setInt(7, user.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    // LIST ALL USERS
    public List<User> afficher() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            users.add(UserMapper.map(rs)); // centralized mapping
        }
        rs.close();
        st.close();
        return users;
    }

    // FIND USER BY ID
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id=?";
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

    // CHANGE USER UserStatus ONLY
    public boolean changeStatus(int id, UserStatus UserStatus) throws SQLException {
        String sql = "UPDATE users SET UserStatus=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, UserStatus.name());
        ps.setInt(2, id);
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        return success;
    }

    // FIND USERS BY ROLE
    public List<User> findByRole(Role role) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role=?";
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
}
