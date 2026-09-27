public class Demanda {
    private String tipoProduto;
    private int quantidadeProdutos;
    private StatusDemanda status;

    public Demanda(String tipoProduto) {
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = 0;
        this.status = StatusDemanda.PENDENTE;
    }

    public boolean atualizarQuantidade(int novaQuantidade) {
        if (novaQuantidade < 0 || status == StatusDemanda.EM_PRODUCAO) {
            return false;
        }

        this.quantidadeProdutos = novaQuantidade;
        this.status = StatusDemanda.PENDENTE;
        return true;
    }

    public boolean iniciarProducao() {
        if (status != StatusDemanda.PENDENTE) {
            return false;
        }

        status = StatusDemanda.EM_PRODUCAO;
        return true;
    }

    public boolean concluir() {
        if (status != StatusDemanda.EM_PRODUCAO) {
            return false; // cobre o caso de tentar concluir uma demanda CANCELADA
        }

        status = StatusDemanda.CONCLUIDA;
        return true;
    }

    public boolean cancelar() {
        if (status == StatusDemanda.CONCLUIDA) {
            return false;
        }
        
        status = StatusDemanda.CANCELADA;
        return true;
    }

    public double calcularMateriaPrimaNecessaria(Produto produto) {
        return this.quantidadeProdutos * produto.getQuantidadeMateriaPrimaPorUnidade();
    }

    public double calcularCustoOperacaoEstimado(double custoOperacaoPorUnidade) {
        return this.quantidadeProdutos * custoOperacaoPorUnidade;
    }

    public boolean isViavelFinanceiramente(double orcamentoDisponivel, double custoOperacaoPorUnidade) {
        return quantidadeProdutos > 0
                && status == StatusDemanda.PENDENTE
                && orcamentoDisponivel >= calcularCustoOperacaoEstimado(custoOperacaoPorUnidade);
    }

    // Getters

    public String getTipoProduto() {
        return tipoProduto;
    }

    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    public StatusDemanda getStatus() {
        return status;
    }
}