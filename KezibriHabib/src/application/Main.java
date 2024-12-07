package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class Main extends Application {
	@Override
    public void start(Stage primaryStage) {
        try {
        	// Load the Welcome Page
            Parent root = FXMLLoader.load(getClass().getResource("/view/WelcomePage.fxml"));

            // Set up the primary stage
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            //Icon
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/util/icon.png")));
            primaryStage.setTitle("Welcome Page");
            // Center the stage on the screen
            primaryStage.centerOnScreen();
            // Disable resizing and fullscreen capability
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	
	public static void main(String[] args) {
		launch(args);
	}
}
