import java.util.Random;

public abstract class Maquina implements Auditavel {
    private String nome;
    private double capacidadeMaxima;
    private boolean ligada;
    protected double probabilidadeFalha;
    private double custoOperacao;
    protected Random random;

    // Desgaste progressivo: saude vai de 100 (nova) a 0 (quebrada)
    private double saude;
    private double fatorDesgaste; // definido pelo cenario ativo, acelera/reduz o desgaste

    private static final double LIMIAR_MANUTENCAO = 30.0;
    private static final double DESGASTE_MINIMO_POR_USO = 0.0;
    private static final double DESGASTE_MAXIMO_POR_USO = 3.0;

    public Maquina(String nome, double capacidadeMaxima, double probabilidadeFalha, double custoOperacao) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.probabilidadeFalha = Math.min(Math.max(probabilidadeFalha, 0), 1.0);
        this.custoOperacao = custoOperacao;
        this.ligada = false;
        this.random = new Random();
        this.saude = 100.0;
        this.fatorDesgaste = 1.0;
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
        return random.nextDouble() < getProbabilidadeFalhaEfetiva();
    }

<<<<<<< Updated upstream
    // Getters
=======
    protected boolean podeProcessar(Produto produto) {
        if (!this.ligada || produto == null || produto.getMassa() > this.capacidadeMaxima || estaQuebrada()) {
            return false;
        }

        registrarUso();
        return true;
    }

    // Cada vez que a maquina efetivamente processa uma unidade ela perde um pouco de saude
    // o fatorDesgaste modela velocidade do desgate
    private void registrarUso() {
        double desgaste = DESGASTE_MINIMO_POR_USO
                + random.nextDouble() * (DESGASTE_MAXIMO_POR_USO - DESGASTE_MINIMO_POR_USO);
        desgaste *= fatorDesgaste;
        saude = Math.max(saude - desgaste, 0);
    }

    public double getProbabilidadeFalhaEfetiva() {
        double fatorSaude = (100.0 - saude) / 100.0;
        double efetiva = probabilidadeFalha + (probabilidadeFalha * fatorSaude);
        return Math.min(efetiva, 0.95); // nunca 100% garantido
    }

    public boolean estaQuebrada() {
        return saude <= 0;
    }
>>>>>>> Stashed changes

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

    public double getSaude() {
        return saude;
    }

    // Setters

    public void setFatorDesgaste(double fatorDesgaste) {
        if (fatorDesgaste > 0) {
            this.fatorDesgaste = fatorDesgaste;
        }
    }

    // Auditavel

    @Override
    public String gerarRelatorioDiagnostico() {
        String estado = estaQuebrada() ? "QUEBRADA" : (precisaManutencao() ? "ATENCAO" : "OK");
        return String.format("%s (%s) | saude: %.1f%% | prob. falha efetiva: %.1f%% | estado: %s",
                nome, getTipo(), saude, getProbabilidadeFalhaEfetiva() * 100, estado);
    }

    @Override
    public boolean precisaManutencao() {
        return saude < LIMIAR_MANUTENCAO;
    }
}