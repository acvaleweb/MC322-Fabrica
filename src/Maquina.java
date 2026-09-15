import java.util.Random;

public abstract class Maquina {
    private String nome;
    private double capacidadeMaxima;
    private boolean ligada;
    protected double probabilidadeFalha;
    private double custoOperacao;
    protected Random random;

    public Maquina(String nome, double capacidadeMaxima, double probabilidadeFalha, double custoOperacao) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.probabilidadeFalha = probabilidadeFalha;
        this.custoOperacao = custoOperacao;
        this.ligada = false;
        this.random = new Random();
    }

    // Metodos Abstratos
    public abstract void processar(Produto produto);

    public abstract String getTipo();

    // Metodos Concretos
    public void ligar() {
        this.ligada = true;
    }

    public void desligar() {
        this.ligada = false;
    }

    protected boolean verificarFalha() {
        return random.nextDouble() < this.probabilidadeFalha;
    }

    // Getters

    public String getNome() {
        return nome;
    }

    public double getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public boolean estaLigada() {
        return ligada;
    }

    public double getCustoOperacao() {
        return custoOperacao;
    }

    public double getProbabilidadeFalha() {
        return probabilidadeFalha;
    }
}