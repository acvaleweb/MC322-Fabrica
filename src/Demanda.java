public class Demanda {
    private String tipoProduto;
    private int quantidadeProdutos;
    private StatusDemanda status;
    private double custoUnitarioEstimado; 

    public Demanda(String tipoProduto) {
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = 0;
        this.status = StatusDemanda.PENDENTE;
        this.custoUnitarioEstimado = 0;
    }

    public void atualizarQuantidade(int novaQuantidade) {
        this.quantidadeProdutos = novaQuantidade;
<<<<<<< Updated upstream
=======
        if (this.status != StatusDemanda.CONCLUIDA) {
            this.status = StatusDemanda.PENDENTE;
        }
>>>>>>> Stashed changes
    }

    public double calcularMateriaPrimaNecessaria(Produto produto) {
        return this.quantidadeProdutos * produto.getQuantidadeMateriaPrimaPorUnidade();
    }


    public boolean iniciarProducao() {
        if (status != StatusDemanda.PENDENTE) {
            return false;
        }
        status = StatusDemanda.EM_PRODUCAO;
        return true;
    }

    public boolean concluir() {
        if (status == StatusDemanda.CANCELADA) {
            return false;
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


    public void setCustoUnitarioEstimado(double custoUnitarioEstimado) {
        this.custoUnitarioEstimado = Math.max(custoUnitarioEstimado, 0);
    }

    public double getCustoUnitarioEstimado() {
        return custoUnitarioEstimado;
    }

    public double calcularCustoTotalEstimado() {
        return this.quantidadeProdutos * this.custoUnitarioEstimado;
    }

    public boolean isViavelFinanceiramente(double orcamentoDisponivel) {
        return quantidadeProdutos > 0 && calcularCustoTotalEstimado() <= orcamentoDisponivel;
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
