public class Esteira {
    private Produto itemProduto;
    private boolean emMovimento;
    private double quantidade; // carga atual na esteira
    private double capacidadeMaxima; // unidade: kg

    public Esteira(double capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
        this.itemProduto = null;
        this.emMovimento = false;
        this.quantidade = 0;
    }

    public void ligar() {
        emMovimento = true;
    }

    public void desligar() {
        emMovimento = false;
    }

    public boolean adicionarItem(Produto produto) {
        if (!emMovimento || !estaVazia() || produto == null) {
            return false;
        }

        if (!verificarCapacidade(produto.getMassa())) {
            return false;
        }

        this.itemProduto = produto;
        this.quantidade = produto.getMassa();
        return true;
    }

    public Produto removerProduto() {
        if (!emMovimento || itemProduto == null) {
            return null;
        }

        Produto removido = itemProduto;
        itemProduto = null;
        quantidade = 0;

        return removido;
    }

    public boolean verificarCapacidade(double peso) {
        return peso > 0 && peso <= capacidadeMaxima;
    }

    public boolean estaVazia() {
        return itemProduto == null;
    }

    public boolean estaEmMovimento() {
        return emMovimento;
    }

    // Getters

    public Produto getProduto() {
        return itemProduto;
    }

    public double getQuantidade() {
        return quantidade;
    }
}