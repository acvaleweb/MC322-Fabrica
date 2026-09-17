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
    // Utilidades
    // ======================================================

    private void pausar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

        System.out.println("\n==================================================");
        System.out.println("INICIANDO PRODUCAO DE " + demanda.getQuantidadeProdutos()
                + " UNIDADE(S) DE " + tipoProduto.toUpperCase());
        System.out.println("==================================================");

        System.out.println("Checando estoque de " + materiaPrima.getNome() + ": "
                + materiaPrima.getQuantidade() + " " + materiaPrima.getUnidade() + "(s)");
        pausar(100);

        if (!materiaPrima.verificarDisponibilidade(materiaPrimaNecessaria)) {
            System.out.println("[ERRO] Estoque insuficiente de " + materiaPrima.getNome()
                    + " para produzir " + demanda.getQuantidadeProdutos() + " unidade(s) de " + tipoProduto);
            return false;
        }

        System.out.println("Checando budget disponivel: R$" + String.format("%.2f", budget));
        pausar(100);

        if (budget < custoOperacaoTotal) {
            System.out.println("[ERRO] Budget insuficiente para o custo de operacao do lote de " + tipoProduto);
            return false;
        }

        System.out.println("Estoque e budget suficientes. Consumindo " + materiaPrimaNecessaria + " "
                + materiaPrima.getUnidade() + "(s) de " + materiaPrima.getNome());
        materiaPrima.consumir(materiaPrimaNecessaria);
        pausar(100);

        System.out.println("Reservando R$" + String.format("%.2f", custoOperacaoTotal)
                + " do budget para custo de operacao do lote");
        budget -= custoOperacaoTotal;
        pausar(100);

        int unidadesPerdidasNaLinha = 0;
        int unidadesRejeitadasNaInspecao = 0;

        for (int unidade = 0; unidade < demanda.getQuantidadeProdutos(); unidade++) {
            System.out.println("\n--- Fabricando unidade " + (unidade + 1) + "/"
                    + demanda.getQuantidadeProdutos() + " de " + tipoProduto + " ---");

            Produto produtoAtual = criarUnidadeDoMolde(molde);
            boolean chegouAoFimDaLinha = processarNasMaquinas(produtoAtual);

            if (!chegouAoFimDaLinha) {
                System.out.println("[AVISO] Unidade travou na esteira e foi perdida");
                unidadesPerdidasNaLinha++;
            } else if (produtoAtual.getStatus() == StatusProduto.INSPECIONADO) {
                System.out.println(produtoAtual.getNome() + " (" + produtoAtual.getId()
                        + ") aprovado na inspecao. Enviando para o armazem.");
                produtosFabricados.add(produtoAtual);
            } else {
                System.out.println("[AVISO] " + produtoAtual.getNome() + " (" + produtoAtual.getId()
                        + ") rejeitado na inspecao.");
                unidadesRejeitadasNaInspecao++;
            }
        }

        demanda.atender();

        int unidadesProduzidas = demanda.getQuantidadeProdutos() - unidadesPerdidasNaLinha
                - unidadesRejeitadasNaInspecao;

        System.out.println("\n==================================================");
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
        System.out.println("==================================================");

        return true;
    }

    // Transporta o produto pela esteira, passando por cada maquina da linha
    // em sequencia (insersora -> montadora -> inspetora), narrando cada etapa
    private boolean processarNasMaquinas(Produto produto) {
        for (Maquina maquina : maquinas) {
            System.out.println("Checando estado da esteira...");
            pausar(100);

            if (!esteira.estaEmMovimento()) {
                System.out.println("Esteira parada. Ligando...");
                esteira.ligar();
                pausar(100);
            } else {
                System.out.println("Esteira ja em movimento.");
            }

            if (!esteira.adicionarItem(produto)) {
                System.out.println("[ERRO] Esteira recusou o item (fora de capacidade ou ocupada). "
                        + "Linha travou para esta unidade.");
                esteira.desligar();
                return false;
            }

            System.out.println(produto.getNome() + " colocado na esteira");
            pausar(100);

            Produto transportado = esteira.removerProduto();

            System.out.println(produto.getNome() + " chegou na " + maquina.getNome()
                    + ". Desligando esteira...");
            esteira.desligar();
            pausar(100);

            System.out.println("Checando estado da " + maquina.getNome() + "...");
            pausar(100);

            if (maquina.estaLigada()) {
                System.out.println(maquina.getNome() + " ja ligada. Desligando antes de reiniciar...");
                maquina.desligar();
                pausar(100);
            }

            System.out.println("Inserindo " + produto.getNome() + " em " + maquina.getNome() + "...");
            pausar(100);

            maquina.ligar();
            System.out.println("Ligando " + maquina.getNome() + "...");
            pausar(100);

            System.out.println("Realizando processamento (" + maquina.getTipo() + ")...");
            maquina.processar(transportado);
            maquina.desligar();

            System.out.println(produto.getNome() + " -> status: " + transportado.getStatus());
            System.out.println("Concluido. Desligando " + maquina.getNome() + "...");
            pausar(100);
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