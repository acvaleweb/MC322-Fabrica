import java.util.List;

/**
 * Estratégia que prioriza o maior lote: entre as demandas pendentes, atende a que tem
 * a maior quantidade de unidades. Em caso de empate vence a que foi registrada primeiro.
 *
 * @author Equipe Andesite Hardware Co.
 */
public class EstrategiaMaiorLote implements EstrategiaProducao {

    /**
     * Cria a estratégia de maior lote, que não guarda estado.
     */
    public EstrategiaMaiorLote() {
        // sem estado para inicializar
    }

    /**
     * Retorna a demanda pendente com a maior quantidade de unidades.
     *
     * @param demandas            demandas registradas
     * @param orcamentoDisponivel ignorado por esta estratégia
     * @return a demanda de maior lote, ou {@code null} se não houver demanda elegível
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        Demanda escolhida = null;

        for (Demanda demanda : demandas) {
            if (demanda.getStatus() != StatusDemanda.PENDENTE || demanda.getQuantidadeProdutos() <= 0) {
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
     * @return "Maior Lote"
     */
    @Override
    public String getNomeEstrategia() {
        return "Maior Lote";
    }
}
