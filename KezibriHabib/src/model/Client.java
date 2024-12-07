package model;

import util.Role;

public class Client extends User {
    private int clientId;
    private String email;
    private String phone;
    private String address;

    // Constructor with all fields
    public Client(int clientId, int userId, String username, String email, String phone, String address, String password) {
        super(userId, username, password, Role.client); // Call User constructor
        this.clientId = clientId;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
    
 // Constructor with client info
    public Client(String username, String email, String phone, String address, String password) {
        super(username, password); // Call User constructor
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
    


    // Getters and setters
    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public void displayInfo() {
        System.out.println("Client Username: " + getUsername());
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);
        System.out.println("Address: " + address);
    }
}
