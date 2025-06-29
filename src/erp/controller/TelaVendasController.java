package erp.controller;

import erp.model.ProdutoVO;
import UTIL.ConexaoBanco;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class TelaVendasController implements Initializable {
    @FXML private ComboBox<ProdutoVO> cbProduto;
    @FXML private TextField txtQuantidadeVendida;
    @FXML private TextField txtValorVenda;
    @FXML private TextField txtNomeCliente;
    @FXML private DatePicker dpDataVenda;
    @FXML private TextField txtDesconto;
    @FXML private CheckBox chkPago;
    @FXML private DatePicker dpDataPrometida;
    @FXML private ComboBox<String> cbMetodoPagamento;
    @FXML private Label lblSubtotalCalculado;
    @FXML private Label lblDescontoAplicado;
    @FXML private Label lblTotalAPagar;

    @FXML private Button btnSalvarVenda;
    @FXML private Button btnLimparCampos;
    
    @FXML private Label lblFaturamento;
    @FXML private Label lblQuantidadeVendas;

    private ObservableList<ProdutoVO> listaProdutosSugeridos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarComboBoxProduto();
        configurarListeners();
        limparFormularioVenda(); 
        
        cbMetodoPagamento.getItems().addAll("Pix", "Cartão de Crédito", "Dinheiro");
        cbMetodoPagamento.getSelectionModel().selectFirst();
    
    }

    

    private void configurarComboBoxProduto() {
        listaProdutosSugeridos = FXCollections.observableArrayList();
        cbProduto.setItems(listaProdutosSugeridos);
        cbProduto.setEditable(true);

        cbProduto.setConverter(new StringConverter<ProdutoVO>() {
            @Override
            public String toString(ProdutoVO produto) {
                return produto == null ? "" : produto.getDescricaoCompleta();
            }

            @Override
            public ProdutoVO fromString(String string) {
                if (cbProduto.getValue() != null && cbProduto.getValue().getDescricaoCompleta().equals(string)) {
                    return cbProduto.getValue();
                }
                return null;
            }
        });

        cbProduto.setCellFactory(_ -> new ListCell<ProdutoVO>() {
            @Override
            protected void updateItem(ProdutoVO produto, boolean empty) {
                super.updateItem(produto, empty);
                setText(empty || produto == null ? null : produto.getDescricaoCompleta() + " (Estoque: " + produto.getQuantidadeEstoque() + ")");
            }
        });
        cbProduto.getEditor().textProperty().addListener((_, _, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                listaProdutosSugeridos.clear();
                cbProduto.hide();
            } else {
                if (cbProduto.isFocused() && (cbProduto.getSelectionModel().getSelectedItem() == null || 
                   !newValue.equals(cbProduto.getSelectionModel().getSelectedItem().getDescricaoCompleta()))) {
                    buscarProdutosSugeridos(newValue);
                }
            }
        });

        cbProduto.valueProperty().addListener((_, _, newProduto) -> {
            if (newProduto != null) {
                txtValorVenda.setText(String.format("%.2f", newProduto.getPrecoVendaAtual()).replace(",", "."));
                Platform.runLater(() -> txtQuantidadeVendida.requestFocus());
            } else {
                txtValorVenda.clear();
            }
            atualizarResumoVenda();
        });
    }

    private void configurarListeners() {
        if (btnSalvarVenda != null) {
            btnSalvarVenda.setOnAction(event -> salvarVenda(event));
        }
        txtQuantidadeVendida.textProperty().addListener((_, _, _) -> atualizarResumoVenda());
        txtValorVenda.textProperty().addListener((_, _, _) -> atualizarResumoVenda());
        txtDesconto.textProperty().addListener((_, _, _) -> atualizarResumoVenda());
        chkPago.setOnAction(_ -> {
            dpDataPrometida.setDisable(chkPago.isSelected());
            if (chkPago.isSelected()) {
                dpDataPrometida.setValue(null);
            }
        });
    }

    private void buscarProdutosSugeridos(String textoBusca) {
        Platform.runLater(() -> {
            listaProdutosSugeridos.clear();
            if (textoBusca == null || textoBusca.length() < 2) {
                cbProduto.hide();
                return;
            }
            String sql = "SELECT ProdutoID, DescricaoCompleta, PrecoVendaAtual, CustoMedioPonderado, QuantidadeEstoque FROM Produtos WHERE (DescricaoCompleta LIKE ?) AND QuantidadeEstoque > 0 LIMIT 10";
            try (Connection con = ConexaoBanco.conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, "%" + textoBusca + "%");
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    listaProdutosSugeridos.add(new ProdutoVO(
                            rs.getInt("ProdutoID"), rs.getString("DescricaoCompleta"), rs.getDouble("PrecoVendaAtual"),
                            rs.getDouble("CustoMedioPonderado"), rs.getInt("QuantidadeEstoque")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (!listaProdutosSugeridos.isEmpty()) cbProduto.show(); else cbProduto.hide();
        });
    }
    
    private void atualizarResumoVenda() {
        try {
            int quantidade = txtQuantidadeVendida.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtQuantidadeVendida.getText().trim());
            double precoUnitario = txtValorVenda.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtValorVenda.getText().trim().replace(",", "."));
            double desconto = txtDesconto.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtDesconto.getText().trim().replace(",", "."));
            double subtotal = quantidade * precoUnitario;
            double totalAPagar = subtotal - desconto;
            lblSubtotalCalculado.setText(String.format("R$ %.2f", subtotal));
            lblDescontoAplicado.setText(String.format("R$ %.2f", desconto));
            lblTotalAPagar.setText(String.format("R$ %.2f", totalAPagar));
        } catch (NumberFormatException e) {
            lblSubtotalCalculado.setText("R$ 0,00");
            lblDescontoAplicado.setText("R$ 0,00");
            lblTotalAPagar.setText("R$ 0,00");
        }
    }

    private void salvarVenda(ActionEvent event) {
        ProdutoVO produtoSelecionado = cbProduto.getValue();
        
        if (produtoSelecionado == null || dpDataVenda.getValue() == null || txtQuantidadeVendida.getText().trim().isEmpty() || txtValorVenda.getText().trim().isEmpty()) {
            mostrarAlerta("Erro de Validação", "Produto, Data, Quantidade e Preço Unitário são obrigatórios.", Alert.AlertType.ERROR);
            return;
        }

        double precoUnitarioVenda;
        int quantidadeVendida;
        double valorDesconto = 0.0;

        try {
            precoUnitarioVenda = Double.parseDouble(txtValorVenda.getText().trim().replace(",", "."));
            quantidadeVendida = Integer.parseInt(txtQuantidadeVendida.getText().trim());
            if (!txtDesconto.getText().trim().isEmpty()) valorDesconto = Double.parseDouble(txtDesconto.getText().trim().replace(",", "."));
            if (quantidadeVendida <= 0) {
                 mostrarAlerta("Erro de Validação", "A quantidade deve ser maior que zero.", Alert.AlertType.ERROR);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Erro de Formato", "Valores numéricos inválidos.", Alert.AlertType.ERROR);
            return;
        }

        if (produtoSelecionado.getQuantidadeEstoque() < quantidadeVendida) {
            mostrarAlerta("Erro de Estoque", "Estoque insuficiente. Disponível: " + produtoSelecionado.getQuantidadeEstoque(), Alert.AlertType.ERROR);
            return;
        }

        
        double valorTotalItens = precoUnitarioVenda * quantidadeVendida;
        double valorFinalVenda = valorTotalItens - valorDesconto;
        String nomeCliente = txtNomeCliente.getText().trim();
        LocalDate dataVendaLocal = dpDataVenda.getValue();
        boolean pago = chkPago.isSelected();
        LocalDate dataPrometidaLocal = dpDataPrometida.getValue();
        String metodoPagamento = cbMetodoPagamento.getValue();

        
        Connection con = null;
        try {
            con = ConexaoBanco.conectar();
            con.setAutoCommit(false);
            Integer clienteId = getClienteID(con, nomeCliente);
            int produtoId = produtoSelecionado.getProdutoID();
            double custoMedio = produtoSelecionado.getCustoMedioPonderado();
            
            String sqlVenda = "INSERT INTO Vendas (ClienteID, DataVenda, ValorTotalItens, ValorDesconto, ValorFinalVenda, StatusPagamento, DataPrometidaPagamento, MetodoPagamento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            long vendaIdGerado;
            try (PreparedStatement pstVenda = con.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                if (clienteId != null) pstVenda.setInt(1, clienteId); else pstVenda.setNull(1, java.sql.Types.INTEGER);
                pstVenda.setDate(2, java.sql.Date.valueOf(dataVendaLocal));
                pstVenda.setDouble(3, valorTotalItens);
                pstVenda.setDouble(4, valorDesconto);
                pstVenda.setDouble(5, valorFinalVenda);
                pstVenda.setString(6, pago ? "Pago" : "Pendente");
                if (!pago && dataPrometidaLocal != null) pstVenda.setDate(7, java.sql.Date.valueOf(dataPrometidaLocal)); else pstVenda.setNull(7, java.sql.Types.DATE);
                pstVenda.setString(8, metodoPagamento);
                pstVenda.executeUpdate();
                ResultSet rsKeys = pstVenda.getGeneratedKeys();
                if (rsKeys.next()) vendaIdGerado = rsKeys.getLong(1); else throw new SQLException("Falha ao obter ID da venda.");
            }

            String sqlItemVenda = "INSERT INTO ItensVenda (VendaID, ProdutoID, Quantidade, PrecoVendaUnitarioRegistrado, CustoMedioUnitarioRegistrado) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstItem = con.prepareStatement(sqlItemVenda)) {
                pstItem.setLong(1, vendaIdGerado);
                pstItem.setInt(2, produtoId);
                pstItem.setInt(3, quantidadeVendida);
                pstItem.setDouble(4, precoUnitarioVenda);
                pstItem.setDouble(5, custoMedio);
                pstItem.executeUpdate();
            }

            String sqlEstoque = "UPDATE Produtos SET QuantidadeEstoque = QuantidadeEstoque - ? WHERE ProdutoID = ?";
            try (PreparedStatement pstEstoque = con.prepareStatement(sqlEstoque)) {
                pstEstoque.setInt(1, quantidadeVendida);
                pstEstoque.setInt(2, produtoId);
                pstEstoque.executeUpdate();
            }

            con.commit();
            mostrarAlerta("Sucesso", "Venda registrada com sucesso! ID da Venda: " + vendaIdGerado, Alert.AlertType.INFORMATION);
            limparFormularioVenda();

        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            mostrarAlerta("Erro de Banco de Dados", "Não foi possível salvar a venda.\nErro: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private Integer getClienteID(Connection con, String nomeCliente) throws SQLException {
        if (nomeCliente == null || nomeCliente.trim().isEmpty()) return null;
        String sqlBusca = "SELECT ClienteID FROM Clientes WHERE NomeCliente = ?";
        try (PreparedStatement pst = con.prepareStatement(sqlBusca)) {
            pst.setString(1, nomeCliente.trim());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt("ClienteID");
            else {
                String sqlNovo = "INSERT INTO Clientes (NomeCliente) VALUES (?)";
                try (PreparedStatement pstNovo = con.prepareStatement(sqlNovo, Statement.RETURN_GENERATED_KEYS)) {
                    pstNovo.setString(1, nomeCliente.trim());
                    pstNovo.executeUpdate();
                    ResultSet chaves = pstNovo.getGeneratedKeys();
                    if (chaves.next()) return chaves.getInt(1);
                    else throw new SQLException("Falha ao cadastrar cliente, nenhum ID obtido.");
                }
            }
        }
    }
    
    @FXML
    private void limparFormularioVenda() {
        txtNomeCliente.clear();
        cbProduto.setValue(null);
        cbProduto.getEditor().clear();
        txtQuantidadeVendida.setText("1");
        txtValorVenda.clear();
        dpDataVenda.setValue(LocalDate.now());
        chkPago.setSelected(false);
        if (cbMetodoPagamento.getItems() != null && !cbMetodoPagamento.getItems().isEmpty()) cbMetodoPagamento.getSelectionModel().selectFirst();
        txtDesconto.clear();
        atualizarResumoVenda();
        cbProduto.requestFocus();
    }
    
    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipoAlerta) {
        Alert alert = new Alert(tipoAlerta);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}