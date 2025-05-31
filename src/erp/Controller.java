package erp;

import javafx.collections.FXCollections; // Adicionado
import javafx.collections.ObservableList; // Adicionado
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.util.StringConverter; // Adicionado

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import javafx.event.ActionEvent;

public class Controller {

    @FXML
    private TextField txtNomeCliente;

    // CAMPOS REMOVIDOS:
    // @FXML private TextField txtModelo;
    // @FXML private TextField txtTamanho;

    // NOVO CAMPO ComboBox para Produtos:
    @FXML
    private ComboBox<ProdutoVO> cbProduto; // Alterado para usar ProdutoVO

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
    private TextField txtValorVenda; // Preço unitário do item selecionado

    @FXML
    private TextField txtQuantidadeVendida;

    @FXML
    private Button btnIrParaEstoque;

    // Lista observável para os itens do ComboBox de produtos
    private ObservableList<ProdutoVO> listaProdutosSugeridos;

    // Método irParaEstoque (sem alterações) ...
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

        // Configurar ComboBox de Produtos
        cbProduto.setEditable(true); // Permite digitação

        // Como o ComboBox exibe o ProdutoVO na lista e no campo de edição
        cbProduto.setConverter(new StringConverter<ProdutoVO>() {
            @Override
            public String toString(ProdutoVO produto) {
                return produto == null ? null : produto.getDescricaoCompleta();
            }

            @Override
            public ProdutoVO fromString(String string) {
                // Se o usuário digitar algo que não corresponde a um ProdutoVO existente,
                // este método pode retornar null ou tentar encontrar um ProdutoVO.
                // Por simplicidade, retornaremos o item selecionado se o texto corresponder.
                // A lógica de busca real acontece no listener do editor.
                if (cbProduto.getValue() != null && cbProduto.getValue().getDescricaoCompleta().equals(string)) {
                    return cbProduto.getValue();
                }
                return null; // Ou criar um novo "placeholder" ProdutoVO se necessário
            }
        });

        // Como cada célula da lista dropdown é renderizada
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

        // Listener para buscar produtos enquanto o usuário digita
        cbProduto.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                listaProdutosSugeridos.clear(); // Limpa sugestões se o campo estiver vazio
                cbProduto.hide(); // Esconde o dropdown
            } else {
                // Evita buscar se o texto foi alterado por uma seleção de item
                if (cbProduto.isFocused() && cbProduto.getSelectionModel().getSelectedItem() == null ||
                    (cbProduto.getSelectionModel().getSelectedItem() != null && !newValue.equals(cbProduto.getSelectionModel().getSelectedItem().getDescricaoCompleta()))) {
                    buscarProdutosSugeridos(newValue);
                }
            }
        });

        // Listener para quando um produto é selecionado (do dropdown ou autocomplete)
        cbProduto.valueProperty().addListener((obs, oldProduto, newProduto) -> {
            if (newProduto != null) {
                txtValorVenda.setText(String.format("%.2f", newProduto.getPrecoVendaAtual()).replace(",", "."));
                // Você pode querer focar na quantidade ou em outro campo aqui
                 Platform.runLater(() -> txtQuantidadeVendida.requestFocus());
            } else {
                txtValorVenda.clear();
            }
        });


        // Restante do seu initialize
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
        // Evita múltiplas buscas rápidas (debounce simples)
        // Platform.runLater é usado para garantir que a atualização da UI ocorra no thread do JavaFX.
        // Uma solução mais robusta de debounce envolveria PauseTransition.
        Platform.runLater(() -> {
            listaProdutosSugeridos.clear();
            if (textoBusca == null || textoBusca.length() < 2) { // Buscar apenas com pelo menos 2 caracteres
                cbProduto.hide();
                return;
            }

            String sql = "SELECT ProdutoID, DescricaoCompleta, PrecoVendaAtual, CustoMedioPonderado, QuantidadeEstoque " +
                         "FROM Produtos " +
                         "WHERE (DescricaoCompleta LIKE ? OR Modelo LIKE ? OR Clube LIKE ?) AND QuantidadeEstoque > 0 " +
                         "LIMIT 10"; // Limita o número de sugestões

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
                cbProduto.show(); // Mostra o dropdown com as sugestões
            } else {
                cbProduto.hide();
            }
        });
    }

    // Método getClienteID (sem alterações, mas pode ser melhorado no futuro)
    private Integer getClienteID(Connection con, String nomeCliente) throws SQLException {
        // ... (código existente) ...
        // Se nomeCliente estiver vazio, pode retornar null diretamente ou tratar
        if (nomeCliente == null || nomeCliente.trim().isEmpty()) {
            return null; // Venda anônima se permitido
        }
        String sqlBuscaCliente = "SELECT ClienteID FROM Clientes WHERE NomeCliente = ?";
        try (PreparedStatement pst = con.prepareStatement(sqlBuscaCliente)) {
            pst.setString(1, nomeCliente);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("ClienteID");
            }
            // Opção: Cadastrar cliente se não existir (descomente e ajuste se necessário)
            /*
            else {
                String sqlNovoCliente = "INSERT INTO Clientes (NomeCliente) VALUES (?)";
                try (PreparedStatement pstNovo = con.prepareStatement(sqlNovoCliente, Statement.RETURN_GENERATED_KEYS)) {
                    pstNovo.setString(1, nomeCliente);
                    pstNovo.executeUpdate();
                    ResultSet generatedKeys = pstNovo.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            */
        }
        return null; // Cliente não encontrado e não cadastrado automaticamente
    }

    // MÉTODO getProdutoID(Connection con, String modelo, String tamanho) NÃO É MAIS NECESSÁRIO DA FORMA ANTIGA
    // POIS O ProdutoID virá do cbProduto.getValue().getProdutoID()


    @FXML
    public void salvarVenda(ActionEvent event) {
        String nomeCliente = txtNomeCliente.getText().trim();
        ProdutoVO produtoSelecionado = cbProduto.getValue(); // Obtém o ProdutoVO selecionado

        LocalDate dataVendaLocal = dpDataVenda.getValue();
        boolean pago = chkPago.isSelected();
        LocalDate dataPrometidaLocal = dpDataPrometida.getValue();
        String metodoPagamento = cbMetodoPagamento.getValue();
        String descontoStr = txtDesconto.getText().trim();
        String precoUnitarioInformadoStr = txtValorVenda.getText().trim(); // Preço que está no campo, pode ter sido alterado
        String quantidadeStr = txtQuantidadeVendida.getText().trim();

        // Validações básicas
        if (produtoSelecionado == null) {
            mostrarAlerta("Erro de Validação", "Selecione um produto válido da lista.");
            return;
        }
        // Se nomeCliente for opcional, ajuste esta validação
        if (/*nomeCliente.isEmpty() ||*/ dataVendaLocal == null || precoUnitarioInformadoStr.isEmpty() || quantidadeStr.isEmpty()) {
            mostrarAlerta("Erro de Validação", "Preencha Data da Venda, Preço Unitário e Quantidade.");
            return;
        }
        if (!pago && dataPrometidaLocal == null) {
            mostrarAlerta("Erro de Validação", "Se não estiver pago, informe a data prometida para o pagamento.");
            return;
        }

        double precoUnitarioVenda;
        int quantidadeVendida;
        double valorDesconto = 0.0;

        try {
            precoUnitarioVenda = Double.parseDouble(precoUnitarioInformadoStr.replace(",","."));
            quantidadeVendida = Integer.parseInt(quantidadeStr);
            if (!descontoStr.isEmpty()) {
                valorDesconto = Double.parseDouble(descontoStr.replace(",","."));
            }
            if (precoUnitarioVenda <= 0 || quantidadeVendida <= 0) {
                mostrarAlerta("Erro de Validação", "Preço unitário e quantidade devem ser maiores que zero.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Erro de Formato", "Preço unitário, quantidade ou desconto inválidos. Informe números válidos.");
            return;
        }

        if (produtoSelecionado.getQuantidadeEstoque() < quantidadeVendida) {
            mostrarAlerta("Erro de Estoque", "Quantidade em estoque (" + produtoSelecionado.getQuantidadeEstoque() + ") insuficiente para esta venda.");
            return;
        }

        Connection con = null;
        try {
            con = UTIL.ConexaoBanco.conectar();
            con.setAutoCommit(false);

            Integer clienteId = getClienteID(con, nomeCliente);

            // Dados do produto já estão em 'produtoSelecionado'
            int produtoId = produtoSelecionado.getProdutoID();
            // Usaremos o precoUnitarioVenda (do campo de texto, que pode ter sido editado)
            // mas o custo vem do ProdutoVO original.
            double custoMedioAtualDoProduto = produtoSelecionado.getCustoMedioPonderado();


            String sqlVenda = "INSERT INTO Vendas (ClienteID, DataVenda, ValorTotalItens, ValorDesconto, ValorFinalVenda, StatusPagamento, DataPrometidaPagamento, MetodoPagamento) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            long vendaId = -1;

            double valorTotalItens = precoUnitarioVenda * quantidadeVendida;
            double valorFinalVenda = valorTotalItens - valorDesconto;

            try (PreparedStatement pstVenda = con.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                if (clienteId != null) {
                    pstVenda.setInt(1, clienteId);
                } else {
                    pstVenda.setNull(1, java.sql.Types.INTEGER);
                }
                pstVenda.setDate(2, java.sql.Date.valueOf(dataVendaLocal));
                pstVenda.setDouble(3, valorTotalItens);
                pstVenda.setDouble(4, valorDesconto);
                pstVenda.setDouble(5, valorFinalVenda);
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
                        vendaId = generatedKeys.getLong(1);
                    }
                } else {
                    throw new SQLException("Falha ao inserir venda, nenhuma linha afetada.");
                }
            }

            if (vendaId == -1) {
                 throw new SQLException("Falha ao obter o ID da venda inserida.");
            }

            String sqlItemVenda = "INSERT INTO ItensVenda (VendaID, ProdutoID, Quantidade, PrecoVendaUnitarioRegistrado, CustoMedioUnitarioRegistrado) " +
                                  "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstItemVenda = con.prepareStatement(sqlItemVenda)) {
                pstItemVenda.setLong(1, vendaId);
                pstItemVenda.setInt(2, produtoId);
                pstItemVenda.setInt(3, quantidadeVendida);
                pstItemVenda.setDouble(4, precoUnitarioVenda); // Preço unitário efetivo da venda
                pstItemVenda.setDouble(5, custoMedioAtualDoProduto);
                pstItemVenda.executeUpdate();
            }

            String sqlAtualizaEstoque = "UPDATE Produtos SET QuantidadeEstoque = QuantidadeEstoque - ? WHERE ProdutoID = ?";
            try(PreparedStatement pstEstoque = con.prepareStatement(sqlAtualizaEstoque)) {
                pstEstoque.setInt(1, quantidadeVendida);
                pstEstoque.setInt(2, produtoId);
                pstEstoque.executeUpdate();
            }

            con.commit();
            mostrarAlerta("Sucesso", "Venda registrada com sucesso!");
            limparFormulario();
            atualizarCabecalho();

        } catch (SQLException e) {
            // ... (bloco catch e finally existente) ...
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            mostrarAlerta("Erro de Banco de Dados", "Erro ao salvar venda: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            mostrarAlerta("Erro Inesperado", "Ocorreu um erro: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    public void voltar(ActionEvent event) {
        limparFormulario();
    }

    private void limparFormulario() {
        txtNomeCliente.clear();
        cbProduto.setValue(null); // Limpa o ComboBox
        cbProduto.getEditor().clear(); // Limpa o texto do editor do ComboBox
        listaProdutosSugeridos.clear(); // Limpa as sugestões
        dpDataVenda.setValue(null);
        chkPago.setSelected(false);
        dpDataPrometida.setValue(null);
        dpDataPrometida.setDisable(true);
        cbMetodoPagamento.getSelectionModel().selectFirst();
        txtDesconto.clear();
        txtValorVenda.clear();
        txtQuantidadeVendida.setText("1");
    }

    // Método mostrarAlerta (sem alterações)
    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (titulo.toLowerCase().contains("erro")) {
            alert.setAlertType(Alert.AlertType.ERROR);
        }
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    
    // Método atualizarCabecalho (sem alterações em relação à última versão fornecida)
    public void atualizarCabecalho() {
        // ... (código existente) ...
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
}