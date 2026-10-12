/**
 * Lançada quando uma operação sobre uma demanda (ou sobre o produto dela) não pode ser
 * realizada.
 *
 * <p>Cobre, por exemplo: demanda inexistente, produto fora do catálogo, demanda já
 * concluída ou cancelada, quantidade menor ou igual a zero para produzir e tentativa de
 * alterar uma demanda que já está em produção.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class DemandaInvalidaException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com a descrição do problema na demanda.
     *
     * @param mensagem explicação do que há de inválido na demanda
     */
    public DemandaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
