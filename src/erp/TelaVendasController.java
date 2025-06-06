package erp;

import javafx.collections.FXCollections; 
import javafx.collections.ObservableList; 
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.util.StringConverter; 


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import javafx.event.ActionEvent;

public class TelaVendasController {

    @FXML
    private TextField txtNomeCliente;

   
    @FXML
    private ComboBox<ProdutoVO> cbProduto;

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
    private TextField txtQuantidadeVendida;

    @FXML
    private Button btnIrParaEstoque;

    @FXML private Label lblSubtotalCalculado;
    @FXML private Label lblDescontoAplicado;
    @FXML private Label lblTotalAPagar;
   
    @FXML private Button btnLimparCampos;

 
    private ObservableList<ProdutoVO> listaProdutosSugeridos;

   
    @FXML
    public void irParaEstoque() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TelaEstoque.fxml"));
            BorderPane estoqueRoot = loader.load();
            Scene estoqueScene = new Scene(estoqueRoot);
            Stage stage = (Stage) btnIrParaEstoque.getScene().getWindow();
            stage.setScene(estoqueScene);
            stage.setTitle("Estoque");
            stage.show();
        } catch (IOException e) {
            System.out.println("Erro ao carregar a tela de estoque: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    public void initialize() {
        listaProdutosSugeridos = FXCollections.observableArrayList();
        cbProduto.setItems(listaProdutosSugeridos);
   
    txtQuantidadeVendida.textProperty().addListener((obs, oldVal, newVal) -> atualizarResumoVenda());
    txtValorVenda.textProperty().addListener((obs, oldVal, newVal) -> atualizarResumoVenda());
    txtDesconto.textProperty().addListener((obs, oldVal, newVal) -> atualizarResumoVenda());

   
    atualizarResumoVenda();

   
        cbProduto.setEditable(true);

       
        cbProduto.setConverter(new StringConverter<ProdutoVO>() {
            @Override
            public String toString(ProdutoVO produto) {
                return produto == null ? null : produto.getDescricaoCompleta();
            }

            @Override
            public ProdutoVO fromString(String string) {
                
                if (cbProduto.getValue() != null && cbProduto.getValue().getDescricaoCompleta().equals(string)) {
                    return cbProduto.getValue();
                }
                return null; 
            }
            
        });

        
        cbProduto.setCellFactory(listView -> new ListCell<ProdutoVO>() {
            @Override
            protected void updateItem(ProdutoVO produto, boolean empty) {
                super.updateItem(produto, empty);
                if (empty || produto == null) {
                    setText(null);
                } else {
                    setText(produto.getDescricaoCompleta() + " (Estoque: " + produto.getQuantidadeEstoque() + ")");
                }
            }
        });

       
        cbProduto.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                listaProdutosSugeridos.clear();
                cbProduto.hide(); 
            } else {
               
                if (cbProduto.isFocused() && cbProduto.getSelectionModel().getSelectedItem() == null ||
                    (cbProduto.getSelectionModel().getSelectedItem() != null && !newValue.equals(cbProduto.getSelectionModel().getSelectedItem().getDescricaoCompleta()))) {
                    buscarProdutosSugeridos(newValue);
                }
            }
        });

        
        cbProduto.valueProperty().addListener((obs, oldProduto, newProduto) -> {
            if (newProduto != null) {
                txtValorVenda.setText(String.format("%.2f", newProduto.getPrecoVendaAtual()).replace(",", "."));
               
                 Platform.runLater(() -> txtQuantidadeVendida.requestFocus());
            } else {
                txtValorVenda.clear();
            }
        });


       
        cbMetodoPagamento.getItems().addAll("Dinheiro", "Cartão de Crédito", "PIX");
        cbMetodoPagamento.getSelectionModel().selectFirst();

        dpDataPrometida.setDisable(true);
        chkPago.setOnAction(e -> {
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

            String sql = "SELECT ProdutoID, DescricaoCompleta, PrecoVendaAtual, CustoMedioPonderado, QuantidadeEstoque " +
                         "FROM Produtos " +
                         "WHERE (DescricaoCompleta LIKE ? OR Modelo LIKE ? OR Clube LIKE ?) AND QuantidadeEstoque > 0 " +
                         "LIMIT 10"; 

            try (Connection con = UTIL.ConexaoBanco.conectar();
                 PreparedStatement pst = con.prepareStatement(sql)) {

                String termoLike = "%" + textoBusca + "%";
                pst.setString(1, termoLike);
                pst.setString(2, termoLike);
                pst.setString(3, termoLike);

                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    listaProdutosSugeridos.add(new ProdutoVO(
                            rs.getInt("ProdutoID"),
                            rs.getString("DescricaoCompleta"),
                            rs.getDouble("PrecoVendaAtual"),
                            rs.getDouble("CustoMedioPonderado"),
                            rs.getInt("QuantidadeEstoque")
                    ));
                }
            } catch (SQLException e) {
                System.err.println("Erro ao buscar produtos sugeridos: " + e.getMessage());
                e.printStackTrace();
            }

            if (!listaProdutosSugeridos.isEmpty()) {
                cbProduto.show(); 
            } else {
                cbProduto.hide();
            }
        });
    }

   
    private Integer getClienteID(Connection con, String nomeCliente) throws SQLException {
        
        if (nomeCliente == null || nomeCliente.trim().isEmpty()) {
            return null;
        }
        String sqlBuscaCliente = "SELECT ClienteID FROM Clientes WHERE NomeCliente = ?";
        try (PreparedStatement pst = con.prepareStatement(sqlBuscaCliente)) {
            pst.setString(1, nomeCliente);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("ClienteID");
            }
           
        }
        return null; 
    }


@FXML
public void salvarVenda(ActionEvent event) {
   
    ProdutoVO produtoSelecionado = cbProduto.getValue(); 
    String nomeCliente = txtNomeCliente.getText().trim();
    LocalDate dataVendaLocal = dpDataVenda.getValue();
    boolean pago = chkPago.isSelected();
    LocalDate dataPrometidaLocal = dpDataPrometida.getValue();
    String metodoPagamento = cbMetodoPagamento.getValue();

    String precoUnitarioInformadoStr = txtValorVenda.getText().trim(); 
    String quantidadeStr = txtQuantidadeVendida.getText().trim();
    String descontoStr = txtDesconto.getText().trim();

 
    if (produtoSelecionado == null) {
        mostrarAlerta("Erro de Validação", "Por favor, selecione um produto válido.", Alert.AlertType.ERROR);
        return;
    }
    if (dataVendaLocal == null) {
        mostrarAlerta("Erro de Validação", "Por favor, informe a data da venda.", Alert.AlertType.ERROR);
        return;
    }
    if (quantidadeStr.isEmpty() || precoUnitarioInformadoStr.isEmpty()) {
        mostrarAlerta("Erro de Validação", "Quantidade e Preço Unitário são obrigatórios.", Alert.AlertType.ERROR);
        return;
    }
    if (!pago && dataPrometidaLocal == null) {
        mostrarAlerta("Erro de Validação", "Se a venda não foi paga, informe a data prometida para o pagamento.", Alert.AlertType.ERROR);
        return;
    }
    if (metodoPagamento == null || metodoPagamento.trim().isEmpty()){
       
    }


    double precoUnitarioVenda;
    int quantidadeVendida;
    double valorDesconto = 0.0;

    try {
        precoUnitarioVenda = Double.parseDouble(precoUnitarioInformadoStr.replace(",", "."));
        quantidadeVendida = Integer.parseInt(quantidadeStr);

        if (!descontoStr.isEmpty()) {
            valorDesconto = Double.parseDouble(descontoStr.replace(",", "."));
        }

        if (quantidadeVendida <= 0) {
            mostrarAlerta("Erro de Validação", "A quantidade vendida deve ser maior que zero.", Alert.AlertType.ERROR);
            return;
        }
        if (precoUnitarioVenda < 0) { 
            mostrarAlerta("Erro de Validação", "O preço unitário não pode ser negativo.", Alert.AlertType.ERROR);
            return;
        }
        if (valorDesconto < 0) {
            mostrarAlerta("Erro de Validação", "O valor do desconto não pode ser negativo.", Alert.AlertType.ERROR);
            return;
        }

    } catch (NumberFormatException e) {
        mostrarAlerta("Erro de Formato", "Valores inválidos para Quantidade, Preço Unitário ou Desconto. Verifique se são números.", Alert.AlertType.ERROR);
        return;
    }


    if (produtoSelecionado.getQuantidadeEstoque() < quantidadeVendida) {
        mostrarAlerta("Erro de Estoque", "Quantidade em estoque (" + produtoSelecionado.getQuantidadeEstoque() + ") do produto '" + produtoSelecionado.getDescricaoCompleta() + "' é insuficiente para esta venda.", Alert.AlertType.ERROR);
        return;
    }

  
    double valorTotalItensCalculado = precoUnitarioVenda * quantidadeVendida;
    double valorFinalVendaCalculado = valorTotalItensCalculado - valorDesconto;

   
    if (valorDesconto > valorTotalItensCalculado) {
         mostrarAlerta("Atenção", "O desconto aplicado (R$ " + String.format("%.2f", valorDesconto) + ") é maior que o subtotal (R$ " + String.format("%.2f", valorTotalItensCalculado) + "). O valor final será R$ " + String.format("%.2f", valorFinalVendaCalculado) + ".", Alert.AlertType.WARNING);
        
    }


    Connection con = null;
    try {
        con = UTIL.ConexaoBanco.conectar();
        con.setAutoCommit(false);  
        
        Integer clienteId = null;  
        if (nomeCliente != null && !nomeCliente.isEmpty()) {
            clienteId = getClienteID(con, nomeCliente);  
        }
        
        
        int produtoIdParaItemVenda = produtoSelecionado.getProdutoID();
        double custoMedioProdutoNoMomentoDaVenda = produtoSelecionado.getCustoMedioPonderado();

        
        String sqlVenda = "INSERT INTO Vendas (ClienteID, DataVenda, ValorTotalItens, ValorDesconto, ValorFinalVenda, StatusPagamento, DataPrometidaPagamento, MetodoPagamento) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        long vendaIdGerado = -1;

        try (PreparedStatement pstVenda = con.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
            if (clienteId != null) {
                pstVenda.setInt(1, clienteId);
            } else {
                pstVenda.setNull(1, java.sql.Types.INTEGER);
            }
            pstVenda.setDate(2, java.sql.Date.valueOf(dataVendaLocal));
            pstVenda.setDouble(3, valorTotalItensCalculado);
            pstVenda.setDouble(4, valorDesconto);
            pstVenda.setDouble(5, valorFinalVendaCalculado);
            pstVenda.setString(6, pago ? "Pago" : "NaoPagoPendente");
            if (!pago && dataPrometidaLocal != null) {
                pstVenda.setDate(7, java.sql.Date.valueOf(dataPrometidaLocal));
            } else {
                pstVenda.setNull(7, java.sql.Types.DATE);
            }
            pstVenda.setString(8, metodoPagamento);

            int affectedRowsVenda = pstVenda.executeUpdate();
            if (affectedRowsVenda > 0) {
                ResultSet generatedKeys = pstVenda.getGeneratedKeys();
                if (generatedKeys.next()) {
                    vendaIdGerado = generatedKeys.getLong(1);
                }
            } else {
                throw new SQLException("Falha ao inserir na tabela Vendas, nenhuma linha afetada.");
            }
        }

        if (vendaIdGerado == -1) {
             throw new SQLException("Falha ao obter o ID da Venda inserida.");
        }

        
        String sqlItemVenda = "INSERT INTO ItensVenda (VendaID, ProdutoID, Quantidade, PrecoVendaUnitarioRegistrado, CustoMedioUnitarioRegistrado) " +
                              "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstItemVenda = con.prepareStatement(sqlItemVenda)) {
            pstItemVenda.setLong(1, vendaIdGerado);
            pstItemVenda.setInt(2, produtoIdParaItemVenda);
            pstItemVenda.setInt(3, quantidadeVendida);
            pstItemVenda.setDouble(4, precoUnitarioVenda); 
            pstItemVenda.setDouble(5, custoMedioProdutoNoMomentoDaVenda);
            
            int affectedRowsItemVenda = pstItemVenda.executeUpdate();
            if (affectedRowsItemVenda == 0) {
                throw new SQLException("Falha ao inserir na tabela ItensVenda, nenhuma linha afetada.");
            }
        }

        
        String sqlAtualizaEstoque = "UPDATE Produtos SET QuantidadeEstoque = QuantidadeEstoque - ? WHERE ProdutoID = ?";
        try(PreparedStatement pstEstoque = con.prepareStatement(sqlAtualizaEstoque)) {
            pstEstoque.setInt(1, quantidadeVendida);
            pstEstoque.setInt(2, produtoIdParaItemVenda);
            
            int affectedRowsEstoque = pstEstoque.executeUpdate();
            if (affectedRowsEstoque == 0) {
                
                throw new SQLException("Falha ao atualizar o estoque do produto.");
            }
        }

        con.commit();  

        mostrarAlerta("Sucesso", "Venda registrada com sucesso! ID da Venda: " + vendaIdGerado, Alert.AlertType.INFORMATION);
        limparFormularioVenda(); 
        

    } catch (SQLException e) {
        if (con != null) {
            try {
                con.rollback();  
                mostrarAlerta("Erro de Banco de Dados", "A transação foi desfeita devido a um erro: " + e.getMessage(), Alert.AlertType.ERROR);
            } catch (SQLException exRollback) {
                mostrarAlerta("Erro Crítico", "Erro ao tentar reverter a transação: " + exRollback.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Erro de Conexão", "Não foi possível conectar ao banco de dados: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        e.printStackTrace();
    } catch (Exception e) {  
        if (con != null) {  
            try { con.rollback(); } catch (SQLException ex) { }
        }
        mostrarAlerta("Erro Inesperado", "Ocorreu um erro inesperado ao salvar a venda: " + e.getMessage(), Alert.AlertType.ERROR);
        e.printStackTrace();
    } finally {
        if (con != null) {
            try {
                con.setAutoCommit(true); 
                con.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexão: " + e.getMessage());
            }
        }
    }
}


private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipoAlerta) {
    Alert alert = new Alert(tipoAlerta);
    alert.setTitle(titulo);
    alert.setHeaderText(null);
    alert.setContentText(mensagem);
    alert.showAndWait();
}


    @FXML
    public void voltar(ActionEvent event) {
        limparFormulario();
    }

    private void limparFormulario() {
        txtNomeCliente.clear();
        cbProduto.setValue(null);
        cbProduto.getEditor().clear();
        listaProdutosSugeridos.clear(); 
        dpDataVenda.setValue(null);
        chkPago.setSelected(false);
        dpDataPrometida.setValue(null);
        dpDataPrometida.setDisable(true);
        cbMetodoPagamento.getSelectionModel().selectFirst();
        txtDesconto.clear();
        txtValorVenda.clear();
        txtQuantidadeVendida.setText("1");
    }

    
    
    public void atualizarCabecalho() {
       
        String sqlFaturamento = "SELECT COALESCE(SUM(ValorFinalVenda), 0) AS total FROM Vendas " +
                                "WHERE StatusPagamento = 'Pago' AND MONTH(DataVenda) = MONTH(CURDATE()) AND YEAR(DataVenda) = YEAR(CURDATE())";

        String sqlQuantidade = "SELECT COUNT(*) AS total_vendas FROM Vendas " +
                               "WHERE MONTH(DataVenda) = MONTH(CURDATE()) AND YEAR(DataVenda) = YEAR(CURDATE())";

        try (Connection con = UTIL.ConexaoBanco.conectar()) {
            try (PreparedStatement pst = con.prepareStatement(sqlFaturamento);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    double faturamento = rs.getDouble("total");
                    lblFaturamento.setText(String.format("Faturamento do Mês (Pago): R$ %.2f", faturamento));
                }
            }
            try (PreparedStatement pst2 = con.prepareStatement(sqlQuantidade);
                 ResultSet rs2 = pst2.executeQuery()) {
                if (rs2.next()) {
                    int qtdVendas = rs2.getInt("total_vendas");
                    lblQuantidadeVendas.setText("Vendas no Mês: " + qtdVendas);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar cabeçalho: " + e.getMessage());
            e.printStackTrace();
            lblFaturamento.setText("Faturamento do Mês: Erro");
            lblQuantidadeVendas.setText("Vendas no Mês: Erro");
        }
    }
private void atualizarResumoVenda() {
    try {
        int quantidade = 0;
        if (!txtQuantidadeVendida.getText().trim().isEmpty()) {
            quantidade = Integer.parseInt(txtQuantidadeVendida.getText().trim());
        }

        double precoUnitario = 0.0;
        if (!txtValorVenda.getText().trim().isEmpty()) {
            
            String precoStr = txtValorVenda.getText().trim().replace(",", ".");
            precoUnitario = Double.parseDouble(precoStr);
        }

        double desconto = 0.0;
        if (!txtDesconto.getText().trim().isEmpty()) {
            String descontoStr = txtDesconto.getText().trim().replace(",", ".");
            desconto = Double.parseDouble(descontoStr);
        }

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
@FXML
private void limparFormularioVenda() { 
    txtNomeCliente.clear();
    cbProduto.setValue(null); 
    cbProduto.getEditor().clear();
    txtQuantidadeVendida.clear();
    txtValorVenda.clear();
    dpDataVenda.setValue(null); 
    chkPago.setSelected(false); 
   
    if (cbMetodoPagamento.getItems() != null && !cbMetodoPagamento.getItems().isEmpty()) {
        cbMetodoPagamento.getSelectionModel().selectFirst();
    }
    txtDesconto.clear();

    atualizarResumoVenda();
    txtNomeCliente.requestFocus();
}

}