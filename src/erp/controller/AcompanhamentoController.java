package erp.controller;

import erp.model.ItemPedidoDetalheVO;
import erp.model.PedidoVO;
import UTIL.ConexaoBanco;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;

public class AcompanhamentoController implements Initializable {

    @FXML private TableView<PedidoVO> tblPedidos;
    @FXML private TableColumn<PedidoVO, Integer> colPedidoId;
    @FXML private TableColumn<PedidoVO, LocalDate> colDataPedido;
    @FXML private TableColumn<PedidoVO, String> colFornecedor;
    @FXML private TableColumn<PedidoVO, Double> colCustoTotal;
    @FXML private TableColumn<PedidoVO, String> colStatus;
    
    @FXML private TableView<ItemPedidoDetalheVO> tblItensPedido;
    @FXML private TableColumn<ItemPedidoDetalheVO, String> colItemProduto;
    @FXML private TableColumn<ItemPedidoDetalheVO, Integer> colItemQtdPedida;
    @FXML private TableColumn<ItemPedidoDetalheVO, Integer> colItemQtdRecebida;
    @FXML private TableColumn<ItemPedidoDetalheVO, Double> colItemCustoUnit;

    @FXML private Button btnRegistrarChegada;

    private final ObservableList<PedidoVO> listaPedidos = FXCollections.observableArrayList();
    private final ObservableList<ItemPedidoDetalheVO> listaItensPedido = FXCollections.observableArrayList();
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabelas();
        configurarListeners();
        carregarPedidos();
    }

    private void configurarTabelas() {
        tblPedidos.setItems(listaPedidos);
        colPedidoId.setCellValueFactory(new PropertyValueFactory<>("pedidoId"));
        colDataPedido.setCellValueFactory(new PropertyValueFactory<>("dataPedido"));
        colFornecedor.setCellValueFactory(new PropertyValueFactory<>("nomeFornecedor"));
        colCustoTotal.setCellValueFactory(new PropertyValueFactory<>("custoTotal"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        formatarColunaMoeda(colCustoTotal);

        tblItensPedido.setItems(listaItensPedido);
        colItemProduto.setCellValueFactory(new PropertyValueFactory<>("descricaoProduto"));
        colItemQtdPedida.setCellValueFactory(new PropertyValueFactory<>("quantidadePedida"));
        colItemQtdRecebida.setCellValueFactory(new PropertyValueFactory<>("quantidadeRecebida"));
        colItemCustoUnit.setCellValueFactory(new PropertyValueFactory<>("custoUnitario"));
        formatarColunaMoeda(colItemCustoUnit);
    }

    private void configurarListeners() {
        tblPedidos.getSelectionModel().selectedItemProperty().addListener((_obs, _oldSelection, newSelection) -> {
            if (newSelection != null) {
                carregarItensDoPedido(newSelection.getPedidoId());
            } else {
                listaItensPedido.clear();
            }
        });

        tblItensPedido.getSelectionModel().selectedItemProperty().addListener((_obs, _oldSelection, newSelection) -> {
            btnRegistrarChegada.setDisable(newSelection == null || newSelection.getQuantidadeRecebida() >= newSelection.getQuantidadePedida());
        });

        btnRegistrarChegada.setOnAction(event -> {
            ItemPedidoDetalheVO itemSelecionado = tblItensPedido.getSelectionModel().getSelectedItem();
            if (itemSelecionado != null) {
                registrarChegadaDialogo(itemSelecionado);
            }
        });
        
        btnRegistrarChegada.setDisable(true);
    }
    
    private void carregarPedidos() {
        listaPedidos.clear();
        String sql = "SELECT PedidoFornecedorID, DataPedido, NomeFornecedor, CustoTotalFinalPedido, StatusPedido FROM PedidosFornecedor ORDER BY DataPedido DESC";

        try (Connection con = ConexaoBanco.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            while(rs.next()) {
                listaPedidos.add(new PedidoVO(
                    rs.getInt("PedidoFornecedorID"),
                    rs.getDate("DataPedido").toLocalDate(),
                    rs.getString("NomeFornecedor"),
                    rs.getDouble("CustoTotalFinalPedido"),
                    rs.getString("StatusPedido")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Erro de Banco de Dados", "Não foi possível carregar os pedidos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void carregarItensDoPedido(int pedidoId) {
        listaItensPedido.clear();
        String sql = "SELECT IP.ItemPedidoFornecedorID, IP.ProdutoID, P.DescricaoCompleta, IP.QuantidadePedida, " +
                     "IP.QuantidadeRecebida, IP.CustoUnitarioFornecedor FROM ItensPedidoFornecedor IP " +
                     "JOIN Produtos P ON IP.ProdutoID = P.ProdutoID WHERE IP.PedidoFornecedorID = ?";

        try (Connection con = ConexaoBanco.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setInt(1, pedidoId);
            ResultSet rs = pst.executeQuery();

            while(rs.next()) {
                listaItensPedido.add(new ItemPedidoDetalheVO(
                    rs.getInt("ItemPedidoFornecedorID"),
                    rs.getInt("ProdutoID"),
                    rs.getString("DescricaoCompleta"),
                    rs.getInt("QuantidadePedida"),
                    rs.getInt("QuantidadeRecebida"),
                    rs.getDouble("CustoUnitarioFornecedor")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Erro de Banco de Dados", "Não foi possível carregar os itens do pedido: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void registrarChegadaDialogo(ItemPedidoDetalheVO itemSelecionado) {
        int qtdFaltante = itemSelecionado.getQuantidadePedida() - itemSelecionado.getQuantidadeRecebida();

        TextInputDialog dialog = new TextInputDialog(String.valueOf(qtdFaltante));
        dialog.setTitle("Registrar Chegada de Item");
        dialog.setHeaderText("Produto: " + itemSelecionado.getDescricaoProduto());
        dialog.setContentText("Digite a quantidade que chegou:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(qtdStr -> {
            try {
                int qtdRecebida = Integer.parseInt(qtdStr);
                if (qtdRecebida <= 0 || qtdRecebida > qtdFaltante) {
                    mostrarAlerta("Erro", "A quantidade deve ser um número positivo e não pode ser maior que a quantidade faltante (" + qtdFaltante + ").", Alert.AlertType.ERROR);
                    return;
                }
                processarChegadaItem(itemSelecionado, qtdRecebida);
            } catch (NumberFormatException e) {
                mostrarAlerta("Erro", "Por favor, digite um número válido.", Alert.AlertType.ERROR);
            }
        });
    }
    
    private void processarChegadaItem(ItemPedidoDetalheVO item, int quantidadeChegou) {
        Connection con = null;
        try {
            con = ConexaoBanco.conectar();
            con.setAutoCommit(false);

            String sqlUpdateItem = "UPDATE ItensPedidoFornecedor SET QuantidadeRecebida = QuantidadeRecebida + ? WHERE ItemPedidoFornecedorID = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlUpdateItem)) {
                pst.setInt(1, quantidadeChegou);
                pst.setInt(2, item.getItemPedidoId());
                pst.executeUpdate();
            }

    
            String sqlSelectProduto = "SELECT QuantidadeEstoque, CustoMedioPonderado FROM Produtos WHERE ProdutoID = ?";
            double estoqueAntigo = 0, custoMedioAntigo = 0;
            try(PreparedStatement pst = con.prepareStatement(sqlSelectProduto)) {
                pst.setInt(1, item.getProdutoId());
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    estoqueAntigo = rs.getInt("QuantidadeEstoque");
                    custoMedioAntigo = rs.getDouble("CustoMedioPonderado");
                }
            }
            
            double novoEstoque = estoqueAntigo + quantidadeChegou;
            double custoDoLote = item.getCustoUnitario();
            double novoCustoMedio = ((estoqueAntigo * custoMedioAntigo) + (quantidadeChegou * custoDoLote)) / novoEstoque;

            String sqlUpdateProduto = "UPDATE Produtos SET QuantidadeEstoque = ?, CustoMedioPonderado = ?, DataUltimaEntradaEstoque = CURDATE() WHERE ProdutoID = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlUpdateProduto)) {
                pst.setDouble(1, novoEstoque);
                pst.setDouble(2, novoCustoMedio);
                pst.setInt(3, item.getProdutoId());
                pst.executeUpdate();
            }

            int pedidoId = tblPedidos.getSelectionModel().getSelectedItem().getPedidoId();
            String sqlVerificaStatus = "SELECT SUM(QuantidadePedida) as totalPedida, SUM(QuantidadeRecebida) as totalRecebida FROM ItensPedidoFornecedor WHERE PedidoFornecedorID = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlVerificaStatus)) {
                pst.setInt(1, pedidoId);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    String novoStatus = (rs.getInt("totalRecebida") >= rs.getInt("totalPedida")) ? "Recebido Integralmente" : "Recebido Parcialmente";
                    String sqlUpdatePedido = "UPDATE PedidosFornecedor SET StatusPedido = ? WHERE PedidoFornecedorID = ?";
                    try (PreparedStatement pstUpdate = con.prepareStatement(sqlUpdatePedido)) {
                        pstUpdate.setString(1, novoStatus);
                        pstUpdate.setInt(2, pedidoId);
                        pstUpdate.executeUpdate();
                    }
                }
            }

            con.commit();
            mostrarAlerta("Sucesso", quantidadeChegou + " unidades de '" + item.getDescricaoProduto() + "' adicionadas ao estoque.", Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            mostrarAlerta("Erro de Banco de Dados", "Falha ao registrar chegada de item. A operação foi desfeita.", Alert.AlertType.ERROR);
        } finally {
            if (con != null) try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            carregarPedidos();
            
           
            if(tblPedidos.getSelectionModel().getSelectedItem() != null) {
                carregarItensDoPedido(tblPedidos.getSelectionModel().getSelectedItem().getPedidoId());
            }
        }
    }
    
    private <T> void formatarColunaMoeda(TableColumn<T, Double> coluna) {
        coluna.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) setText(null);
                else {
                    setText(currencyFormat.format(valor));
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}