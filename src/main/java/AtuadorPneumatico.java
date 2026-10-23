/**
 * Parte da {@link Maquina} (composição 1:N): um atuador pneumático que executa o
 * movimento físico da máquina. Cada máquina tem vários.
 *
 * <p>A integridade começa em 100 e cai a cada uso. Com integridade 0 o atuador está
 * quebrado e a máquina dona dele perde desempenho (mais chance de falha). Abaixo de
 * {@value #LIMIAR_MANUTENCAO} ele já pede manutenção.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class AtuadorPneumatico implements Auditavel {
    private static final double INTEGRIDADE_MAXIMA = 100.0;
    private static final double LIMIAR_MANUTENCAO = 30.0;

    private final int numero;
    private double integridade;

    /**
     * Cria um atuador novo.
     *
     * @param numero posição do atuador dentro da máquina (usada nos relatórios)
     */
    AtuadorPneumatico(int numero) {
        this.numero = numero;
        this.integridade = INTEGRIDADE_MAXIMA;
    }

    /**
     * Reduz a integridade, sem passar de zero.
     *
     * @param perda pontos de integridade perdidos (valores negativos são ignorados)
     */
    void desgastar(double perda) {
        if (perda > 0) {
            integridade = Math.max(integridade - perda, 0.0);
        }
    }

    /**
     * Restaura a integridade ao máximo.
     */
    void reparar() {
        integridade = INTEGRIDADE_MAXIMA;
    }

    /**
     * Informa se o atuador quebrou.
     *
     * @return {@code true} se a integridade chegou a zero
     */
    public boolean estaQuebrado() {
        return integridade <= 0.0;
    }

    /**
     * Retorna a integridade atual.
     *
     * @return integridade, de 0 a 100
     */
    public double getIntegridade() {
        return integridade;
    }

    /**
     * Um atuador precisa de manutenção quando a integridade fica abaixo do limiar.
     *
     * @return {@code true} se precisa de manutenção (inclui o atuador quebrado)
     */
    @Override
    public boolean precisaManutencao() {
        return integridade < LIMIAR_MANUTENCAO;
    }

    /**
     * Diagnóstico do atuador em um trecho curto, ex.: {@code Atuador 1: OK (100%)}.
     *
     * @return o texto do diagnóstico
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        String situacao = "OK";
        if (estaQuebrado()) {
            situacao = "Quebrado";
        } else if (precisaManutencao()) {
            situacao = "Desgastado";
        }
        return "Atuador " + numero + ": " + situacao + " (" + String.format("%.0f", integridade) + "%)";
    }
}
