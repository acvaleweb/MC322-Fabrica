/**
 * Exceção base de todos os erros de regra de negócio da planta industrial.
 *
 * <p>É uma exceção não verificada ({@link RuntimeException}) para que os métodos do
 * domínio não precisem declarar {@code throws} em cada assinatura. Quem interage com o
 * usuário (o {@link MenuConsole}) captura esta classe para apresentar uma mensagem
 * clara e devolver o usuário ao menu, sem encerrar o programa.</p>
 *
 * <p>As subclasses indicam a natureza do problema:</p>
 * <ul>
 *   <li>{@link RecursoInsuficienteException}: falta de matéria-prima ou de budget;</li>
 *   <li>{@link DemandaInvalidaException}: demanda inexistente ou em estado incompatível
 *       com a operação pedida;</li>
 *   <li>{@link MaquinaIndisponivelException}: máquina quebrada ou desligada;</li>
 *   <li>{@link QuantidadeInvalidaException}: quantidade fora das regras de compra ou
 *       de consumo.</li>
 * </ul>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class PlantaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria uma exceção da planta com uma mensagem descritiva.
     *
     * @param mensagem explicação do problema, apresentável ao usuário
     */
    public PlantaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria uma exceção da planta que encapsula a causa original.
     *
     * @param mensagem explicação do problema, apresentável ao usuário
     * @param causa    exceção que originou este erro
     */
    public PlantaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
