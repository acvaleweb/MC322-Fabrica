import java.util.List;

// escolhe demanda que maximiza quantidade de unidades fabricadas

public class EstrategiaMaximoProdutos implements EstrategiaProducao {

    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        Demanda escolhida = null;

        for (Demanda demanda : demandas) {
            if (demanda.getStatus() != StatusDemanda.PENDENTE || demanda.getQuantidadeProdutos() <= 0) {
                continue;
            }
            if (!demanda.isViavelFinanceiramente(orcamentoDisponivel)) {
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
        return "Maximo de Produtos";
    }
}
