package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando o arquivo de persistência existe, mas não pode ser interpretado: está
 * vazio, não é um XML bem formado, não tem a estrutura esperada (elementos ou atributos
 * obrigatórios ausentes ou repetidos) ou foi gravado em uma versão do formato que esta
 * aplicação não conhece.
 *
 * <p>Diferente de {@link DadosInvalidosException}, aqui o problema está na
 * <b>forma</b> do arquivo, e não nos valores que ele carrega.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class ArquivoCorrompidoException extends PersistenciaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com a descrição do problema de formato.
     *
     * @param mensagem explicação do que há de errado no arquivo
     */
    public ArquivoCorrompidoException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria a exceção encapsulando a causa original (por exemplo, o erro de leitura do
     * XML).
     *
     * @param mensagem explicação do que há de errado no arquivo
     * @param causa    exceção que originou o problema
     */
    public ArquivoCorrompidoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
