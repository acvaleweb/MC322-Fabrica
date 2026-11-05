package br.unicamp.ic.mc322.model;

/**
 * Parte da {@link Maquina} (composição 1:1): o motor que aciona a máquina.
 *
 * <p>O motor esquenta a cada uso e esfria um pouco quando a máquina é desligada. Acima
 * de {@value #TEMPERATURA_SUPERAQUECIMENTO} °C ele está superaquecido, o que aumenta a
 * chance de falha da máquina dona dele. A classe não conhece budget nem estratégias:
 * só cuida da própria temperatura.</p>
 *
 * <p>O construtor é visível apenas no pacote, e só a {@link Maquina} instancia o
 * motor, o que garante a composição estrita.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class MotorDeAcionamento implements Auditavel {
    private static final double TEMPERATURA_AMBIENTE = 25.0;
    private static final double TEMPERATURA_SUPERAQUECIMENTO = 80.0;
    private static final double TEMPERATURA_MAXIMA = 120.0;

    private double temperatura;

    /**
     * Cria um motor na temperatura ambiente.
     */
    MotorDeAcionamento() {
        this.temperatura = TEMPERATURA_AMBIENTE;
    }

    /**
     * Esquenta o motor, limitado à temperatura máxima.
     *
     * @param graus quanto a temperatura sobe (valores negativos são ignorados)
     */
    void aquecer(double graus) {
        if (graus > 0) {
            temperatura = Math.min(temperatura + graus, TEMPERATURA_MAXIMA);
        }
    }

    /**
     * Esfria o motor, sem passar da temperatura ambiente.
     *
     * @param graus quanto a temperatura desce (valores negativos são ignorados)
     */
    void resfriar(double graus) {
        if (graus > 0) {
            temperatura = Math.max(temperatura - graus, TEMPERATURA_AMBIENTE);
        }
    }

    /**
     * Devolve o motor à temperatura ambiente (reparo/troca de óleo).
     */
    void reparar() {
        temperatura = TEMPERATURA_AMBIENTE;
    }

    /**
     * Confere se uma temperatura é possível para o motor: um número entre a temperatura
     * ambiente e a temperatura máxima. Não altera nenhum motor.
     *
     * @param temperatura temperatura a conferir, em °C
     * @throws IllegalArgumentException se a temperatura estiver fora da faixa
     */
    static void validarTemperatura(double temperatura) {
        if (!(temperatura >= TEMPERATURA_AMBIENTE && temperatura <= TEMPERATURA_MAXIMA)) {
            throw new IllegalArgumentException("A temperatura do motor precisa estar entre "
                    + TEMPERATURA_AMBIENTE + " e " + TEMPERATURA_MAXIMA + " °C (informado: " + temperatura + ")");
        }
    }

    /**
     * Restaura uma temperatura salva anteriormente, depois de validá-la.
     *
     * @param temperatura temperatura a restaurar, em °C
     * @throws IllegalArgumentException se a temperatura estiver fora da faixa (o motor
     *                                  não é alterado)
     */
    void restaurarTemperatura(double temperatura) {
        validarTemperatura(temperatura);
        this.temperatura = temperatura;
    }

    /**
     * Informa se o motor está superaquecido.
     *
     * @return {@code true} se a temperatura atingiu o limite de superaquecimento
     */
    public boolean estaSuperaquecido() {
        return temperatura >= TEMPERATURA_SUPERAQUECIMENTO;
    }

    /**
     * Retorna a temperatura atual.
     *
     * @return temperatura, em °C
     */
    public double getTemperatura() {
        return temperatura;
    }

    /**
     * Um motor superaquecido precisa de manutenção.
     *
     * @return {@code true} se está superaquecido
     */
    @Override
    public boolean precisaManutencao() {
        return estaSuperaquecido();
    }

    /**
     * Diagnóstico do motor em um trecho curto, ex.: {@code Motor: OK (25°C)}.
     *
     * @return o texto do diagnóstico
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        String situacao = estaSuperaquecido() ? "Superaquecido" : "OK";
        return "Motor: " + situacao + " (" + String.format("%.0f", temperatura) + "°C)";
    }
}
