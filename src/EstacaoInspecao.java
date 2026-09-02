public class EstacaoInspecao {
    private boolean ativa;
    private int produtosInspecionados;

    public EstacaoInspecao() {
        this.ativa = false;
        this.produtosInspecionados = 0;
    }

    public void ativar() {
        this.ativa = true;
    }

    public void desativar() {
        this.ativa = false;
    }

    public boolean inspecionar(Produto produto) {
        if (!this.ativa || produto == null || produto.getStatus() != StatusProduto.PROCESSADO) {
            return false;
        }

        this.produtosInspecionados++;
        produto.setStatus(StatusProduto.INSPECIONADO);
        return true;
    }

    public int getTotalInspecionados() {
        return this.produtosInspecionados;
    }
}