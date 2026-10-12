/**
 * Linhas de produto fabricadas pela Andesite Hardware Co.
 *
 * <p>Cada categoria define um tempo base de produção, que é multiplicado pelo fator
 * de tempo do tier do produto (Entrada, Performance ou Flagship) em
 * {@link Produto#calcularTempoProducao()}.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public enum CategoriaProduto {
    /** Placas de vídeo (GPUs), tempo base de produção 10. */
    PLACA_DE_VIDEO("Placa de Vídeo", 10.0),
    /** Processadores (CPUs), tempo base de produção 6. */
    PROCESSADOR("Processador", 6.0),
    /** Placas-mãe, a categoria mais demorada: tempo base de produção 14. */
    PLACA_MAE("Placa-Mãe", 14.0);

    private final String nomeExibicao;
    private final double tempoBaseProducao;

    CategoriaProduto(String nomeExibicao, double tempoBaseProducao) {
        this.nomeExibicao = nomeExibicao;
        this.tempoBaseProducao = tempoBaseProducao;
    }

    /**
     * Retorna o nome da categoria como deve aparecer para o usuário.
     *
     * @return nome de exibição (ex.: "Placa de Vídeo")
     */
    public String getNomeExibicao() {
        return nomeExibicao;
    }

    /**
     * Retorna o tempo base de produção da categoria, antes de aplicar o fator do tier.
     *
     * @return tempo base de produção
     */
    public double getTempoBaseProducao() {
        return tempoBaseProducao;
    }
}
