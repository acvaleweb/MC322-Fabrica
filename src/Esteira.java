public class Esteira {
    private MateriaPrima itemMateriaPrima;
    private Produto itemProduto;
    private boolean emMovimento;
    private double quantidade; // carga atual na esteira
    private double capacidadeMaxima; // unidade: kg

    public Esteira(double capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
        this.itemMateriaPrima = null;
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

    public boolean adicionarItem(MateriaPrima materiaPrima, double quantidade) {
        if (!emMovimento || !estaVazia() || materiaPrima == null) {
            return false;
        }

        if (!verificarCapacidade(quantidade)) {
            return false;
        }

        this.itemMateriaPrima = materiaPrima;
        this.quantidade = quantidade;
        return true;
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

    public MateriaPrima removerMateriaPrima() {
        if (!emMovimento || itemMateriaPrima == null) {
            return null;
        }

        MateriaPrima removida = itemMateriaPrima;
        itemMateriaPrima = null;
        quantidade = 0;

        return removida;
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
        return itemMateriaPrima == null && itemProduto == null;
    }

    public boolean estaEmMovimento() {
        return emMovimento;
    }

    // Getters

    public MateriaPrima getMateriaPrima() {
        return itemMateriaPrima;
    }

    public Produto getProduto() {
        return itemProduto;
    }

    public double getQuantidade() {
        return quantidade;
    }
}
