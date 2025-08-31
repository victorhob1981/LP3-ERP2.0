package erp.controller;

import erp.model.VendaHistoricoVO;
import UTIL.ConexaoBanco;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class HistoricoVendasController implements Initializable {

    @FXML private TextField txtFiltro;
    @FXML private DatePicker dpDataInicio;
    @FXML private DatePicker dpDataFim;
    @FXML private Button btnFiltrarPorData;
    @FXML private Button btnMarcarComoPago;
    @FXML private TableView<VendaHistoricoVO> tblHistorico;

    @FXML private TableColumn<VendaHistoricoVO, Integer> colVendaId;
    @FXML private TableColumn<VendaHistoricoVO, LocalDate> colDataVenda;
    @FXML private TableColumn<VendaHistoricoVO, String> colCliente;
    @FXML private TableColumn<VendaHistoricoVO, String> colProduto;
    @FXML private TableColumn<VendaHistoricoVO, String> colTamanho;
    @FXML private TableColumn<VendaHistoricoVO, Integer> colQuantidade;
    @FXML private TableColumn<VendaHistoricoVO, Double> colValorTotal;
    @FXML private TableColumn<VendaHistoricoVO, String> colStatusPgto;
    @FXML private TableColumn<VendaHistoricoVO, String> colMetodoPgto;
    
    @FXML private Label lblResumoFaturamento;
    @FXML private Label lblResumoLucro;
    @FXML private Label lblResumoItens;

    private final ObservableList<VendaHistoricoVO> listaVendasMaster = FXCollections.observableArrayList();
    private FilteredList<VendaHistoricoVO> listaFiltrada;
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabela();
        
        dpDataInicio.setValue(LocalDate.now().withDayOfMonth(1));
        dpDataFim.setValue(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));

        carregarDadosHistorico(dpDataInicio.getValue(), dpDataFim.getValue());
        
        configurarFiltrosEAcoes();
        configurarCliqueTabela();
    }

    private void configurarTabela() {
        colVendaId.setCellValueFactory(new PropertyValueFactory<>("vendaId"));
        colDataVenda.setCellValueFactory(new PropertyValueFactory<>("dataVenda"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        colProduto.setCellValueFactory(new PropertyValueFactory<>("descricaoProduto"));
        colTamanho.setCellValueFactory(new PropertyValueFactory<>("tamanho"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colMetodoPgto.setCellValueFactory(new PropertyValueFactory<>("metodoPagamento"));
        colValorTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotalItem"));
        colStatusPgto.setCellValueFactory(new PropertyValueFactory<>("statusPagamento"));

        colValorTotal.setCellFactory(_ -> formatarCelulaMoeda());
        colStatusPgto.setCellFactory(tc -> formatarCelulaStatus());
        colDataVenda.setCellFactory(tc -> formatarCelulaData());
    }
    
    private void configurarCliqueTabela() {
        tblHistorico.setRowFactory(tv -> {
            TableRow<VendaHistoricoVO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    VendaHistoricoVO rowData = row.getItem();

                    if ("Pendente".equalsIgnoreCase(rowData.getStatusPagamento())) {
                        String valorPendenteFormatado = currencyFormat.format(rowData.getValorPendente());
                        
                        String dataPrometidaFormatada;
                        if (rowData.getDataPrometidaPagamento() != null) {
                            dataPrometidaFormatada = rowData.getDataPrometidaPagamento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        } else {
                            dataPrometidaFormatada = "Não definida";
                        }

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Detalhes do Pagamento Pendente");
                        alert.setHeaderText("Informações da Venda ID: " + rowData.getVendaId());
                        alert.setContentText("Valor Pendente: " + valorPendenteFormatado + "\n" +
                                             "Data Prometida para Pagamento: " + dataPrometidaFormatada);

                        alert.showAndWait();
                    }
                }
            });
            return row;
        });
    }

    private void carregarDadosHistorico(LocalDate dataInicio, LocalDate dataFim) {
        listaVendasMaster.clear();
        
        String sql = "SELECT V.VendaID, V.DataVenda, V.ValorFinalVenda, V.ValorPago, IFNULL(C.NomeCliente, 'N/A') AS NomeCliente, " +
                     "CONCAT(P.clube, ' ', P.modelo) AS DescricaoProduto, " +
                     "P.tamanho, " +
                     "IV.Quantidade, IV.PrecoVendaUnitarioRegistrado, " +
                     "IF(IV.CustoMedioUnitarioRegistrado > 0, IV.CustoMedioUnitarioRegistrado, P.CustoMedioPonderado) as CustoReal, " +
                     "V.StatusPagamento, V.MetodoPagamento, V.DataPrometidaPagamento " +
                     "FROM ItensVenda IV " +
                     "JOIN Vendas V ON IV.VendaID = V.VendaID " +
                     "JOIN Produtos P ON IV.ProdutoID = P.ProdutoID " +
                     "LEFT JOIN Clientes C ON V.ClienteID = C.ClienteID " +
                     "WHERE V.DataVenda BETWEEN ? AND ? " +
                     "ORDER BY V.VendaID DESC";

        try (Connection con = UTIL.ConexaoBanco.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setDate(1, java.sql.Date.valueOf(dataInicio));
            pst.setDate(2, java.sql.Date.valueOf(dataFim));
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                java.sql.Date dataPrometidaSql = rs.getDate("DataPrometidaPagamento");
                LocalDate dataPrometida = (dataPrometidaSql != null) ? dataPrometidaSql.toLocalDate() : null;
                
                VendaHistoricoVO venda = new VendaHistoricoVO(
                    rs.getInt("VendaID"), rs.getDate("DataVenda").toLocalDate(),
                    rs.getString("NomeCliente"), 
                    rs.getString("DescricaoProduto"),
                    rs.getString("tamanho"),
                    rs.getInt("Quantidade"), 
                    rs.getDouble("PrecoVendaUnitarioRegistrado"),
                    rs.getDouble("CustoReal"),
                    rs.getString("StatusPagamento"),
                    rs.getString("MetodoPagamento"), 
                    dataPrometida,
                    rs.getDouble("ValorPago"), 
                    rs.getDouble("ValorFinalVenda")
                );
                listaVendasMaster.add(venda);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Erro de Banco de Dados", "Não foi possível carregar o histórico de vendas.", Alert.AlertType.ERROR);
        }
    }

    private void configurarFiltrosEAcoes() {
        listaFiltrada = new FilteredList<>(listaVendasMaster, p -> true);
        txtFiltro.textProperty().addListener((_obs, _oldVal, newValue) -> {
            listaFiltrada.setPredicate(venda -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (venda.getNomeCliente().toLowerCase().contains(lowerCaseFilter)) return true;
                if (venda.getDescricaoProduto().toLowerCase().contains(lowerCaseFilter)) return true;
                return String.valueOf(venda.getVendaId()).contains(lowerCaseFilter);
            });
            atualizarPaineisDeResumo();
        });
        SortedList<VendaHistoricoVO> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tblHistorico.comparatorProperty());
        tblHistorico.setItems(sortedData);
        btnFiltrarPorData.setOnAction(event -> {
            if (dpDataInicio.getValue() != null && dpDataFim.getValue() != null) {
                carregarDadosHistorico(dpDataInicio.getValue(), dpDataFim.getValue());
                atualizarPaineisDeResumo();
            } else {
                mostrarAlerta("Datas Inválidas", "Por favor, selecione uma data de início e de fim.", Alert.AlertType.WARNING);
            }
        });
        tblHistorico.getSelectionModel().selectedItemProperty().addListener((_obs, _oldSelection, newSelection) -> {
            btnMarcarComoPago.setDisable(newSelection == null || !"Pendente".equals(newSelection.getStatusPagamento()));
        });
        btnMarcarComoPago.setOnAction(event -> {
            VendaHistoricoVO vendaSelecionada = tblHistorico.getSelectionModel().getSelectedItem();
            if (vendaSelecionada != null) {
                realizarPagamentoDialogo(vendaSelecionada);
            }
        });
        atualizarPaineisDeResumo();
    }
    
    private void realizarPagamentoDialogo(VendaHistoricoVO vendaSelecionada) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Registrar Pagamento");
        dialog.setHeaderText("Valor Pendente: " + currencyFormat.format(vendaSelecionada.getValorPendente()));
        ButtonType okButtonType = new ButtonType("Confirmar Pagamento", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField valorPagoField = new TextField();
        valorPagoField.setPromptText("Valor Pago");
        ComboBox<String> metodoPagamentoCombo = new ComboBox<>();
        metodoPagamentoCombo.getItems().addAll("Dinheiro", "Cartão de Crédito", "PIX", "Transferência Bancária");
        metodoPagamentoCombo.getSelectionModel().selectFirst();
        grid.add(new Label("Valor Pago:"), 0, 0);
        grid.add(valorPagoField, 1, 0);
        grid.add(new Label("Método de Pagamento:"), 0, 1);
        grid.add(metodoPagamentoCombo, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().lookupButton(okButtonType).setDisable(true);
        valorPagoField.textProperty().addListener((_obs, _oldVal, newVal) -> {
            try {
                double valorDigitado = Double.parseDouble(newVal.replace(",", "."));
                boolean invalido = valorDigitado <= 0 || valorDigitado > (vendaSelecionada.getValorPendente() + 0.001);
                dialog.getDialogPane().lookupButton(okButtonType).setDisable(invalido);
            } catch (NumberFormatException e) {
                dialog.getDialogPane().lookupButton(okButtonType).setDisable(true);
            }
        });
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) return new Pair<>(valorPagoField.getText(), metodoPagamentoCombo.getValue());
            return null;
        });
        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(dadosPagamento -> {
            try {
                double valorPago = Double.parseDouble(dadosPagamento.getKey().replace(",", "."));
                String metodoPagamento = dadosPagamento.getValue();
                processarPagamento(vendaSelecionada.getVendaId(), valorPago, metodoPagamento);
            } catch (NumberFormatException e) {
                mostrarAlerta("Erro de Formato", "O valor pago é inválido.", Alert.AlertType.ERROR);
            }
        });
    }

    private void processarPagamento(int vendaId, double valorPago, String metodoPagamento) {
        Connection con = null;
        try {
            con = ConexaoBanco.conectar();
            con.setAutoCommit(false);
            String sqlUpdate = "UPDATE Vendas SET ValorPago = ValorPago + ?, MetodoPagamento = ? WHERE VendaID = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlUpdate)) {
                pst.setDouble(1, valorPago);
                pst.setString(2, metodoPagamento);
                pst.setInt(3, vendaId);
                pst.executeUpdate();
            }
            String sqlSelect = "SELECT ValorPago, ValorFinalVenda FROM Vendas WHERE VendaID = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlSelect)) {
                pst.setInt(1, vendaId);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    if (rs.getDouble("ValorPago") >= rs.getDouble("ValorFinalVenda")) {
                        String sqlQuitar = "UPDATE Vendas SET StatusPagamento = 'Pago', DataPrometidaPagamento = NULL WHERE VendaID = ?";
                        try (PreparedStatement pstQuitacao = con.prepareStatement(sqlQuitar)) {
                            pstQuitacao.setInt(1, vendaId);
                            pstQuitacao.executeUpdate();
                        }
                    }
                }
            }
            con.commit();
            mostrarAlerta("Sucesso", "Pagamento registrado com sucesso!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            mostrarAlerta("Erro de Banco de Dados", "Não foi possível registrar o pagamento.", Alert.AlertType.ERROR);
        } finally {
            if (con != null) try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            carregarDadosHistorico(dpDataInicio.getValue(), dpDataFim.getValue());
            atualizarPaineisDeResumo();
        }
    }

    private void atualizarPaineisDeResumo() {
        double faturamento = 0, custoTotal = 0, totalItens = 0;
        Set<Integer> vendasUnicas = new HashSet<>();
        for (VendaHistoricoVO venda : listaFiltrada) {
            faturamento += venda.getValorTotalItem();
            custoTotal += venda.getCustoVendaUnitario() * venda.getQuantidade();
            totalItens += venda.getQuantidade();
            vendasUnicas.add(venda.getVendaId());
        }
        double lucro = faturamento - custoTotal;
        lblResumoFaturamento.setText(currencyFormat.format(faturamento));
        lblResumoLucro.setText(currencyFormat.format(lucro));
        lblResumoItens.setText(String.format("%.0f", totalItens));
    }
    
    private TableCell<VendaHistoricoVO, Double> formatarCelulaMoeda() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(currencyFormat.format(item));
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        };
    }
    
    private TableCell<VendaHistoricoVO, String> formatarCelulaStatus() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if ("Pago".equals(item)) {
                        setText("✅ Pago");
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setText("⌛ Pendente");
                        setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;");
                    }
                    setAlignment(Pos.CENTER);
                }
            }
        };
    }

    private TableCell<VendaHistoricoVO, LocalDate> formatarCelulaData() {
        return new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate data, boolean empty) {
                super.updateItem(data, empty);
                if (empty || data == null) {
                    setText(null);
                } else {
                    setText(formatter.format(data));
                }
            }
        };
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}