package tn.esprit.educlass;

import javafx.application.Application;
import javafx.stage.Stage;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class EduClass extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/view/login.fxml")
        );

        Scene scene = new Scene(root);
        stage.setTitle("tn.esprit.educlass.EduClass - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args); // starts JavaFX
    }
}
