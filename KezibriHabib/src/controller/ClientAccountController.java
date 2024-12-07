package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Client;
import service.UserService;
import util.SessionContext;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import util.UserInputValidator;

public class ClientAccountController {

    @FXML private TextField UsernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField ConfirmField;
    @FXML private TextField EmailField;
    @FXML private TextField PhoneField;
    @FXML private TextField AddressField;
    @FXML private Button update,clientAccount;
    @FXML private Button clientLogout;

    private Client client;
    private final UserService userService = new UserService();
    
    @FXML
    public void initialize() {
        try {
            // Retrieve logged-in user
            Object loggedInUser = SessionContext.getInstance().getLoggedInUser();

            if (loggedInUser instanceof Client) {
                client = (Client) loggedInUser;
                int clientId = client.getClientId(); // Retrieve Client ID
                System.out.println("Client ID: " + clientId);

                // Fetch client details using UserService
                client = userService.getClientById(clientId);

                // Populate fields
                populateFields(client);
                clientAccount.setText(formatUsername(client.getUsername()));
            } else {
                showError("Logged-in user is not a Client.");
            }
        } catch (SQLException e) {
            showError("Failed to retrieve client information: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void populateFields(Client client) {
        if (client == null) {
            showError("Client information is not available.");
            return;
        }

        // Debugging: log client data to ensure proper values
        System.out.println("Populating fields with client data:");
        System.out.println("Username: " + client.getUsername());
        System.out.println("Email: " + client.getEmail());
        System.out.println("Phone: " + client.getPhone());
        System.out.println("Address: " + client.getAddress());

        // Populate fields with the client's current data
        UsernameField.setText(Optional.ofNullable(client.getUsername()).orElse(""));
        EmailField.setText(Optional.ofNullable(client.getEmail()).orElse(""));
        PhoneField.setText(Optional.ofNullable(client.getPhone()).orElse(""));
        AddressField.setText(Optional.ofNullable(client.getAddress()).orElse(""));
    }



    @FXML
    private void handleUpdateClientInfo() {
        if (client == null) {
            showError("No client information available to update.");
            return;
        }

        try {
            // Validate username
            String username = UsernameField.getText();
            if (!UserInputValidator.isValidUsername(username)) {
                showError("Invalid username. Please provide a first and last name.");
                return;
            }

            // Validate email
            String email = EmailField.getText();
            if (!UserInputValidator.isValidEmail(email)) {
                showError("Invalid email format. Please enter a valid email address.");
                return;
            }

            // Validate phone number
            String phone = PhoneField.getText();
            if (!UserInputValidator.isValidPhoneNumber(phone)) {
                showError("Invalid phone number. It must start with '0' and have 10 digits.");
                return;
            }

            // Validate address
            String address = AddressField.getText();
            if (!UserInputValidator.isValidAddress(address)) {
                showError("Invalid address. Ensure it follows the format: street, house or apartment number, city, country (separated by commas).");
                return;
            }

            // Validate password (if provided)
            String password = passwordField.getText();
            if (!password.isEmpty()) {
                if (!UserInputValidator.isValidPassword(password)) {
                    showError("Invalid password. It must be at least 8 characters long and contain letters and numbers.");
                    return;
                }
                String confirmPassword = ConfirmField.getText();
                if (!password.equals(confirmPassword)) {
                    showError("Passwords do not match.");
                    return;
                }
            }

            // Update client object
            client.setUsername(username);
            client.setEmail(email);
            client.setPhone(phone);
            client.setAddress(address);

            // Update password only if provided
            if (!password.isEmpty()) {
                client.setPassword(password);
            }

            // Update the client in the database
            userService.updateClient(client);

            // Success message
            showInfo("Profile updated successfully.");
        } catch (SQLException e) {
            showError("Failed to update profile: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void DeleteAccountButton() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Are you sure you want to delete your account?");
        alert.setContentText("This action cannot be undone.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                userService.deleteUser(client.getClientId());
                showInfo("Account deleted successfully.");

                // Log out and return to the login page
                SessionContext.getInstance().setLoggedInUser(null);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginPage.fxml"));
                Stage stage = (Stage) update.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Login Page");
                stage.show();
            } catch (SQLException | IOException e) {
                showError("Failed to delete account: " + e.getMessage());
            }
        }
    }

    @FXML
    void GoToHomePage(ActionEvent event) {
        navigateTo("/view/ClientShoppingPage.fxml", "Home Page");
    }

    @FXML
    void goToOrders(ActionEvent event) {
        navigateTo("/view/ClientOrdersPage.fxml", "Your Orders");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("You will be redirected to the login page.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            navigateTo("/view/WelcomePage.fxml", "Welcome Page");
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            Stage stage = (Stage) update.getScene().getWindow();
            stage.setScene(new Scene(page));
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Failed to load the page: " + title + ". Error: " + e.getMessage());
        }
    }

 
    
    /**
     * Utility method to capitalize the first letter of each word in the username.
     * Example: "john doe" -> "John Doe"
     */
    private String formatUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "";
        }

        String[] words = username.split("\\s+"); // Split by whitespace
        StringBuilder formattedUsername = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formattedUsername.append(Character.toUpperCase(word.charAt(0)))  // Capitalize first letter
                                 .append(word.substring(1).toLowerCase())       // Rest in lowercase
                                 .append(" ");
            }
        }

        return formattedUsername.toString().trim(); // Remove trailing space
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
