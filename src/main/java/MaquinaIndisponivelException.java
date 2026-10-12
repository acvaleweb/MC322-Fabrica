/**
 * Lançada quando um equipamento da linha (máquina ou esteira) não pode processar ou
 * transportar um produto: por estar quebrado, desligado, ocupado, por não suportar a
 * massa do produto ou por receber um produto em estado incompatível com a etapa.
 *
 * @author Equipe Andesite Hardware Co.
 */
public class MaquinaIndisponivelException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com o motivo da indisponibilidade.
     *
     * @param mensagem explicação de por que a máquina não pode operar
     */
    public MaquinaIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
