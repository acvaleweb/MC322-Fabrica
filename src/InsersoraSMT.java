public class InsersoraSMT extends Maquina {

    public InsersoraSMT(String nome, double capacidadeMaxima, double probabilidadeFalha, double custoOperacao) {
        super(nome, capacidadeMaxima, probabilidadeFalha, custoOperacao);
    }

    @Override
    public void processar(Produto produto) {
        if (!podeProcessar(produto)) {
            return;
        }

        // A insersora é quem efetivamente "fabrica" o produto: solda o
        // die (GPU/CPU/chipset) no substrato e muda o status para PROCESSADO
        produto.processar();

        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha();
        }
    }

    // Getters

    @Override
    public String getTipo() {
        return "Insersora SMT";
    }
}