package erp.model;

import java.time.LocalDate;
import javafx.beans.property.*;

public class EncomendaVO {

    private final IntegerProperty encomendaId;
    private final StringProperty nomeCliente;
    private final StringProperty clube;
    private final StringProperty modelo;
    private final StringProperty tipo;
    private final StringProperty tamanho;
    private final ObjectProperty<LocalDate> dataEncomenda;
    private final StringProperty status;

    public EncomendaVO(int encomendaId, String nomeCliente, String clube, String modelo, 
                       String tipo, String tamanho, LocalDate dataEncomenda, String status) {
        this.encomendaId = new SimpleIntegerProperty(encomendaId);
        this.nomeCliente = new SimpleStringProperty(nomeCliente);
        this.clube = new SimpleStringProperty(clube);
        this.modelo = new SimpleStringProperty(modelo);
        this.tipo = new SimpleStringProperty(tipo);
        this.tamanho = new SimpleStringProperty(tamanho);
        this.dataEncomenda = new SimpleObjectProperty<>(dataEncomenda);
        this.status = new SimpleStringProperty(status);
    }

   
    public int getEncomendaId() { return encomendaId.get(); }
    public String getNomeCliente() { return nomeCliente.get(); }
    public String getClube() { return clube.get(); }
    public String getModelo() { return modelo.get(); }
    public String getTipo() { return tipo.get(); }
    public String getTamanho() { return tamanho.get(); }
    public LocalDate getDataEncomenda() { return dataEncomenda.get(); }
    public String getStatus() { return status.get(); }

 
    public IntegerProperty encomendaIdProperty() { return encomendaId; }
    public StringProperty nomeClienteProperty() { return nomeCliente; }
    public StringProperty clubeProperty() { return clube; }
    public StringProperty modeloProperty() { return modelo; }
    public StringProperty tipoProperty() { return tipo; }
    public StringProperty tamanhoProperty() { return tamanho; }
    public ObjectProperty<LocalDate> dataEncomendaProperty() { return dataEncomenda; }
    public StringProperty statusProperty() { return status; }
}