package tn.esprit.educlass.model;

import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.enums.UserStatus;

public class User extends BaseEntity {

    private int id;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private UserStatus status;

    private Role role;

    // Default constructor
    public User() {}

    // Constructor without ID (for new entities)
    public User(String firstName, String lastName, String email, String password, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    // Convenience method
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
