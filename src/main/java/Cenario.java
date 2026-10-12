/**
 * Cenários de operação da fábrica, escolhidos no início da execução.
 *
 * <p>O cenário define o budget inicial e o quanto as máquinas são frágeis: o fator de
 * probabilidade de falha multiplica a probabilidade base de cada máquina e a faixa de
 * desgaste define quanta saúde a máquina perde a cada uso. Essas regras são aplicadas
 * por {@link GerenciadorProducao#adicionarMaquina(Maquina)}.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public enum Cenario {
    /** Operação tranquila: budget de 3000, falhas reduzidas à metade e desgaste de 0 a 1 por uso. */
    IDEAL("Ideal", 3000.0, 0.5, 0.0, 1.0),
    /** Fábrica sob pressão: budget de 1500, falhas dobradas e desgaste de 2 a 5 por uso. */
    APOCALIPTICO("Apocaliptico", 1500.0, 2.0, 2.0, 5.0);

    private final String nomeExibicao;
    private final double budgetInicial;
    private final double fatorProbabilidadeFalha; // multiplica a probabilidadeFalha base das maquinas
    private final double desgasteMinimoPorUso;
    private final double desgasteMaximoPorUso;

    Cenario(String nomeExibicao, double budgetInicial, double fatorProbabilidadeFalha,
            double desgasteMinimoPorUso, double desgasteMaximoPorUso) {
        this.nomeExibicao = nomeExibicao;
        this.budgetInicial = budgetInicial;
        this.fatorProbabilidadeFalha = fatorProbabilidadeFalha;
        this.desgasteMinimoPorUso = desgasteMinimoPorUso;
        this.desgasteMaximoPorUso = desgasteMaximoPorUso;
    }

    /**
     * Retorna o nome do cenário como deve aparecer para o usuário.
     *
     * @return nome de exibição do cenário
     */
    public String getNomeExibicao() {
        return nomeExibicao;
    }

    /**
     * Retorna o budget com que a fábrica começa neste cenário.
     *
     * @return budget inicial, em reais
     */
    public double getBudgetInicial() {
        return budgetInicial;
    }

    /**
     * Retorna o fator que multiplica a probabilidade de falha base das máquinas.
     *
     * @return fator de probabilidade de falha (menor que 1 reduz, maior que 1 aumenta)
     */
    public double getFatorProbabilidadeFalha() {
        return fatorProbabilidadeFalha;
    }

    /**
     * Retorna o desgaste mínimo que uma máquina sofre a cada uso.
     *
     * @return perda mínima de saúde por uso
     */
    public double getDesgasteMinimoPorUso() {
        return desgasteMinimoPorUso;
    }

    /**
     * Retorna o desgaste máximo que uma máquina sofre a cada uso.
     *
     * @return perda máxima de saúde por uso
     */
    public double getDesgasteMaximoPorUso() {
        return desgasteMaximoPorUso;
    }
}
