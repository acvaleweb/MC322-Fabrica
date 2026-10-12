/**
 * Esteira que transporta um produto por vez entre as máquinas da linha.
 *
 * <p>A esteira só aceita ou entrega itens enquanto está em movimento, comporta um item
 * por vez e recusa produtos mais pesados que a sua capacidade. Quando uma operação é
 * recusada, a esteira continua exatamente como estava.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class Esteira {
    private Produto itemProduto;
    private boolean emMovimento;
    private double quantidade; // carga atual na esteira
    private double capacidadeMaxima; // unidade: kg

    /**
     * Cria uma esteira parada e vazia.
     *
     * @param capacidadeMaxima maior massa, em kg, que a esteira suporta
     */
    public Esteira(double capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
        this.itemProduto = null;
        this.emMovimento = false;
        this.quantidade = 0;
    }

    /**
     * Coloca a esteira em movimento.
     */
    public void ligar() {
        emMovimento = true;
    }

    /**
     * Para a esteira.
     */
    public void desligar() {
        emMovimento = false;
    }

    /**
     * Coloca um produto na esteira. Todas as validações vêm antes de qualquer alteração:
     * se o item for recusado, a esteira continua como estava.
     *
     * @param produto produto a transportar
     * @throws IllegalArgumentException     se o produto for nulo
     * @throws MaquinaIndisponivelException se a esteira estiver parada, já estiver
     *                                      ocupada ou o produto exceder a capacidade
     */
    public void adicionarItem(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Não é possível colocar um produto nulo na esteira");
        }

        if (!emMovimento) {
            throw new MaquinaIndisponivelException("A esteira está parada e não aceita novos itens");
        }

        if (!estaVazia()) {
            throw new MaquinaIndisponivelException("A esteira já está ocupada por outro item");
        }

        if (!verificarCapacidade(produto.getMassa())) {
            throw new MaquinaIndisponivelException("O produto " + produto.getNome() + " (massa "
                    + produto.getMassa() + " kg) excede a capacidade da esteira (" + capacidadeMaxima + " kg)");
        }

        this.itemProduto = produto;
        this.quantidade = produto.getMassa();
    }

    /**
     * Retira o produto da esteira e a deixa vazia.
     *
     * @return o produto que estava na esteira
     * @throws MaquinaIndisponivelException se a esteira estiver parada ou vazia
     */
    public Produto removerProduto() {
        if (!emMovimento) {
            throw new MaquinaIndisponivelException("A esteira está parada e não pode entregar o item");
        }

        if (itemProduto == null) {
            throw new MaquinaIndisponivelException("A esteira está vazia: não há item para remover");
        }

        Produto removido = itemProduto;
        itemProduto = null;
        quantidade = 0;

        return removido;
    }

    /**
     * Verifica se uma massa cabe na esteira.
     *
     * @param peso massa a verificar, em kg
     * @return {@code true} se o peso é positivo e não excede a capacidade
     */
    public boolean verificarCapacidade(double peso) {
        return peso > 0 && peso <= capacidadeMaxima;
    }

    /**
     * Informa se a esteira está vazia.
     *
     * @return {@code true} se não há item na esteira
     */
    public boolean estaVazia() {
        return itemProduto == null;
    }

    /**
     * Informa se a esteira está em movimento.
     *
     * @return {@code true} se está em movimento
     */
    public boolean estaEmMovimento() {
        return emMovimento;
    }

    // Getters

    /**
     * Retorna o produto que está na esteira.
     *
     * @return o produto, ou {@code null} se a esteira estiver vazia
     */
    public Produto getProduto() {
        return itemProduto;
    }

    /**
     * Retorna a carga atual da esteira.
     *
     * @return massa transportada, em kg (zero se estiver vazia)
     */
    public double getQuantidade() {
        return quantidade;
    }

    /**
     * Retorna a capacidade máxima da esteira.
     *
     * @return capacidade, em kg
     */
    public double getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
}
