package tn.esprit.educlass.model;

import tn.esprit.educlass.enums.Role;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private Role role;  // using enum now
    private String fullName;

    public User() {}

    public User(int id, String username, String email, String password, Role role, String fullName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
    }
}
