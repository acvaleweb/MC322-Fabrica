/**
 * Primeira etapa da linha: a insersora SMT, que solda o die (GPU, CPU ou chipset) no
 * substrato e, com isso, efetivamente fabrica o produto.
 *
 * <p>Ao processar, o produto passa para {@link StatusProduto#PROCESSADO}. Se a máquina
 * falhar no sorteio, o produto sai com a probabilidade de falha acumulada aumentada, o
 * que o torna mais propenso a ser rejeitado na inspeção.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class InsersoraSMT extends Maquina {

    /**
     * Cria uma insersora SMT.
     *
     * @param nome               nome de identificação da máquina
     * @param capacidadeMaxima   maior massa de produto, em kg, que a máquina suporta
     * @param probabilidadeFalha probabilidade base de falha, entre 0 e 1
     * @param custoOperacao      custo de operação, em reais, por unidade
     */
    public InsersoraSMT(String nome, double capacidadeMaxima, double probabilidadeFalha, double custoOperacao) {
        super(nome, capacidadeMaxima, probabilidadeFalha, custoOperacao);
    }

    /**
     * Fabrica o produto (muda o status para processado) e sorteia uma possível falha.
     *
     * @param produto produto a ser soldado
     * @throws MaquinaIndisponivelException se a máquina estiver quebrada, desligada ou
     *                                      não suportar a massa do produto
     * @throws IllegalArgumentException     se o produto for nulo
     */
    @Override
    public void processar(Produto produto) {
        validarOperacao(produto);

        // A insersora é quem efetivamente "fabrica" o produto: solda o
        // die (GPU/CPU/chipset) no substrato e muda o status para PROCESSADO
        produto.processar();

        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha();
        }
    }

    // Getters

    /**
     * Retorna o tipo da máquina.
     *
     * @return "Insersora SMT"
     */
    @Override
    public String getTipo() {
        return "Insersora SMT";
    }
}
