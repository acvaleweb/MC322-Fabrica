public class MateriaPrima {

    // atributos obrigatorios
    private String id;
    private String nome;
    private double quantidade;
    private String unidade;
    private double quantidadeMinima; // isso e o menor lote que o fornecedor vende
	
    // construtor
    public MateriaPrima(String id, String nome, double quantidade, String unidade, double quantidadeMinima) {
	this.id = id;
	this.nome = nome;
	this.quantidade = quantidade;
	this.unidade = unidade;
	this.quantidadeMinima = quantidadeMinima;
    }

    // metodos publicos

    // metodo consumir 
    // reduz qtd em estoque de acordo com a quantidade demandada
    public void consumir(double quantidadeDemanda) {
        if (quantidade - quantidadeDemanda >= quantidadeMinima) {
            quantidade = quantidade - quantidadeDemanda;
        }
    }

    // metodo adicionarEstoque 
    // adiciona qtd de materia prima ao estoque
    public void adicionarEstoque(double quantidadeAdiciona) {
		quantidade = quantidade + quantidadeAdiciona;
	}

    // metodo verificarDisponibilidade 
    // verifica se há qtd para uma demanda
    public void verificarDisponibilidade(int quantidadeProdutoFinal, Produto produto) {
        if (quantidadeProdutoFinal*produto.getDemandaMateriaPrima() <=quantidade) {
		System.out.println("Há matéria prima suficiente em estoque para a produção da quantidade de produto solicitada.");
		System.out.println("Produto: "+produto.getNome());
		System.out.println("Quantidade solicitada: "+quantidadeProdutoFinal);
		System.out.println("----------------");
		System.out.println("Quantidade de matéria prima que será utilizada: "+(quantidadeProdutoFinal*produto.getDemandaMateriaPrima()));
		System.out.println("Quantidade de matéria prima restante após manufatura: "+(quantidadeProdutoFinal*produto.getDemandaMateriaPrima() - quantidade));
	} else {
		System.out.println("Não há matéria prima suficiente em estoque para a produção da quantidade de produto solicitada.");
		System.out.println("Produto: "+produto.getNome());
		System.out.println("Quantidade solicitada: "+quantidadeProdutoFinal);
		System.out.println("----------------");
		System.out.println("Quantidade de matéria prima necessária: "+(quantidadeProdutoFinal*produto.getDemandaMateriaPrima()));
		System.out.println("Quantidade de matéria em estoque: "+quantidade);
	}
    }


    // metodo getId 
    // retorna id
    public String getId() {
        return id;
    }

    // metodo getQuantidade 
    // retorna quantiadade disponivel
    public double getQuantidade() {
	    return quantidade;
    }

}
