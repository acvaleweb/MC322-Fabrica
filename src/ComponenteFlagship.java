public class ComponenteFlagship extends Produto {
    // linha premium
    private static final double QUALIDADE = 0.9;
    private static final double FATOR_TEMPO = 1.3;

    public ComponenteFlagship(String id, String nome, CategoriaProduto categoria,
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
        return "Flagship";
    }

    public ComponenteFlagship criarUnidade(String novoId) {
        return new ComponenteFlagship(novoId, getNome(), getCategoria(),
                getQuantidadeMateriaPrimaPorUnidade(), getMassa());
    }
}