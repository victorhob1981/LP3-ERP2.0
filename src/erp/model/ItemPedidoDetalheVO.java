package erp.model;

import javafx.beans.property.*;

public class ItemPedidoDetalheVO {
    private final IntegerProperty itemPedidoId;
    private final IntegerProperty produtoId;
    private final StringProperty descricaoProduto;
    private final IntegerProperty quantidadePedida;
    private final IntegerProperty quantidadeRecebida;
    private final DoubleProperty custoUnitario;

    public ItemPedidoDetalheVO(int itemPedidoId, int produtoId, String descricaoProduto, int quantidadePedida, int quantidadeRecebida, double custoUnitario) {
        this.itemPedidoId = new SimpleIntegerProperty(itemPedidoId);
        this.produtoId = new SimpleIntegerProperty(produtoId);
        this.descricaoProduto = new SimpleStringProperty(descricaoProduto);
        this.quantidadePedida = new SimpleIntegerProperty(quantidadePedida);
        this.quantidadeRecebida = new SimpleIntegerProperty(quantidadeRecebida);
        this.custoUnitario = new SimpleDoubleProperty(custoUnitario);
    }

  
    public int getItemPedidoId() { return itemPedidoId.get(); }
    public IntegerProperty itemPedidoIdProperty() { return itemPedidoId; }
    public int getProdutoId() { return produtoId.get(); }
    public IntegerProperty produtoIdProperty() { return produtoId; }
    public String getDescricaoProduto() { return descricaoProduto.get(); }
    public StringProperty descricaoProdutoProperty() { return descricaoProduto; }
    public int getQuantidadePedida() { return quantidadePedida.get(); }
    public IntegerProperty quantidadePedidaProperty() { return quantidadePedida; }
    public int getQuantidadeRecebida() { return quantidadeRecebida.get(); }
    public IntegerProperty quantidadeRecebidaProperty() { return quantidadeRecebida; }
    public double getCustoUnitario() { return custoUnitario.get(); }
    public DoubleProperty custoUnitarioProperty() { return custoUnitario; }
}