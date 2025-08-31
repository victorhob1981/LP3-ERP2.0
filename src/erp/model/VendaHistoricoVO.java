package erp.model;

import java.time.LocalDate;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class VendaHistoricoVO {
    private final DoubleProperty valorPendente;
    private final IntegerProperty vendaId;
    private final ObjectProperty<LocalDate> dataVenda;
    private final StringProperty nomeCliente;
    private final StringProperty descricaoProduto;
    private final StringProperty tamanho; // <-- MUDANÇA 1: Novo campo
    private final IntegerProperty quantidade;
    private final DoubleProperty precoVendaUnitario;
    private final DoubleProperty valorTotalItem;
    private final DoubleProperty custoVendaUnitario;
    private final StringProperty statusPagamento;
    private final StringProperty metodoPagamento;
    private final ObjectProperty<LocalDate> dataPrometidaPagamento;

    public VendaHistoricoVO(int vendaId, LocalDate dataVenda, String nomeCliente, String descricaoProduto, 
                            String tamanho, int quantidade, double precoVendaUnitario, double custoVendaUnitario, // <-- MUDANÇA 2: Novo parâmetro
                            String statusPagamento, String metodoPagamento, LocalDate dataPrometida, 
                            double valorPago, double valorTotalVenda) {
        
        this.vendaId = new SimpleIntegerProperty(vendaId);
        this.dataVenda = new SimpleObjectProperty<>(dataVenda);
        this.nomeCliente = new SimpleStringProperty(nomeCliente);
        this.descricaoProduto = new SimpleStringProperty(descricaoProduto);
        this.tamanho = new SimpleStringProperty(tamanho); // <-- MUDANÇA 2: Atribuição do novo campo
        this.quantidade = new SimpleIntegerProperty(quantidade);
        this.precoVendaUnitario = new SimpleDoubleProperty(precoVendaUnitario);
        this.valorTotalItem = new SimpleDoubleProperty(quantidade * precoVendaUnitario);
        this.custoVendaUnitario = new SimpleDoubleProperty(custoVendaUnitario);
        this.statusPagamento = new SimpleStringProperty(statusPagamento);
        this.metodoPagamento = new SimpleStringProperty(metodoPagamento);
        this.dataPrometidaPagamento = new SimpleObjectProperty<>(dataPrometida);

        if ("Pago".equals(statusPagamento)) {
            this.valorPendente = new SimpleDoubleProperty(0);
        } else {
            this.valorPendente = new SimpleDoubleProperty(valorTotalVenda - valorPago);
        }
    }

    // --- MUDANÇA 3: Novos métodos para o campo 'tamanho' ---
    public String getTamanho() { return tamanho.get(); }
    public StringProperty tamanhoProperty() { return tamanho; }
    // --- Fim da Mudança 3 ---

    public double getValorPendente() { return valorPendente.get(); }
    public int getVendaId() { return vendaId.get(); }
    public LocalDate getDataVenda() { return dataVenda.get(); }
    public String getNomeCliente() { return nomeCliente.get(); }
    public String getDescricaoProduto() { return descricaoProduto.get(); }
    public int getQuantidade() { return quantidade.get(); }
    public double getPrecoVendaUnitario() { return precoVendaUnitario.get(); }
    public double getValorTotalItem() { return valorTotalItem.get(); }
    public double getCustoVendaUnitario() { return custoVendaUnitario.get(); }
    public String getStatusPagamento() { return statusPagamento.get(); }
    public String getMetodoPagamento() { return metodoPagamento.get(); }
    public LocalDate getDataPrometidaPagamento() { return dataPrometidaPagamento.get(); }
    public DoubleProperty valorPendenteProperty() { return valorPendente; }
    public IntegerProperty vendaIdProperty() { return vendaId; }
    public ObjectProperty<LocalDate> dataVendaProperty() { return dataVenda; }
    public StringProperty nomeClienteProperty() { return nomeCliente; }
    public StringProperty descricaoProdutoProperty() { return descricaoProduto; }
    public IntegerProperty quantidadeProperty() { return quantidade; }
    public DoubleProperty precoVendaUnitarioProperty() { return precoVendaUnitario; }
    public DoubleProperty valorTotalItemProperty() { return valorTotalItem; }
    public DoubleProperty custoVendaUnitarioProperty() { return custoVendaUnitario; }
    public StringProperty statusPagamentoProperty() { return statusPagamento; }
    public StringProperty metodoPagamentoProperty() { return metodoPagamento; }
    public ObjectProperty<LocalDate> dataPrometidaPagamentoProperty() { return dataPrometidaPagamento; }
}