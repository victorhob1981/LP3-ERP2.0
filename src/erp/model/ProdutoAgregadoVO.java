package erp.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap; // Importar TreeMap para ordenação

public class ProdutoAgregadoVO {

    private final String descricaoModelo;
    // Estrutura aninhada: Chave -> Tipo (ex: "Masculina"), Valor -> Mapa de Tamanhos
    private final Map<String, Map<String, DetalheTamanho>> tiposDisponiveis = new HashMap<>();

    public ProdutoAgregadoVO(String descricaoModelo) {
        this.descricaoModelo = descricaoModelo;
    }

    // --- NOVO MÉTODO ---
    // Adiciona uma variação de produto (tipo e tamanho específicos)
    public void adicionarVariante(String tipo, String tamanho, int produtoId, double preco, int estoque, double custoMedio) {
        // Garante que existe um mapa para o tipo, depois adiciona o tamanho a ele
        tiposDisponiveis.computeIfAbsent(tipo, k -> new TreeMap<>()).put(tamanho, new DetalheTamanho(produtoId, preco, estoque, custoMedio));
    }

    public String getDescricaoModelo() {
        return descricaoModelo;
    }

    // --- NOVO MÉTODO ---
    // Retorna todos os tipos disponíveis para este produto (ex: ["Feminina", "Masculina"])
    public Set<String> getTipos() {
        return tiposDisponiveis.keySet();
    }

    // --- MÉTODO MODIFICADO ---
    // Agora busca os detalhes do tamanho DENTRO de um tipo específico
    public DetalheTamanho getDetalhePorTipoETamanho(String tipo, String tamanho) {
        Map<String, DetalheTamanho> tamanhosDoTipo = tiposDisponiveis.get(tipo);
        if (tamanhosDoTipo != null) {
            return tamanhosDoTipo.get(tamanho);
        }
        return null;
    }
    
    // --- NOVO MÉTODO ---
    // Retorna o mapa de tamanhos para um tipo específico
    public Map<String, DetalheTamanho> getTamanhosPorTipo(String tipo) {
        return tiposDisponiveis.getOrDefault(tipo, new HashMap<>());
    }


    // Classe interna para guardar os detalhes de um tamanho específico
    public static class DetalheTamanho {
        private final int produtoId;
        private final double precoVenda;
        private final int estoque;
        private final double custoMedio; 

        public DetalheTamanho(int produtoId, double precoVenda, int estoque, double custoMedio) {
            this.produtoId = produtoId;
            this.precoVenda = precoVenda;
            this.estoque = estoque;
            this.custoMedio = custoMedio;
        }

        public int getProdutoId() { return produtoId; }
        public double getPrecoVenda() { return precoVenda; }
        public int getEstoque() { return estoque; }
        public double getCustoMedio() { return custoMedio; }
    }
}