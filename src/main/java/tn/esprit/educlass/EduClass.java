package tn.esprit.educlass;

import javafx.application.Application;
import javafx.stage.Stage;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class EduClass extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/academic_structure.fxml"));
            Scene scene = new Scene(root);
            stage.setTitle("EduClass - Management");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Failed to load application view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args); // starts JavaFX
    }
}
