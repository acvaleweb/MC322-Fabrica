/**
 * Estados possíveis de uma {@link Demanda}.
 *
 * <p>O fluxo normal é {@code PENDENTE} para {@code EM_PRODUCAO} e depois para
 * {@code CONCLUIDA}. Uma demanda pode ser {@code CANCELADA} enquanto não estiver
 * concluída. Concluir ou cancelar não é definitivo para a quantidade: atualizar a
 * quantidade de uma demanda concluída ou cancelada a reabre como {@code PENDENTE}.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public enum StatusDemanda {
    /** Aguardando o início da produção. */
    PENDENTE("Pendente"),
    /** Lote em fabricação; a demanda não pode ser alterada nesse estado. */
    EM_PRODUCAO("Em produção"),
    /** Lote já processado pela linha de produção. */
    CONCLUIDA("Concluída"),
    /** Demanda cancelada pelo usuário. */
    CANCELADA("Cancelada");

    private final String descricao;

    StatusDemanda(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna a descrição do estado para exibição ao usuário.
     *
     * @return descrição legível (ex.: "Em produção")
     */
    public String getDescricao() {
        return descricao;
    }
}
