public class PlacaDeVideo extends Produto {

    // Atributos privados

    private double demandaMateriaPrima;
    private double massa; // unidade: kg
    private String tipo;

    // Construtor

    public PlacaDeVideo(String id, String nome, double demandaMateriaPrima, double massa, String tipo) {
        super(id, nome);
        this.demandaMateriaPrima = demandaMateriaPrima;
        this.massa = massa;
        this.tipo = tipo;
    }
    @Override
    public void processar() {
        setStatus(StatusProduto.PROCESSADO);
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

    // Outros

    @Override
    double calcularTempoProducao() {
        return 0;   // placeholder

    };

    @Override
	public String getTipo() {
        return tipo;
        
    }
}
