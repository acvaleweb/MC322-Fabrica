public class ComponenteEntrada extends Produto {
    // linha de entrada
    private static final double QUALIDADE = 0.5;
    private static final double FATOR_TEMPO = 0.8;

    public ComponenteEntrada(String id, String nome, CategoriaProduto categoria,
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
        return "Entrada";
    }

    public ComponenteEntrada criarUnidade(String novoId) {
        return new ComponenteEntrada(novoId, getNome(), getCategoria(),
                getQuantidadeMateriaPrimaPorUnidade(), getMassa());
    }
}