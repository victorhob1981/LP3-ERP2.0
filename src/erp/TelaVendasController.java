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

public class TelaVendasController {

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

    @FXML private Label lblSubtotalCalculado;
    @FXML private Label lblDescontoAplicado;
    @FXML private Label lblTotalAPagar;
    // Se o botão de limpar mudou de fx:id ou de nome de método:
    @FXML private Button btnLimparCampos;

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
    // ... (seu código de initialize existente, como popular ComboBoxes) ...

    // Listeners para atualizar o resumo dinamicamente
    txtQuantidadeVendida.textProperty().addListener((obs, oldVal, newVal) -> atualizarResumoVenda());
    txtValorVenda.textProperty().addListener((obs, oldVal, newVal) -> atualizarResumoVenda());
    txtDesconto.textProperty().addListener((obs, oldVal, newVal) -> atualizarResumoVenda());

    // Chama uma vez para inicializar os valores do resumo (caso haja dados pré-carregados, o que não é o caso aqui)
    atualizarResumoVenda();

    // ... (resto do seu initialize) ...
    // Se o método para atualizar o CABEÇALHO (faturamento, qtd vendas)
    // ainda estiver aqui, ele continua como está:
    // Platform.runLater(this::atualizarCabecalho); 

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


    // Este método pertence ao seu Controller.java da tela de Registrar Vendas
// Certifique-se de ter as importações necessárias no topo do arquivo:
// import javafx.event.ActionEvent;
// import javafx.scene.control.Alert;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Statement; // Para Statement.RETURN_GENERATED_KEYS
// import java.sql.Date; // Para java.sql.Date
// import java.time.LocalDate; // Se você estiver usando LocalDate dos DatePickers

@FXML
public void salvarVenda(ActionEvent event) {
    // 1. Obter dados do formulário
    ProdutoVO produtoSelecionado = cbProduto.getValue(); // ProdutoVO selecionado no ComboBox
    String nomeCliente = txtNomeCliente.getText().trim();
    LocalDate dataVendaLocal = dpDataVenda.getValue();
    boolean pago = chkPago.isSelected();
    LocalDate dataPrometidaLocal = dpDataPrometida.getValue();
    String metodoPagamento = cbMetodoPagamento.getValue();

    String precoUnitarioInformadoStr = txtValorVenda.getText().trim(); // txtValorVenda é PREÇO UNITÁRIO
    String quantidadeStr = txtQuantidadeVendida.getText().trim();
    String descontoStr = txtDesconto.getText().trim();

    // 2. Validações de Entrada Essenciais
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
        // Considerar se o método de pagamento é sempre obrigatório
        // mostrarAlerta("Erro de Validação", "Por favor, selecione um método de pagamento.", Alert.AlertType.ERROR);
        // return;
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
        if (precoUnitarioVenda < 0) { // Permitir preço zero se for brinde? Para este exemplo, não.
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

    // 3. Verificar Estoque do Produto Selecionado
    if (produtoSelecionado.getQuantidadeEstoque() < quantidadeVendida) {
        mostrarAlerta("Erro de Estoque", "Quantidade em estoque (" + produtoSelecionado.getQuantidadeEstoque() + ") do produto '" + produtoSelecionado.getDescricaoCompleta() + "' é insuficiente para esta venda.", Alert.AlertType.ERROR);
        return;
    }

    // 4. Cálculos Financeiros para a Venda
    double valorTotalItensCalculado = precoUnitarioVenda * quantidadeVendida;
    double valorFinalVendaCalculado = valorTotalItensCalculado - valorDesconto;

    // Opcional: Validação se o desconto torna o valor final negativo
    if (valorDesconto > valorTotalItensCalculado) {
         mostrarAlerta("Atenção", "O desconto aplicado (R$ " + String.format("%.2f", valorDesconto) + ") é maior que o subtotal (R$ " + String.format("%.2f", valorTotalItensCalculado) + "). O valor final será R$ " + String.format("%.2f", valorFinalVendaCalculado) + ".", Alert.AlertType.WARNING);
        // Se quiser impedir, adicione um return aqui ou use um Alert de confirmação.
    }


    Connection con = null;
    try {
        con = UTIL.ConexaoBanco.conectar();
        con.setAutoCommit(false); // Iniciar transação

        // 5. Obter ClienteID (se houver cliente)
        Integer clienteId = null; // Definir como null por padrão para vendas anônimas
        if (nomeCliente != null && !nomeCliente.isEmpty()) {
            clienteId = getClienteID(con, nomeCliente); // Seu método auxiliar getClienteID
            // Adicione lógica aqui se clienteId retornar null mas um nome foi fornecido
            // e você quiser forçar o cadastro ou impedir a venda.
        }
        
        // 6. Preparar dados do produto para ItensVenda
        int produtoIdParaItemVenda = produtoSelecionado.getProdutoID();
        double custoMedioProdutoNoMomentoDaVenda = produtoSelecionado.getCustoMedioPonderado();

        // 7. Inserir na tabela 'Vendas'
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

        // 8. Inserir na tabela 'ItensVenda'
        String sqlItemVenda = "INSERT INTO ItensVenda (VendaID, ProdutoID, Quantidade, PrecoVendaUnitarioRegistrado, CustoMedioUnitarioRegistrado) " +
                              "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstItemVenda = con.prepareStatement(sqlItemVenda)) {
            pstItemVenda.setLong(1, vendaIdGerado);
            pstItemVenda.setInt(2, produtoIdParaItemVenda);
            pstItemVenda.setInt(3, quantidadeVendida);
            pstItemVenda.setDouble(4, precoUnitarioVenda); // Preço unitário efetivo usado na venda
            pstItemVenda.setDouble(5, custoMedioProdutoNoMomentoDaVenda);
            
            int affectedRowsItemVenda = pstItemVenda.executeUpdate();
            if (affectedRowsItemVenda == 0) {
                throw new SQLException("Falha ao inserir na tabela ItensVenda, nenhuma linha afetada.");
            }
        }

        // 9. Atualizar estoque na tabela 'Produtos'
        String sqlAtualizaEstoque = "UPDATE Produtos SET QuantidadeEstoque = QuantidadeEstoque - ? WHERE ProdutoID = ?";
        try(PreparedStatement pstEstoque = con.prepareStatement(sqlAtualizaEstoque)) {
            pstEstoque.setInt(1, quantidadeVendida);
            pstEstoque.setInt(2, produtoIdParaItemVenda);
            
            int affectedRowsEstoque = pstEstoque.executeUpdate();
            if (affectedRowsEstoque == 0) {
                // Isso pode acontecer se o ProdutoID for inválido, mas a verificação de estoque já deveria ter pego isso
                // ou se houver uma condição de corrida (outro processo alterou o estoque).
                // Para um sistema simples, um erro aqui pode indicar um problema maior.
                throw new SQLException("Falha ao atualizar o estoque do produto.");
            }
        }

        con.commit(); // Efetivar a transação se tudo deu certo

        mostrarAlerta("Sucesso", "Venda registrada com sucesso! ID da Venda: " + vendaIdGerado, Alert.AlertType.INFORMATION);
        limparFormularioVenda(); // Seu método para limpar os campos do formulário de venda
        
      
        // Ou, se o atualizarCabecalho() era um método deste controller:
        // atualizarCabecalho();


    } catch (SQLException e) {
        if (con != null) {
            try {
                con.rollback(); // Desfazer operações em caso de erro no BD
                mostrarAlerta("Erro de Banco de Dados", "A transação foi desfeita devido a um erro: " + e.getMessage(), Alert.AlertType.ERROR);
            } catch (SQLException exRollback) {
                mostrarAlerta("Erro Crítico", "Erro ao tentar reverter a transação: " + exRollback.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Erro de Conexão", "Não foi possível conectar ao banco de dados: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        e.printStackTrace();
    } catch (Exception e) { // Captura outras exceções não SQL (como NullPointerException se algo não foi inicializado)
        if (con != null) { // Tenta rollback mesmo para exceções não-SQL se a conexão foi aberta
            try { con.rollback(); } catch (SQLException ex) { /* Ignora falha no rollback aqui */ }
        }
        mostrarAlerta("Erro Inesperado", "Ocorreu um erro inesperado ao salvar a venda: " + e.getMessage(), Alert.AlertType.ERROR);
        e.printStackTrace();
    } finally {
        if (con != null) {
            try {
                con.setAutoCommit(true); // Restaurar o modo auto-commit
                con.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexão: " + e.getMessage());
            }
        }
    }
}

// Não se esqueça do método auxiliar getClienteID, se ele estiver neste controller
// e do mostrarAlerta, e do limparFormularioVenda()


// Método mostrarAlerta (se ainda não o tem ou está diferente)
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
private void atualizarResumoVenda() {
    try {
        int quantidade = 0;
        if (!txtQuantidadeVendida.getText().trim().isEmpty()) {
            quantidade = Integer.parseInt(txtQuantidadeVendida.getText().trim());
        }

        double precoUnitario = 0.0;
        if (!txtValorVenda.getText().trim().isEmpty()) {
            // Lidar com vírgula ou ponto como separador decimal
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
        lblDescontoAplicado.setText(String.format("R$ %.2f", desconto)); // Ou "- R$ %.2f"
        lblTotalAPagar.setText(String.format("R$ %.2f", totalAPagar));

    } catch (NumberFormatException e) {
        // Se os campos não puderem ser convertidos para número, limpa o resumo ou mostra R$ 0,00
        lblSubtotalCalculado.setText("R$ 0,00");
        lblDescontoAplicado.setText("R$ 0,00");
        lblTotalAPagar.setText("R$ 0,00");
    }
}
@FXML
private void limparFormularioVenda() { // Novo nome para o método do botão
    txtNomeCliente.clear();
    cbProduto.setValue(null); // Limpa seleção do ComboBox de produto
    cbProduto.getEditor().clear(); // Limpa o texto do editor se for editável
    txtQuantidadeVendida.clear();
    txtValorVenda.clear();
    dpDataVenda.setValue(null); // ou LocalDate.now() se preferir
    chkPago.setSelected(false); // Garante que dpDataPrometida será desabilitado pelo listener
    // dpDataPrometida.setValue(null); // Já é feito pelo listener do chkPago
    // dpDataPrometida.setDisable(true); // Já é feito pelo listener do chkPago
    if (cbMetodoPagamento.getItems() != null && !cbMetodoPagamento.getItems().isEmpty()) {
        cbMetodoPagamento.getSelectionModel().selectFirst();
    }
    txtDesconto.clear();

    atualizarResumoVenda(); // Reseta os labels do resumo
    txtNomeCliente.requestFocus(); // Ou cbProduto.requestFocus();
}

}