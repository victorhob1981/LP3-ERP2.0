package erp.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProdutoAgregadoVO {

    private final String descricaoModelo;
    private final Map<String, DetalheTamanho> tamanhosDisponiveis = new HashMap<>();

    public ProdutoAgregadoVO(String descricaoModelo) {
        this.descricaoModelo = descricaoModelo;
    }

    // --- CORREÇÃO AQUI: Adicionamos o parâmetro 'custoMedio' ---
    public void adicionarTamanho(String tamanho, int produtoId, double preco, int estoque, double custoMedio) {
        // E passamos ele para o construtor do DetalheTamanho
        tamanhosDisponiveis.put(tamanho, new DetalheTamanho(produtoId, preco, estoque, custoMedio));
    }

    public String getDescricaoModelo() {
        return descricaoModelo;
    }

    public Set<String> getTamanhos() {
        return tamanhosDisponiveis.keySet();
    }

    public DetalheTamanho getDetalhePorTamanho(String tamanho) {
        return tamanhosDisponiveis.get(tamanho);
    }

    // Classe interna para guardar os detalhes de um tamanho específico
    public static class DetalheTamanho {
        private final int produtoId;
        private final double precoVenda;
        private final int estoque;
        private final double custoMedio; // <-- NOVO CAMPO ADICIONADO

        // --- CORREÇÃO AQUI: Construtor atualizado para receber o custoMedio ---
        public DetalheTamanho(int produtoId, double precoVenda, int estoque, double custoMedio) {
            this.produtoId = produtoId;
            this.precoVenda = precoVenda;
            this.estoque = estoque;
            this.custoMedio = custoMedio; // <-- ATRIBUIÇÃO DO NOVO CAMPO
        }

        public int getProdutoId() { return produtoId; }
        public double getPrecoVenda() { return precoVenda; }
        public int getEstoque() { return estoque; }
        
        // --- CORREÇÃO AQUI: Novo método getter para o custoMedio ---
        public double getCustoMedio() { return custoMedio; }
    }
}