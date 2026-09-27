import java.util.Random;

public abstract class Maquina implements Auditavel {
    private String nome;
    private double capacidadeMaxima;
    private boolean ligada;
    protected double probabilidadeFalha;
    private double custoOperacao;
    protected Random random;

    private double saude;
    private double limiarManutencao;
    private double desgasteMinimoPorUso;
    private double desgasteMaximoPorUso;

    private static final double SAUDE_MAXIMA = 100.0;
    private static final double SAUDE_MINIMA = 0.0;

    public Maquina(String nome, double capacidadeMaxima, double probabilidadeFalha, double custoOperacao) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.probabilidadeFalha = probabilidadeFalha;
        this.custoOperacao = custoOperacao;
        this.ligada = false;
        this.random = new Random();

        this.saude = SAUDE_MAXIMA;
        this.limiarManutencao = 30.0;
        this.desgasteMinimoPorUso = 0.0;
        this.desgasteMaximoPorUso = 3.0;
    }

    public abstract void processar(Produto produto);

    public abstract String getTipo();

    public void ligar() {
        this.ligada = true;
    }

    public void desligar() {
        this.ligada = false;
    }

    // Reduz a saude em um valor aleatorio dentro da faixa de desgaste configurada
    // Precisa ser chamado uma vez a cada ciclo de fabricacao em que a maquina for usada
    public void desgastar() {
        double perda = desgasteMinimoPorUso + random.nextDouble() * (desgasteMaximoPorUso - desgasteMinimoPorUso);
        saude -= perda;

        if (saude < SAUDE_MINIMA) {
            saude = SAUDE_MINIMA;
        }
    }

    public void reparar() {
        saude = SAUDE_MAXIMA;
    }

    public boolean estaQuebrada() {
        return saude <= SAUDE_MINIMA;
    }

    public boolean precisaManutencao() {
        return saude < limiarManutencao;
    }

    public String gerarRelatorioDiagnostico() {
        String risco = "OK";
        if (precisaManutencao()) {
            risco = "MANUTENCAO NECESSARIA";
        }
        if (estaQuebrada()) {
            risco = "QUEBRADA";
        }

        return getTipo() + " " + nome
                + " | saude: " + String.format("%.0f", saude)
                + " | falha efetiva: " + String.format("%.0f%%", calcularProbabilidadeFalhaEfetiva() * 100)
                + " | " + risco;
    }

    // Quanto menor a saude, maior a chance de falha: com saude 0 a probabilidade
    // base chega a dobrar. O teto de 0.95 evita falha garantida
    protected double calcularProbabilidadeFalhaEfetiva() {
        double fatorDesgaste = (SAUDE_MAXIMA - saude) / SAUDE_MAXIMA;
        double probabilidadeEfetiva = probabilidadeFalha + probabilidadeFalha * fatorDesgaste;

        if (probabilidadeEfetiva > 0.95) {
            return 0.95;
        }
        return probabilidadeEfetiva;
    }

    protected boolean verificarFalha() {
        return random.nextDouble() < calcularProbabilidadeFalhaEfetiva();
    }

    protected boolean podeProcessar(Produto produto) {
        if (estaQuebrada()) {
            return false;
        }

        return this.ligada && produto != null && produto.getMassa() <= this.capacidadeMaxima;
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

    public double getSaude() {
        return saude;
    }

    public double getLimiarManutencao() {
        return limiarManutencao;
    }

    // Setters

    public void setLimiarManutencao(double limiarManutencao) {
        this.limiarManutencao = limiarManutencao;
    }

    public void setProbabilidadeFalha(double probabilidadeFalha) {
        this.probabilidadeFalha = probabilidadeFalha;
    }

    public void setDesgastePorUso(double minimo, double maximo) {
        this.desgasteMinimoPorUso = minimo;
        this.desgasteMaximoPorUso = maximo;
    }
}