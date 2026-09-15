abstract class Produto {
	private String id;
	private String nome;
	private StatusProduto status;
	private double quantidadeMateriaPrimaPorUnidade;
	private double qualidade;
	private double  probabilidadeFalhaAcumulada;
	private int totalProdutosFabricados;
	
	public abstract void processar();
	public abstract double calcularTempoProducao();
	public abstract String getTipo();


	// Construtor

	protected Produto(String id, String nome, StatusProduto status) {
        this.id = id;
        this.nome = nome;
        this.status = StatusProduto.AGUARDANDO_PROCESSAMENTO;
	this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
	this.qualidade = qualidade;
	this.probabilidadeFalhaAcumulada = 0.0;
	this.massa = massa;
	totalProdutosFabricados++;
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

	public double getProbabilidadeFalhaAcumulada() {
		return probabilidadeFalhaAcumulada;
	}

	public double getMassa() {
		return massa;
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
