public class Produto {
    private String id;
    private String nome;
    private StatusProduto status;
    private double quantidadeMateriaPrimaNecessaria;

    public Produto(String id, String nome, Double quantidadeMateriaPrimaNecessaria) {
        this.id = id;
        this.nome = nome;
        this.status = StatusProduto.AGUARDANDO_PROCESSAMENTO;
        this.quantidadeMateriaPrimaNecessaria = quantidadeMateriaPrimaNecessaria;
    }

    public void processar() {
        status = StatusProduto.PROCESSADO;
    }

    public void definirDemandaMateriaPrima(double quantidade) {

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