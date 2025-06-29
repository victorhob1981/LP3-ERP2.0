package erp.controller;

import UTIL.ConexaoBanco;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class TelaInicialController implements Initializable {

   
    @FXML private Label lblFaturamentoMes;
    @FXML private Label lblLucroMes;
    @FXML private Label lblEncomendasAbertas;
    @FXML private Label lblPedidosAbertos;
    @FXML private ListView<String> lvEncomendasPendentes;
    @FXML private ListView<String> lvPedidosEmTransito;

    private MainLayoutController mainLayoutController;
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Platform.runLater(() -> carregarDadosDashboard());
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    private void carregarDadosDashboard() {
        carregarKPIs();
        carregarListaEncomendas();
        carregarListaPedidos();
    }
    
    private void carregarKPIs() {
        String sql = "SELECT " +
            "(SELECT COALESCE(SUM(IV.PrecoVendaUnitarioRegistrado * IV.Quantidade) - COALESCE(SUM(IV.CustoMedioUnitarioRegistrado * IV.Quantidade), 0), 0) FROM ItensVenda IV JOIN Vendas V ON IV.VendaID = V.VendaID WHERE MONTH(V.DataVenda) = MONTH(CURDATE()) AND YEAR(V.DataVenda) = YEAR(CURDATE())) as LucroMes, " +
            "(SELECT COALESCE(SUM(V.ValorFinalVenda), 0) FROM Vendas V WHERE V.StatusPagamento = 'Pago' AND MONTH(V.DataVenda) = MONTH(CURDATE()) AND YEAR(V.DataVenda) = YEAR(CURDATE())) as FaturamentoMes, " +
            "(SELECT COUNT(*) FROM EncomendasCliente WHERE StatusEncomenda = 'Pendente') as EncomendasAbertas, " +
            "(SELECT COUNT(*) FROM PedidosFornecedor WHERE StatusPedido != 'Recebido Integralmente') as PedidosAbertos";

        try (Connection con = ConexaoBanco.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            if (rs.next()) {
                lblFaturamentoMes.setText(currencyFormat.format(rs.getDouble("FaturamentoMes")));
                lblLucroMes.setText(currencyFormat.format(rs.getDouble("LucroMes")));
                lblEncomendasAbertas.setText(String.valueOf(rs.getInt("EncomendasAbertas")));
                lblPedidosAbertos.setText(String.valueOf(rs.getInt("PedidosAbertos")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarListaEncomendas() {
    ObservableList<String> encomendas = FXCollections.observableArrayList();
    String sql = "SELECT C.NomeCliente, E.Clube, E.Modelo, E.Tamanho FROM EncomendasCliente E JOIN Clientes C ON E.ClienteID = C.ClienteID WHERE E.StatusEncomenda = 'Pendente' ORDER BY E.DataEncomenda ASC LIMIT 5";
    
    try (Connection con = ConexaoBanco.conectar();
         PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while(rs.next()) {
            String descricaoCompleta = rs.getString("Clube") + " " + rs.getString("Modelo") + " " + rs.getString("Tamanho");
            encomendas.add(descricaoCompleta + " (Cliente: " + rs.getString("NomeCliente") + ")");
        }
        lvEncomendasPendentes.setItems(encomendas);
        if (encomendas.isEmpty()) {
            lvEncomendasPendentes.setPlaceholder(new Label("Nenhuma encomenda pendente."));
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    private void carregarListaPedidos() {
        ObservableList<String> pedidos = FXCollections.observableArrayList();
        String sql = "SELECT PedidoFornecedorID, NomeFornecedor, StatusPedido FROM PedidosFornecedor WHERE StatusPedido IN ('Realizado', 'Recebido Parcialmente') ORDER BY DataPedido ASC LIMIT 5";
        
        try (Connection con = ConexaoBanco.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while(rs.next()) {
                pedidos.add("Pedido #" + rs.getInt("PedidoFornecedorID") + " (" + rs.getString("NomeFornecedor") + ") - " + rs.getString("StatusPedido"));
            }
            lvPedidosEmTransito.setItems(pedidos);
            if (pedidos.isEmpty()) {
                lvPedidosEmTransito.setPlaceholder(new Label("Nenhum pedido em trânsito."));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private void navNovaVenda(ActionEvent event) {
        if (mainLayoutController != null) mainLayoutController.irParaRegistrarVenda(event);
    }
    @FXML private void navHistoricoVendas(ActionEvent event) {
        if (mainLayoutController != null) mainLayoutController.irParaHistoricoVendas(event);
    }
    @FXML private void navEstoque(ActionEvent event) {
        if (mainLayoutController != null) mainLayoutController.irParaGerenciarEstoque(event);
    }
    @FXML private void navFinanceiro(ActionEvent event) {
        if (mainLayoutController != null) mainLayoutController.irParaFinanceiro(event);
    }
    @FXML private void navNovaEncomenda(ActionEvent event) {
        if (mainLayoutController != null) mainLayoutController.irParaEncomendas(event);
    }
    @FXML private void navAcompanharPedidos(ActionEvent event) {
        if (mainLayoutController != null) mainLayoutController.irParaAcompanhamento(event);
    }
    @FXML private void navNovoPedido(ActionEvent event) {
    if (mainLayoutController != null) mainLayoutController.irParaRegistrarPedido(event);

    }
}