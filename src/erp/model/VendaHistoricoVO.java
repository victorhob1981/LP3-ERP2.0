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
    // CAMPOS ADICIONADOS PARA A FUNCIONALIDADE DE TROCA
    private final IntegerProperty itemVendaId;
    private int produtoOriginalID; // Não precisa ser uma Property, pois não vai para a tabela

    // Campos existentes
    private final DoubleProperty valorFinalVenda;
    private final DoubleProperty valorPendente;
    private final IntegerProperty vendaId;
    private final ObjectProperty<LocalDate> dataVenda;
    private final StringProperty nomeCliente;
    private final StringProperty descricaoProduto;
    private final StringProperty tamanho;
    private final IntegerProperty quantidade;
    private final DoubleProperty precoVendaUnitario;
    private final DoubleProperty valorTotalItem;
    private final DoubleProperty custoVendaUnitario;
    private final StringProperty statusPagamento;
    private final StringProperty metodoPagamento;
    private final ObjectProperty<LocalDate> dataPrometidaPagamento;
    private final StringProperty tipo;

    // CONSTRUTOR CORRIGIDO
    public VendaHistoricoVO(int itemVendaId, int vendaId, LocalDate dataVenda, String nomeCliente, String descricaoProduto, 
                            String tamanho, String tipo, int quantidade, double precoVendaUnitario, double custoVendaUnitario,
                            String statusPagamento, String metodoPagamento, LocalDate dataPrometida, 
                            double valorPago, double valorTotalVenda) {
        this.itemVendaId = new SimpleIntegerProperty(itemVendaId); // Linha corrigida
        this.valorFinalVenda = new SimpleDoubleProperty(valorTotalVenda);
        this.vendaId = new SimpleIntegerProperty(vendaId);
        this.dataVenda = new SimpleObjectProperty<>(dataVenda);
        this.nomeCliente = new SimpleStringProperty(nomeCliente);
        this.descricaoProduto = new SimpleStringProperty(descricaoProduto);
        this.tamanho = new SimpleStringProperty(tamanho);
        this.tipo = new SimpleStringProperty(tipo);
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

    // GETTERS E SETTERS ADICIONADOS
    public int getItemVendaId() { return itemVendaId.get(); }
    public int getProdutoOriginalID() { return produtoOriginalID; }
    public void setProdutoOriginalID(int produtoOriginalID) { this.produtoOriginalID = produtoOriginalID; }


    // Getters e Properties existentes
    public String getTamanho() { return tamanho.get(); }
    public StringProperty tamanhoProperty() { return tamanho; }
    public String getTipo() { return tipo.get(); }
    public StringProperty tipoProperty() { return tipo; }
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
    public double getValorFinalVenda() { return valorFinalVenda.get(); }
    public DoubleProperty valorFinalVendaProperty() { return valorFinalVenda; }
}