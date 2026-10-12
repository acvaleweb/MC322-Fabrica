/**
 * Estados de um {@link Produto} ao longo da linha de produção.
 *
 * <p>O ciclo normal é {@code AGUARDANDO_PROCESSAMENTO}, depois {@code PROCESSADO}
 * (pela {@link InsersoraSMT}) e, por fim, {@code INSPECIONADO} ou {@code REJEITADO}
 * (pela {@link Inspetora}).</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public enum StatusProduto {
    /** Produto recém-criado, ainda sem passar pela linha. */
    AGUARDANDO_PROCESSAMENTO,
    /** Produto fabricado pela insersora, pendente de inspeção. */
    PROCESSADO,
    /** Produto aprovado na inspeção, apto a ir para o armazém. */
    INSPECIONADO,
    /** Produto reprovado na inspeção e descartado do lote. */
    REJEITADO
}
