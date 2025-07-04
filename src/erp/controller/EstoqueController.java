package erp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import erp.model.ProdutoEstoque;


public class EstoqueController {
    private MainLayoutController mainLayoutController;

    @FXML
    private ComboBox<String> cbClube;

    @FXML
    private TableView<ProdutoEstoque> tblEstoque;

    @FXML
    private TableColumn<ProdutoEstoque, String> colModelo;
    @FXML
    private TableColumn<ProdutoEstoque, String> colClubeNome; 
    @FXML
    private TableColumn<ProdutoEstoque, String> colTipo;
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
    
    @FXML
    private Button btnAtualizarEstoque;
    @FXML
    private Button btnIrParaCadastro;


    private ObservableList<ProdutoEstoque> listaCompletaProdutosEstoque;

public void setMainLayoutController(MainLayoutController mainLayoutController) {
    this.mainLayoutController = mainLayoutController;
}



    @FXML
    public void initialize() {
        listaCompletaProdutosEstoque = FXCollections.observableArrayList();

      
        colModelo.setCellValueFactory(cellData -> cellData.getValue().modeloProperty());
        colClubeNome.setCellValueFactory(cellData -> cellData.getValue().clubeProperty());
        colTipo.setCellValueFactory(cellData -> cellData.getValue().tipoProperty());     
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

        cbClube.setOnAction(_ -> filtrarEstoquePorClube());
        
        
        if (btnAtualizarEstoque != null) {
            btnAtualizarEstoque.setOnAction(_ -> acaoAtualizarEstoque());
        }
        
    }

    private void carregarDadosIniciais() {
        carregarProdutosDoBanco();
        carregarClubesParaFiltro(); 
        if (cbClube.getItems().isEmpty() || !"Todos".equals(cbClube.getItems().get(0))) {
            cbClube.getItems().add(0, "Todos");
        }
        cbClube.getSelectionModel().select("Todos"); 
       
    }

    private void carregarClubesParaFiltro() {
        ObservableList<String> clubes = FXCollections.observableArrayList();
        clubes.add("Todos");

        TreeSet<String> nomesClubesUnicos = new TreeSet<>();
       
        for (ProdutoEstoque pe : listaCompletaProdutosEstoque) {
            nomesClubesUnicos.add(pe.getClube());
        }
        clubes.addAll(nomesClubesUnicos);
        cbClube.setItems(clubes);
    }

    private void carregarProdutosDoBanco() {
        listaCompletaProdutosEstoque.clear();
        Map<String, ProdutoEstoque> mapaProdutosAgregados = new HashMap<>();

        
        String sql = "SELECT Modelo, Clube, Tipo, Tamanho, QuantidadeEstoque " +
                     "FROM Produtos " +
                     "WHERE QuantidadeEstoque > 0 " + 
                     "ORDER BY Clube, Modelo, Tipo, Tamanho";

        try (Connection con = UTIL.ConexaoBanco.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String modelo = rs.getString("Modelo");
                String clube = rs.getString("Clube");
                String tipo = rs.getString("Tipo"); 
                String tamanho = rs.getString("Tamanho");
                int quantidade = rs.getInt("QuantidadeEstoque"); 

                
                String chaveProduto = clube + "|" + modelo + "|" + tipo;

                ProdutoEstoque produtoEstoqueLinha = mapaProdutosAgregados.get(chaveProduto);
                if (produtoEstoqueLinha == null) {
                    produtoEstoqueLinha = new ProdutoEstoque(modelo, clube, tipo);
                    mapaProdutosAgregados.put(chaveProduto, produtoEstoqueLinha);
                }
               
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
    
   
    public void acaoAtualizarEstoque() {
        carregarDadosIniciais(); 
        mostrarAlerta("Informação", "Lista de estoque atualizada.");
    }

   
    @FXML
public void irParaCadastroEstoque(ActionEvent event) {
    if (mainLayoutController != null) {
        mainLayoutController.irParaRegistrarPedido(event);
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