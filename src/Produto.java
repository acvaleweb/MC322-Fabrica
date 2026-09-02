public class Produto {
    private String id;
    private String nome;
    private StatusProduto status;
    private double demandaMateriaPrima;

    public Produto(String id, String nome, double quantidadeMateriaPrimaNecessaria) {
        this.id = id;
        this.nome = nome;
        this.status = StatusProduto.AGUARDANDO_PROCESSAMENTO;
        this.demandaMateriaPrima = quantidadeMateriaPrimaNecessaria;
    }

    public void processar() {
        status = StatusProduto.PROCESSADO;
    }

    public boolean definirDemandaMateriaPrima(double quantidade) {
        if (quantidade > 0) {
            this.demandaMateriaPrima = quantidade;
            return true;
        }

        return false;
    }

    public double getDemandaMateriaPrima() {
        return demandaMateriaPrima;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public StatusProduto getStatus() {
        return status;
    }

    public void setStatus(StatusProduto status) {
        this.status = status;
    }

}