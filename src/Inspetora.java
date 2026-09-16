public class Inspetora extends Maquina {

    private static int totalInspecionados = 0;

    // Quanto peso a qualidade do produto tem na rigidez da inspeção
    private static final double FATOR_RIGOR_QUALIDADE = 0.3;

    public Inspetora(String nome, double capacidadeMaxima, double probabilidadeFalha, double custoOperacao) {
        super(nome, capacidadeMaxima, probabilidadeFalha, custoOperacao);
    }

    @Override
    public void processar(Produto produto) {
        if (!podeProcessar(produto) || produto.getStatus() != StatusProduto.PROCESSADO) {
            return;
        }

        if (falhouNaInspecao(produto)) {
            produto.setStatus(StatusProduto.REJEITADO);
            return;
        }

        produto.setStatus(StatusProduto.INSPECIONADO);
        totalInspecionados++;
    }

    private boolean falhouNaInspecao(Produto produto) {
        double chanceRejeicao = probabilidadeFalha
                + produto.getProbabilidadeFalhaAcumulada()
                + (produto.getQualidade() * FATOR_RIGOR_QUALIDADE);

        chanceRejeicao = Math.min(chanceRejeicao, 0.95); // nunca 100% garantido

        return random.nextDouble() < chanceRejeicao;
    }

    // Getters

    @Override
    public String getTipo() {
        return "Inspetora";
    }

    public static int getTotalInspecionados() {
        return totalInspecionados;
    }
}