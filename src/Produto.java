public class Produto {
    private String id;
    private String nome;
    private StatusProduto status;
    private double demandaMateriaPrima;
    private double massa; // unidade: kg

    public Produto(String id, String nome, double demandaMateriaPrima, double massa) {
        this.id = id;
        this.nome = nome;
        this.status = StatusProduto.AGUARDANDO_PROCESSAMENTO;
        this.demandaMateriaPrima = demandaMateriaPrima;
        this.massa = massa;
    }

    public void processar() {
        this.status = StatusProduto.PROCESSADO;
    }

    public boolean definirDemandaMateriaPrima(double quantidade) {
        if (quantidade > 0) {
            this.demandaMateriaPrima = quantidade;
            return true;
        }
        return false;
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public StatusProduto getStatus() {
        return status;
    }

    public double getDemandaMateriaPrima() {
        return demandaMateriaPrima;
    }

    public double getMassa() {
        return massa;
    }

    // Setters

    public void setStatus(StatusProduto status) {
        this.status = status;
    }
}
