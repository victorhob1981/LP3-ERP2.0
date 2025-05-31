package erp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime; // Para DataUltimaEntradaEstoque

public class CadastroProdutoController {

    // Campos do formulário
    @FXML private TextField txtModelo;
    @FXML private TextField txtClube;
    @FXML private ComboBox<String> cbTipo;         // Renomeado de cbPublicoAlvo
    @FXML private ComboBox<String> cbTamanho;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtPrecoVenda;      // Renomeado de txtValor
    @FXML private TextField txtCustoCompra;     // NOVO CAMPO

    // Lista de visualização dos produtos adicionados
    @FXML private TableView<ProdutoCadastro> tblListaProdutos;
    @FXML private TableColumn<ProdutoCadastro, String> colModelo;
    @FXML private TableColumn<ProdutoCadastro, String> colClube;
    @FXML private TableColumn<ProdutoCadastro, String> colTipo; // Renomeado de colPublicoAlvo
    @FXML private TableColumn<ProdutoCadastro, String> colTamanho;
    @FXML private TableColumn<ProdutoCadastro, Integer> colQuantidade;
    @FXML private TableColumn<ProdutoCadastro, Double> colPrecoVenda; // Renomeado de colValor
    @FXML private TableColumn<ProdutoCadastro, Double> colCustoCompra; // NOVA COLUNA

    private ObservableList<ProdutoCadastro> listaProdutosTemporaria; // Renomeado para clareza

    @FXML
    public void initialize() {
        listaProdutosTemporaria = FXCollections.observableArrayList();
        tblListaProdutos.setItems(listaProdutosTemporaria);

        // Configura as colunas da tabela
        colModelo.setCellValueFactory(cellData -> cellData.getValue().modeloProperty());
        colClube.setCellValueFactory(cellData -> cellData.getValue().clubeProperty());
        colTipo.setCellValueFactory(cellData -> cellData.getValue().tipoProperty());
        colTamanho.setCellValueFactory(cellData -> cellData.getValue().tamanhoProperty());
        colQuantidade.setCellValueFactory(cellData -> cellData.getValue().quantidadeProperty().asObject());
        colPrecoVenda.setCellValueFactory(cellData -> cellData.getValue().precoVendaProperty().asObject());
        colCustoCompra.setCellValueFactory(cellData -> cellData.getValue().custoCompraProperty().asObject()); // Configura nova coluna

        // Inicializa as ComboBox com as opções fixas
        ObservableList<String> tipoOptions = FXCollections.observableArrayList("Masculina", "Feminina", "Infantil");
        cbTipo.setItems(tipoOptions);
        if (!tipoOptions.isEmpty()) cbTipo.getSelectionModel().selectFirst();

        ObservableList<String> tamanhoOptions = FXCollections.observableArrayList("P", "M", "G", "GG", "2GG", "3GG", "4GG"); // Usei XXL etc.
        // Ou mantenha "2GG", "3GG", "4GG" se preferir, mas seja consistente com ProdutoEstoque.java
        cbTamanho.setItems(tamanhoOptions);
        if (!tamanhoOptions.isEmpty()) cbTamanho.getSelectionModel().selectFirst();
    }

    @FXML
    public void adicionarProdutoNaLista() { // Renomeado para clareza
        String modelo = txtModelo.getText().trim();
        String clube = txtClube.getText().trim();
        String tipo = cbTipo.getValue();
        String tamanho = cbTamanho.getValue();
        
        // Validações de entrada
        if (modelo.isEmpty() || clube.isEmpty() || tipo == null || tamanho == null ||
            txtQuantidade.getText().trim().isEmpty() || 
            txtPrecoVenda.getText().trim().isEmpty() || 
            txtCustoCompra.getText().trim().isEmpty()) {
            mostrarAlerta("Erro de Validação", "Todos os campos são obrigatórios.", Alert.AlertType.ERROR);
            return;
        }

        try {
            int quantidade = Integer.parseInt(txtQuantidade.getText().trim());
            double precoVenda = Double.parseDouble(txtPrecoVenda.getText().trim().replace(",","."));
            double custoCompra = Double.parseDouble(txtCustoCompra.getText().trim().replace(",","."));

            if (quantidade <= 0 || precoVenda <= 0 || custoCompra <= 0) {
                mostrarAlerta("Erro de Validação", "Quantidade, Preço de Venda e Custo de Compra devem ser maiores que zero.", Alert.AlertType.ERROR);
                return;
            }

            ProdutoCadastro produto = new ProdutoCadastro(modelo, clube, tipo, tamanho, quantidade, precoVenda, custoCompra);
            listaProdutosTemporaria.add(produto);

            limparFormulario();
        } catch (NumberFormatException e) {
            mostrarAlerta("Erro de Formato", "Quantidade, Preço ou Custo inválidos. Use números válidos.", Alert.AlertType.ERROR);
        }
    }

    private void limparFormulario() {
        txtModelo.clear();
        txtClube.clear();
        // Não limpa a seleção de ComboBox para facilitar múltiplas adições do mesmo tipo/tamanho
        // cbTipo.getSelectionModel().clearSelection(); 
        // cbTamanho.getSelectionModel().clearSelection(); 
        if (!cbTipo.getItems().isEmpty()) cbTipo.getSelectionModel().selectFirst();
        if (!cbTamanho.getItems().isEmpty()) cbTamanho.getSelectionModel().selectFirst();
        txtQuantidade.clear();
        txtPrecoVenda.clear();
        txtCustoCompra.clear();
        txtModelo.requestFocus(); // Volta o foco para o primeiro campo
    }

    @FXML
    public void salvarProdutosNoBanco() { // Renomeado de inserirEstoque
        if (listaProdutosTemporaria.isEmpty()) {
            mostrarAlerta("Informação", "Nenhum produto na lista para salvar.", Alert.AlertType.INFORMATION);
            return;
        }

        Connection con = null;
        try {
            con = UTIL.ConexaoBanco.conectar();
            con.setAutoCommit(false); // Inicia transação

            String sqlSelect = "SELECT ProdutoID, QuantidadeEstoque, CustoMedioPonderado FROM Produtos WHERE Modelo = ? AND Clube = ? AND Tipo = ? AND Tamanho = ?";
            String sqlInsert = "INSERT INTO Produtos (Modelo, Clube, Tipo, Tamanho, DescricaoCompleta, PrecoVendaAtual, QuantidadeEstoque, CustoMedioPonderado, DataUltimaEntradaEstoque) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String sqlUpdate = "UPDATE Produtos SET PrecoVendaAtual = ?, QuantidadeEstoque = ?, CustoMedioPonderado = ?, DataUltimaEntradaEstoque = ? WHERE ProdutoID = ?";

            for (ProdutoCadastro prodCadastro : listaProdutosTemporaria) {
                String descricaoCompleta = String.format("%s %s %s %s", prodCadastro.getModelo(), prodCadastro.getClube(), prodCadastro.getTipo(), prodCadastro.getTamanho());
                Timestamp dataEntrada = Timestamp.valueOf(LocalDateTime.now());

                try (PreparedStatement pstSelect = con.prepareStatement(sqlSelect)) {
                    pstSelect.setString(1, prodCadastro.getModelo());
                    pstSelect.setString(2, prodCadastro.getClube());
                    pstSelect.setString(3, prodCadastro.getTipo());
                    pstSelect.setString(4, prodCadastro.getTamanho());
                    ResultSet rs = pstSelect.executeQuery();

                    if (rs.next()) { // Produto EXISTE, então ATUALIZA
                        int produtoIDExistente = rs.getInt("ProdutoID");
                        int estoqueAntigo = rs.getInt("QuantidadeEstoque");
                        double custoMedioAntigo = rs.getDouble("CustoMedioPonderado");

                        int quantidadeNovaNoLote = prodCadastro.getQuantidade();
                        double custoNovoLote = prodCadastro.getCustoCompra();
                        
                        int novoEstoqueTotal = estoqueAntigo + quantidadeNovaNoLote;
                        double novoCustoMedioPonderado;

                        if (novoEstoqueTotal == 0) { // Caso raro, se estoque antigo e novo lote fossem 0 ou se subtraísse
                            novoCustoMedioPonderado = custoNovoLote; // Ou manter o antigo, ou zerar.
                        } else if (estoqueAntigo == 0) { // Se não havia estoque antes
                             novoCustoMedioPonderado = custoNovoLote;
                        }
                        else {
                            novoCustoMedioPonderado = ((estoqueAntigo * custoMedioAntigo) + (quantidadeNovaNoLote * custoNovoLote)) / novoEstoqueTotal;
                        }
                        
                        try (PreparedStatement pstUpdate = con.prepareStatement(sqlUpdate)) {
                            pstUpdate.setDouble(1, prodCadastro.getPrecoVenda()); // Atualiza o preço de venda
                            pstUpdate.setInt(2, novoEstoqueTotal);
                            pstUpdate.setDouble(3, novoCustoMedioPonderado);
                            pstUpdate.setTimestamp(4, dataEntrada);
                            pstUpdate.setInt(5, produtoIDExistente);
                            pstUpdate.executeUpdate();
                        }
                    } else { // Produto NÃO EXISTE, então INSERE
                        try (PreparedStatement pstInsert = con.prepareStatement(sqlInsert)) {
                            pstInsert.setString(1, prodCadastro.getModelo());
                            pstInsert.setString(2, prodCadastro.getClube());
                            pstInsert.setString(3, prodCadastro.getTipo());
                            pstInsert.setString(4, prodCadastro.getTamanho());
                            pstInsert.setString(5, descricaoCompleta);
                            pstInsert.setDouble(6, prodCadastro.getPrecoVenda());
                            pstInsert.setInt(7, prodCadastro.getQuantidade());
                            pstInsert.setDouble(8, prodCadastro.getCustoCompra()); // Custo médio inicial é o custo de compra
                            pstInsert.setTimestamp(9, dataEntrada);
                            pstInsert.executeUpdate();
                        }
                    }
                    rs.close();
                }
            }
            con.commit(); // Efetiva todas as operações
            listaProdutosTemporaria.clear(); // Limpa a lista da tela
            mostrarAlerta("Sucesso", "Produtos salvos no banco de dados com sucesso!", Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback(); // Desfaz em caso de erro
                } catch (SQLException ex) {
                    System.err.println("Erro ao fazer rollback: " + ex.getMessage());
                }
            }
            mostrarAlerta("Erro de Banco", "Erro ao salvar produtos: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true); // Restaura auto commit
                    con.close();
                } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão: " + e.getMessage());
                }
            }
        }
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}