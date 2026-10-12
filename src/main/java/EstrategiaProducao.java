import java.util.List;

/**
 * Estratégia de produção (padrão Strategy): decide qual demanda pendente a fábrica
 * deve atender em seguida.
 *
 * <p>O {@link GerenciadorProducao} é o contexto do padrão. Ele delega a escolha a uma
 * implementação desta interface, que pode ser trocada em tempo de execução.</p>
 *
 * @author Equipe Andesite Hardware Co.
 * @see EstrategiaFilaDaFundicao
 * @see EstrategiaMaiorLote
 * @see EstrategiaMaximoRendimento
 */
public interface EstrategiaProducao {
    /**
     * Seleciona a próxima demanda a ser produzida. Só devem ser consideradas demandas
     * {@link StatusDemanda#PENDENTE} com quantidade maior que zero.
     *
     * @param demandas            todas as demandas registradas na planta
     * @param orcamentoDisponivel quantas unidades ainda cabem no orçamento (o budget
     *                            já convertido pelo gerenciador, e não o valor em reais)
     * @return a demanda escolhida, ou {@code null} se nenhuma for elegível
     */
    Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);

    /**
     * Retorna o nome da estratégia, exibido no menu.
     *
     * @return nome legível da estratégia
     */
    String getNomeEstrategia();
}
