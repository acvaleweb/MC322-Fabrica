/**
 * Contrato dos elementos da planta que podem ser auditados, ou seja, que sabem
 * descrever o próprio estado e indicar se precisam de atenção.
 *
 * <p>É implementada por {@link Maquina} (desgaste e saúde) e por {@link Produto}
 * (probabilidade de falha acumulada), o que permite ao
 * {@link GerenciadorProducao} gerar um único relatório de auditoria com máquinas e
 * produtos misturados.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public interface Auditavel {
    /**
     * Gera uma linha de texto com o diagnóstico atual do elemento (identificação,
     * indicadores principais e situação de risco).
     *
     * @return o relatório de diagnóstico, pronto para ser exibido no terminal
     */
    String gerarRelatorioDiagnostico();

    /**
     * Informa se o elemento está em situação de risco e merece atenção.
     *
     * @return {@code true} se o elemento precisa de manutenção ou está em risco;
     *         {@code false} caso contrário
     */
    boolean precisaManutencao();
}
