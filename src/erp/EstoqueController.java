package erp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
// import javafx.beans.property.SimpleStringProperty; // Não mais necessário aqui se ProdutoEstoque lida com properties

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class EstoqueController {

    @FXML
    private ComboBox<String> cbClube;

    @FXML
    private TableView<ProdutoEstoque> tblEstoque;

    @FXML
    private TableColumn<ProdutoEstoque, String> colModelo;
    @FXML
    private TableColumn<ProdutoEstoque, String> colClubeNome; // Coluna para o nome do Clube
    @FXML
    private TableColumn<ProdutoEstoque, String> colTipo;    // NOVA COLUNA PARA O TIPO
    @FXML
    private TableColumn<ProdutoEstoque, Integer> colQuantidade; // Total
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
    
    @FXML
    private Button btnAtualizarEstoque; // Botão para recarregar os dados
    @FXML
    private Button btnIrParaCadastro; // Botão para ir para tela de cadastro/gerenciamento de produtos


    private ObservableList<ProdutoEstoque> listaCompletaProdutosEstoque;

    @FXML
    public void initialize() {
        listaCompletaProdutosEstoque = FXCollections.observableArrayList();

        // Configura as cell value factories
        colModelo.setCellValueFactory(cellData -> cellData.getValue().modeloProperty());
        colClubeNome.setCellValueFactory(cellData -> cellData.getValue().clubeProperty()); // Vincula à propriedade clube
        colTipo.setCellValueFactory(cellData -> cellData.getValue().tipoProperty());      // Vincula à nova propriedade tipo
        colQuantidade.setCellValueFactory(cellData -> cellData.getValue().quantidadeTotalProperty().asObject());
        colP.setCellValueFactory(cellData -> cellData.getValue().quantidadePProperty().asObject());
        colM.setCellValueFactory(cellData -> cellData.getValue().quantidadeMProperty().asObject());
        colG.setCellValueFactory(cellData -> cellData.getValue().quantidadeGProperty().asObject());
        colGG.setCellValueFactory(cellData -> cellData.getValue().quantidadeGGProperty().asObject());
        col2GG.setCellValueFactory(cellData -> cellData.getValue().quantidade2GGProperty().asObject());
        col3GG.setCellValueFactory(cellData -> cellData.getValue().quantidade3GGProperty().asObject());
        col4GG.setCellValueFactory(cellData -> cellData.getValue().quantidade4GGProperty().asObject());

        tblEstoque.setItems(listaCompletaProdutosEstoque);

        carregarDadosIniciais();

        cbClube.setOnAction(e -> filtrarEstoquePorClube());
        
        // Ação para o botão de atualizar (se não estiver no FXML, adicione onAction lá)
        if (btnAtualizarEstoque != null) {
            btnAtualizarEstoque.setOnAction(e -> acaoAtualizarEstoque());
        }
        // Ação para o botão de ir para cadastro (se não estiver no FXML, adicione onAction lá)
        if (btnIrParaCadastro != null) {
            btnIrParaCadastro.setOnAction(e -> irParaCadastroEstoque());
        }
    }

    private void carregarDadosIniciais() {
        carregarProdutosDoBanco();
        carregarClubesParaFiltro(); // Popula o ComboBox de clubes
        if (cbClube.getItems().isEmpty() || !"Todos".equals(cbClube.getItems().get(0))) {
            cbClube.getItems().add(0, "Todos"); // Garante que "Todos" seja a primeira opção
        }
        cbClube.getSelectionModel().select("Todos"); // Seleciona "Todos" por padrão
        // A filtragem inicial é feita implicitamente ao setar "Todos"
        // ou você pode chamar filtrarEstoquePorClube() explicitamente se necessário.
    }

    private void carregarClubesParaFiltro() {
        ObservableList<String> clubes = FXCollections.observableArrayList();
        clubes.add("Todos");

        TreeSet<String> nomesClubesUnicos = new TreeSet<>();
        // Itera sobre a lista já carregada para pegar os clubes
        for (ProdutoEstoque pe : listaCompletaProdutosEstoque) {
            nomesClubesUnicos.add(pe.getClube());
        }
        clubes.addAll(nomesClubesUnicos);
        cbClube.setItems(clubes);
    }

    private void carregarProdutosDoBanco() {
        listaCompletaProdutosEstoque.clear();
        Map<String, ProdutoEstoque> mapaProdutosAgregados = new HashMap<>();

        // Query SQL para buscar os dados necessários.
        // Não precisamos mais de SUM() ou GROUP BY aqui, pois cada Tipo será uma linha.
        String sql = "SELECT Modelo, Clube, Tipo, Tamanho, QuantidadeEstoque " +
                     "FROM Produtos " +
                     "WHERE QuantidadeEstoque > 0 " + // Opcional: mostrar apenas o que tem estoque
                     "ORDER BY Clube, Modelo, Tipo, Tamanho";

        try (Connection con = UTIL.ConexaoBanco.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String modelo = rs.getString("Modelo");
                String clube = rs.getString("Clube");
                String tipo = rs.getString("Tipo"); // Obtém o Tipo do produto
                String tamanho = rs.getString("Tamanho");
                int quantidade = rs.getInt("QuantidadeEstoque"); // Quantidade específica desta variante

                // Chave para o mapa: combina clube, modelo e tipo
                String chaveProduto = clube + "|" + modelo + "|" + tipo;

                ProdutoEstoque produtoEstoqueLinha = mapaProdutosAgregados.get(chaveProduto);
                if (produtoEstoqueLinha == null) {
                    produtoEstoqueLinha = new ProdutoEstoque(modelo, clube, tipo);
                    mapaProdutosAgregados.put(chaveProduto, produtoEstoqueLinha);
                }
                // Define a quantidade para o tamanho específico desta linha (Modelo-Clube-Tipo)
                produtoEstoqueLinha.setQuantidadeParaTamanho(tamanho, quantidade);
            }

            listaCompletaProdutosEstoque.addAll(mapaProdutosAgregados.values());

        } catch (SQLException e) {
            System.err.println("Erro ao carregar produtos do banco: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Erro de Banco", "Não foi possível carregar os dados do estoque.");
        }
    }

    private void filtrarEstoquePorClube() {
        String clubeSelecionado = cbClube.getValue();

        if (clubeSelecionado == null || clubeSelecionado.equals("Todos")) {
            tblEstoque.setItems(listaCompletaProdutosEstoque);
        } else {
            ObservableList<ProdutoEstoque> produtosFiltrados = FXCollections.observableArrayList();
            for (ProdutoEstoque produto : listaCompletaProdutosEstoque) {
                if (produto.getClube().equals(clubeSelecionado)) {
                    produtosFiltrados.add(produto);
                }
            }
            tblEstoque.setItems(produtosFiltrados);
        }
    }
    
    // @FXML // Removido @FXML se o onAction for definido programaticamente no initialize
    public void acaoAtualizarEstoque() {
        carregarDadosIniciais(); // Recarrega os clubes e produtos, e aplica o filtro
        mostrarAlerta("Informação", "Lista de estoque atualizada.");
    }

    // @FXML // Removido @FXML se o onAction for definido programaticamente no initialize
    public void irParaCadastroEstoque() {
        try {
            // Tente usar um caminho relativo mais simples se estiver no mesmo pacote
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TelaCadastroProduto.fxml"));
            if (loader.getLocation() == null) { // Fallback para caminho absoluto se o relativo falhar
                 loader.setLocation(getClass().getResource("/erp/TelaCadastroProduto.fxml"));
            }
            
            BorderPane cadastroEstoqueRoot = loader.load();
            Scene cadastroEstoqueScene = new Scene(cadastroEstoqueRoot);
            
            // Tenta obter o Stage de um componente visível
            Stage stage = null;
            if (tblEstoque.getScene() != null && tblEstoque.getScene().getWindow() != null) {
                stage = (Stage) tblEstoque.getScene().getWindow();
            } else if (cbClube.getScene() != null && cbClube.getScene().getWindow() != null ) {
                 stage = (Stage) cbClube.getScene().getWindow();
            } else {
                 mostrarAlerta("Erro de Navegação", "Não foi possível determinar a janela atual para navegação.");
                return;
            }

            stage.setScene(cadastroEstoqueScene);
            stage.setTitle("Gerenciar Produtos");
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Erro de Carregamento", "Erro ao carregar a tela de Gerenciar Produtos: " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            mostrarAlerta("Erro de Configuração", "Não foi possível encontrar o arquivo FXML 'TelaCadastroProduto.fxml'. Verifique o caminho.");
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert.AlertType tipoAlerta = titulo.toLowerCase().contains("erro") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;
        Alert alert = new Alert(tipoAlerta);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}