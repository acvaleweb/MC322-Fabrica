<<<<<<< Updated upstream
abstract class Produto {
=======
public abstract class Produto implements Auditavel {
>>>>>>> Stashed changes
	private String id;
	private String nome;
	private StatusProduto status;
	private double quantidadeMateriaPrimaPorUnidade;
	private double qualidade;
<<<<<<< Updated upstream
	private double  probabilidadeFalhaAcumulada;
	private int totalProdutosFabricados;
	
=======
	private double probabilidadeFalhaAcumulada;
	private double massa; // unidade: kg, usada pela Esteira

	private static int totalProdutosFabricados = 0;

	private static final double INCREMENTO_FALHA = 0.1;

	// Limiar base de risco acumulado tolerado antes do lote ser sinalizado na auditoria;
	// e ajustado pela qualidade 
	private static final double LIMIAR_RISCO_BASE = 0.3;

>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
}
=======
	// Auditavel

	@Override
	public String gerarRelatorioDiagnostico() {
		return String.format("%s (%s) | tier: %s | qualidade: %.2f | risco acumulado: %.1f%% | status: %s",
				nome, id, getTipo(), qualidade, probabilidadeFalhaAcumulada * 100, status);
	}

	@Override
	public boolean precisaManutencao() {
		// produtos de qualidade menor toleram menos risco acumulado antes de virar alerta
		double limiarAjustado = LIMIAR_RISCO_BASE * qualidade;
		return probabilidadeFalhaAcumulada >= limiarAjustado;
	}
}
>>>>>>> Stashed changes
