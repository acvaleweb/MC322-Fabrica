import java.util.List;

public class EstrategiaMaximoRendimento implements EstrategiaProducao {

    // orcamentoDisponivel chega aqui ja convertido pelo GerenciadorProducao para
    // "quantas unidades cabem no orcamento restante" (budget / custo de operacao
    // por unidade, ver calcularOrcamentoEmUnidades em GerenciadorProducao), entao
    // comparar diretamente com quantidadeProdutos reflete a viabilidade real do
    // lote, e nao apenas o valor bruto do budget em reais. Entre as demandas que
    // cabem no orcamento, escolhe a que produz mais unidades
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