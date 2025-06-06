package erp; 

public class ProdutoVO {
    private int produtoID;
    private String descricaoCompleta; 
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

 
    public int getProdutoID() { return produtoID; }
    public String getDescricaoCompleta() { return descricaoCompleta; }
    public double getPrecoVendaAtual() { return precoVendaAtual; }
    public double getCustoMedioPonderado() { return custoMedioPonderado; }
    public int getQuantidadeEstoque() { return quantidadeEstoque; }

   
    @Override
    public String toString() {
        return descricaoCompleta; 
    }

  
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