package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import service.AuthentificationService;
import java.io.IOException;
import util.SessionContext;

public class LoginController {

    @FXML
    private TextField LoginUsernameField;

    @FXML
    private PasswordField LoginpasswordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;
    
    @FXML
    private Hyperlink RegisterPage;
    
    @FXML
    private Hyperlink backToWelcome;

    private final AuthentificationService authService = new AuthentificationService();

    /**
     * Handles login attempts by validating inputs and authenticating the user.
     */
    @FXML
    void handleLogin(ActionEvent event) {
        // Reset the error label
        resetErrorLabel();

        // Retrieve input from text fields
        String username = LoginUsernameField.getText().trim();
        String password = LoginpasswordField.getText().trim();

        // Validate input
        if (username.isBlank() || password.isBlank()) {
            setError("Please enter both username and password.");
            return;
        }

        try {
            // Debug: Logging the login attempt
            System.out.println("Attempting login with username: " + username);

            // Authenticate the user
            User authenticatedUser = authService.authenticate(username, password);

            if (authenticatedUser == null) {
                setError("Authentication failed: User not found.");
                return;
            }

            // Debug: Log successful authentication
            System.out.println("Login successful for: " + authenticatedUser.getUsername());

            // Store the authenticated user in the SessionContext
            SessionContext.getInstance().setLoggedInUser(authenticatedUser);

            // Determine the appropriate FXML to load based on the user's role
            FXMLLoader loader;
            String fxmlPath;
            if (authenticatedUser instanceof model.Admin) {
                fxmlPath = "/view/AdminPage.fxml";
            } else if (authenticatedUser instanceof model.Client) {
                fxmlPath = "/view/ClientShoppingPage.fxml";
            } else {
                setError("Unrecognized user role.");
                return;
            }

            // Load the FXML
            loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Navigate to the appropriate page
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(authenticatedUser instanceof model.Admin ? "Admin Dashboard" : "Client Shopping");

            // Center the stage on the screen
            stage.centerOnScreen();

            // Disable resizing and fullscreen capability
            stage.setResizable(false);

            // Show the stage with the specified settings
            stage.show();

            // Clear login fields
            LoginUsernameField.clear();
            LoginpasswordField.clear();

        } catch (AuthentificationService.AuthenticationException e) {
            setError("Login failed: " + e.getMessage());
        } catch (IOException e) {
            setError("Failed to load the page. Please try again.");
            e.printStackTrace();
        } catch (Exception e) {
            setError("An unexpected error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the registration page.
     */
    @FXML
    void handleRegisterPage(ActionEvent event) {
        try {
            // Load the Register.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Register.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage currentStage = (Stage) RegisterPage.getScene().getWindow();

            // Set the new scene and title
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Register");

            // Center the stage on the screen
            currentStage.centerOnScreen();

            // Disable resizing and fullscreen capability
            currentStage.setResizable(false);

            // Show the registration page
            currentStage.show();
        } catch (Exception e) {
            setError("Failed to navigate to the register page.");
            e.printStackTrace(); // Log stack trace for debugging
        }
    }

    /**
     * Resets the error label to its default state.
     */
    private void resetErrorLabel() {
        errorLabel.setText("");
        errorLabel.setStyle("");
    }

    /**
     * Sets an error message to the error label with red text color.
     *
     * @param message The error message to display.
     */
    private void setError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;"); // Style for error
    }
    
    
    @FXML
    void handleBackToWelcome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/WelcomePage.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) backToWelcome.getScene().getWindow();

            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Welcome Page");
            currentStage.centerOnScreen();

            currentStage.show();
        } catch (Exception e) {
            setError("Failed to navigate to the welcome page.");
            e.printStackTrace();
        }
    }

}
