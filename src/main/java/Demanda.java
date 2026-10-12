/**
 * Pedido de fabricação de um produto do catálogo.
 *
 * <p>Uma demanda nasce {@link StatusDemanda#PENDENTE} com quantidade zero e segue o
 * ciclo descrito em {@link StatusDemanda}. Operações incompatíveis com o estado atual
 * lançam {@link DemandaInvalidaException} sem alterar a demanda.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class Demanda {
    private String tipoProduto;
    private int quantidadeProdutos;
    private StatusDemanda status;

    /**
     * Cria uma demanda pendente, com quantidade zero.
     *
     * @param tipoProduto nome do produto demandado, igual ao do catálogo
     */
    public Demanda(String tipoProduto) {
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = 0;
        this.status = StatusDemanda.PENDENTE;
    }

    /**
     * Atualiza a quantidade demandada. Não permite alterar uma demanda em produção. Uma
     * demanda concluída ou cancelada pode ser reaberta com uma nova quantidade: ela volta
     * a ser {@link StatusDemanda#PENDENTE}.
     *
     * @param novaQuantidade nova quantidade de unidades (zero ou mais)
     * @throws DemandaInvalidaException se a quantidade for negativa ou se a demanda
     *                                  estiver em produção
     */
    public void atualizarQuantidade(int novaQuantidade) {
        if (novaQuantidade < 0) {
            throw new DemandaInvalidaException("A quantidade demandada de " + tipoProduto
                    + " não pode ser negativa (informado: " + novaQuantidade + ")");
        }

        if (status == StatusDemanda.EM_PRODUCAO) {
            throw new DemandaInvalidaException("A demanda de " + tipoProduto
                    + " está em produção e não pode ser alterada agora");
        }

        this.quantidadeProdutos = novaQuantidade;
        this.status = StatusDemanda.PENDENTE;
    }

    /**
     * Marca a demanda como em produção.
     *
     * @throws DemandaInvalidaException se a demanda não estiver pendente
     */
    public void iniciarProducao() {
        if (status != StatusDemanda.PENDENTE) {
            throw new DemandaInvalidaException("Não é possível iniciar a produção de " + tipoProduto
                    + ": a demanda está " + status.getDescricao().toLowerCase());
        }

        status = StatusDemanda.EM_PRODUCAO;
    }

    /**
     * Marca a demanda como concluída.
     *
     * @throws DemandaInvalidaException se a demanda não estiver em produção (inclui o
     *                                  caso de uma demanda cancelada)
     */
    public void concluir() {
        if (status != StatusDemanda.EM_PRODUCAO) {
            // cobre o caso de tentar concluir uma demanda CANCELADA
            throw new DemandaInvalidaException("Não é possível concluir a demanda de " + tipoProduto
                    + ": ela está " + status.getDescricao().toLowerCase());
        }

        status = StatusDemanda.CONCLUIDA;
    }

    /**
     * Cancela a demanda.
     *
     * @throws DemandaInvalidaException se a demanda já estiver concluída
     */
    public void cancelar() {
        if (status == StatusDemanda.CONCLUIDA) {
            throw new DemandaInvalidaException("Não é possível cancelar a demanda de " + tipoProduto
                    + ": ela já foi concluída");
        }

        status = StatusDemanda.CANCELADA;
    }

    /**
     * Calcula quanta matéria-prima o lote inteiro exige.
     *
     * @param produto produto do catálogo que será fabricado
     * @return quantidade demandada vezes a matéria-prima por unidade do produto
     */
    public double calcularMateriaPrimaNecessaria(Produto produto) {
        return this.quantidadeProdutos * produto.getQuantidadeMateriaPrimaPorUnidade();
    }

    /**
     * Estima o custo de operação do lote inteiro.
     *
     * @param custoOperacaoPorUnidade custo de operação da linha para uma unidade
     * @return quantidade demandada vezes o custo por unidade, em reais
     */
    public double calcularCustoOperacaoEstimado(double custoOperacaoPorUnidade) {
        return this.quantidadeProdutos * custoOperacaoPorUnidade;
    }

    /**
     * Informa se a demanda pode ser produzida com o orçamento atual: precisa estar
     * pendente, ter quantidade positiva e ter custo estimado dentro do orçamento.
     *
     * @param orcamentoDisponivel     orçamento disponível, em reais
     * @param custoOperacaoPorUnidade custo de operação da linha para uma unidade
     * @return {@code true} se a demanda é viável financeiramente
     */
    public boolean isViavelFinanceiramente(double orcamentoDisponivel, double custoOperacaoPorUnidade) {
        return quantidadeProdutos > 0
                && status == StatusDemanda.PENDENTE
                && orcamentoDisponivel >= calcularCustoOperacaoEstimado(custoOperacaoPorUnidade);
    }

    // Getters

    /**
     * Retorna o nome do produto demandado.
     *
     * @return nome do produto
     */
    public String getTipoProduto() {
        return tipoProduto;
    }

    /**
     * Retorna a quantidade demandada.
     *
     * @return quantidade de unidades
     */
    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    /**
     * Retorna o estado atual da demanda.
     *
     * @return status da demanda
     */
    public StatusDemanda getStatus() {
        return status;
    }
}
