/**
 * Produto do tier de entrada (linha básica): qualidade 0,5 e produção 20% mais rápida
 * que a base da categoria (fator de tempo 0,8).
 *
 * @author Equipe Andesite Hardware Co.
 */
public class ComponenteEntrada extends Produto {
    // linha de entrada
    private static final double QUALIDADE = 0.5;
    private static final double FATOR_TEMPO = 0.8;

    /**
     * Cria um produto do tier de entrada.
     *
     * @param id                               identificador do produto ou do lote
     * @param nome                             nome comercial
     * @param categoria                        categoria do produto
     * @param quantidadeMateriaPrimaPorUnidade matéria-prima consumida por unidade
     * @param massa                            massa do produto, em kg
     */
    public ComponenteEntrada(String id, String nome, CategoriaProduto categoria,
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
     * Calcula o tempo de produção: tempo base da categoria vezes 0,8.
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
     * @return "Entrada"
     */
    @Override
    public String getTipo() {
        return "Entrada";
    }

    /**
     * Cria uma nova unidade com as mesmas características deste produto, usado como
     * molde do catálogo.
     *
     * @param novoId identificador da nova unidade
     * @return nova unidade, ainda aguardando processamento
     */
    public ComponenteEntrada criarUnidade(String novoId) {
        return new ComponenteEntrada(novoId, getNome(), getCategoria(),
                getQuantidadeMateriaPrimaPorUnidade(), getMassa());
    }
}
