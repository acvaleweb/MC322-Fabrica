public enum Cenario {
    IDEAL("Ideal", 3000.0, 0.5, 0.0, 1.0),
    APOCALIPTICO("Apocaliptico", 1500.0, 2.0, 2.0, 5.0);

    private final String nomeExibicao;
    private final double budgetInicial;
    private final double fatorProbabilidadeFalha; // multiplica a probabilidadeFalha base das maquinas
    private final double desgasteMinimoPorUso;
    private final double desgasteMaximoPorUso;

    Cenario(String nomeExibicao, double budgetInicial, double fatorProbabilidadeFalha,
            double desgasteMinimoPorUso, double desgasteMaximoPorUso) {
        this.nomeExibicao = nomeExibicao;
        this.budgetInicial = budgetInicial;
        this.fatorProbabilidadeFalha = fatorProbabilidadeFalha;
        this.desgasteMinimoPorUso = desgasteMinimoPorUso;
        this.desgasteMaximoPorUso = desgasteMaximoPorUso;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public double getBudgetInicial() {
        return budgetInicial;
    }

    public double getFatorProbabilidadeFalha() {
        return fatorProbabilidadeFalha;
    }

    public double getDesgasteMinimoPorUso() {
        return desgasteMinimoPorUso;
    }

    public double getDesgasteMaximoPorUso() {
        return desgasteMaximoPorUso;
    }
}