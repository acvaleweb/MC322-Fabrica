public class Montadora extends Maquina {

    public Montadora(String nome, double capacidadeMaxima, double probabilidadeFalha, double custoOperacao) {
        super(nome, capacidadeMaxima, probabilidadeFalha, custoOperacao);
    }

    @Override
    public void processar(Produto produto) {
        if (!podeProcessar(produto)) {
            return;
        }

        // Etapa de montagem final: encaixe de cooler/heatsink em GPUs e CPUs,
        // ou dos conectores/soquetes em placas-mãe
        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha();
        }
    }

    // Getters

    @Override
    public String getTipo() {
        return "Montadora";
    }
}