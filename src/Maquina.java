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

    public void processar(MateriaPrima materiaPrima, double demanda) {
        // irei fazer
    }

    public String getNome() {
        return nome;
    }

    public boolean estaLigada() {
        return ligada;
    }

}
