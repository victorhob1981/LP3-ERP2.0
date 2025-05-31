package erp;

import javafx.beans.property.*;

public class ProdutoCadastro {

    private final StringProperty modelo;
    private final StringProperty clube;
    private final StringProperty publicoAlvo;
    private final StringProperty tamanho;
    private final IntegerProperty quantidade;
    private final DoubleProperty valor;

    public ProdutoCadastro(String modelo, String clube, String publicoAlvo, String tamanho, int quantidade, double valor) {
        this.modelo = new SimpleStringProperty(modelo);
        this.clube = new SimpleStringProperty(clube);
        this.publicoAlvo = new SimpleStringProperty(publicoAlvo);
        this.tamanho = new SimpleStringProperty(tamanho);
        this.quantidade = new SimpleIntegerProperty(quantidade);
        this.valor = new SimpleDoubleProperty(valor);
    }

    // Métodos getters
    public String getModelo() {
        return modelo.get();
    }

    public String getClube() {
        return clube.get();
    }

    public String getPublicoAlvo() {
        return publicoAlvo.get();
    }

    public String getTamanho() {
        return tamanho.get();
    }

    public int getQuantidade() {
        return quantidade.get();
    }

    public double getValor() {
        return valor.get();
    }

    // Métodos setters
    public void setModelo(String modelo) {
        this.modelo.set(modelo);
    }

    public void setClube(String clube) {
        this.clube.set(clube);
    }

    public void setPublicoAlvo(String publicoAlvo) {
        this.publicoAlvo.set(publicoAlvo);
    }

    public void setTamanho(String tamanho) {
        this.tamanho.set(tamanho);
    }

    public void setQuantidade(int quantidade) {
        this.quantidade.set(quantidade);
    }

    public void setValor(double valor) {
        this.valor.set(valor);
    }

    // Métodos Property (para o TableView)
    public StringProperty modeloProperty() {
        return modelo;
    }

    public StringProperty clubeProperty() {
        return clube;
    }

    public StringProperty publicoAlvoProperty() {
        return publicoAlvo;
    }

    public StringProperty tamanhoProperty() {
        return tamanho;
    }

    public IntegerProperty quantidadeProperty() {
        return quantidade;
    }

    public DoubleProperty valorProperty() {
        return valor;
    }
}
