package erp; // Ou o pacote que você está utilizando

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProdutoCadastro {
    private final StringProperty modelo;
    private final StringProperty clube;
    private final StringProperty tipo; 
    private final StringProperty tamanho;
    private final IntegerProperty quantidade;
    private final DoubleProperty precoVenda; 
    private final DoubleProperty custoCompra; 

    public ProdutoCadastro(String modelo, String clube, String tipo, String tamanho, 
                           int quantidade, double precoVenda, double custoCompra) {
        this.modelo = new SimpleStringProperty(modelo);
        this.clube = new SimpleStringProperty(clube);
        this.tipo = new SimpleStringProperty(tipo);
        this.tamanho = new SimpleStringProperty(tamanho);
        this.quantidade = new SimpleIntegerProperty(quantidade);
        this.precoVenda = new SimpleDoubleProperty(precoVenda);
        this.custoCompra = new SimpleDoubleProperty(custoCompra);
    }

   
    public String getModelo() { return modelo.get(); }
    public String getClube() { return clube.get(); }
    public String getTipo() { return tipo.get(); }
    public String getTamanho() { return tamanho.get(); }
    public int getQuantidade() { return quantidade.get(); }
    public double getPrecoVenda() { return precoVenda.get(); }
    public double getCustoCompra() { return custoCompra.get(); }

   
    public StringProperty modeloProperty() { return modelo; }
    public StringProperty clubeProperty() { return clube; }
    public StringProperty tipoProperty() { return tipo; }
    public StringProperty tamanhoProperty() { return tamanho; }
    public IntegerProperty quantidadeProperty() { return quantidade; }
    public DoubleProperty precoVendaProperty() { return precoVenda; }
    public DoubleProperty custoCompraProperty() { return custoCompra; }
}