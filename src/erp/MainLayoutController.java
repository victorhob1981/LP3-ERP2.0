package erp;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar; 
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import java.io.IOException;
import java.net.URL;

public class MainLayoutController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private MenuBar mainMenuBar;

    @FXML
    private BorderPane contentArea;

    @FXML
    public void initialize() {
        irParaDashboard(null); 
    }

   
    public void loadPage(String fxmlFileName) { 
        try {
            String fxmlPath = "/" + getClass().getPackage().getName().replace('.', '/') + "/" + fxmlFileName;
            URL fxmlUrl = getClass().getResource(fxmlPath);

            if (fxmlUrl == null) {
                System.err.println("Erro: Não foi possível encontrar o arquivo FXML: " + fxmlPath);
                mostrarAlerta("Erro de Carregamento", "Arquivo da interface '" + fxmlFileName + "' não encontrado.", Alert.AlertType.ERROR);
                contentArea.setCenter(new Label("Erro ao carregar: " + fxmlFileName + " não encontrado."));
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent pageRoot = loader.load();
            
           
            if ("TelaInicialContent.fxml".equals(fxmlFileName)) {
                Object loadedController = loader.getController();
                if (loadedController instanceof TelaInicialController) {
                    ((TelaInicialController) loadedController).setMainLayoutController(this);
                }
            }
            
            contentArea.setCenter(pageRoot);
        } catch (IOException e) {
            System.err.println("Erro ao carregar a página FXML: " + fxmlFileName);
            e.printStackTrace();
            mostrarAlerta("Erro de Carregamento", "Não foi possível carregar a página: " + fxmlFileName, Alert.AlertType.ERROR);
            contentArea.setCenter(new Label("Erro ao carregar: " + e.getMessage())); 
        }
    }

    @FXML
    public void irParaDashboard(ActionEvent event) { 
        loadPage("TelaInicialContent.fxml");
    }

    @FXML
    public void irParaRegistrarVenda(ActionEvent event) { 
        loadPage("TelaVendas.fxml");
    }

    @FXML
    public void irParaGerenciarEstoque(ActionEvent event) { 
        loadPage("TelaEstoque.fxml");
    }

    @FXML
    public void irParaGerenciarProdutos(ActionEvent event) {
        loadPage("TelaCadastroProduto.fxml");
    }

    @FXML
    private void mostrarSobre(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sobre");
        alert.setHeaderText("Sistema de Gestão de Loja Esportiva ERP"); 
        alert.setContentText("Desenvolvido para fins de aprendizado e demonstração.\nVersão: 2.0.0\nVictor Hugo de Oliveira Barbosa");
        alert.showAndWait();
    }

    @FXML
    private void acaoSair(ActionEvent event) {
        Platform.exit();
    }
    
    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}