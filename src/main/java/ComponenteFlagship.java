/**
 * Produto do tier premium (linha flagship): qualidade 0,9 e produção 30% mais lenta
 * que a base da categoria (fator de tempo 1,3). Por ter qualidade alta, é inspecionado
 * com mais rigor pela {@link Inspetora}.
 *
 * @author Equipe Andesite Hardware Co.
 */
public class ComponenteFlagship extends Produto {
    // linha premium
    private static final double QUALIDADE = 0.9;
    private static final double FATOR_TEMPO = 1.3;

    /**
     * Cria um produto do tier flagship.
     *
     * @param id                               identificador do produto ou do lote
     * @param nome                             nome comercial
     * @param categoria                        categoria do produto
     * @param quantidadeMateriaPrimaPorUnidade matéria-prima consumida por unidade
     * @param massa                            massa do produto, em kg
     */
    public ComponenteFlagship(String id, String nome, CategoriaProduto categoria,
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
     * Calcula o tempo de produção: tempo base da categoria vezes 1,3.
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
     * @return "Flagship"
     */
    @Override
    public String getTipo() {
        return "Flagship";
    }

    /**
     * Cria uma nova unidade com as mesmas características deste produto, usado como
     * molde do catálogo.
     *
     * @param novoId identificador da nova unidade
     * @return nova unidade, ainda aguardando processamento
     */
    public ComponenteFlagship criarUnidade(String novoId) {
        return new ComponenteFlagship(novoId, getNome(), getCategoria(),
                getQuantidadeMateriaPrimaPorUnidade(), getMassa());
    }
}
