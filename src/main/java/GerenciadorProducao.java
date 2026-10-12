import java.util.ArrayList;
import java.util.List;

/**
 * Núcleo da planta: orquestra demandas, estoque, budget, máquinas e esteira.
 *
 * <p>Regras de negócio principais:</p>
 * <ul>
 *   <li><b>Budget:</b> começa com o valor do {@link Cenario} e é debitado ao comprar
 *       matéria-prima e ao reservar o custo de operação de cada lote.</li>
 *   <li><b>Consistência de estado:</b> toda operação valida tudo antes de alterar
 *       qualquer coisa (estoque, budget ou status da demanda). Quando uma exceção é
 *       lançada, a planta continua exatamente como estava.</li>
 *   <li><b>Estratégia de produção:</b> a escolha da próxima demanda é delegada a uma
 *       {@link EstrategiaProducao}, que pode ser trocada em tempo de execução.</li>
 *   <li><b>Linha de produção:</b> cada unidade passa pela esteira e por todas as
 *       máquinas na ordem em que foram adicionadas. Se uma máquina quebrar no meio do
 *       lote, a unidade é perdida e o lote continua.</li>
 * </ul>
 *
 * <p>Os erros de regra de negócio são sinalizados com subclasses de
 * {@link PlantaException}; a apresentação deles ao usuário é responsabilidade do
 * {@link MenuConsole}.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class GerenciadorProducao {
    private List<Demanda> demandas;
    private List<Produto> catalogoProdutos;
    private List<Produto> produtosFabricados;
    private List<Maquina> maquinas;
    private MateriaPrima materiaPrima;
    private Esteira esteira;
    private Cenario cenarioAtual;
    private EstrategiaProducao estrategiaAtual;
    private double budget;

    private static int contadorUnidadesCriadas = 0;

    /**
     * Cria o gerenciador da planta, sem máquinas e sem demandas. O budget inicial vem do
     * cenário.
     *
     * @param materiaPrima      estoque de matéria-prima da fábrica
     * @param cenario           cenário ativo (define budget, falha e desgaste)
     * @param capacidadeEsteira maior massa, em kg, que a esteira suporta
     * @param catalogoProdutos  produtos que a fábrica sabe fabricar (usados como moldes)
     * @param estrategiaInicial estratégia de produção inicial
     */
    public GerenciadorProducao(MateriaPrima materiaPrima, Cenario cenario, double capacidadeEsteira,
            List<Produto> catalogoProdutos, EstrategiaProducao estrategiaInicial) {
        this.demandas = new ArrayList<>();
        this.produtosFabricados = new ArrayList<>();
        this.maquinas = new ArrayList<>();
        this.materiaPrima = materiaPrima;
        this.esteira = new Esteira(capacidadeEsteira);
        this.cenarioAtual = cenario;
        this.estrategiaAtual = estrategiaInicial;
        this.budget = cenario.getBudgetInicial();
        this.catalogoProdutos = catalogoProdutos;
    }

    /**
     * Troca a estratégia usada para escolher a próxima demanda.
     *
     * @param novaEstrategia a nova estratégia de produção
     * @throws IllegalArgumentException se a estratégia for nula
     */
    public void setEstrategia(EstrategiaProducao novaEstrategia) {
        if (novaEstrategia == null) {
            throw new IllegalArgumentException("A estratégia de produção não pode ser nula");
        }
        this.estrategiaAtual = novaEstrategia;
    }

    /**
     * Pergunta à estratégia ativa qual é a próxima demanda e a fabrica. O orçamento
     * repassado à estratégia é convertido em "quantas unidades cabem no budget",
     * dividindo o budget pelo custo de operação da linha por unidade.
     *
     * @throws DemandaInvalidaException     se nenhuma demanda for elegível para a
     *                                      estratégia ativa
     * @throws MaquinaIndisponivelException se a linha não puder produzir o lote
     * @throws RecursoInsuficienteException se faltar matéria-prima ou budget
     * @throws QuantidadeInvalidaException  se o lote ficar abaixo do lote mínimo
     */
    public void executarProximaProducao() {
        // Converte o budget em "quantas unidades cabem no orcamento", ja que
        // EstrategiaMaximoRendimento compara orcamentoDisponivel diretamente
        // com a quantidade de unidades de uma demanda (ver comentario na classe)
        double orcamentoEmUnidades = calcularOrcamentoEmUnidades();

        Demanda demandaEscolhida = estrategiaAtual.selecionarDemanda(demandas, orcamentoEmUnidades);

        if (demandaEscolhida == null) {
            throw new DemandaInvalidaException("Nenhuma demanda elegível para a estratégia "
                    + estrategiaAtual.getNomeEstrategia()
                    + ". Defina a quantidade de pelo menos um produto em Demandas");
        }

        System.out.println("[ESTRATEGIA: " + estrategiaAtual.getNomeEstrategia() + "] Selecionada: "
                + demandaEscolhida.getTipoProduto());
        fabricarDemanda(demandaEscolhida.getTipoProduto());
    }

    /**
     * Imprime o relatório de auditoria com o diagnóstico de todas as máquinas e de todos
     * os produtos do armazém, e o total de itens que precisam de atenção.
     */
    public void gerarAuditoriaGeral() {
        List<Auditavel> itensAuditaveis = new ArrayList<>();
        itensAuditaveis.addAll(maquinas);
        itensAuditaveis.addAll(produtosFabricados);

        System.out.println("\n==================================================");
        System.out.println("RELATORIO DE AUDITORIA DA PLANTA");
        System.out.println("==================================================");

        if (itensAuditaveis.isEmpty()) {
            System.out.println("Nenhum componente auditavel registrado ainda.");
            return;
        }

        int itensEmRisco = 0;
        for (Auditavel item : itensAuditaveis) {
            System.out.println(" - " + item.gerarRelatorioDiagnostico());
            if (item.precisaManutencao()) {
                itensEmRisco++;
            }
        }

        System.out.println("--------------------------------------------------");
        System.out.println("Total auditado: " + itensAuditaveis.size()
                + " | itens que precisam de atencao: " + itensEmRisco);
        System.out.println("==================================================");
    }

    private void pausar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Traduz o tempo de producao (categoria x tier) num atraso perceptivel na
    // tela, de ~70ms (CPU Entrada) a ~270ms (Placa-Mae Flagship)
    private long calcularDuracaoProcessamento(Produto produto) {
        return Math.round(produto.calcularTempoProducao() * 15);
    }

    // As maquinas herdam a fragilidade e o desgaste do cenario ativo no
    // momento em que entram na linha de producao
    /**
     * Adiciona uma máquina ao fim da linha de produção. A máquina herda a fragilidade
     * (probabilidade de falha multiplicada pelo fator do cenário) e o desgaste por uso
     * do cenário ativo. A ordem de adição define a ordem de processamento.
     *
     * @param maquina máquina a incluir na linha
     * @throws IllegalArgumentException se a máquina for nula
     */
    public void adicionarMaquina(Maquina maquina) {
        if (maquina == null) {
            throw new IllegalArgumentException("Não é possível adicionar uma máquina nula à linha");
        }

        maquina.setProbabilidadeFalha(maquina.getProbabilidadeFalha() * cenarioAtual.getFatorProbabilidadeFalha());
        maquina.setDesgastePorUso(cenarioAtual.getDesgasteMinimoPorUso(), cenarioAtual.getDesgasteMaximoPorUso());
        maquinas.add(maquina);
    }

    // Repara (restaura a saude) as maquinas que precisam de manutencao, isto e,
    // com saude abaixo do limiar ou quebradas. Retorna quantas foram reparadas
    /**
     * Repara todas as máquinas que precisam de manutenção.
     *
     * @return quantas máquinas foram reparadas (zero se nenhuma precisava)
     */
    public int repararMaquinas() {
        int reparadas = 0;

        for (Maquina maquina : maquinas) {
            if (maquina.precisaManutencao()) {
                maquina.reparar();
                reparadas++;
            }
        }
        return reparadas;
    }

    /**
     * Registra uma demanda pendente, com quantidade zero, para um produto.
     *
     * @param tipoProduto nome do produto, igual ao do catálogo
     * @throws DemandaInvalidaException se o nome for nulo ou vazio, ou se já existir
     *                                  uma demanda para o produto
     */
    public void registrarDemanda(String tipoProduto) {
        if (tipoProduto == null || tipoProduto.isBlank()) {
            throw new DemandaInvalidaException("O tipo de produto da demanda não pode ser vazio");
        }

        if (buscarDemanda(tipoProduto) != null) {
            throw new DemandaInvalidaException("Já existe uma demanda registrada para " + tipoProduto);
        }

        demandas.add(new Demanda(tipoProduto));
    }

    /**
     * Atualiza a quantidade demandada de um produto. Uma demanda concluída ou cancelada
     * é reaberta como pendente.
     *
     * @param tipoProduto    nome do produto
     * @param novaQuantidade nova quantidade de unidades (zero ou mais)
     * @throws DemandaInvalidaException se não houver demanda para o produto, se a
     *                                  quantidade for negativa ou se a demanda estiver
     *                                  em produção
     */
    public void atualizarDemanda(String tipoProduto, int novaQuantidade) {
        obterDemanda(tipoProduto).atualizarQuantidade(novaQuantidade);
    }

    /**
     * Cancela a demanda de um produto.
     *
     * @param tipoProduto nome do produto
     * @throws DemandaInvalidaException se não houver demanda para o produto ou se ela
     *                                  já estiver concluída
     */
    public void cancelarDemanda(String tipoProduto) {
        obterDemanda(tipoProduto).cancelar();
    }

    private Demanda buscarDemanda(String tipoProduto) {
        for (Demanda demanda : demandas) {
            if (demanda.getTipoProduto().equals(tipoProduto)) {
                return demanda;
            }
        }
        return null;
    }

    private Demanda obterDemanda(String tipoProduto) {
        Demanda demanda = buscarDemanda(tipoProduto);

        if (demanda == null) {
            throw new DemandaInvalidaException("Nenhuma demanda registrada para " + tipoProduto);
        }
        return demanda;
    }

    private Produto buscarNoCatalogo(String tipoProduto) {
        for (Produto produto : catalogoProdutos) {
            if (produto.getNome().equals(tipoProduto)) {
                return produto;
            }
        }
        return null;
    }

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
        throw new IllegalStateException("Tipo de produto desconhecido: " + molde.getClass().getSimpleName());
    }

    // ======================================================
    // Validacoes que antecedem a producao (nao alteram estado)
    // ======================================================

    private void validarDemandaParaProducao(Demanda demanda) {
        String tipoProduto = demanda.getTipoProduto();

        switch (demanda.getStatus()) {
            case CONCLUIDA:
                throw new DemandaInvalidaException("A demanda de " + tipoProduto
                        + " já foi atendida. Atualize a quantidade em Demandas para produzir novamente");
            case CANCELADA:
                throw new DemandaInvalidaException("A demanda de " + tipoProduto
                        + " foi cancelada. Atualize a quantidade em Demandas para reativá-la");
            case EM_PRODUCAO:
                throw new DemandaInvalidaException("A demanda de " + tipoProduto + " já está em produção");
            default:
                break;
        }

        if (demanda.getQuantidadeProdutos() <= 0) {
            throw new DemandaInvalidaException("A quantidade demandada de " + tipoProduto
                    + " precisa ser maior que zero. Defina a quantidade em Demandas");
        }
    }

    private void validarLinhaDisponivel(Produto molde) {
        if (maquinas.isEmpty()) {
            throw new MaquinaIndisponivelException("Nenhuma máquina cadastrada na linha de produção");
        }

        if (!esteira.verificarCapacidade(molde.getMassa())) {
            throw new MaquinaIndisponivelException("O produto " + molde.getNome() + " (massa "
                    + molde.getMassa() + " kg) excede a capacidade da esteira ("
                    + esteira.getCapacidadeMaxima() + " kg)");
        }

        for (Maquina maquina : maquinas) {
            if (maquina.estaQuebrada()) {
                throw new MaquinaIndisponivelException("A " + maquina.getNome()
                        + " está quebrada e precisa de reparo antes de iniciar a produção."
                        + " Use a opção Reparar maquinas no menu Auditoria");
            }

            if (molde.getMassa() > maquina.getCapacidadeMaxima()) {
                throw new MaquinaIndisponivelException("O produto " + molde.getNome() + " (massa "
                        + molde.getMassa() + " kg) excede a capacidade da " + maquina.getNome()
                        + " (" + maquina.getCapacidadeMaxima() + " kg)");
            }
        }
    }

    private void validarRecursos(double materiaPrimaNecessaria, double custoOperacaoTotal) {
        if (!materiaPrima.atendeLoteMinimo(materiaPrimaNecessaria)) {
            throw new QuantidadeInvalidaException("A matéria-prima necessária (" + materiaPrimaNecessaria
                    + ") está abaixo do lote mínimo de consumo (" + materiaPrima.getQuantidadeMinima() + ")");
        }

        if (!materiaPrima.verificarDisponibilidade(materiaPrimaNecessaria)) {
            throw new RecursoInsuficienteException(materiaPrima.getNome(), materiaPrimaNecessaria,
                    materiaPrima.getQuantidade());
        }

        if (budget < custoOperacaoTotal) {
            throw new RecursoInsuficienteException("Budget", custoOperacaoTotal, budget);
        }
    }

    /**
     * Fabrica o lote inteiro de uma demanda. Primeiro valida tudo (demanda, catálogo,
     * linha de produção, matéria-prima e budget); só depois consome estoque, debita o
     * budget e muda o status da demanda. Assim, um erro de validação nunca deixa a
     * planta parcialmente alterada.
     *
     * <p>Durante a fabricação, cada unidade percorre a esteira e as máquinas. Unidades
     * que travam por uma máquina quebrada são contadas como perdidas, e as reprovadas na
     * inspeção como rejeitadas; o lote continua e a demanda termina concluída. As
     * unidades aprovadas vão para o armazém.</p>
     *
     * @param tipoProduto nome do produto cuja demanda será produzida
     * @throws DemandaInvalidaException     se não houver demanda para o produto, se ela
     *                                      não estiver pendente, se a quantidade for zero
     *                                      ou se o produto não estiver no catálogo
     * @throws MaquinaIndisponivelException se não houver máquinas, se o produto exceder a
     *                                      capacidade da esteira ou de alguma máquina, ou
     *                                      se alguma máquina estiver quebrada
     * @throws QuantidadeInvalidaException  se a matéria-prima necessária estiver abaixo
     *                                      do lote mínimo de consumo
     * @throws RecursoInsuficienteException se faltar matéria-prima ou budget
     */
    public void fabricarDemanda(String tipoProduto) {
        Demanda demanda = obterDemanda(tipoProduto);
        validarDemandaParaProducao(demanda);

        Produto molde = buscarNoCatalogo(tipoProduto);

        if (molde == null) {
            throw new DemandaInvalidaException(tipoProduto + " não está cadastrado no catálogo de produtos");
        }

        double materiaPrimaNecessaria = demanda.calcularMateriaPrimaNecessaria(molde);
        double custoOperacaoTotal = calcularCustoProducao(demanda.getQuantidadeProdutos());

        validarLinhaDisponivel(molde);
        validarRecursos(materiaPrimaNecessaria, custoOperacaoTotal);

        System.out.println("\n==================================================");
        System.out.println("INICIANDO PRODUCAO DE " + demanda.getQuantidadeProdutos()
                + " UNIDADE(S) DE " + tipoProduto.toUpperCase());
        System.out.println("==================================================");

        System.out.println("Checando estoque de " + materiaPrima.getNome() + ": "
                + materiaPrima.getQuantidade() + " " + materiaPrima.getUnidade() + "(s)");
        pausar(100);

        System.out.println("Checando budget disponivel: R$" + String.format("%.2f", budget));
        pausar(100);

        System.out.println("Estoque e budget suficientes. Consumindo " + materiaPrimaNecessaria + " "
                + materiaPrima.getUnidade() + "(s) de " + materiaPrima.getNome());
        materiaPrima.consumir(materiaPrimaNecessaria);
        budget -= custoOperacaoTotal;
        demanda.iniciarProducao();
        pausar(100);

        System.out.println("Reservando R$" + String.format("%.2f", custoOperacaoTotal)
                + " do budget para custo de operacao do lote");
        pausar(100);

        int unidadesPerdidasNaLinha = 0;
        int unidadesRejeitadasNaInspecao = 0;

        for (int unidade = 0; unidade < demanda.getQuantidadeProdutos(); unidade++) {
            System.out.println("\n--- Fabricando unidade " + (unidade + 1) + "/"
                    + demanda.getQuantidadeProdutos() + " de " + tipoProduto + " ---");

            Produto produtoAtual = criarUnidadeDoMolde(molde);

            try {
                processarNasMaquinas(produtoAtual);
            } catch (MaquinaIndisponivelException e) {
                System.out.println("[AVISO] Unidade perdida na linha: " + e.getMessage());
                unidadesPerdidasNaLinha++;
                continue;
            }

            if (produtoAtual.getStatus() == StatusProduto.INSPECIONADO) {
                System.out.println(produtoAtual.getNome() + " (" + produtoAtual.getId()
                        + ") aprovado na inspecao. Enviando para o armazem.");
                produtosFabricados.add(produtoAtual);
            } else if (produtoAtual.getStatus() == StatusProduto.REJEITADO) {
                System.out.println("[AVISO] " + produtoAtual.getNome() + " (" + produtoAtual.getId()
                        + ") rejeitado na inspecao.");
                unidadesRejeitadasNaInspecao++;
            } else {
                System.out.println("[AVISO] " + produtoAtual.getNome() + " (" + produtoAtual.getId()
                        + ") nao completou a linha (status: " + produtoAtual.getStatus() + ").");
                unidadesRejeitadasNaInspecao++;
            }
        }

        demanda.concluir();

        int unidadesProduzidas = demanda.getQuantidadeProdutos() - unidadesPerdidasNaLinha
                - unidadesRejeitadasNaInspecao;

        System.out.println("\n==================================================");
        System.out.println("[OK] " +
                unidadesProduzidas + " unidade(s) de " + tipoProduto + " fabricada(s) com sucesso");

        if (unidadesPerdidasNaLinha > 0) {
            System.out.println("[AVISO] " + unidadesPerdidasNaLinha
                    + " unidade(s) travou/travaram na linha e foram perdidas");
        }

        if (unidadesRejeitadasNaInspecao > 0) {
            System.out.println("[AVISO] " + unidadesRejeitadasNaInspecao
                    + " unidade(s) foram rejeitadas na inspecao");
        }
        System.out.println("==================================================");
    }

    // Lanca MaquinaIndisponivelException se a esteira ou alguma maquina nao
    // puder atender o produto (ex.: quebra no meio do lote). O finally garante
    // que maquina e esteira terminem desligadas mesmo quando ha erro
    private void processarNasMaquinas(Produto produto) {
        try {
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

                esteira.adicionarItem(produto);

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

                try {
                    System.out.println("Realizando processamento (" + maquina.getTipo() + ")...");
                    pausar(calcularDuracaoProcessamento(produto));
                    maquina.processar(transportado);
                    maquina.desgastar();
                } finally {
                    maquina.desligar();
                }

                System.out.println(produto.getNome() + " -> status: " + transportado.getStatus());
                System.out.println("Concluido. Desligando " + maquina.getNome() + "...");
                pausar(100);
            }
        } finally {
            esteira.desligar();
        }
    }

    // Custo de operacao (soma de todas as maquinas da linha) para produzir 1
    // unidade, usado tanto para orcar um lote (calcularCustoProducao) quanto
    // para converter o budget em "unidades que cabem no orcamento" na hora de
    // escolher a proxima demanda (calcularOrcamentoEmUnidades)
    private double calcularCustoOperacaoPorUnidade() {
        double custoPorUnidade = 0;

        for (Maquina maquina : maquinas) {
            custoPorUnidade += maquina.getCustoOperacao();
        }
        return custoPorUnidade;
    }

    private double calcularCustoProducao(int quantidadeUnidades) {
        return calcularCustoOperacaoPorUnidade() * quantidadeUnidades;
    }

    private double calcularOrcamentoEmUnidades() {
        double custoPorUnidade = calcularCustoOperacaoPorUnidade();

        if (custoPorUnidade <= 0) {
            // sem maquinas cadastradas ainda: nao ha custo por unidade, entao
            // nada limita a quantidade produzivel pelo orcamento
            return Double.MAX_VALUE;
        }
        return budget / custoPorUnidade;
    }

    /**
     * Compra matéria-prima, debitando o custo do budget. Valida a quantidade e o budget
     * antes de mexer em estoque ou budget.
     *
     * @param quantidade quantidade a comprar
     * @throws QuantidadeInvalidaException  se a quantidade for menor ou igual a zero ou
     *                                      estiver abaixo do lote mínimo de compra
     * @throws RecursoInsuficienteException se o budget não cobrir o custo da compra
     */
    public void comprarMateriaPrima(double quantidade) {
        if (quantidade <= 0) {
            throw new QuantidadeInvalidaException("A quantidade a comprar precisa ser maior que zero");
        }

        if (!materiaPrima.atendeLoteMinimo(quantidade)) {
            throw new QuantidadeInvalidaException("A quantidade (" + quantidade
                    + ") está abaixo do lote mínimo de compra (" + materiaPrima.getQuantidadeMinima() + ")");
        }

        double custoTotal = quantidade * materiaPrima.getCustoPorUnidade();

        if (budget < custoTotal) {
            throw new RecursoInsuficienteException("Budget", custoTotal, budget);
        }

        materiaPrima.adicionarEstoque(quantidade);
        budget -= custoTotal;
    }

    /**
     * Imprime o budget atual no terminal.
     */
    public void exibirBudget() {
        System.out.printf("Budget atual: R$%.2f\n", budget);
    }

    /**
     * Imprime o armazém: a quantidade em estoque por produto e o detalhamento de cada
     * unidade (lote, categoria, tier, qualidade e situação de risco).
     */
    public void exibirArmazem() {
        if (produtosFabricados.isEmpty()) {
            System.out.println("Armazem vazio.");
            return;
        }

        List<String> nomesContados = new ArrayList<>();

        System.out.println("Quantidade em estoque por produto:");

        for (Produto produto : produtosFabricados) {
            if (nomesContados.contains(produto.getNome())) {
                continue;
            }
            nomesContados.add(produto.getNome());

            int quantidade = 0;

            for (Produto outro : produtosFabricados) {
                if (outro.getNome().equals(produto.getNome())) {
                    quantidade++;
                }
            }
            System.out.println(" - " + produto.getNome() + ": " + quantidade + " unidade(s)");
        }

        System.out.println("\nDetalhamento por unidade (lote):");
        for (Produto produto : produtosFabricados) {
            String statusRisco = "OK";

            if (produto.precisaManutencao()) {
                statusRisco = "RISCO";
            }

            System.out.println(" - " + produto.getNome()
                    + " | lote: " + produto.getId()
                    + " | " + produto.getCategoria().getNomeExibicao()
                    + " | tier: " + produto.getTipo()
                    + " | qualidade: " + produto.getQualidade()
                    + " | risco: " + statusRisco);
        }
    }

    // Getters

    /**
     * Retorna o budget atual.
     *
     * @return budget, em reais
     */
    public double getBudget() {
        return budget;
    }

    /**
     * Retorna o cenário ativo.
     *
     * @return cenário escolhido no início da execução
     */
    public Cenario getCenarioAtual() {
        return cenarioAtual;
    }

    /**
     * Retorna a estratégia de produção ativa.
     *
     * @return estratégia atual
     */
    public EstrategiaProducao getEstrategiaAtual() {
        return estrategiaAtual;
    }

    /**
     * Retorna as máquinas da linha, na ordem de processamento.
     *
     * @return lista de máquinas
     */
    public List<Maquina> getMaquinas() {
        return maquinas;
    }

    /**
     * Retorna os produtos acabados e aprovados que estão no armazém.
     *
     * @return lista de produtos do armazém
     */
    public List<Produto> getArmazem() {
        return produtosFabricados;
    }

    /**
     * Retorna todas as demandas registradas.
     *
     * @return lista de demandas
     */
    public List<Demanda> getDemandas() {
        return demandas;
    }

    /**
     * Retorna o catálogo de produtos que a fábrica sabe fabricar.
     *
     * @return lista de produtos do catálogo
     */
    public List<Produto> getCatalogoProdutos() {
        return catalogoProdutos;
    }

    /**
     * Retorna o estoque de matéria-prima da fábrica.
     *
     * @return a matéria-prima
     */
    public MateriaPrima getMateriaPrima() {
        return materiaPrima;
    }
}
