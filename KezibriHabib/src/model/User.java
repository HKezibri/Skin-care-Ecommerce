package model;

import util.Role;

public abstract class User {
    private int userId; // auto-generated
    private String username;
    private String password;
    private Role role;

    // Unified constructors
    public User(int userId, String username, String password, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    //for testing
    public User(String username, String password, Role role) {
        this(0, username, password, role); // Default userId to 0
    }
    
    public User(String username, String password) {
        this.username = username; 
    	this.password = password;

    }
    


    // Getters and setters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    public void setUserId(int userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }

    public abstract void displayInfo(); // Method to be overridden by subclasses
}
