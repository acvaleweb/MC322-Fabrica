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
