package tn.esprit.educlass.model;

import tn.esprit.educlass.enums.Role;

public class User extends BaseEntity {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;  // using enum now

    public User() {}

    public User(int id,String firstName, String lastName, String email,Role role, String fullName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

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

    // Convenience method
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
