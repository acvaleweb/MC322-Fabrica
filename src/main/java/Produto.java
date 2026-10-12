/**
 * Produto fabricado pela planta (placa de vídeo, processador ou placa-mãe).
 *
 * <p>Cada produto percorre a linha de produção mudando de {@link StatusProduto} e
 * acumula uma <b>probabilidade de falha</b>: sempre que uma máquina falha durante o
 * processamento, a probabilidade do produto sobe 10 pontos percentuais (limitada a
 * 100%). Quando chega a 50%, o produto é considerado em risco
 * ({@link #precisaManutencao()}).</p>
 *
 * <p>As subclasses representam os tiers de qualidade: {@link ComponenteEntrada},
 * {@link ComponentePerformance} e {@link ComponenteFlagship}.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public abstract class Produto implements Auditavel {
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
	private static final double LIMIAR_RISCO = 0.5;

	/**
	 * Realiza o processamento do produto na insersora. Cada tier decide como o
	 * produto é processado; todos passam para {@link StatusProduto#PROCESSADO}.
	 */
	public abstract void processar();

	/**
	 * Calcula o tempo de produção: o tempo base da categoria multiplicado pelo fator do
	 * tier (0,8 para Entrada, 1,0 para Performance e 1,3 para Flagship).
	 *
	 * @return o tempo de produção de uma unidade
	 */
	public abstract double calcularTempoProducao();

	/**
	 * Retorna o nome do tier do produto, usado nos relatórios.
	 *
	 * @return nome do tier (ex.: "Entrada", "Performance" ou "Flagship")
	 */
	public abstract String getTipo();

	/**
	 * Cria um produto no estado {@link StatusProduto#AGUARDANDO_PROCESSAMENTO}, sem
	 * falhas acumuladas, e contabiliza-o no total de produtos criados.
	 *
	 * @param id                              identificador do produto ou do lote
	 * @param nome                            nome comercial (ex.: "Andesite A7000")
	 * @param categoria                       categoria do produto
	 * @param quantidadeMateriaPrimaPorUnidade matéria-prima consumida por unidade
	 * @param qualidade                       qualidade do tier, entre 0 e 1
	 * @param massa                           massa do produto, em kg
	 */
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

	/**
	 * Aumenta a probabilidade de falha acumulada em 10 pontos percentuais, sem passar de
	 * 100%. Chamado pelas máquinas quando falham durante o processamento.
	 */
	public void aumentarProbabilidadeFalha() {
		this.probabilidadeFalhaAcumulada = Math.min(probabilidadeFalhaAcumulada + INCREMENTO_FALHA, 1.0);
	}

	/**
	 * Informa se o produto está em risco, isto é, se a probabilidade de falha acumulada
	 * chegou a 50%.
	 *
	 * @return {@code true} se o produto está em risco
	 */
	@Override
	public boolean precisaManutencao() {
		return probabilidadeFalhaAcumulada >= LIMIAR_RISCO;
	}

	/**
	 * Gera o diagnóstico do produto em uma linha: tier, id, nome, qualidade, falha
	 * acumulada e situação ("OK", "RISCO" ou "REJEITADO").
	 *
	 * @return o texto do diagnóstico
	 */
	@Override
	public String gerarRelatorioDiagnostico() {
		String risco = "OK";
		if (precisaManutencao()) {
			risco = "RISCO";
		}
		if (status == StatusProduto.REJEITADO) {
			risco = "REJEITADO";
		}

		return getTipo() + " " + id + " (" + nome + ")"
				+ " | qualidade: " + qualidade
				+ " | falha acumulada: " + String.format("%.0f%%", probabilidadeFalhaAcumulada * 100)
				+ " | " + risco;
	}

	// Getters

	/**
	 * Retorna o identificador do produto.
	 *
	 * @return identificador
	 */
	public String getId() {
		return id;
	}

	/**
	 * Retorna o nome comercial do produto.
	 *
	 * @return nome do produto
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Retorna a categoria do produto.
	 *
	 * @return categoria
	 */
	public CategoriaProduto getCategoria() {
		return categoria;
	}

	/**
	 * Retorna o estado atual do produto na linha.
	 *
	 * @return status do produto
	 */
	public StatusProduto getStatus() {
		return status;
	}

	/**
	 * Retorna a quantidade de matéria-prima consumida para fabricar uma unidade.
	 *
	 * @return matéria-prima por unidade
	 */
	public double getQuantidadeMateriaPrimaPorUnidade() {
		return quantidadeMateriaPrimaPorUnidade;
	}

	/**
	 * Retorna a qualidade do produto, definida pelo tier.
	 *
	 * @return qualidade, entre 0 e 1
	 */
	public double getQualidade() {
		return qualidade;
	}

	/**
	 * Retorna a probabilidade de falha acumulada nas etapas da linha.
	 *
	 * @return probabilidade acumulada, entre 0 e 1
	 */
	public double getProbabilidadeFalhaAcumulada() {
		return probabilidadeFalhaAcumulada;
	}

	/**
	 * Retorna a massa do produto, usada para checar a capacidade da esteira e das
	 * máquinas.
	 *
	 * @return massa, em kg
	 */
	public double getMassa() {
		return massa;
	}

	/**
	 * Retorna quantos produtos já foram criados desde o início da execução (inclui os
	 * moldes do catálogo).
	 *
	 * @return total de produtos criados
	 */
	public static int getTotalProdutosFabricados() {
		return totalProdutosFabricados;
	}

	// Setters

	/**
	 * Altera o estado do produto na linha.
	 *
	 * @param status novo estado
	 */
	public void setStatus(StatusProduto status) {
		this.status = status;
	}
}
