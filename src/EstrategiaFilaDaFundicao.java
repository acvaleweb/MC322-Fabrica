import java.util.List;

public class EstrategiaFilaDaFundicao implements EstrategiaProducao {

    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        for (Demanda demanda : demandas) {
            if (demanda.getStatus() == StatusDemanda.PENDENTE && demanda.getQuantidadeProdutos() > 0) {
                return demanda;
            }
        }
        
        return null;
    }

    @Override
    public String getNomeEstrategia() {
        return "Fila da Fundição (FIFO)";
    }
}