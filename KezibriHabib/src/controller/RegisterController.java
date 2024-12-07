package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Client;
import service.UserService;
import util.Role;
import util.UserInputValidator;
import java.sql.SQLException;


public class RegisterController {
	
    @FXML private VBox RegisterPage;
    @FXML private Hyperlink backToLogin;

    @FXML private TextField RegisterUsernameField;
    @FXML private TextField RegisterEmailField;
    @FXML private TextField RegisterPhoneField;
    @FXML private TextField RegisterAddressField;
    @FXML private PasswordField RegisterpasswordField;
    @FXML private PasswordField RegisterConfirmField;
    
    @FXML private Button RegisterButton;

    @FXML private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    void handleRegister(ActionEvent event) {
        // Reset error label
        errorLabel.setText("");
        errorLabel.setStyle(""); // Reset label style

        // Retrieve inputs
        String username = RegisterUsernameField.getText().trim();
        String password = RegisterpasswordField.getText().trim();
        String confirmPassword = RegisterConfirmField.getText().trim();
        String email = RegisterEmailField.getText().trim();
        String phone = RegisterPhoneField.getText().trim();
        String address = RegisterAddressField.getText().trim();

        // Validate inputs sequentially, one field at a time
        if (username.isEmpty()) {
            setError("Username is required.");
            return;
        }
        if (!UserInputValidator.isValidUsername(username)) {
            setError("Invalid username. It must consist of a first and last name separated by a space.");
            return;
        }

        if (email.isEmpty()) {
            setError("Email is required.");
            return;
        }
        if (!UserInputValidator.isValidEmail(email)) {
            setError("Invalid email format.");
            return;
        }

        if (phone.isEmpty()) {
            setError("Phone number is required.");
            return;
        }
        if (!UserInputValidator.isValidPhoneNumber(phone)) {
            setError("Invalid phone number. It must start with '0' and contain exactly 10 digits.");
            return;
        }

        if (address.isEmpty()) {
            setError("Address is required.");
            return;
        }
        if (!UserInputValidator.isValidAddress(address)) {
            setError("Invalid address. Ensure it follows: street, house/apartment, city, country (separated by commas).");
            return;
        }

        if (password.isEmpty()) {
            setError("Password is required.");
            return;
        }
        if (!UserInputValidator.isValidPassword(password)) {
            setError("Password must be at least 8 characters long and contain both letters and numbers.");
            return;
        }
        if (confirmPassword.isEmpty()) {
            setError("Confirm Password is required.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            setError("Passwords do not match.");
            return;
        }

        try {
            // Create a new Client object
            Client newClient = new Client(username, email, phone, address,  password);
            newClient.setRole(Role.client); // Explicitly set role as client

            // Call UserService to create the user
            userService.createUser(newClient);

            // Clear the form after successful registration
            clearForm();

            // Notify the user
            setSuccess("Registration successful. You can now log in.");

        } catch (SQLException e) {
            // Handle SQL exceptions
            setError("Failed to register: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    void handleBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            System.out.println("Loading FXML: /view/Login.fxml"); //
            Parent root = loader.load();

            Stage currentStage = (Stage) backToLogin.getScene().getWindow(); // Use the Hyperlink
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Login");
            currentStage.show();
        } catch (Exception e) {
            setError("Failed to navigate to the login page.");
            e.printStackTrace();
        }
    }


    private void clearForm() {
        RegisterUsernameField.clear();
        RegisterpasswordField.clear();
        RegisterConfirmField.clear();
        RegisterEmailField.clear();
        RegisterPhoneField.clear();
        RegisterAddressField.clear();
    }

    private void setError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void setSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: green;");
    }
}
