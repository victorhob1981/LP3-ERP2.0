package erp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Carrega o arquivo FXML da interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainLayout.fxml"));
        Parent root = loader.load();

        // Cria a cena com o root carregado
        Scene scene = new Scene(root);

        // Configura a janela principal
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
