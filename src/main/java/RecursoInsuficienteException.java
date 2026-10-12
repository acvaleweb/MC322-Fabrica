/**
 * Lançada quando falta um recurso necessário para executar uma operação, como
 * matéria-prima em estoque ou budget disponível.
 *
 * <p>Guarda o nome do recurso, a quantidade necessária e a quantidade disponível, para
 * que a mensagem mostrada ao usuário diga exatamente o que faltou.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class RecursoInsuficienteException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /** Nome do recurso que faltou. */
    private final String recurso;
    /** Quantidade que a operação exigia. */
    private final double necessario;
    /** Quantidade disponível no momento da operação. */
    private final double disponivel;

    /**
     * Cria a exceção descrevendo o recurso que faltou.
     *
     * @param recurso    nome do recurso (ex.: "Wafer de Silício" ou "Budget")
     * @param necessario quantidade exigida pela operação
     * @param disponivel quantidade realmente disponível no momento
     */
    public RecursoInsuficienteException(String recurso, double necessario, double disponivel) {
        super(String.format("%s insuficiente (necessário: %.2f, disponível: %.2f)",
                recurso, necessario, disponivel));
        this.recurso = recurso;
        this.necessario = necessario;
        this.disponivel = disponivel;
    }

    /**
     * Retorna o nome do recurso que faltou.
     *
     * @return nome do recurso
     */
    public String getRecurso() {
        return recurso;
    }

    /**
     * Retorna a quantidade que a operação exigia.
     *
     * @return quantidade necessária
     */
    public double getNecessario() {
        return necessario;
    }

    /**
     * Retorna a quantidade que estava disponível.
     *
     * @return quantidade disponível
     */
    public double getDisponivel() {
        return disponivel;
    }
}
