public class MateriaPrima {
    private String id;
    private String nome;
    private double quantidade;
    private String unidade;	     // unidade de medida
    private double quantidadeMinima; // isso é o menor lote que o fornecedor vende
    private double densidade;	     // massa de uma unidade 
        
    
    public MateriaPrima(String id, String nome, double quantidade, String unidade, double quantidadeMinima, double densidade) {
	this.id = id;
	this.nome = nome;
	this.quantidade = quantidade;
	this.unidade = unidade;
	this.quantidadeMinima = quantidadeMinima;
	this.densidade = densidade;
    }

    public void consumir(double quantidadeDemanda) {
        if (quantidade - quantidadeDemanda >= quantidadeMinima) {
            quantidade = quantidade - quantidadeDemanda;
        }
    }

    public void adicionarEstoque(double quantidadeAdiciona) {
	    if(quantidadeAdiciona >= quantidadeMinima) {
		    quantidade = quantidade + quantidadeAdiciona;
	    }
	
    }

    // método verificarDisponibilidade - verifica se há qtd para uma demanda
    public boolean verificarDisponibilidade(int quantidadeProdutoFinal, Produto produto) {
        if (quantidadeProdutoFinal*produto.getDemandaMateriaPrima() <=quantidade) {
		return true;
	} else {
		return false;
	}
    }

    public String getId() {
        return id;
    }

    public double getQuantidade() {
	    return quantidade;
    }

    public double getDensidade() {
	    return densidade;
    }

}
