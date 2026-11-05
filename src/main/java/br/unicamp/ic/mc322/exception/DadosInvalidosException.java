package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando o arquivo é um XML bem formado, mas os dados que ele carrega violam as
 * regras do domínio: quantidade negativa, saúde fora de 0 a 100, status inexistente,
 * número em formato inválido, produto fora do catálogo e assim por diante.
 *
 * <p>É lançada durante a validação do carregamento, antes de qualquer alteração na
 * planta atual, de modo que o estado válido anterior é preservado.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class DadosInvalidosException extends PersistenciaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com a descrição do dado inválido.
     *
     * @param mensagem explicação de qual dado é inválido e por quê
     */
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria a exceção encapsulando a causa original (por exemplo, a exceção de domínio
     * lançada ao reconstruir o objeto).
     *
     * @param mensagem explicação de qual dado é inválido e por quê
     * @param causa    exceção que originou o problema
     */
    public DadosInvalidosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
