public class MateriaPrima {

	// atributos obrigatorios
	private String id;
	private String nome;
	private double quantidade;
	private String unidade;

	// menor lote possivel do fornecedor
	private int quantidadeMinima;


	// metodos publicos
	// metodo consumir - reduz qtd em estoque de acordo com a quantidade demandada
	public void consumir(double quantidadeDemanda) {
		if(quantidade - quantidadeDemanda >= quantidadeMinima) {
			quantidade = quantidade - quantidadeDemanda;
		}
	}

	// metodo adicionarEstoque - adiciona qtd de materia prima ao estoque
	public void adicionarEstoque(double quantidadeAdiciona) {
		if
		quantidade = quantidade + quantidadeAdiciona;
	}

	// metodo verificarDisponibilidade - verifica se há qtd para uma demanda
	//
	//arrumar - ver nome do metodo de class produto para arrumar
	public void verificarDisponibilidade(int quantidadeProdutoFinal) {
		if(quantidadeProdutquantidade){
		
		}
	}

	// metodo getId - retorna id
	public string getId() {
		return id;
	}

	// metodo getQuantidade - retorna quantiadade disponivel
	
	public float =

}
