public class Maquina {
    private String nome;
    private double capacidadeMaxima;
    private boolean ligada;

    public Maquina(String nome, double capacidadeMaxima) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        ligada = false;
    }

    public void ligar() {
        ligada = true;
    }

    public void desligar() {
        ligada = false;
    }

    public boolean processar(MateriaPrima materiaPrima, Produto produto) {
        if (!this.ligada || materiaPrima == null || produto == null) {
            return false;
        }

        double demanda = produto.getDemandaMateriaPrima();

        if (demanda > this.capacidadeMaxima) {
            return false;
        }

        boolean conseguiuConsumir = materiaPrima.consumir(demanda);

        if (!conseguiuConsumir) {
            return false;
        }

        produto.processar();
        return true;
    }

    public String getNome() {
        return nome;
    }

    public boolean estaLigada() {
        return ligada;
    }

}