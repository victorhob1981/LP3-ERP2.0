package erp; // Ou seu pacote apropriado

public class ProdutoVO {
    private int produtoID;
    private String descricaoCompleta; // Ex: "Camisa Flamengo I Masculina M"
    private double precoVendaAtual;
    private double custoMedioPonderado;
    private int quantidadeEstoque;

    public ProdutoVO(int produtoID, String descricaoCompleta, double precoVendaAtual, double custoMedioPonderado, int quantidadeEstoque) {
        this.produtoID = produtoID;
        this.descricaoCompleta = descricaoCompleta;
        this.precoVendaAtual = precoVendaAtual;
        this.custoMedioPonderado = custoMedioPonderado;
        this.quantidadeEstoque = quantidadeEstoque;
    }

    // Getters
    public int getProdutoID() { return produtoID; }
    public String getDescricaoCompleta() { return descricaoCompleta; }
    public double getPrecoVendaAtual() { return precoVendaAtual; }
    public double getCustoMedioPonderado() { return custoMedioPonderado; }
    public int getQuantidadeEstoque() { return quantidadeEstoque; }

    // É importante sobrescrever o toString() para que o ComboBox saiba o que exibir
    // por padrão se nenhum converter for eficaz ou durante a digitação.
    // No entanto, vamos usar um StringConverter e CellFactory para melhor controle.
    @Override
    public String toString() {
        return descricaoCompleta; // Ou como você preferir a representação textual
    }

    // Opcional: equals e hashCode se for adicionar a coleções que dependem deles.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProdutoVO produtoVO = (ProdutoVO) o;
        return produtoID == produtoVO.produtoID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(produtoID);
    }
}