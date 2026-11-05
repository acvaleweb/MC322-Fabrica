package br.unicamp.ic.mc322.exception;

/**
 * Lançada ao tentar carregar o estado da planta de um arquivo que não existe.
 *
 * <p>Na primeira execução da aplicação isso é esperado e não é tratado como erro: o
 * menu inicia com os dados padrão. Já ao pedir explicitamente para carregar um estado
 * salvo, a ausência do arquivo é informada ao usuário.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class ArquivoNaoEncontradoException extends PersistenciaException {
    private static final long serialVersionUID = 1L;

    /** Caminho do arquivo que não foi encontrado. */
    private final String caminho;

    /**
     * Cria a exceção informando o caminho do arquivo que faltou.
     *
     * @param caminho caminho do arquivo de persistência procurado
     */
    public ArquivoNaoEncontradoException(String caminho) {
        super("O arquivo de persistência não foi encontrado: " + caminho);
        this.caminho = caminho;
    }

    /**
     * Retorna o caminho do arquivo que não foi encontrado.
     *
     * @return caminho do arquivo
     */
    public String getCaminho() {
        return caminho;
    }
}
