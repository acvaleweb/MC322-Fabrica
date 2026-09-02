public class MateriaPrima {
    private String id;
    private String nome;
    private double quantidade;
    private String unidade;
    private double quantidadeMinima;

    public MateriaPrima(String id, String nome, double quantidade, String unidade, double quantidadeMinima) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
    }

    public boolean consumir(double quantidadeDemandada) {
        if (quantidadeDemandada <= 0) {
            return false;
        }

        if (!atendeLoteMinimo(quantidadeDemandada)) {
            return false;
        }

        if (!verificarDisponibilidade(quantidadeDemandada)) {
            return false;
        }

        this.quantidade -= quantidadeDemandada;
        return true;
    }

    public boolean atendeLoteMinimo(double quantidadeDemandada) {
        return quantidadeDemandada >= this.quantidadeMinima;
    }

    public boolean adicionarEstoque(double quantidadeAdicional) {
        if (quantidadeAdicional > 0) {
            this.quantidade += quantidadeAdicional;
            return true;
        }
        return false;
    }

    public boolean verificarDisponibilidade(double quantidadeDemandada) {
        return this.quantidade >= quantidadeDemandada;
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getQuantidadeMinima() {
        return quantidadeMinima;
    }
}