/**
 * Lançada quando uma quantidade informada viola as regras de negócio, como comprar ou
 * consumir uma quantidade menor ou igual a zero, ou comprar abaixo do lote mínimo do
 * fornecedor.
 *
 * @author Equipe Andesite Hardware Co.
 */
public class QuantidadeInvalidaException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com a descrição da quantidade inválida.
     *
     * @param mensagem explicação de por que a quantidade não é aceita
     */
    public QuantidadeInvalidaException(String mensagem) {
        super(mensagem);
    }
}
