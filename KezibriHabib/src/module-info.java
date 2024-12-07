module KezibriHabib {
	requires javafx.controls;
	
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base;
	requires javafx.graphics;

     // Makes the `controller` package accessible to other modules.
    opens controller to javafx.fxml; // Allows reflection for FXML controllers.
	
	opens application to javafx.graphics, javafx.fxml;
}
