/**
 * Segunda etapa da linha: a montadora, que faz o encaixe final (cooler e heatsink em
 * GPUs e CPUs, conectores e soquetes em placas-mãe).
 *
 * <p>A montadora não muda o status do produto. Se a máquina falhar no sorteio, a
 * probabilidade de falha acumulada do produto aumenta.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class Montadora extends Maquina {

    /**
     * Cria uma montadora.
     *
     * @param nome               nome de identificação da máquina
     * @param capacidadeMaxima   maior massa de produto, em kg, que a máquina suporta
     * @param probabilidadeFalha probabilidade base de falha, entre 0 e 1
     * @param custoOperacao      custo de operação, em reais, por unidade
     */
    public Montadora(String nome, double capacidadeMaxima, double probabilidadeFalha, double custoOperacao) {
        super(nome, capacidadeMaxima, probabilidadeFalha, custoOperacao);
    }

    /**
     * Realiza a montagem final e sorteia uma possível falha.
     *
     * @param produto produto a ser montado
     * @throws MaquinaIndisponivelException se a máquina estiver quebrada, desligada ou
     *                                      não suportar a massa do produto
     * @throws IllegalArgumentException     se o produto for nulo
     */
    @Override
    public void processar(Produto produto) {
        validarOperacao(produto);

        // Etapa de montagem final: encaixe de cooler/heatsink em GPUs e CPUs,
        // ou dos conectores/soquetes em placas-mãe
        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha();
        }
    }

    // Getters

    /**
     * Retorna o tipo da máquina.
     *
     * @return "Montadora"
     */
    @Override
    public String getTipo() {
        return "Montadora";
    }
}
