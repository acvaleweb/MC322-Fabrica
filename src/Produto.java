public class Produto {
    private String id;
    private String nome;
    private StatusProduto status;
    private double quantidadeMateriaPrimaNecessaria;

    public Produto(String id, String nome, double quantidadeMateriaPrimaNecessaria) {
        this.id = id;
        this.nome = nome;
        this.status = StatusProduto.AGUARDANDO_PROCESSAMENTO;
        this.quantidadeMateriaPrimaNecessaria = quantidadeMateriaPrimaNecessaria;
    }

    public void processar() {
        status = StatusProduto.PROCESSADO;
    }

    public boolean definirDemandaMateriaPrima(double quantidade) {
        if (quantidade > 0) {
            this.quantidadeMateriaPrimaNecessaria = quantidade;
            return true;
        }

        return false;
    }

    public double getDemandaMateriaPrima() {
        return quantidadeMateriaPrimaNecessaria;
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

}