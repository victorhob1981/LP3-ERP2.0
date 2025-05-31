package erp;

import javafx.beans.property.*;

public class ProdutoEstoque {

    private final StringProperty modelo;
    private final StringProperty clube;
    private final IntegerProperty quantidadeP;
    private final IntegerProperty quantidadeM;
    private final IntegerProperty quantidadeG;
    private final IntegerProperty quantidadeGG;
    private final IntegerProperty quantidade2GG;
    private final IntegerProperty quantidade3GG;
    private final IntegerProperty quantidade4GG;

    public ProdutoEstoque(String modelo, String clube) {
        this.modelo = new SimpleStringProperty(modelo);
        this.clube = new SimpleStringProperty(clube);
        this.quantidadeP = new SimpleIntegerProperty(0);
        this.quantidadeM = new SimpleIntegerProperty(0);
        this.quantidadeG = new SimpleIntegerProperty(0);
        this.quantidadeGG = new SimpleIntegerProperty(0);
        this.quantidade2GG = new SimpleIntegerProperty(0);
        this.quantidade3GG = new SimpleIntegerProperty(0);
        this.quantidade4GG = new SimpleIntegerProperty(0);
    }

    // Adiciona quantidade dependendo do tamanho
    public void addQuantidadePorTamanho(String tamanho, int quantidade) {
        switch (tamanho) {
            case "P": this.quantidadeP.set(this.quantidadeP.get() + quantidade); break;
            case "M": this.quantidadeM.set(this.quantidadeM.get() + quantidade); break;
            case "G": this.quantidadeG.set(this.quantidadeG.get() + quantidade); break;
            case "GG": this.quantidadeGG.set(this.quantidadeGG.get() + quantidade); break;
            case "2GG": this.quantidade2GG.set(this.quantidade2GG.get() + quantidade); break;
            case "3GG": this.quantidade3GG.set(this.quantidade3GG.get() + quantidade); break;
            case "4GG": this.quantidade4GG.set(this.quantidade4GG.get() + quantidade); break;
        }
    }

    // Método para calcular a quantidade total
    public int getQuantidadeTotal() {
        return quantidadeP.get() + quantidadeM.get() + quantidadeG.get() + quantidadeGG.get()
               + quantidade2GG.get() + quantidade3GG.get() + quantidade4GG.get();
    }

    // Método que retorna a quantidade total como uma Property (para ligação com TableView)
    public IntegerProperty quantidadeTotalProperty() {
        return new SimpleIntegerProperty(getQuantidadeTotal());
    }

    // Getters para as propriedades
    public String getModelo() { return modelo.get(); }
    public String getClube() { return clube.get(); }
    public int getQuantidadeP() { return quantidadeP.get(); }
    public int getQuantidadeM() { return quantidadeM.get(); }
    public int getQuantidadeG() { return quantidadeG.get(); }
    public int getQuantidadeGG() { return quantidadeGG.get(); }
    public int getQuantidade2GG() { return quantidade2GG.get(); }
    public int getQuantidade3GG() { return quantidade3GG.get(); }
    public int getQuantidade4GG() { return quantidade4GG.get(); }

    // Propriedades de Binding
    public StringProperty modeloProperty() { return modelo; }
    public StringProperty clubeProperty() { return clube; }
    public IntegerProperty quantidadePProperty() { return quantidadeP; }
    public IntegerProperty quantidadeMProperty() { return quantidadeM; }
    public IntegerProperty quantidadeGProperty() { return quantidadeG; }
    public IntegerProperty quantidadeGGProperty() { return quantidadeGG; }
    public IntegerProperty quantidade2GGProperty() { return quantidade2GG; }
    public IntegerProperty quantidade3GGProperty() { return quantidade3GG; }
    public IntegerProperty quantidade4GGProperty() { return quantidade4GG; }
}
