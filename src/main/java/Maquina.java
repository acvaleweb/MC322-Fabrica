import java.util.ArrayList;
import java.util.List;
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
 * <p><b>Composição:</b> a máquina é o Todo e cria, no próprio construtor, as suas Partes:
 * um {@link MotorDeAcionamento} (1:1), um {@link SensorDeCalibracao} (1:1) e vários
 * {@link AtuadorPneumatico} (1:N). As Partes nunca saem da máquina: o código externo só
 * usa métodos de delegação como {@link #lerTemperaturaMotor()}. Motor superaquecido,
 * sensor descalibrado e atuadores quebrados aumentam a probabilidade de falha efetiva.</p>
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

    // Partes da composicao: criadas aqui dentro e nunca expostas
    private final MotorDeAcionamento motor;
    private final SensorDeCalibracao sensor;
    private final List<AtuadorPneumatico> atuadores;

    private static final double SAUDE_MAXIMA = 100.0;
    private static final double SAUDE_MINIMA = 0.0;
    private static final int QUANTIDADE_ATUADORES_PADRAO = 2;
    private static final double AQUECIMENTO_BASE = 5.0;
    private static final double FATOR_AQUECIMENTO = 3.0;
    private static final double RESFRIAMENTO_AO_DESLIGAR = 5.0;
    private static final double PENALIDADE_MOTOR_SUPERAQUECIDO = 0.10;
    private static final double PENALIDADE_SENSOR_DESCALIBRADO = 0.10;
    private static final double PENALIDADE_ATUADOR_QUEBRADO = 0.05;
    private static final String RECUO_PARTES = "  > ";

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
        this(nome, capacidadeMaxima, probabilidadeFalha, custoOperacao, QUANTIDADE_ATUADORES_PADRAO);
    }

    /**
     * Cria uma máquina com um número específico de atuadores. É aqui, na fase de
     * construção, que o Todo instancia todas as suas Partes (composição estrita): nenhuma
     * Parte vem de fora.
     *
     * @param nome                  nome de identificação da máquina
     * @param capacidadeMaxima      maior massa de produto, em kg, que a máquina suporta
     * @param probabilidadeFalha    probabilidade base de falha, entre 0 e 1
     * @param custoOperacao         custo, em reais, de operar a máquina para uma unidade
     * @param quantidadeAtuadores   quantos atuadores a máquina terá (pelo menos 1)
     * @throws IllegalArgumentException se a quantidade de atuadores for menor que 1
     */
    public Maquina(String nome, double capacidadeMaxima, double probabilidadeFalha, double custoOperacao,
            int quantidadeAtuadores) {
        if (quantidadeAtuadores < 1) {
            throw new IllegalArgumentException("A máquina precisa de pelo menos 1 atuador");
        }

        this.motor = new MotorDeAcionamento();
        this.sensor = new SensorDeCalibracao();
        this.atuadores = new ArrayList<>();
        for (int i = 1; i <= quantidadeAtuadores; i++) {
            this.atuadores.add(new AtuadorPneumatico(i));
        }

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
        if (ligada) {
            motor.resfriar(RESFRIAMENTO_AO_DESLIGAR);
        }
        this.ligada = false;
    }

    /**
     * Reduz a saúde em um valor aleatório dentro da faixa de desgaste configurada. Deve
     * ser chamado uma vez a cada ciclo de fabricação em que a máquina for usada. A saúde
     * nunca fica abaixo de zero.
     */
    public void desgastar() {
        double perda = sortearPerda();
        saude -= perda;

        // O desgaste se propaga para as Partes
        motor.aquecer(AQUECIMENTO_BASE + perda * FATOR_AQUECIMENTO);
        sensor.desgastar(sortearPerda());
        for (AtuadorPneumatico atuador : atuadores) {
            atuador.desgastar(sortearPerda());
        }

        if (saude < SAUDE_MINIMA) {
            saude = SAUDE_MINIMA;
        }
    }

    private double sortearPerda() {
        return desgasteMinimoPorUso + random.nextDouble() * (desgasteMaximoPorUso - desgasteMinimoPorUso);
    }

    /**
     * Repara a máquina, restaurando a saúde ao máximo (100) e reparando todas as Partes.
     */
    public void reparar() {
        saude = SAUDE_MAXIMA;
        motor.reparar();
        sensor.recalibrar();
        for (AtuadorPneumatico atuador : atuadores) {
            atuador.reparar();
        }
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
     * Informa se a máquina precisa de manutenção: saúde abaixo do limiar (uma máquina
     * quebrada também entra aqui) ou alguma Parte precisando de manutenção.
     *
     * @return {@code true} se a máquina precisa de manutenção
     */
    @Override
    public boolean precisaManutencao() {
        if (saude < limiarManutencao || motor.precisaManutencao() || sensor.precisaManutencao()) {
            return true;
        }
        for (AtuadorPneumatico atuador : atuadores) {
            if (atuador.precisaManutencao()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gera o diagnóstico da máquina: uma linha com tipo, nome, saúde, probabilidade de
     * falha efetiva e situação ("OK", "MANUTENCAO NECESSARIA" ou "QUEBRADA"), seguida de
     * uma linha indentada com o diagnóstico de cada Parte (auditoria em cascata).
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
                + " | " + risco
                + "\n" + RECUO_PARTES + gerarRelatorioPartes();
    }

    // Monta o trecho das Partes, ex.: Motor: OK (25C) | Sensor: OK (100%) | Atuador 1: OK (100%)
    private String gerarRelatorioPartes() {
        StringBuilder partes = new StringBuilder();
        partes.append(motor.gerarRelatorioDiagnostico());
        partes.append(" | ").append(sensor.gerarRelatorioDiagnostico());
        for (AtuadorPneumatico atuador : atuadores) {
            partes.append(" | ").append(atuador.gerarRelatorioDiagnostico());
        }
        return partes.toString();
    }

    // Cada Parte com problema aumenta a chance de falha da maquina (a Parte nao sabe disso)
    private double calcularPenalidadePartes() {
        double penalidade = 0;
        if (motor.estaSuperaquecido()) {
            penalidade += PENALIDADE_MOTOR_SUPERAQUECIDO;
        }
        if (sensor.estaDescalibrado()) {
            penalidade += PENALIDADE_SENSOR_DESCALIBRADO;
        }
        for (AtuadorPneumatico atuador : atuadores) {
            if (atuador.estaQuebrado()) {
                penalidade += PENALIDADE_ATUADOR_QUEBRADO;
            }
        }
        return penalidade;
    }

    // ----- Metodos de delegacao (Lei de Demeter): o cliente fala so com a maquina -----

    /**
     * Lê a temperatura do motor interno.
     *
     * @return temperatura, em °C
     */
    public double lerTemperaturaMotor() {
        return motor.getTemperatura();
    }

    /**
     * Lê a calibração do sensor interno.
     *
     * @return calibração, de 0 a 100
     */
    public double lerCalibracaoSensor() {
        return sensor.getCalibracao();
    }

    /**
     * Retorna quantos atuadores a máquina tem.
     *
     * @return quantidade de atuadores
     */
    public int getQuantidadeAtuadores() {
        return atuadores.size();
    }

    /**
     * Conta quantos atuadores ainda não quebraram.
     *
     * @return quantidade de atuadores operacionais
     */
    public int contarAtuadoresOperacionais() {
        int operacionais = 0;
        for (AtuadorPneumatico atuador : atuadores) {
            if (!atuador.estaQuebrado()) {
                operacionais++;
            }
        }
        return operacionais;
    }

    /**
     * Calcula a probabilidade de falha considerando o desgaste e o estado das Partes.
     * Quanto menor a saúde, maior a chance de falha: com saúde 0 a probabilidade base
     * chega a dobrar. Motor superaquecido (+0,10), sensor descalibrado (+0,10) e cada
     * atuador quebrado (+0,05) somam à probabilidade. O resultado é limitado a 0,95 para
     * que a falha nunca seja garantida.
     *
     * @return a probabilidade de falha efetiva, entre 0 e 0,95
     */
    protected double calcularProbabilidadeFalhaEfetiva() {
        double fatorDesgaste = (SAUDE_MAXIMA - saude) / SAUDE_MAXIMA;
        double probabilidadeEfetiva = probabilidadeFalha + probabilidadeFalha * fatorDesgaste
                + calcularPenalidadePartes();

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
