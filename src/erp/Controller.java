package erp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;

public class Controller {

    @FXML
    private TextField txtNomeCliente;

    @FXML
    private TextField txtModelo;

    @FXML
    private TextField txtTamanho;

    @FXML
    private DatePicker dpDataVenda;

    @FXML
    private CheckBox chkPago;

    @FXML
    private DatePicker dpDataPrometida;

    @FXML
    private ComboBox<String> cbMetodoPagamento;

    @FXML
    private TextField txtDesconto;

    @FXML
    private Button btnSalvarVenda;

    @FXML
    private Button btnVoltar;


    @FXML
    private Label lblFaturamento;

    @FXML
    private Label lblQuantidadeVendas;

    @FXML
    private TextField txtValorVenda;

    @FXML
    private Button btnIrParaEstoque;

    // Método para ir para a tela de Estoque
    @FXML
    public void irParaEstoque() {
        try {
            // Carregar o FXML da tela de Estoque
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TelaEstoque.fxml"));
            BorderPane estoqueRoot = loader.load();

            // Criar uma nova cena com o conteúdo da tela de Estoque
            Scene estoqueScene = new Scene(estoqueRoot);

            // Obter a janela principal (Stage)
            Stage stage = (Stage) btnIrParaEstoque.getScene().getWindow();
            
            // Setar a nova cena no Stage (janela)
            stage.setScene(estoqueScene);
            stage.setTitle("Estoque");
            stage.show();
        } catch (IOException e) {
            System.out.println("Erro ao carregar a tela de estoque: " + e.getMessage());
        }
    }

    

@FXML
public void initialize() {
    // Popula ComboBox com opções
    cbMetodoPagamento.getItems().addAll("Dinheiro", "Cartão", "Pix");
    cbMetodoPagamento.getSelectionModel().selectFirst();

    // Desabilita dpDataPrometida inicialmente
    dpDataPrometida.setDisable(true);

    // Listener para habilitar/desabilitar dpDataPrometida conforme chkPago
    chkPago.setOnAction(e -> {
        dpDataPrometida.setDisable(chkPago.isSelected());
        if (chkPago.isSelected()) {
            dpDataPrometida.setValue(null);
        }
    });

    // Atualiza o cabeçalho após o FXML ser carregado
    Platform.runLater(() -> {
        atualizarCabecalho();
    });
}


    @FXML
public void salvarVenda(ActionEvent event) {
    String sql = "INSERT INTO venda "
            + "(nome_cliente, modelo, tamanho, data_venda, pago, data_prometida, metodo_pagamento, desconto, valor_total) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection con = UTIL.ConexaoBanco.conectar();
         PreparedStatement pst = con.prepareStatement(sql)) {

        String nomeCliente = txtNomeCliente.getText().trim();
        String modelo = txtModelo.getText().trim();
        String tamanho = txtTamanho.getText().trim();
        String metodoPagamento = cbMetodoPagamento.getValue();
        String descontoStr = txtDesconto.getText().trim();
        String valorVendaStr = txtValorVenda.getText().trim(); // Valor da venda

        double desconto = 0.0;
        if (!descontoStr.isEmpty()) {
            desconto = Double.parseDouble(descontoStr);
        }

        double valorVenda = 0.0;
        if (!valorVendaStr.isEmpty()) {
            valorVenda = Double.parseDouble(valorVendaStr); // Valor da venda

            // Aplica o desconto no valor da venda
            valorVenda -= desconto;
        }

        java.sql.Date dataVenda = null;
        if (dpDataVenda.getValue() != null) {
            dataVenda = java.sql.Date.valueOf(dpDataVenda.getValue());
        }

        java.sql.Date dataPrometida = null;
        if (dpDataPrometida.getValue() != null) {
            dataPrometida = java.sql.Date.valueOf(dpDataPrometida.getValue());
        }

        boolean pago = chkPago.isSelected();

        // Validações básicas
        if (nomeCliente.isEmpty() || modelo.isEmpty() || tamanho.isEmpty() || dataVenda == null || valorVenda == 0) {
            mostrarAlerta("Erro", "Por favor, preencha todos os campos obrigatórios.");
            return;
        }

        if (!pago && dataPrometida == null) {
            mostrarAlerta("Erro", "Informe a data prometida para o pagamento.");
            return;
        }

        // Preenche parâmetros do PreparedStatement
        pst.setString(1, nomeCliente);
        pst.setString(2, modelo);
        pst.setString(3, tamanho);
        pst.setDate(4, dataVenda);
        pst.setBoolean(5, pago);
        pst.setDate(6, dataPrometida);
        pst.setString(7, metodoPagamento);
        pst.setDouble(8, desconto);
        pst.setDouble(9, valorVenda); // Valor total da venda

        int affectedRows = pst.executeUpdate();

        if (affectedRows > 0) {
            mostrarAlerta("Sucesso", "Venda salva com sucesso!");
            limparFormulario();
            atualizarCabecalho();  // Atualiza o cabeçalho após salvar
        } else {
            mostrarAlerta("Erro", "Erro ao salvar a venda.");
        }

    } catch (NumberFormatException e) {
        mostrarAlerta("Erro", "Valor ou desconto inválido. Informe um número válido.");
    } catch (SQLException e) {
        mostrarAlerta("Erro", "Erro ao conectar ou inserir no banco: " + e.getMessage());
    } catch (Exception e) {
        mostrarAlerta("Erro", "Erro inesperado: " + e.getMessage());
    }
}



    @FXML
    public void voltar(ActionEvent event) {
        limparFormulario();
        // Você pode implementar lógica para fechar tela ou voltar para outra, se quiser
        System.out.println("Botão Voltar clicado - formulário limpo.");
    }

    private void limparFormulario() {
        txtNomeCliente.clear();
        txtModelo.clear();
        txtTamanho.clear();
        dpDataVenda.setValue(null);
        chkPago.setSelected(false);
        dpDataPrometida.setValue(null);
        dpDataPrometida.setDisable(true);
        cbMetodoPagamento.getSelectionModel().selectFirst();
        txtDesconto.clear();
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    public void atualizarCabecalho() {
    String sqlFaturamento = "SELECT COALESCE(SUM(valor_total),0) AS total FROM venda "
            + "WHERE MONTH(data_venda) = MONTH(CURDATE()) AND YEAR(data_venda) = YEAR(CURDATE())";

    String sqlQuantidade = "SELECT COUNT(*) AS total_vendas FROM venda "
            + "WHERE MONTH(data_venda) = MONTH(CURDATE()) AND YEAR(data_venda) = YEAR(CURDATE())";

    try (Connection con = UTIL.ConexaoBanco.conectar()) {
        // Faturamento
        try (PreparedStatement pst = con.prepareStatement(sqlFaturamento);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                double faturamento = rs.getDouble("total");
                lblFaturamento.setText(String.format("Faturamento do Mês: R$ %.2f", faturamento));
            }
        }

        // Quantidade de vendas
        try (PreparedStatement pst2 = con.prepareStatement(sqlQuantidade);
             ResultSet rs2 = pst2.executeQuery()) {
            if (rs2.next()) {
                int qtdVendas = rs2.getInt("total_vendas");
                lblQuantidadeVendas.setText("Quantidade de Vendas: " + qtdVendas);
            }
        }
    } catch (SQLException e) {
        System.err.println("Erro ao atualizar cabeçalho: " + e.getMessage());
        // Opcional: mostrar alerta ou definir textos padrão
    }
}
}
