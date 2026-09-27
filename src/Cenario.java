public enum Cenario {
    IDEAL("Ideal", 5000.0, 0.5, 0.5),
    APOCALIPTICO("Apocalíptico", 400.0, 2.5, 2.5);

    private final String nomeExibicao;
    private final double budgetInicial;
    private final double fatorFalha;
    private final double fatorDesgaste; 

    Cenario(String nomeExibicao, double budgetInicial, double fatorFalha, double fatorDesgaste) {
        this.nomeExibicao = nomeExibicao;
        this.budgetInicial = budgetInicial;
        this.fatorFalha = fatorFalha;
        this.fatorDesgaste = fatorDesgaste;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public double getBudgetInicial() {
        return budgetInicial;
    }

    public double getFatorFalha() {
        return fatorFalha;
    }

    public double getFatorDesgaste() {
        return fatorDesgaste;
    }
}
