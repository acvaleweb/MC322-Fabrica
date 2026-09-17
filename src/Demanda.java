public class Demanda {
    private String tipoProduto;
    private int quantidadeProdutos;
    private boolean atendida;

    public Demanda(String tipoProduto) {
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = 0;
        this.atendida = false;
    }

    public void atualizarQuantidade(int novaQuantidade) {
        this.quantidadeProdutos = novaQuantidade;
        this.atendida = false;
    }

    public double calcularMateriaPrimaNecessaria(Produto produto) {
        return this.quantidadeProdutos * produto.getQuantidadeMateriaPrimaPorUnidade();
    }

    public void atender() {
        this.atendida = true;
    }

    // Getters

    public String getTipoProduto() {
        return tipoProduto;
    }

    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    public boolean isAtendida() {
        return atendida;
    }
}