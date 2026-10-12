/**
 * Produto do tier intermediário (linha performance): qualidade 0,7 e tempo de produção
 * igual ao da base da categoria (fator de tempo 1,0).
 *
 * @author Equipe Andesite Hardware Co.
 */
public class ComponentePerformance extends Produto {
    // linha intermediária
    private static final double QUALIDADE = 0.7;
    private static final double FATOR_TEMPO = 1.0;

    /**
     * Cria um produto do tier performance.
     *
     * @param id                               identificador do produto ou do lote
     * @param nome                             nome comercial
     * @param categoria                        categoria do produto
     * @param quantidadeMateriaPrimaPorUnidade matéria-prima consumida por unidade
     * @param massa                            massa do produto, em kg
     */
    public ComponentePerformance(String id, String nome, CategoriaProduto categoria,
            double quantidadeMateriaPrimaPorUnidade, double massa) {
        super(id, nome, categoria, quantidadeMateriaPrimaPorUnidade, QUALIDADE, massa);
    }

    /**
     * Processa o produto, mudando o status para {@link StatusProduto#PROCESSADO}.
     */
    @Override
    public void processar() {
        setStatus(StatusProduto.PROCESSADO);
    }

    /**
     * Calcula o tempo de produção: tempo base da categoria vezes 1,0.
     *
     * @return o tempo de produção de uma unidade
     */
    @Override
    public double calcularTempoProducao() {
        return getCategoria().getTempoBaseProducao() * FATOR_TEMPO;
    }

    /**
     * Retorna o nome do tier.
     *
     * @return "Performance"
     */
    @Override
    public String getTipo() {
        return "Performance";
    }

    /**
     * Cria uma nova unidade com as mesmas características deste produto, usado como
     * molde do catálogo.
     *
     * @param novoId identificador da nova unidade
     * @return nova unidade, ainda aguardando processamento
     */
    public ComponentePerformance criarUnidade(String novoId) {
        return new ComponentePerformance(novoId, getNome(), getCategoria(),
                getQuantidadeMateriaPrimaPorUnidade(), getMassa());
    }
}
