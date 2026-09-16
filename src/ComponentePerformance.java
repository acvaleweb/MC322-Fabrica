public class ComponentePerformance extends Produto {
    // linha intermediária
    private static final double QUALIDADE = 0.7;
    private static final double FATOR_TEMPO = 1.0;

    public ComponentePerformance(String id, String nome, CategoriaProduto categoria,
            double quantidadeMateriaPrimaPorUnidade, double massa) {
        super(id, nome, categoria, quantidadeMateriaPrimaPorUnidade, QUALIDADE, massa);
    }

    @Override
    public void processar() {
        setStatus(StatusProduto.PROCESSADO);
    }

    @Override
    public double calcularTempoProducao() {
        return getCategoria().getTempoBaseProducao() * FATOR_TEMPO;
    }

    @Override
    public String getTipo() {
        return "Performance";
    }

    public ComponentePerformance criarUnidade(String novoId) {
        return new ComponentePerformance(novoId, getNome(), getCategoria(),
                getQuantidadeMateriaPrimaPorUnidade(), getMassa());
    }
}