import java.util.List;

/**
 * Estratégia que tenta extrair o máximo de produção do orçamento disponível: entre as
 * demandas pendentes cujo lote cabe no orçamento, atende a que produz mais unidades.
 *
 * @author Equipe Andesite Hardware Co.
 */
public class EstrategiaMaximoRendimento implements EstrategiaProducao {

    /**
     * Cria a estratégia de máximo rendimento, que não guarda estado.
     */
    public EstrategiaMaximoRendimento() {
        // sem estado para inicializar
    }

    // orcamentoDisponivel chega aqui ja convertido pelo GerenciadorProducao para
    // "quantas unidades cabem no orcamento restante" (budget / custo de operacao
    // por unidade, ver calcularOrcamentoEmUnidades em GerenciadorProducao), entao
    // comparar diretamente com quantidadeProdutos reflete a viabilidade real do
    // lote, e nao apenas o valor bruto do budget em reais. Entre as demandas que
    // cabem no orcamento, escolhe a que produz mais unidades

    /**
     * Retorna a maior demanda pendente que cabe no orçamento.
     *
     * @param demandas            demandas registradas
     * @param orcamentoDisponivel quantas unidades o budget restante ainda comporta
     *                            (budget dividido pelo custo de operação por unidade)
     * @return a demanda escolhida, ou {@code null} se nenhuma estiver pendente ou
     *         nenhuma couber no orçamento
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        Demanda escolhida = null;

        for (Demanda demanda : demandas) {
            if (demanda.getStatus() != StatusDemanda.PENDENTE || demanda.getQuantidadeProdutos() <= 0) {
                continue;
            }

            if (demanda.getQuantidadeProdutos() > orcamentoDisponivel) {
                continue;
            }

            if (escolhida == null || demanda.getQuantidadeProdutos() > escolhida.getQuantidadeProdutos()) {
                escolhida = demanda;
            }
        }
        return escolhida;
    }

    /**
     * Retorna o nome da estratégia.
     *
     * @return "Máximo Rendimento"
     */
    @Override
    public String getNomeEstrategia() {
        return "Máximo Rendimento";
    }
}
