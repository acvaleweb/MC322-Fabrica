public class Esteira {
	
	private String item;
	private boolean emMovimento;
	private double capacidadeMaxima;
	private boolean temItem;	
	
	public Esteira(String item, boolean emMovimento, double capacidadeMaxima, boolean temItem) {
		this.item = item;
		this.emMovimento = emMovimento;
		this.capacidadeMaxima = capacidadeMaxima;
		this.temItem = temItem;
	}

	public void ligar() {
		emMovimento = true;
	}

	public void desligar() {
		emMovimento = false;
	}

	public boolean  adicionarItem(MateriaPrima materiaPrima, double quantidade) {  
		if(temItem == false && verificarCapacidade(materiaPrima, quantidade)==true) {
			temItem = true;
			item = materiaPrima.getNome();
			return true;
		}
		return false;
	}

	public boolean adicionarItem(Produto produto) {
		if(temItem == false && verificarCapacidade(produto)==true) {
			
			temItem = true;
			item = produto.getNome();
			return true;
		}
		return false;
	}
	
	public String removerItem() {
		if(temItem == true) {

			temItem = false;
			String removido = item;
			item = "";
			return removido;
		}
		return "Nenhum.";
	}

	public boolean verificarCapacidade(MateriaPrima materiaPrima, double quantidade) {
		if(materiaPrima.getDensidade()*quantidade <= capacidadeMaxima) {
			return true;
		}
		return false;
	}

	public boolean verificarCapacidade(Produto produto) {
		if (produto.getMassa() <= capacidadeMaxima) {
			return true;
		}
		return false;
	}
}

