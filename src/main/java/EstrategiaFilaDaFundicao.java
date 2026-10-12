import java.util.List;

/**
 * Estratégia FIFO: atende as demandas na ordem em que foram registradas.
 *
 * <p>Escolhe a primeira demanda pendente com quantidade maior que zero, sem
 * considerar tamanho do lote nem orçamento.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class EstrategiaFilaDaFundicao implements EstrategiaProducao {

    /**
     * Cria a estratégia FIFO, que não guarda estado.
     */
    public EstrategiaFilaDaFundicao() {
        // sem estado para inicializar
    }

    /**
     * Retorna a primeira demanda pendente da lista, com quantidade positiva.
     *
     * @param demandas            demandas registradas, na ordem de registro
     * @param orcamentoDisponivel ignorado por esta estratégia
     * @return a primeira demanda elegível, ou {@code null} se não houver nenhuma
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        for (Demanda demanda : demandas) {
            if (demanda.getStatus() == StatusDemanda.PENDENTE && demanda.getQuantidadeProdutos() > 0) {
                return demanda;
            }
        }

        return null;
    }

    /**
     * Retorna o nome da estratégia.
     *
     * @return "Fila da Fundição (FIFO)"
     */
    @Override
    public String getNomeEstrategia() {
        return "Fila da Fundição (FIFO)";
    }
}
