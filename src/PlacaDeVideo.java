public class PlacaDeVideo extends Produto {

    // Atributos privados

    private double demandaMateriaPrima;
    private double massa; // unidade: kg

    // Construtor

    public PlacaDeVideo(String id, String nome, double demandaMateriaPrima, double massa) {
        super(id, nome, status);
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
