package erp;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button; 
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TelaInicialController {

    @FXML private Label lblFaturamentoMes;
    @FXML private Label lblVendasMes;
    @FXML private Label lblTicketMedioMes;
    @FXML private ListView<String> lvUltimasEntradas;
    @FXML private ListView<String> lvMaisVendidosMes;
    @FXML private Label lblEncomendasPendentes;
    @FXML private Label lblPedidosEmTransito;
    @FXML private Button btnDashboardNovaVenda;
    @FXML private Button btnDashboardGerenciarEstoque;
    @FXML private Button btnDashboardGerenciarProdutos;

    private MainLayoutController mainLayoutController; 

  
    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    @FXML
    public void initialize() {
        lvUltimasEntradas.setPlaceholder(new Label("Carregando..."));
        lvMaisVendidosMes.setPlaceholder(new Label("Carregando..."));
        carregarDadosDashboard();
    }

    private void carregarDadosDashboard() {
    
        try (Connection con = UTIL.ConexaoBanco.conectar()) {
          
            String sqlFaturamentoVendas = "SELECT COALESCE(SUM(ValorFinalVenda), 0) AS faturamento, COUNT(*) AS qtdVendas FROM Vendas WHERE StatusPagamento = 'Pago' AND MONTH(DataVenda) = MONTH(CURDATE()) AND YEAR(DataVenda) = YEAR(CURDATE())";
            try (PreparedStatement pst = con.prepareStatement(sqlFaturamentoVendas);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    double faturamento = rs.getDouble("faturamento");
                    int qtdVendas = rs.getInt("qtdVendas");
                    lblFaturamentoMes.setText(String.format("R$ %.2f", faturamento));
                    lblVendasMes.setText(String.valueOf(qtdVendas));
                    if (qtdVendas > 0) {
                        lblTicketMedioMes.setText(String.format("R$ %.2f", faturamento / qtdVendas));
                    } else {
                        lblTicketMedioMes.setText("R$ 0,00");
                    }
                }
            }

     
            String sqlUltimasEntradas = "SELECT DescricaoCompleta FROM Produtos WHERE QuantidadeEstoque > 0 AND DataUltimaEntradaEstoque IS NOT NULL ORDER BY DataUltimaEntradaEstoque DESC LIMIT 5";
            ObservableList<String> itensUltimasEntradas = FXCollections.observableArrayList();
            try (PreparedStatement pst = con.prepareStatement(sqlUltimasEntradas);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    itensUltimasEntradas.add(rs.getString("DescricaoCompleta"));
                }
                lvUltimasEntradas.setItems(itensUltimasEntradas);
                if(itensUltimasEntradas.isEmpty()){
                    lvUltimasEntradas.setPlaceholder(new Label("Nenhuma entrada recente."));
                }
            }

           
            String sqlMaisVendidos = "SELECT P.DescricaoCompleta, SUM(IV.Quantidade) as TotalVendido " +
                                     "FROM ItensVenda IV " + //
                                     "JOIN Produtos P ON IV.ProdutoID = P.ProdutoID " +
                                     "JOIN Vendas V ON IV.VendaID = V.VendaID " +
                                     "WHERE MONTH(V.DataVenda) = MONTH(CURDATE()) AND YEAR(V.DataVenda) = YEAR(CURDATE()) " +
                                     "GROUP BY P.ProdutoID, P.DescricaoCompleta ORDER BY TotalVendido DESC LIMIT 5";
            ObservableList<String> itensMaisVendidos = FXCollections.observableArrayList();
            try (PreparedStatement pst = con.prepareStatement(sqlMaisVendidos);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    itensMaisVendidos.add(String.format("%s (%d vendidos)", rs.getString("DescricaoCompleta"), rs.getInt("TotalVendido")));
                }
                lvMaisVendidosMes.setItems(itensMaisVendidos);
                 if(itensMaisVendidos.isEmpty()){
                    lvMaisVendidosMes.setPlaceholder(new Label("Nenhuma venda este mês."));
                }
            }

            String sqlEncomendas = "SELECT COUNT(*) AS total FROM EncomendasCliente WHERE StatusEncomenda IN ('Pendente', 'PedidoAoFornecedorFeito')";
            try (PreparedStatement pst = con.prepareStatement(sqlEncomendas);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    lblEncomendasPendentes.setText(String.valueOf(rs.getInt("total")));
                }
            }

           
            String sqlPedidosFornecedor = "SELECT COUNT(*) AS total FROM PedidosFornecedor WHERE StatusPedido = 'EmTransito'";
            try (PreparedStatement pst = con.prepareStatement(sqlPedidosFornecedor);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    lblPedidosEmTransito.setText(String.valueOf(rs.getInt("total")));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao carregar dados do dashboard: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                lblFaturamentoMes.setText("Erro ao carregar");
                lblVendasMes.setText("-");
                lblTicketMedioMes.setText("-");
                lvUltimasEntradas.setPlaceholder(new Label("Erro ao carregar dados."));
                lvMaisVendidosMes.setPlaceholder(new Label("Erro ao carregar dados."));
                lblEncomendasPendentes.setText("-");
                lblPedidosEmTransito.setText("-");
            });
        }
    }

    
    @FXML
    private void botaoDashboardIrParaVendas(ActionEvent event) {
        if (mainLayoutController != null) {
            mainLayoutController.irParaRegistrarVenda(event); 
        } else {
            System.err.println("MainLayoutController não injetado no TelaInicialController.");
            mostrarAlerta("Erro de Navegação", "Não foi possível mudar de tela.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void botaoDashboardIrParaEstoque(ActionEvent event) {
        if (mainLayoutController != null) {
            mainLayoutController.irParaGerenciarEstoque(event);
        } else {
            System.err.println("MainLayoutController não injetado no TelaInicialController.");
            mostrarAlerta("Erro de Navegação", "Não foi possível mudar de tela.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void botaoDashboardIrParaProdutos(ActionEvent event) {
        if (mainLayoutController != null) {
            mainLayoutController.irParaGerenciarProdutos(event);
        } else {
            System.err.println("MainLayoutController não injetado no TelaInicialController.");
            mostrarAlerta("Erro de Navegação", "Não foi possível mudar de tela.", Alert.AlertType.ERROR);
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