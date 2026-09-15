abstract class Produto {
	private String id;
	private String nome;
	private StatusProduto status;
	private double quantidadeMateriaPrimaPorUnidade;
	private double qualidade;
	private double  probabilidadeFalhaAcumulada;
	private int totalProdutosFabricados;
	
	abstract void processar();
	abstract double calcularTempoProducao();
	abstract String getTipo();


	// Construtor

	public Produto(String id, String nome, StatusProduto status) {
        this.id = id;
        this.nome = nome;
        this.status = StatusProduto.AGUARDANDO_PROCESSAMENTO;
    }


	// Getters

	public String getId() {
		return id;
    }
	public String getNome() {
		return nome;
    }

	public StatusProduto getStatus() {
		return status;
    }

	public double getQuantidadeMateriaPrimaPorUnidade() {
		return quantidadeMateriaPrimaPorUnidade;
	}

	public double getQualidade() {
		return qualidade;

	}

	
	// Setters

	public void setStatus(StatusProduto status) {
		this.status = status;
    }

	// Outros

	public void aumentarProbabilidadeFalha() {
		probabilidadeFalhaAcumulada = 
		Math.max(
			probabilidadeFalhaAcumulada++,
			1
		);
	}

}
