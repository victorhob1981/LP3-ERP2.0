package erp.model;

import java.time.LocalDate;
import javafx.beans.property.*;

public class PedidoVO {
    private final IntegerProperty pedidoId;
    private final ObjectProperty<LocalDate> dataPedido;
    private final StringProperty nomeFornecedor;
    private final DoubleProperty custoTotal;
    private final StringProperty status;

    public PedidoVO(int pedidoId, LocalDate dataPedido, String nomeFornecedor, double custoTotal, String status) {
        this.pedidoId = new SimpleIntegerProperty(pedidoId);
        this.dataPedido = new SimpleObjectProperty<>(dataPedido);
        this.nomeFornecedor = new SimpleStringProperty(nomeFornecedor);
        this.custoTotal = new SimpleDoubleProperty(custoTotal);
        this.status = new SimpleStringProperty(status);
    }

  
    public int getPedidoId() { return pedidoId.get(); }
    public IntegerProperty pedidoIdProperty() { return pedidoId; }
    public LocalDate getDataPedido() { return dataPedido.get(); }
    public ObjectProperty<LocalDate> dataPedidoProperty() { return dataPedido; }
    public String getNomeFornecedor() { return nomeFornecedor.get(); }
    public StringProperty nomeFornecedorProperty() { return nomeFornecedor; }
    public double getCustoTotal() { return custoTotal.get(); }
    public DoubleProperty custoTotalProperty() { return custoTotal; }
    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
}