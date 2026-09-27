import java.util.List;

public class EstrategiaMaximoRendimento implements EstrategiaProducao {

    // O metodo so recebe o orcamento disponivel, nao recebe o custo de cada
    // produto. Por isso é usado quantidadeProdutos no lugar do custo, no
    // projeto, todo produto tem o mesmo custo de producao por unidade, entao
    // mais unidades pedidas = mais caro, na mesma proporcao. Entre as
    // demandas que cabem no orcamento (usando essa comparacao), escolhe a
    // que produz mais unidades
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

    @Override
    public String getNomeEstrategia() {
        return "Máximo Rendimento";
    }
}