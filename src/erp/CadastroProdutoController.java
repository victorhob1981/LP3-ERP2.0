package erp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CadastroProdutoController {

    // Campos do formulário
    @FXML private TextField txtModelo;
    @FXML private TextField txtClube;
    @FXML private ComboBox<String> cbPublicoAlvo;  // ComboBox para Público-alvo
    @FXML private ComboBox<String> cbTamanho;      // ComboBox para Tamanho
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtValor;

    // Lista de visualização dos produtos adicionados
    @FXML private TableView<ProdutoCadastro> tblListaProdutos;
    @FXML private TableColumn<ProdutoCadastro, String> colModelo;
    @FXML private TableColumn<ProdutoCadastro, String> colClube;
    @FXML private TableColumn<ProdutoCadastro, String> colPublicoAlvo;
    @FXML private TableColumn<ProdutoCadastro, String> colTamanho;
    @FXML private TableColumn<ProdutoCadastro, Integer> colQuantidade;
    @FXML private TableColumn<ProdutoCadastro, Double> colValor;

    // Lista de produtos para exibição na tabela
    private ObservableList<ProdutoCadastro> listaProdutos;

    @FXML
public void initialize() {
    listaProdutos = FXCollections.observableArrayList();

    // Configura as colunas da tabela
    colModelo.setCellValueFactory(cellData -> cellData.getValue().modeloProperty());
    colClube.setCellValueFactory(cellData -> cellData.getValue().clubeProperty());
    colPublicoAlvo.setCellValueFactory(cellData -> cellData.getValue().publicoAlvoProperty());
    colTamanho.setCellValueFactory(cellData -> cellData.getValue().tamanhoProperty());
    colQuantidade.setCellValueFactory(cellData -> cellData.getValue().quantidadeProperty().asObject());
    colValor.setCellValueFactory(cellData -> cellData.getValue().valorProperty().asObject());

    // Inicializa as ComboBox com as opções fixas
    ObservableList<String> publicoAlvoOptions = FXCollections.observableArrayList("Masculina", "Feminina", "Infantil");
    cbPublicoAlvo.setItems(publicoAlvoOptions);  // Define as opções da ComboBox
    cbPublicoAlvo.getSelectionModel().selectFirst();  // Seleciona a primeira opção por padrão

    ObservableList<String> tamanhoOptions = FXCollections.observableArrayList("P", "M", "G", "GG", "2GG", "3GG", "4GG");
    cbTamanho.setItems(tamanhoOptions);  // Define as opções da ComboBox
    cbTamanho.getSelectionModel().selectFirst();  // Seleciona a primeira opção por padrão
}


    // Método para adicionar produto à lista de visualização
   @FXML
public void adicionarProduto() {
    String modelo = txtModelo.getText();
    String clube = txtClube.getText();
    String publicoAlvo = cbPublicoAlvo.getValue();  // Obtém o valor da ComboBox
    String tamanho = cbTamanho.getValue();        // Obtém o valor da ComboBox
    int quantidade = Integer.parseInt(txtQuantidade.getText());
    double valor = Double.parseDouble(txtValor.getText());

    // Cria o produto e adiciona à lista
    ProdutoCadastro produto = new ProdutoCadastro(modelo, clube, publicoAlvo, tamanho, quantidade, valor);
    listaProdutos.add(produto);  // Adiciona o produto à lista

    // Atualiza a tabela de visualização
    tblListaProdutos.setItems(listaProdutos);

    // Limpa os campos do formulário
    limparFormulario();
}


    // Método para limpar os campos do formulário
    private void limparFormulario() {
        txtModelo.clear();
        txtClube.clear();
        cbPublicoAlvo.getSelectionModel().clearSelection();  // Limpa a seleção da ComboBox
        cbTamanho.getSelectionModel().clearSelection();      // Limpa a seleção da ComboBox
        txtQuantidade.clear();
        txtValor.clear();
    }

    // Método para inserir os produtos no banco de dados
    @FXML
    public void inserirEstoque() {
        String sql = "INSERT INTO estoque (modelo, clube, publico_alvo, tamanho, quantidade, valor_unitario) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = UTIL.ConexaoBanco.conectar()) {
            for (ProdutoCadastro produto : listaProdutos) {
                try (PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setString(1, produto.getModelo());
                    pst.setString(2, produto.getClube());
                    pst.setString(3, produto.getPublicoAlvo());
                    pst.setString(4, produto.getTamanho());
                    pst.setInt(5, produto.getQuantidade());
                    pst.setDouble(6, produto.getValor());

                    pst.executeUpdate();  // Executa a inserção no banco
                }
            }

            // Limpa a lista e a tabela após inserção
            listaProdutos.clear();
            tblListaProdutos.setItems(listaProdutos);

            // Mostra alerta de sucesso
            mostrarAlerta("Sucesso", "Produtos inseridos no estoque com sucesso!");

        } catch (SQLException e) {
            mostrarAlerta("Erro", "Erro ao inserir no estoque: " + e.getMessage());
        }
    }

    // Método para mostrar alertas
    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
