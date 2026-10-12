import java.util.Random;

/**
 * Máquina genérica da linha de produção.
 *
 * <p>Concentra o que é comum a todas as máquinas: estado ligada/desligada, capacidade
 * de massa, custo de operação e, principalmente, as regras de <b>desgaste</b> e de
 * <b>falha</b>:</p>
 * <ul>
 *   <li><b>Saúde:</b> começa em 100 e diminui a cada uso, por um valor sorteado dentro
 *       da faixa de desgaste do cenário ativo (ver {@link #desgastar()}). Com saúde 0 a
 *       máquina está quebrada e não opera até ser reparada.</li>
 *   <li><b>Manutenção:</b> a máquina precisa de manutenção quando a saúde fica abaixo do
 *       limiar de manutenção (30 por padrão).</li>
 *   <li><b>Falha efetiva:</b> quanto menor a saúde, maior a chance de falha. Com saúde 0
 *       a probabilidade base chega a dobrar, limitada a 95% (ver
 *       {@link #calcularProbabilidadeFalhaEfetiva()}).</li>
 * </ul>
 *
 * <p>As subclasses definem o que cada etapa faz com o produto em
 * {@link #processar(Produto)}.</p>
 *
 * @author Equipe Andesite Hardware Co.
 * @see InsersoraSMT
 * @see Montadora
 * @see Inspetora
 */
public abstract class Maquina implements Auditavel {
    private String nome;
    private double capacidadeMaxima;
    private boolean ligada;
    /** Probabilidade base de falha (0 a 1), já ajustada pelo fator do cenário. */
    protected double probabilidadeFalha;
    private double custoOperacao;
    /** Gerador de números aleatórios usado nos sorteios de falha e de desgaste. */
    protected Random random;

    private double saude;
    private double limiarManutencao;
    private double desgasteMinimoPorUso;
    private double desgasteMaximoPorUso;

    private static final double SAUDE_MAXIMA = 100.0;
    private static final double SAUDE_MINIMA = 0.0;

    /**
     * Cria uma máquina desligada, com saúde máxima, limiar de manutenção 30 e desgaste
     * por uso entre 0 e 3 (valores que o cenário sobrescreve ao entrar na linha).
     *
     * @param nome               nome de identificação da máquina
     * @param capacidadeMaxima   maior massa de produto, em kg, que a máquina suporta
     * @param probabilidadeFalha probabilidade base de falha, entre 0 e 1
     * @param custoOperacao      custo, em reais, de operar a máquina para uma unidade
     */
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

    /**
     * Executa a etapa da máquina sobre o produto. Cada subclasse implementa a sua etapa
     * e deve chamar {@link #validarOperacao(Produto)} antes de alterar o produto.
     *
     * @param produto produto a ser processado
     * @throws MaquinaIndisponivelException se a máquina estiver quebrada, desligada, não
     *                                      suportar a massa do produto ou receber um
     *                                      produto em estado incompatível com a etapa
     * @throws IllegalArgumentException     se o produto for nulo
     */
    public abstract void processar(Produto produto);

    /**
     * Retorna o tipo da máquina, usado nos relatórios.
     *
     * @return nome do tipo (ex.: "Insersora SMT")
     */
    public abstract String getTipo();

    /**
     * Liga a máquina.
     */
    public void ligar() {
        this.ligada = true;
    }

    /**
     * Desliga a máquina.
     */
    public void desligar() {
        this.ligada = false;
    }

    /**
     * Reduz a saúde em um valor aleatório dentro da faixa de desgaste configurada. Deve
     * ser chamado uma vez a cada ciclo de fabricação em que a máquina for usada. A saúde
     * nunca fica abaixo de zero.
     */
    public void desgastar() {
        double perda = desgasteMinimoPorUso + random.nextDouble() * (desgasteMaximoPorUso - desgasteMinimoPorUso);
        saude -= perda;

        if (saude < SAUDE_MINIMA) {
            saude = SAUDE_MINIMA;
        }
    }

    /**
     * Repara a máquina, restaurando a saúde ao máximo (100).
     */
    public void reparar() {
        saude = SAUDE_MAXIMA;
    }

    /**
     * Informa se a máquina quebrou, isto é, se a saúde chegou a zero.
     *
     * @return {@code true} se a máquina está quebrada
     */
    public boolean estaQuebrada() {
        return saude <= SAUDE_MINIMA;
    }

    /**
     * Informa se a saúde está abaixo do limiar de manutenção. Uma máquina quebrada
     * também precisa de manutenção.
     *
     * @return {@code true} se a máquina precisa de manutenção
     */
    @Override
    public boolean precisaManutencao() {
        return saude < limiarManutencao;
    }

    /**
     * Gera o diagnóstico da máquina em uma linha: tipo, nome, saúde, probabilidade de
     * falha efetiva e situação ("OK", "MANUTENCAO NECESSARIA" ou "QUEBRADA").
     *
     * @return o texto do diagnóstico
     */
    @Override
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

    /**
     * Calcula a probabilidade de falha considerando o desgaste. Quanto menor a saúde,
     * maior a chance de falha: com saúde 0 a probabilidade base chega a dobrar. O
     * resultado é limitado a 0,95 para que a falha nunca seja garantida.
     *
     * @return a probabilidade de falha efetiva, entre 0 e 0,95
     */
    protected double calcularProbabilidadeFalhaEfetiva() {
        double fatorDesgaste = (SAUDE_MAXIMA - saude) / SAUDE_MAXIMA;
        double probabilidadeEfetiva = probabilidadeFalha + probabilidadeFalha * fatorDesgaste;

        if (probabilidadeEfetiva > 0.95) {
            return 0.95;
        }
        return probabilidadeEfetiva;
    }

    /**
     * Sorteia se ocorre uma falha neste uso, com base na probabilidade efetiva.
     *
     * @return {@code true} se a máquina falhou
     */
    protected boolean verificarFalha() {
        return random.nextDouble() < calcularProbabilidadeFalhaEfetiva();
    }

    /**
     * Garante que a máquina pode receber o produto. Deve ser chamado no início de
     * {@link #processar(Produto)}. Não altera nenhum estado: apenas lança a exceção
     * adequada quando a operação é impossível.
     *
     * @param produto produto que a máquina vai processar
     * @throws IllegalArgumentException     se o produto for nulo
     * @throws MaquinaIndisponivelException se a máquina estiver quebrada, estiver
     *                                      desligada ou o produto exceder a capacidade
     */
    protected void validarOperacao(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("A " + nome + " não pode processar um produto nulo");
        }

        if (estaQuebrada()) {
            throw new MaquinaIndisponivelException(
                    "A " + nome + " está quebrada e precisa de reparo antes de operar");
        }

        if (!ligada) {
            throw new MaquinaIndisponivelException("A " + nome + " está desligada");
        }

        if (produto.getMassa() > capacidadeMaxima) {
            throw new MaquinaIndisponivelException("O produto " + produto.getNome() + " (massa "
                    + produto.getMassa() + " kg) excede a capacidade da " + nome + " ("
                    + capacidadeMaxima + " kg)");
        }
    }

    // Getters

    /**
     * Retorna o nome da máquina.
     *
     * @return nome de identificação
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna a maior massa de produto que a máquina suporta.
     *
     * @return capacidade máxima, em kg
     */
    public double getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    /**
     * Informa se a máquina está ligada.
     *
     * @return {@code true} se está ligada
     */
    public boolean estaLigada() {
        return ligada;
    }

    /**
     * Retorna o custo de operação da máquina por unidade produzida.
     *
     * @return custo de operação, em reais
     */
    public double getCustoOperacao() {
        return custoOperacao;
    }

    /**
     * Retorna a probabilidade base de falha, sem considerar o desgaste.
     *
     * @return probabilidade base, entre 0 e 1
     */
    public double getProbabilidadeFalha() {
        return probabilidadeFalha;
    }

    /**
     * Retorna a saúde atual da máquina.
     *
     * @return saúde, de 0 (quebrada) a 100 (perfeita)
     */
    public double getSaude() {
        return saude;
    }

    /**
     * Retorna o limiar de saúde abaixo do qual a máquina precisa de manutenção.
     *
     * @return limiar de manutenção
     */
    public double getLimiarManutencao() {
        return limiarManutencao;
    }

    // Setters

    /**
     * Define o limiar de saúde abaixo do qual a máquina precisa de manutenção.
     *
     * @param limiarManutencao novo limiar de manutenção
     */
    public void setLimiarManutencao(double limiarManutencao) {
        this.limiarManutencao = limiarManutencao;
    }

    /**
     * Define a probabilidade base de falha. Usado pelo gerenciador para aplicar o fator
     * de fragilidade do cenário.
     *
     * @param probabilidadeFalha nova probabilidade base de falha
     */
    public void setProbabilidadeFalha(double probabilidadeFalha) {
        this.probabilidadeFalha = probabilidadeFalha;
    }

    /**
     * Define a faixa de desgaste sofrido a cada uso.
     *
     * @param minimo menor perda de saúde por uso
     * @param maximo maior perda de saúde por uso
     */
    public void setDesgastePorUso(double minimo, double maximo) {
        this.desgasteMinimoPorUso = minimo;
        this.desgasteMaximoPorUso = maximo;
    }
}
