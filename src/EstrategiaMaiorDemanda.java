import java.util.List;

// prioriza sempre o maior lote de wafers pendente, ou seja, a demanda com a maior quantidade total de unidades a fabricar
public class EstrategiaMaiorDemanda implements EstrategiaProducao {

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

    @Override
    public String getNomeEstrategia() {
        return "Maior Lote (maior demanda pendente)";
    }
}
