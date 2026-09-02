public class Produto {
    private String id;
    private String nome;
    private StatusProduto status;
    private double demandaMateriaPrima;
    private double massa;	// em kg	

    public Produto(String id, String nome, double demandaMateriaPrima) {
        this.id = id;
        this.nome = nome;
        this.status = StatusProduto.AGUARDANDO_PROCESSAMENTO;
        this.demandaMateriaPrima = demandaMateriaPrima;
	this.massa = massa;
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
    
    public double getMassa() {
	    return massa;
    }

    public StatusProduto getStatus() {
        return status;
    }

    public void setStatus(StatusProduto status) {
        this.status = status;
    }

}


