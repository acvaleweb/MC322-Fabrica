/**
 * Parte da {@link Maquina} (composição 1:1): o sensor que mantém a precisão da máquina.
 *
 * <p>A calibração começa em 100% e cai a cada uso. Abaixo do limite de
 * {@value #LIMIAR_DESCALIBRADO}% o sensor está descalibrado, e a máquina dona dele
 * passa a falhar mais. Só a {@link Maquina} cria e recalibra o sensor.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class SensorDeCalibracao implements Auditavel {
    private static final double CALIBRACAO_MAXIMA = 100.0;
    private static final double LIMIAR_DESCALIBRADO = 50.0;

    private double calibracao;

    /**
     * Cria um sensor perfeitamente calibrado.
     */
    SensorDeCalibracao() {
        this.calibracao = CALIBRACAO_MAXIMA;
    }

    /**
     * Reduz a calibração, sem passar de zero.
     *
     * @param perda pontos de calibração perdidos (valores negativos são ignorados)
     */
    void desgastar(double perda) {
        if (perda > 0) {
            calibracao = Math.max(calibracao - perda, 0.0);
        }
    }

    /**
     * Recalibra o sensor ao máximo.
     */
    void recalibrar() {
        calibracao = CALIBRACAO_MAXIMA;
    }

    /**
     * Informa se o sensor está descalibrado.
     *
     * @return {@code true} se a calibração está abaixo do limite
     */
    public boolean estaDescalibrado() {
        return calibracao < LIMIAR_DESCALIBRADO;
    }

    /**
     * Retorna a calibração atual.
     *
     * @return calibração, de 0 a 100
     */
    public double getCalibracao() {
        return calibracao;
    }

    /**
     * Um sensor descalibrado precisa de manutenção.
     *
     * @return {@code true} se está descalibrado
     */
    @Override
    public boolean precisaManutencao() {
        return estaDescalibrado();
    }

    /**
     * Diagnóstico do sensor em um trecho curto, ex.: {@code Sensor: OK (100%)}.
     *
     * @return o texto do diagnóstico
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        String situacao = estaDescalibrado() ? "Descalibrado" : "OK";
        return "Sensor: " + situacao + " (" + String.format("%.0f", calibracao) + "%)";
    }
}
