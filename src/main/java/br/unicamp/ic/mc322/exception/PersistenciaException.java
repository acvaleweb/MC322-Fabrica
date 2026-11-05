package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando uma operação de salvar ou carregar o estado da planta falha.
 *
 * <p>Usada diretamente para falhas de leitura e escrita (disco cheio, falta de
 * permissão, arquivo bloqueado etc.), com a causa original encapsulada. Os problemas
 * mais específicos têm subclasses próprias:</p>
 * <ul>
 *   <li>{@link ArquivoNaoEncontradoException}: o arquivo a carregar não existe;</li>
 *   <li>{@link ArquivoCorrompidoException}: o arquivo está vazio ou não é um XML
 *       válido;</li>
 *   <li>{@link DadosInvalidosException}: o XML é válido, mas os dados violam as regras
 *       do domínio.</li>
 * </ul>
 *
 * <p>Como herda de {@link PlantaException}, o menu a trata como qualquer outro erro de
 * regra de negócio: mostra o motivo e devolve o usuário ao menu.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class PersistenciaException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com a descrição da falha de persistência.
     *
     * @param mensagem explicação do problema, apresentável ao usuário
     */
    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria a exceção encapsulando a causa original (por exemplo, uma
     * {@link java.io.IOException}).
     *
     * @param mensagem explicação do problema, apresentável ao usuário
     * @param causa    exceção que originou a falha
     */
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
