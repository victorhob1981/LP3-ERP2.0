package erp; // Certifique-se de que o pacote está correto

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProdutoEstoque {
    private final StringProperty modelo;
    private final StringProperty clube;
    private final StringProperty tipo; // NOVA PROPRIEDADE

    private final IntegerProperty quantidadeP;
    private final IntegerProperty quantidadeM;
    private final IntegerProperty quantidadeG;
    private final IntegerProperty quantidadeGG;
    private final IntegerProperty quantidade2GG;
    private final IntegerProperty quantidade3GG;
    private final IntegerProperty quantidade4GG;
    private final IntegerProperty quantidadeTotal;

    public ProdutoEstoque(String modelo, String clube, String tipo) { // CONSTRUTOR ATUALIZADO
        this.modelo = new SimpleStringProperty(modelo);
        this.clube = new SimpleStringProperty(clube);
        this.tipo = new SimpleStringProperty(tipo); // INICIALIZA O TIPO
        this.quantidadeP = new SimpleIntegerProperty(0);
        this.quantidadeM = new SimpleIntegerProperty(0);
        this.quantidadeG = new SimpleIntegerProperty(0);
        this.quantidadeGG = new SimpleIntegerProperty(0);
        this.quantidade2GG = new SimpleIntegerProperty(0);
        this.quantidade3GG = new SimpleIntegerProperty(0);
        this.quantidade4GG = new SimpleIntegerProperty(0);
        this.quantidadeTotal = new SimpleIntegerProperty(0);
    }

    // Getters e Property Getters
    public String getModelo() { return modelo.get(); }
    public StringProperty modeloProperty() { return modelo; }

    public String getClube() { return clube.get(); }
    public StringProperty clubeProperty() { return clube; }

    public String getTipo() { return tipo.get(); } // NOVO GETTER
    public StringProperty tipoProperty() { return tipo; } // NOVO PROPERTY GETTER

    // Quantidade P
    public int getQuantidadeP() { return quantidadeP.get(); }
    public IntegerProperty quantidadePProperty() { return quantidadeP; }
    // public void setQuantidadeP(int value) { this.quantidadeP.set(value); } // Removido set individual, usar setQuantidadeParaTamanho

    // Quantidade M
    public int getQuantidadeM() { return quantidadeM.get(); }
    public IntegerProperty quantidadeMProperty() { return quantidadeM; }

    // Quantidade G
    public int getQuantidadeG() { return quantidadeG.get(); }
    public IntegerProperty quantidadeGProperty() { return quantidadeG; }

    // Quantidade GG
    public int getQuantidadeGG() { return quantidadeGG.get(); }
    public IntegerProperty quantidadeGGProperty() { return quantidadeGG; }

    // Quantidade 2GG
    public int getQuantidade2GG() { return quantidade2GG.get(); }
    public IntegerProperty quantidade2GGProperty() { return quantidade2GG; }

    // Quantidade 3GG
    public int getQuantidade3GG() { return quantidade3GG.get(); }
    public IntegerProperty quantidade3GGProperty() { return quantidade3GG; }

    // Quantidade 4GG
    public int getQuantidade4GG() { return quantidade4GG.get(); }
    public IntegerProperty quantidade4GGProperty() { return quantidade4GG; }
    
    // Quantidade Total
    public int getQuantidadeTotal() { return quantidadeTotal.get(); }
    public IntegerProperty quantidadeTotalProperty() { return quantidadeTotal; }

    public void setQuantidadeParaTamanho(String tamanho, int quantidade) {
        if (tamanho == null) return;
        // Normaliza o tamanho para consistência (opcional, mas recomendado)
        String tamanhoNormalizado = tamanho.trim().toUpperCase();
        switch (tamanhoNormalizado) {
            case "P": this.quantidadeP.set(quantidade); break;
            case "M": this.quantidadeM.set(quantidade); break;
            case "G": this.quantidadeG.set(quantidade); break;
            case "GG": this.quantidadeGG.set(quantidade); break;
            case "XXL": // Mapeando XXL para 2GG
            case "2GG": this.quantidade2GG.set(quantidade); break;
            case "XXXL": // Mapeando XXXL para 3GG
            case "3GG": this.quantidade3GG.set(quantidade); break;
            case "XXXXL": // Mapeando XXXXL para 4GG
            case "4GG": this.quantidade4GG.set(quantidade); break;
            default:
                // Considerar registrar um log ou aviso se um tamanho não esperado for encontrado
                // System.out.println("Aviso: Tamanho não mapeado '" + tamanho + "' para o modelo " + getModelo() + ", tipo " + getTipo());
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