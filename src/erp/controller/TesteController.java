package erp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class TesteController {

    @FXML
    private Label meuLabelDeTeste;

    @FXML
    private TextField meuTextFieldDeTeste;

    @FXML
    public void initialize() {
        System.out.println("--- CONTROLLER DE TESTE INICIALIZADO ---");
        
        if (meuLabelDeTeste != null) {
            meuLabelDeTeste.setText("O Label está visível!");
            System.out.println("--- Label de teste foi encontrado e configurado. ---");
        } else {
            System.out.println("--- ERRO: Label de teste é NULO. ---");
        }

        if (meuTextFieldDeTeste != null) {
            meuTextFieldDeTeste.setPromptText("O TextField está visível!");
            System.out.println("--- TextField de teste foi encontrado e configurado. ---");
        } else {
            System.out.println("--- ERRO: TextField de teste é NULO. ---");
        }
    }
}