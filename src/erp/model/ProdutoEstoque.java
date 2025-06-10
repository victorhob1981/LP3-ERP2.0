package erp.model; 

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProdutoEstoque {
    private final StringProperty modelo;
    private final StringProperty clube;
    private final StringProperty tipo; 

    private final IntegerProperty quantidadeP;
    private final IntegerProperty quantidadeM;
    private final IntegerProperty quantidadeG;
    private final IntegerProperty quantidadeGG;
    private final IntegerProperty quantidade2GG;
    private final IntegerProperty quantidade3GG;
    private final IntegerProperty quantidade4GG;
    private final IntegerProperty quantidadeTotal;

    public ProdutoEstoque(String modelo, String clube, String tipo) { 
        this.modelo = new SimpleStringProperty(modelo);
        this.clube = new SimpleStringProperty(clube);
        this.tipo = new SimpleStringProperty(tipo); 
        this.quantidadeP = new SimpleIntegerProperty(0);
        this.quantidadeM = new SimpleIntegerProperty(0);
        this.quantidadeG = new SimpleIntegerProperty(0);
        this.quantidadeGG = new SimpleIntegerProperty(0);
        this.quantidade2GG = new SimpleIntegerProperty(0);
        this.quantidade3GG = new SimpleIntegerProperty(0);
        this.quantidade4GG = new SimpleIntegerProperty(0);
        this.quantidadeTotal = new SimpleIntegerProperty(0);
    }

  
    public String getModelo() { return modelo.get(); }
    public StringProperty modeloProperty() { return modelo; }

    public String getClube() { return clube.get(); }
    public StringProperty clubeProperty() { return clube; }

    public String getTipo() { return tipo.get(); } 
    public StringProperty tipoProperty() { return tipo; }

  
    public int getQuantidadeP() { return quantidadeP.get(); }
    public IntegerProperty quantidadePProperty() { return quantidadeP; }
   

   
    public int getQuantidadeM() { return quantidadeM.get(); }
    public IntegerProperty quantidadeMProperty() { return quantidadeM; }

   
    public int getQuantidadeG() { return quantidadeG.get(); }
    public IntegerProperty quantidadeGProperty() { return quantidadeG; }

  
    public int getQuantidadeGG() { return quantidadeGG.get(); }
    public IntegerProperty quantidadeGGProperty() { return quantidadeGG; }

   
    public int getQuantidade2GG() { return quantidade2GG.get(); }
    public IntegerProperty quantidade2GGProperty() { return quantidade2GG; }

   
    public int getQuantidade3GG() { return quantidade3GG.get(); }
    public IntegerProperty quantidade3GGProperty() { return quantidade3GG; }

   
    public int getQuantidade4GG() { return quantidade4GG.get(); }
    public IntegerProperty quantidade4GGProperty() { return quantidade4GG; }
    
  
    public int getQuantidadeTotal() { return quantidadeTotal.get(); }
    public IntegerProperty quantidadeTotalProperty() { return quantidadeTotal; }

    public void setQuantidadeParaTamanho(String tamanho, int quantidade) {
        if (tamanho == null) return;
       
        String tamanhoNormalizado = tamanho.trim().toUpperCase();
        switch (tamanhoNormalizado) {
            case "P": this.quantidadeP.set(quantidade); break;
            case "M": this.quantidadeM.set(quantidade); break;
            case "G": this.quantidadeG.set(quantidade); break;
            case "GG": this.quantidadeGG.set(quantidade); break;
            case "2GG": this.quantidade2GG.set(quantidade); break;
            case "3GG": this.quantidade3GG.set(quantidade); break;
            case "4GG": this.quantidade4GG.set(quantidade); break;
            default:
                break;
        }
        recalcularTotal();
    }

    private void recalcularTotal() {
        int total = quantidadeP.get() + quantidadeM.get() +
                    quantidadeG.get() + quantidadeGG.get() +
                    quantidade2GG.get() + quantidade3GG.get() +
                    quantidade4GG.get();
        this.quantidadeTotal.set(total);
    }
}