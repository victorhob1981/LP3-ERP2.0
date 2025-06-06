package erp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
       
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainLayout.fxml"));
        Parent root = loader.load();

       
        Scene scene = new Scene(root);

        stage.setTitle("ERP 2.0");
        stage.setScene(scene);
        stage.setWidth(800);
        stage.setHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
