public abstract class Produto {
	private String id;
	private String nome;
	private CategoriaProduto categoria;
	private StatusProduto status;
	private double quantidadeMateriaPrimaPorUnidade;
	private double qualidade;
	private double probabilidadeFalhaAcumulada;
	private double massa; // unidade: kg, usada pela Esteira

	private static int totalProdutosFabricados = 0;

	private static final double INCREMENTO_FALHA = 0.1;

	public abstract void processar();

	public abstract double calcularTempoProducao();

	public abstract String getTipo();

	protected Produto(String id, String nome, CategoriaProduto categoria, double quantidadeMateriaPrimaPorUnidade,
			double qualidade, double massa) {
		this.id = id;
		this.nome = nome;
		this.categoria = categoria;
		this.status = StatusProduto.AGUARDANDO_PROCESSAMENTO;
		this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
		this.qualidade = qualidade;
		this.probabilidadeFalhaAcumulada = 0.0;
		this.massa = massa;
		totalProdutosFabricados++;
	}

	public void aumentarProbabilidadeFalha() {
		this.probabilidadeFalhaAcumulada = Math.min(probabilidadeFalhaAcumulada + INCREMENTO_FALHA, 1.0);
	}

	// Getters

	public String getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public CategoriaProduto getCategoria() {
		return categoria;
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

	public static int getTotalProdutosFabricados() {
		return totalProdutosFabricados;
	}

	// Setters

	public void setStatus(StatusProduto status) {
		this.status = status;
	}
}