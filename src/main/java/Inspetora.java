/**
 * Última etapa da linha: a inspetora, que aprova ou rejeita cada produto já processado.
 *
 * <p>O <b>rigor da inspeção</b> é a chance de rejeição, que soma três parcelas e é
 * limitada a 95%:</p>
 * <ul>
 *   <li>a probabilidade de falha efetiva da própria inspetora (máquina desgastada
 *       inspeciona pior);</li>
 *   <li>a probabilidade de falha acumulada do produto, aumentada por falhas nas etapas
 *       anteriores;</li>
 *   <li>a qualidade do produto multiplicada por um fator de rigor (0,3): produtos de
 *       tier mais alto são inspecionados com mais rigidez.</li>
 * </ul>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class Inspetora extends Maquina {

    private static int totalInspecionados = 0;

    // Quanto peso a qualidade do produto tem na rigidez da inspeção
    private static final double FATOR_RIGOR_QUALIDADE = 0.3;

    /**
     * Cria uma inspetora.
     *
     * @param nome               nome de identificação da máquina
     * @param capacidadeMaxima   maior massa de produto, em kg, que a máquina suporta
     * @param probabilidadeFalha probabilidade base de falha, entre 0 e 1
     * @param custoOperacao      custo de operação, em reais, por unidade
     */
    public Inspetora(String nome, double capacidadeMaxima, double probabilidadeFalha, double custoOperacao) {
        super(nome, capacidadeMaxima, probabilidadeFalha, custoOperacao);
    }

    /**
     * Inspeciona o produto: ele passa para {@link StatusProduto#INSPECIONADO} se for
     * aprovado ou para {@link StatusProduto#REJEITADO} caso contrário.
     *
     * @param produto produto já processado pela insersora
     * @throws MaquinaIndisponivelException se a máquina estiver quebrada, desligada, não
     *                                      suportar a massa do produto ou se o produto
     *                                      ainda não estiver com status processado
     * @throws IllegalArgumentException     se o produto for nulo
     */
    @Override
    public void processar(Produto produto) {
        validarOperacao(produto);

        if (produto.getStatus() != StatusProduto.PROCESSADO) {
            throw new MaquinaIndisponivelException("A " + getNome() + " só inspeciona produtos já processados, mas "
                    + produto.getNome() + " está com status " + produto.getStatus());
        }

        if (falhouNaInspecao(produto)) {
            produto.setStatus(StatusProduto.REJEITADO);
            return;
        }

        produto.setStatus(StatusProduto.INSPECIONADO);
        totalInspecionados++;
    }

    private boolean falhouNaInspecao(Produto produto) {
        double chanceRejeicao = calcularProbabilidadeFalhaEfetiva()
                + produto.getProbabilidadeFalhaAcumulada()
                + (produto.getQualidade() * FATOR_RIGOR_QUALIDADE);

        chanceRejeicao = Math.min(chanceRejeicao, 0.95); // nunca 100% garantido

        return random.nextDouble() < chanceRejeicao;
    }

    // Getters

    /**
     * Retorna o tipo da máquina.
     *
     * @return "Inspetora"
     */
    @Override
    public String getTipo() {
        return "Inspetora";
    }

    /**
     * Retorna quantos produtos foram aprovados por todas as inspetoras desde o início
     * da execução.
     *
     * @return total de produtos aprovados na inspeção
     */
    public static int getTotalInspecionados() {
        return totalInspecionados;
    }
}
