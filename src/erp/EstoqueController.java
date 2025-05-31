package erp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.*;

public class EstoqueController {

    // ComboBox para filtrar por Clube
    @FXML
    private ComboBox<String> cbClube;

    // TableView para exibir os dados do estoque
    @FXML
    private TableView<ProdutoEstoque> tblEstoque;

    // Colunas da tabela
    @FXML
    private TableColumn<ProdutoEstoque, String> colModelo;
    @FXML
    private TableColumn<ProdutoEstoque, Integer> colQuantidade;
    @FXML
    private TableColumn<ProdutoEstoque, Integer> colP;
    @FXML
    private TableColumn<ProdutoEstoque, Integer> colM;
    @FXML
    private TableColumn<ProdutoEstoque, Integer> colG;
    @FXML
    private TableColumn<ProdutoEstoque, Integer> colGG;
    @FXML
    private TableColumn<ProdutoEstoque, Integer> col2GG;
    @FXML
    private TableColumn<ProdutoEstoque, Integer> col3GG;
    @FXML
    private TableColumn<ProdutoEstoque, Integer> col4GG;

    // Lista de produtos para a tabela
    private ObservableList<ProdutoEstoque> listaProdutos;

    // Inicializa a tabela e os filtros
    @FXML
    public void initialize() {
        // Inicializa a lista de produtos
        listaProdutos = FXCollections.observableArrayList();

        // Configura as colunas da tabela
        colModelo.setCellValueFactory(cellData -> cellData.getValue().modeloProperty());
        colQuantidade.setCellValueFactory(cellData -> cellData.getValue().quantidadeTotalProperty().asObject());
        colP.setCellValueFactory(cellData -> cellData.getValue().quantidadePProperty().asObject());
        colM.setCellValueFactory(cellData -> cellData.getValue().quantidadeMProperty().asObject());
        colG.setCellValueFactory(cellData -> cellData.getValue().quantidadeGProperty().asObject());
        colGG.setCellValueFactory(cellData -> cellData.getValue().quantidadeGGProperty().asObject());
        col2GG.setCellValueFactory(cellData -> cellData.getValue().quantidade2GGProperty().asObject());
        col3GG.setCellValueFactory(cellData -> cellData.getValue().quantidade3GGProperty().asObject());
        col4GG.setCellValueFactory(cellData -> cellData.getValue().quantidade4GGProperty().asObject());

        // Carrega os dados do banco
        carregarProdutos();

        // Filtra por Clube
        cbClube.setItems(FXCollections.observableArrayList("Todos", "Clube A", "Clube B", "Clube C"));
        cbClube.getSelectionModel().select(0); // "Todos" selecionado por padrão

        // Listener para filtrar quando o Clube for alterado
        cbClube.setOnAction(e -> filtrarEstoque());
    }

    // Método para carregar os dados do estoque do banco
    private void carregarProdutos() {
        // SQL para pegar a quantidade total e por tamanho de cada modelo
        String sql = "SELECT modelo, clube, tamanho, SUM(quantidade) as quantidade_total FROM estoque GROUP BY modelo, tamanho, clube";

        try (Connection con = UTIL.ConexaoBanco.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String modelo = rs.getString("modelo");
                String clube = rs.getString("clube");
                String tamanho = rs.getString("tamanho");
                int quantidade = rs.getInt("quantidade_total");

                ProdutoEstoque produto = findOrCreateProduto(modelo, clube);
                produto.addQuantidadePorTamanho(tamanho, quantidade);

                listaProdutos.add(produto);
            }

            // Adiciona os produtos à tabela
            tblEstoque.setItems(listaProdutos);
        } catch (SQLException e) {
            System.err.println("Erro ao carregar produtos: " + e.getMessage());
        }
    }

    // Método para encontrar ou criar um ProdutoEstoque baseado no modelo e clube
    private ProdutoEstoque findOrCreateProduto(String modelo, String clube) {
        for (ProdutoEstoque produto : listaProdutos) {
            if (produto.getModelo().equals(modelo) && produto.getClube().equals(clube)) {
                return produto;
            }
        }
        ProdutoEstoque novoProduto = new ProdutoEstoque(modelo, clube);
        listaProdutos.add(novoProduto);
        return novoProduto;
    }

    // Método para filtrar os dados do estoque com base no Clube selecionado
    private void filtrarEstoque() {
        String filtro = cbClube.getSelectionModel().getSelectedItem();

        if (filtro.equals("Todos")) {
            tblEstoque.setItems(listaProdutos);  // Exibe todos os produtos
        } else {
            ObservableList<ProdutoEstoque> produtosFiltrados = FXCollections.observableArrayList();
            for (ProdutoEstoque produto : listaProdutos) {
                if (produto.getClube().equals(filtro)) {
                    produtosFiltrados.add(produto);
                }
            }
            tblEstoque.setItems(produtosFiltrados);  // Exibe apenas os produtos do clube selecionado
        }
    }
    @FXML
    private Button btnAtualizarEstoque;

    // Método para ir para a tela de Cadastro de Estoque
    @FXML
    public void irParaCadastroEstoque() {
        try {
            // Carregar o FXML da tela de Cadastro de Estoque
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/erp/TelaCadastroProduto.fxml"));
            BorderPane cadastroEstoqueRoot = loader.load();

            // Criar uma nova cena com o conteúdo da tela de Cadastro de Estoque
            Scene cadastroEstoqueScene = new Scene(cadastroEstoqueRoot);

            // Obter a janela principal (Stage)
            Stage stage = (Stage) btnAtualizarEstoque.getScene().getWindow();

            // Setar a nova cena no Stage (janela)
            stage.setScene(cadastroEstoqueScene);
            stage.setTitle("Cadastro de Estoque");
            stage.show();
        } catch (IOException e) {
            System.out.println("Erro ao carregar a tela de Cadastro de Estoque: " + e.getMessage());
        }
    }
}

