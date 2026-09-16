import java.util.ArrayList;
import java.util.List;

public class GerenciadorProducao {
    private ArrayList<Demanda> demandas;
    private ArrayList<Produto> catalogoProdutos; // os produtos "modelo" da fábrica (moldes)
    private ArrayList<Produto> produtosFabricados; // armazém
    private ArrayList<Maquina> maquinas;
    private MateriaPrima materiaPrima;
    private Esteira esteira;
    private double budget;

    private static int contadorUnidadesCriadas = 0; // usado pra gerar id unico de cada unidade

    public GerenciadorProducao(MateriaPrima materiaPrima, double budgetInicial,
            double capacidadeEsteira, ArrayList<Produto> catalogoProdutos) {
        this.demandas = new ArrayList<>();
        this.produtosFabricados = new ArrayList<>();
        this.maquinas = new ArrayList<>();
        this.materiaPrima = materiaPrima;
        this.esteira = new Esteira(capacidadeEsteira);
        this.budget = budgetInicial;
        this.catalogoProdutos = catalogoProdutos;
    }

    // ======================================================
    // Configuração (chamada uma vez, na montagem da fábrica)
    // ======================================================

    // As máquinas devem ser adicionadas na ordem em que o produto passa por
    // elas (insersora -> montadora -> inspetora), pois processarNasMaquinas
    // percorre a lista nessa mesma sequência
    public void adicionarMaquina(Maquina maquina) {
        if (maquina != null) {
            maquinas.add(maquina);
        }
    }

    // ======================================================
    // Demandas
    // ======================================================

    public boolean registrarDemanda(String tipoProduto) {
        if (tipoProduto == null || buscarDemanda(tipoProduto) != null) {
            return false; // ja existe demanda cadastrada para esse tipo
        }

        demandas.add(new Demanda(tipoProduto));
        return true;
    }

    public boolean atualizarDemanda(String tipoProduto, int novaQuantidade) {
        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda == null || novaQuantidade < 0) {
            return false;
        }

        demanda.atualizarQuantidade(novaQuantidade);
        return true;
    }

    private Demanda buscarDemanda(String tipoProduto) {
        for (Demanda demanda : demandas) {
            if (demanda.getTipoProduto().equals(tipoProduto)) {
                return demanda;
            }
        }
        return null;
    }

    private Produto buscarNoCatalogo(String tipoProduto) {
        for (Produto produto : catalogoProdutos) {
            if (produto.getNome().equals(tipoProduto)) {
                return produto;
            }
        }
        return null;
    }

    // Cria uma unidade nova a partir do molde do catálogo
    private Produto criarUnidadeDoMolde(Produto molde) {
        String novoId = molde.getId() + "-" + (++contadorUnidadesCriadas);

        if (molde instanceof ComponenteFlagship flagship) {
            return flagship.criarUnidade(novoId);
        }
        if (molde instanceof ComponentePerformance performance) {
            return performance.criarUnidade(novoId);
        }
        if (molde instanceof ComponenteEntrada entrada) {
            return entrada.criarUnidade(novoId);
        }
        return null;
    }

    // ======================================================
    // Fabricação
    // ======================================================

    // Tenta atender integralmente a demanda de um tipo de produto
    public boolean fabricarDemanda(String tipoProduto) {
        Demanda demanda = buscarDemanda(tipoProduto);

        if (demanda == null) {
            System.out.println("[ERRO] Nenhuma demanda registrada para " + tipoProduto);
            return false;
        }

        if (demanda.isAtendida()) {
            System.out.println("[ERRO] Demanda de " + tipoProduto + " ja foi atendida");
            return false;
        }

        if (demanda.getQuantidadeProdutos() <= 0) {
            System.out.println("[ERRO] Quantidade demandada de " + tipoProduto + " precisa ser maior que zero");
            return false;
        }

        Produto molde = buscarNoCatalogo(tipoProduto);

        if (molde == null) {
            System.out.println("[ERRO] " + tipoProduto + " nao esta cadastrado no catalogo de produtos");
            return false;
        }

        double materiaPrimaNecessaria = demanda.calcularMateriaPrimaNecessaria(molde);
        double custoOperacaoTotal = calcularCustoProducao(demanda.getQuantidadeProdutos());

        if (!materiaPrima.verificarDisponibilidade(materiaPrimaNecessaria)) {
            System.out.println("[ERRO] Estoque insuficiente de " + materiaPrima.getNome()
                    + " para produzir " + demanda.getQuantidadeProdutos() + " unidade(s) de " + tipoProduto);
            return false;
        }

        if (budget < custoOperacaoTotal) {
            System.out.println("[ERRO] Budget insuficiente para o custo de operacao do lote de " + tipoProduto);
            return false;
        }

        materiaPrima.consumir(materiaPrimaNecessaria);
        budget -= custoOperacaoTotal;

        int unidadesPerdidasNaLinha = 0;
        int unidadesRejeitadasNaInspecao = 0;

        for (int unidade = 0; unidade < demanda.getQuantidadeProdutos(); unidade++) {
            Produto produtoAtual = criarUnidadeDoMolde(molde);
            boolean chegouAoFimDaLinha = processarNasMaquinas(produtoAtual);

            if (!chegouAoFimDaLinha) {
                unidadesPerdidasNaLinha++;
            } else if (produtoAtual.getStatus() == StatusProduto.INSPECIONADO) {
                produtosFabricados.add(produtoAtual);
            } else {
                unidadesRejeitadasNaInspecao++;
            }
        }

        demanda.atender();

        int unidadesProduzidas = demanda.getQuantidadeProdutos() - unidadesPerdidasNaLinha
                - unidadesRejeitadasNaInspecao;
        System.out.println("[OK] " +
                unidadesProduzidas + " unidade(s) de " + tipoProduto + " fabricada(s) com sucesso");

        if (unidadesPerdidasNaLinha > 0) {
            System.out.println("[AVISO] " + unidadesPerdidasNaLinha
                    + " unidade(s) travou/travaram na esteira e foram perdidas");
        }
        if (unidadesRejeitadasNaInspecao > 0) {
            System.out.println("[AVISO] " + unidadesRejeitadasNaInspecao
                    + " unidade(s) foram rejeitadas na inspecao");
        }

        return true;
    }

    // Transporta o produto pela esteira, passando por cada maquina da linha
    // em sequencia (insersora -> montadora -> inspetora)
    private boolean processarNasMaquinas(Produto produto) {
        for (Maquina maquina : maquinas) {
            if (!esteira.estaEmMovimento()) {
                esteira.ligar();
            }

            if (!esteira.adicionarItem(produto)) {
                esteira.desligar();
                return false; // esteira recusou -- linha travou pra essa unidade
            }

            Produto transportado = esteira.removerProduto();

            maquina.ligar();
            maquina.processar(transportado);
            maquina.desligar();
        }

        esteira.desligar();
        return true;
    }

    private double calcularCustoProducao(int quantidadeUnidades) {
        double custoPorUnidade = 0;
        for (Maquina maquina : maquinas) {
            custoPorUnidade += maquina.getCustoOperacao();
        }
        return custoPorUnidade * quantidadeUnidades;
    }

    // ======================================================
    // Matéria-prima
    // ======================================================

    public boolean comprarMateriaPrima(double quantidade) {
        if (quantidade <= 0 || !materiaPrima.atendeLoteMinimo(quantidade)) {
            return false;
        }

        double custoTotal = quantidade * materiaPrima.getCustoPorUnidade();
        if (budget < custoTotal) {
            return false;
        }

        materiaPrima.adicionarEstoque(quantidade);
        budget -= custoTotal;
        return true;
    }

    // ======================================================
    // Consultas
    // ======================================================

    public void exibirBudget() {
        System.out.printf("Budget atual: R$%.2f/n", budget);
    }

    public void exibirArmazem() {
        if (produtosFabricados.isEmpty()) {
            System.out.println("Armazem vazio.");
            return;
        }
        for (Produto produto : produtosFabricados) {
            System.out.println(" - " + produto.getNome() + " (" + produto.getId() + ") | "
                    + produto.getCategoria().getNomeExibicao() + " | tier: " + produto.getTipo()
                    + " | qualidade: " + produto.getQualidade());
        }
    }

    // Getters

    public double getBudget() {
        return budget;
    }

    public List<Produto> getArmazem() {
        return produtosFabricados;
    }

    public List<Demanda> getDemandas() {
        return demandas;
    }

    public List<Produto> getCatalogoProdutos() {
        return catalogoProdutos;
    }

    public MateriaPrima getMateriaPrima() {
        return materiaPrima;
    }
}