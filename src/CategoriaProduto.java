public enum CategoriaProduto {
    PLACA_DE_VIDEO("Placa de Vídeo", 10.0),
    PROCESSADOR("Processador", 6.0),
    PLACA_MAE("Placa-Mãe", 14.0);
    
    private final String nomeExibicao;
    private final double tempoBaseProducao;

    CategoriaProduto(String nomeExibicao, double tempoBaseProducao) {
        this.nomeExibicao = nomeExibicao;
        this.tempoBaseProducao = tempoBaseProducao;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public double getTempoBaseProducao() {
        return tempoBaseProducao;
    }
}