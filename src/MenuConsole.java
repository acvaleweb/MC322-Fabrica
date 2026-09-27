import java.util.Scanner;

public class MenuConsole {
    private static final int LARGURA_TELA = 54;
    private final Scanner scanner;
<<<<<<< Updated upstream
    private final MateriaPrima gpu;
    private final Produto placaVideoLow;
    private final Produto placaVideoMid;
    private final Produto placaVideoHigh;
    private final Maquina maquinaSMT;
    private final Esteira esteiraProducao;
    private final EstacaoInspecao estacaoInspecao;
=======
    private final GerenciadorProducao gerenciador;
    private final List<Produto> catalogo;
    private final MateriaPrima materiaPrima;
    private final Cenario cenarioAtivo;
>>>>>>> Stashed changes

    public MenuConsole() {

        scanner = new Scanner(System.in);

<<<<<<< Updated upstream
        gpu = new MateriaPrima(
                "MP-GPU-001",
                "Andesite 5nm 3840C 2.5GHz",
=======
        cabecalho("ANDESITE HARDWARE CO.");
        titulo("\"Silício, solda e ambição\"");
        linhaFina();
        cenarioAtivo = escolherCenario();

        materiaPrima = new MateriaPrima(
                "MP-WAF-001",
                "Wafer de Silício Andesite 5nm",
>>>>>>> Stashed changes
                10,
                "unidade",
                50.0,
                1);

        placaVideoHigh = new Produto(
                "PROD-VGA-003",
                "Andesite A9000",
                3,
                3.5);

        placaVideoMid = new Produto(
                "PROD-VGA-002",
                "Andesite A8000",
                2,
                2.5);

        placaVideoLow = new Produto(
                "PROD-VGA-001",
                "Andesite A7000",
                1,
                1.5);

        maquinaSMT = new Maquina("Soldadora SMT-01", 5);

<<<<<<< Updated upstream
        esteiraProducao = new Esteira(5.5);

        estacaoInspecao = new EstacaoInspecao();
=======
        gerenciador = new GerenciadorProducao(materiaPrima, cenarioAtivo.getBudgetInicial(), 5.5,
                (ArrayList<Produto>) catalogo);

        // Ordem de montagem: insersora -> montadora -> inspetora
        // As probabilidades de falha base e o desgaste por uso sao ajustados pelo cenario escolhido
        InsersoraSMT insersora = new InsersoraSMT("Insersora SMT-01", 5, 0.10 * cenarioAtivo.getFatorFalha(), 15.0);
        Montadora montadora = new Montadora("Montadora MT-01", 5, 0.15 * cenarioAtivo.getFatorFalha(), 10.0);
        Inspetora inspetora = new Inspetora("Inspetora INS-01", 5, 0.05 * cenarioAtivo.getFatorFalha(), 8.0);

        insersora.setFatorDesgaste(cenarioAtivo.getFatorDesgaste());
        montadora.setFatorDesgaste(cenarioAtivo.getFatorDesgaste());
        inspetora.setFatorDesgaste(cenarioAtivo.getFatorDesgaste());

        gerenciador.adicionarMaquina(insersora);
        gerenciador.adicionarMaquina(montadora);
        gerenciador.adicionarMaquina(inspetora);

        for (Produto produto : catalogo) {
            gerenciador.registrarDemanda(produto.getNome());
        }

        ok("Cenario " + cenarioAtivo.getNomeExibicao() + " carregado. Bem-vindo a producao!");
    }

    // ======================================================
    // Selecao de cenario (executada uma vez, no boot da fabrica)
    // ======================================================

    private Cenario escolherCenario() {
        titulo("Selecione o cenario de operação da fábrica");
        linhaFina();
        System.out.println(" 1 - Ideal ");
        System.out.println(" 2 - Apocalíptico ");
        linhaFina();

        while (true) {
            String opcao = lerEntrada();
            if (opcao.equals("1")) {
                return Cenario.IDEAL;
            }
            if (opcao.equals("2")) {
                return Cenario.APOCALIPTICO;
            }
            erro("Opção inválida. Escolha 1 ou 2");
        }
>>>>>>> Stashed changes
    }

    // ======================================================
    // Utilidades de formatação de tela
    // ======================================================

    private void pausar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String repetir(String s, int vezes) {
        return s.repeat(Math.max(vezes, 0));
    }

    private void linha() {
        System.out.println(repetir("=", LARGURA_TELA));
    }

    private void linhaFina() {
        System.out.println(repetir("-", LARGURA_TELA));
    }

    /* Centraliza um texto */
    private void titulo(String texto) {
        int espacos = Math.max((LARGURA_TELA - texto.length()) / 2, 0);
        System.out.println(repetir(" ", espacos) + texto);
    }

    private void cabecalho(String texto) {
        linha();
        titulo(texto);
        linha();
    }

    private void ok(String mensagem) {
        System.out.println("[OK]   " + mensagem);
    }

    private void erro(String mensagem) {
        System.out.println("[ERRO] " + mensagem);
    }

    private void aviso(String mensagem) {
        System.out.println("[AVISO] " + mensagem);
    }

    private void info(String mensagem) {
        System.out.println("       " + mensagem);
    }

    private String descreverStatus(StatusProduto status) {
        return switch (status) {
            case AGUARDANDO_PROCESSAMENTO -> "Aguardando processamento";
            case PROCESSADO -> "Processado";
            case INSPECIONADO -> "Inspecionado e aprovado";
        };
    }

    // ======================================================
    // Telas
    // ======================================================

    private void exibirIntroducao() {
        cabecalho("ANDESITE HARDWARE CO.");
        titulo("\"Silício, solda e ambição\"");
        linha();
        System.out.println();
        System.out.println("Bem-vindos à nossa fábrica automatizada de hardware!");
        System.out.println("Componentes passam pela esteira, são montados nas nossas");
        System.out.println("maquinas e inspecionados antes de saírem para o mercado");
        System.out.println();
        System.out.println("Matéria-prima principal: " + gpu.getNome());
        System.out.println("Linha de produção atual: placas de vídeo Andesite");
        System.out.println();
        System.out.println("Desenvolvido por: João Victor de Oliveira Viegas e NOME_2");
        System.out.println();
        linha();
        System.out.println("O que você deseja fazer?");
        System.out.println(" [1] Consultar estoque");
        System.out.println(" [2] Comprar matéria-prima");
        System.out.println(" [3] Fabricar produtos");
        System.out.println(" [0] Fechar programa");
        linhaFina();
    }

    private void exibirInventario() {
        cabecalho("INVENTÁRIO DA FÁBRICA");
        System.out.println("Produtos:");
        exibirLinhaProduto(placaVideoLow);
        exibirLinhaProduto(placaVideoMid);
        exibirLinhaProduto(placaVideoHigh);
        linhaFina();
        System.out.println("Matérias-primas em estoque:");
        System.out.println(" - " + gpu.getNome());
        System.out.println("   Quantidade: " + gpu.getQuantidade() + " " + gpu.getUnidade() + "(s)");
        System.out.println("   Mínimo para produção: " + gpu.getQuantidadeMinima() + " " + gpu.getUnidade() + "(s)");
        linha();
        System.out.println(" [0] Voltar ao menu principal");
    }

    private void exibirLinhaProduto(Produto produto) {
        System.out.println(" - " + produto.getNome() + " (" + produto.getId() + ")");
        System.out.println("   Status: " + descreverStatus(produto.getStatus()));
        System.out.println("   Demanda de GPU: " + produto.getDemandaMateriaPrima() + " " + gpu.getUnidade() + "(s)");
        System.out.println();
    }

    private void exibirMenuCompra() {
        cabecalho("COMPRAR MATÉRIA-PRIMA");
        System.out.println("Fornecedor: Andesite Foundries Ltda");
        linhaFina();
        System.out.println("- " + gpu.getNome());
        System.out.println("  Estoque atual: " + gpu.getQuantidade() + " " + gpu.getUnidade());
        System.out.println("  Lote mínimo para compra: " + gpu.getQuantidadeMinima() + " " + gpu.getUnidade());
    }

    private void exibirMenuFabricar() {
        cabecalho("LINHA DE PRODUÇÃO");
        System.out.println("Linha ativa no momento: placas de vídeo Andesite");
        System.out.println("Escolha qual produto será fabricado:");
        linhaFina();
        exibirLinhaProduto(placaVideoLow);
        exibirLinhaProduto(placaVideoMid);
        exibirLinhaProduto(placaVideoHigh);
    }

    // ======================================================
    // Leitura de entrada (somente numérica, com validação)
    // ======================================================

    private String lerEntrada() {
        System.out.print("\nEscolha uma opção: ");
        return scanner.nextLine().trim();
    }

    private double lerEntradaDouble(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = scanner.nextLine().trim().replace(",", ".");
            try {
                return Double.parseDouble(entrada);
            } catch (NumberFormatException e) {
                erro("Entrada inválida. Digite apenas números (ex: 2 ou 2.5)");
            }
        }
    }

    // ======================================================
    // Fluxo: consulta de estoque
    // ======================================================

<<<<<<< Updated upstream
    public void iniciarMenuInventario() {
        exibirInventario();
        lerEntrada(); // aguarda o usuário confirmar para voltar
=======
    private List<Produto> produtosDaCategoria(CategoriaProduto categoria) {
        List<Produto> resultado = new ArrayList<>();
        for (Produto produto : catalogo) {
            if (produto.getCategoria() == categoria) {
                resultado.add(produto);
            }
        }
        return resultado;
    }

    // ======================================================
    // Tela principal
    // ======================================================

    private void exibirMenuPrincipal() {
        cabecalho("ANDESITE HARDWARE CO.");
        titulo("\"Silício, solda e ambição\"");
        linha();
        System.out.println("ESTRATÉGIA ATUAL: " + gerenciador.getEstrategiaAtual().getNomeEstrategia());
        System.out.println("CENÁRIO ATIVO:    " + cenarioAtivo.getNomeExibicao());
        System.out.printf("BUDGET ATUAL:     R$%.2f%n", gerenciador.getBudget());
        linhaFina();

        System.out.println("LINHAS DE PRODUÇÃO");
        System.out.println(" 1 - Placas de Vídeo (Andesite)");
        System.out.println(" 2 - Processadores (Basalt)");
        System.out.println(" 3 - Placas-Mãe (Granite)");
        System.out.println();

        System.out.println("PRODUÇÃO AUTOMÁTICA");
        System.out.println(" 4 - Processar próxima demanda (usa a estratégia ativa)");
        System.out.println();

        System.out.println("CONSULTAR");
        System.out.println(" 5 - Ver armazém");
        System.out.println(" 6 - Ver estoque de matéria-prima");
        System.out.println(" 7 - Ver demandas");
        System.out.println();

        System.out.println("COMPRAR MATÉRIA-PRIMA");
        System.out.println(" 8 - Comprar " + materiaPrima.getNome());
        System.out.println();

        System.out.println("GERENCIAMENTO");
        System.out.println(" 9  - Alterar estratégia de produção");
        System.out.println(" 10 - Auditoria da planta (Auditável)");
        System.out.println();

        System.out.println(" 0 - SAIR");
        linhaFina();
    }

    // ======================================================
    // Produção automática e gerenciamento de estratégia
    // ======================================================

    private void iniciarProducaoAutomatica() {
        cabecalho("PRODUÇÃO AUTOMÁTICA");
        info("Estratégia ativa: " + gerenciador.getEstrategiaAtual().getNomeEstrategia());
        linhaFina();
        gerenciador.executarProximaProducao();
        info("Budget restante: R$" + String.format("%.2f", gerenciador.getBudget()));
    }

    private void iniciarMenuEstrategia() {
        while (true) {
            cabecalho("ESTRATÉGIA DE PRODUÇÃO");
            info("Estratégia atual: " + gerenciador.getEstrategiaAtual().getNomeEstrategia());
            linhaFina();
            System.out.println(" 1 - Ordem de Chegada (fila FIFO)");
            System.out.println(" 2 - Maior Demanda (prioriza o maior lote)");
            System.out.println(" 3 - Máximo de Produtos (maximiza rendimento sob orçamento)");
            System.out.println(" 0 - Voltar ao menu principal");
            linhaFina();

            String opcao = lerEntrada();
            switch (opcao) {
                case "1":
                    gerenciador.setEstrategia(new EstrategiaOrdemChegada());
                    ok("Estratégia alterada para Ordem de Chegada");
                    break;
                case "2":
                    gerenciador.setEstrategia(new EstrategiaMaiorDemanda());
                    ok("Estratégia alterada para Maior Demanda");
                    break;
                case "3":
                    gerenciador.setEstrategia(new EstrategiaMaximoProdutos());
                    ok("Estratégia alterada para Máximo de Produtos");
                    break;
                case "0":
                    return;
                default:
                    erro("Opção inválida. Escolha um número do menu");
            }
        }
    }

    // ======================================================
    // Auditoria da planta 
    // ======================================================

    private void iniciarAuditoria() {
        cabecalho("AUDITORIA DA PLANTA");
        gerenciador.gerarAuditoriaGeral();
    }

    // ======================================================
    // Submenu de uma linha de produto (categoria)
    // ======================================================

    private void iniciarMenuCategoria(CategoriaProduto categoria) {
        List<Produto> produtos = produtosDaCategoria(categoria);
        int n = produtos.size();

        while (true) {
            cabecalho(("LINHA " + categoria.getNomeExibicao()).toUpperCase());
            System.out.printf("BUDGET ATUAL: R$%.2f%n", gerenciador.getBudget());
            linhaFina();

            System.out.println("ATUALIZAR DEMANDAS");
            for (int i = 0; i < n; i++) {
                System.out.println(" " + (i + 1) + " - Atualizar demanda de " + produtos.get(i).getNome());
            }
            System.out.println();

            System.out.println("FABRICAR");
            for (int i = 0; i < n; i++) {
                System.out.println(" " + (n + i + 1) + " - Fabricar " + produtos.get(i).getNome());
            }
            System.out.println();

            System.out.println(" 0 - Voltar ao menu principal");
            linhaFina();

            String opcao = lerEntrada();

            if (opcao.equals("0")) {
                return;
            }

            int escolha;
            try {
                escolha = Integer.parseInt(opcao);
            } catch (NumberFormatException e) {
                erro("Opção inválida. Escolha um número do menu");
                continue;
            }

            if (escolha >= 1 && escolha <= n) {
                iniciarMenuAtualizarDemanda(produtos.get(escolha - 1));
            } else if (escolha >= n + 1 && escolha <= 2 * n) {
                iniciarMenuFabricar(produtos.get(escolha - n - 1));
            } else {
                erro("Opção inválida. Escolha um número do menu");
            }
        }
    }

    // ======================================================
    // Fluxo: atualizar demanda
    // ======================================================

    private void iniciarMenuAtualizarDemanda(Produto produto) {
        cabecalho("ATUALIZAR DEMANDA: " + produto.getNome());
        int novaQuantidade = lerEntradaInt("Quantas unidades de " + produto.getNome() + " deseja demandar? ");

        if (!gerenciador.atualizarDemanda(produto.getNome(), novaQuantidade)) {
            erro("Não foi possível atualizar a demanda. Verifique se a quantidade é válida");
            return;
        }

        ok("Demanda de " + produto.getNome() + " atualizada para " + novaQuantidade + " unidade(s)");
    }

    // ======================================================
    // Fluxo: fabricação
    // ======================================================

    private void iniciarMenuFabricar(Produto produto) {
        cabecalho("FABRICANDO: " + produto.getNome());
        // fabricarDemanda já imprime seus próprios status de [OK]/[ERRO]/[AVISO]
        gerenciador.fabricarDemanda(produto.getNome());
        info("Budget restante: R$" + String.format("%.2f", gerenciador.getBudget()));
    }

    // ======================================================
    // Fluxo: consultas
    // ======================================================

    private void exibirTelaArmazem() {
        cabecalho("ARMAZÉM DA FÁBRICA");
        gerenciador.exibirArmazem();
        linhaFina();
    }

    private void exibirTelaEstoque() {
        cabecalho("ESTOQUE DE MATÉRIA-PRIMA");
        System.out.println(" - " + materiaPrima.getNome() + " (" + materiaPrima.getId() + ")");
        System.out.println("   Quantidade em estoque: " + materiaPrima.getQuantidade() + " "
                + materiaPrima.getUnidade() + "(s)");
        System.out.println("   Lote mínimo de compra: " + materiaPrima.getQuantidadeMinima() + " "
                + materiaPrima.getUnidade() + "(s)");
        System.out.println("   Custo por unidade: R$" + String.format("%.2f", materiaPrima.getCustoPorUnidade()));
        linhaFina();
    }

    private void exibirTelaDemandas() {
        cabecalho("DEMANDAS REGISTRADAS");
        List<Demanda> demandas = gerenciador.getDemandas();

        if (demandas.isEmpty()) {
            System.out.println("Nenhuma demanda registrada.");
        }

        for (Demanda demanda : demandas) {
            System.out.println(" - " + demanda.getTipoProduto()
                    + " | quantidade: " + demanda.getQuantidadeProdutos()
                    + " | status: " + demanda.getStatus().getDescricao());
        }
        linhaFina();
>>>>>>> Stashed changes
    }

    // ======================================================
    // Fluxo: compra de matéria-prima
    // ======================================================

    public void iniciarMenuCompra() {
        exibirMenuCompra();
        System.out.println("\nO que você deseja fazer?");
        System.out.println(" [1] Comprar " + gpu.getNome());
        System.out.println(" [0] Voltar");

        switch (lerEntrada()) {
            case "1":
                processarCompra();
                break;
            case "0":
                return;
            default:
                erro("Opção inválida");
        }
    }

    private void processarCompra() {
        boolean continuarComprando = true;

        while (continuarComprando) {
            System.out.println();
            System.out.println(
                    "Quantidade mínima por lote: " + gpu.getQuantidadeMinima() + " " + gpu.getUnidade() + "(s)");
            double quantidade = lerEntradaDouble("Quantas unidades deseja comprar? (0 para cancelar): ");

            if (quantidade == 0) {
                info("Compra cancelada");
                return;
            }

            if (quantidade < 0) {
                erro("Não é possível comprar uma quantidade negativa");
                continue;
            }

            if (quantidade < gpu.getQuantidadeMinima()) {
                erro("O fornecedor só vende em lotes de no mínimo "
                        + gpu.getQuantidadeMinima() + " " + gpu.getUnidade() + "(s)");
                continue;
            }

            gpu.adicionarEstoque(quantidade);
            ok("Compra realizada com sucesso!");
            info("Estoque atual de " + gpu.getNome() + ": " + gpu.getQuantidade() + " " + gpu.getUnidade());

            System.out.println();
            System.out.println(" [1] Comprar novamente");
            System.out.println(" [0] Voltar ao menu principal");

            String opcao = lerEntrada();
            continuarComprando = opcao.equals("1");
        }
    }

    // ======================================================
    // Fluxo: produção
    // ======================================================

    public void iniciarMenuProducao() {
        exibirMenuFabricar();

        System.out.println("\nO que você deseja fazer?");
        System.out.println(" [1] Fabricar " + placaVideoLow.getNome());
        System.out.println(" [2] Fabricar " + placaVideoMid.getNome());
        System.out.println(" [3] Fabricar " + placaVideoHigh.getNome());
        System.out.println(" [0] Voltar");

        Produto produtoEscolhido;

        switch (lerEntrada()) {
            case "1":
                produtoEscolhido = placaVideoLow;
                break;
            case "2":
                produtoEscolhido = placaVideoMid;
                break;
            case "3":
                produtoEscolhido = placaVideoHigh;
                break;
            case "0":
                return;
            default:
                erro("Opção inválida");
                return;
        }

        definirDemandaEProduzir(produtoEscolhido);
    }

    private void definirDemandaEProduzir(Produto produto) {
        System.out.println();
        linhaFina();
        System.out.println("Produto selecionado: " + produto.getNome());
        System.out.println("Demanda padrão de " + gpu.getNome() + ": "
                + produto.getDemandaMateriaPrima() + " " + gpu.getUnidade() + "(s)");
        System.out.println();
        System.out.println(" [1] Usar demanda padrão");
        System.out.println(" [2] Definir outra quantidade de matéria-prima");
        System.out.println(" [0] Cancelar e voltar");

        switch (lerEntrada()) {
            case "1":
                fabricarProduto(produto);
                break;

            case "2":
                double novaDemanda = lerEntradaDouble("Informe a nova demanda de matéria-prima: ");

                if (!produto.definirDemandaMateriaPrima(novaDemanda)) {
                    erro("A demanda precisa ser maior que zero. Produção cancelada");
                    return;
                }

                fabricarProduto(produto);
                break;

            case "0":
                info("Produção cancelada");
                break;

            default:
                erro("Opção inválida");
        }
    }

    // Executa o fluxo completo da linha de produção:
    // estoque -> esteira -> máquina -> esteira -> inspeção -> produto final

    private void fabricarProduto(Produto produto) {
        cabecalho("INICIANDO PRODUÇÃO: " + produto.getNome());
        double demanda = produto.getDemandaMateriaPrima();

        // 1) Verifica estoque de matéria-prima
        System.out.println("Checando estoque de " + gpu.getNome() + "...");
        info("Demanda: " + demanda + " " + gpu.getUnidade() + "(s) | Estoque atual: " + gpu.getQuantidade() + " "
                + gpu.getUnidade() + "(s)");

        pausar(80);

        if (!gpu.verificarDisponibilidade(demanda)) {
            erro("Estoque insuficiente de " + gpu.getNome() + ". Produção cancelada");
            aviso("Compre mais matéria-prima no menu principal e tente novamente");
            return;
        }

        ok("Estoque suficiente para atender a demanda");
        pausar(80);

        // 2) Liga a esteira e coloca a matéria-prima
        if (esteiraProducao.estaEmMovimento()) {
            esteiraProducao.desligar();
        }

        esteiraProducao.ligar();
        ok("Esteira ligada");
        pausar(80);

        if (!esteiraProducao.adicionarItem(gpu, demanda)) {
            erro("Não foi possível colocar " + gpu.getNome() + " na esteira");
            aviso("Verifique se a quantidade não excede a capacidade da esteira");
            esteiraProducao.desligar();
            return;
        }

        ok(gpu.getNome() + " colocado(a) na esteira (" + esteiraProducao.getQuantidade() + " " + gpu.getUnidade()
                + ")");
        pausar(100);

        // 3) Transporta até a máquina e retira da esteira
        System.out.println("Transportando matéria-prima até a " + maquinaSMT.getNome() + "...");
        pausar(120);

        MateriaPrima materiaTransportada = esteiraProducao.removerMateriaPrima();
        esteiraProducao.desligar();

        ok("Matéria-prima chegou à máquina. Esteira desligada");
        pausar(80);

        // 4) Liga a máquina e processa (a própria Maquina consome o estoque)
        maquinaSMT.ligar();
        ok(maquinaSMT.getNome() + " ligada");

        System.out.println("Processando " + demanda + " " + gpu.getUnidade() + "(s) de " + gpu.getNome() + "...");
        pausar(150);

        boolean processado = maquinaSMT.processar(materiaTransportada, produto);
        maquinaSMT.desligar();

        if (!processado) {
            erro("Falha no processamento. Verifique a capacidade da máquina ou o estoque");
            aviso(maquinaSMT.getNome() + " desligada por segurança");
            return;
        }

        ok(produto.getNome() + " fabricado(a) com sucesso! Status: " + descreverStatus(produto.getStatus()));
        info("Novo estoque de " + gpu.getNome() + ": " + gpu.getQuantidade() + " " + gpu.getUnidade() + "(s)");
        pausar(80);

        // 5) Transporta o produto até a inspeção
        esteiraProducao.ligar();

        ok("Esteira religada para transportar o produto");
        pausar(80);

        if (!esteiraProducao.adicionarItem(produto)) {
            erro("Não foi possível colocar " + produto.getNome() + " na esteira");
            esteiraProducao.desligar();
            return;
        }

        System.out.println("Transportando " + produto.getNome() + " até a estação de inspeção...");
        pausar(120);

        Produto produtoInspecionar = esteiraProducao.removerProduto();
        esteiraProducao.desligar();
        ok("Produto chegou na inspeção. Esteira desligada");
        pausar(80);

        // 6) Inspeção final
        estacaoInspecao.ativar();
        ok("Estação de inspeção ativada");

        boolean aprovado = estacaoInspecao.inspecionar(produtoInspecionar);
        estacaoInspecao.desativar();

        if (!aprovado) {
            erro("O produto não passou na inspeção");
            return;
        }

        linha();
        titulo("PRODUÇÃO CONCLUÍDA COM SUCESSO");
        linha();
        System.out.println(produto.getNome() + " está " + descreverStatus(produto.getStatus()).toLowerCase() + "!");
        System.out.println("Total de produtos inspecionados até agora: " + estacaoInspecao.getTotalInspecionados());
        System.out
                .println("Estoque restante de " + gpu.getNome() + ": " + gpu.getQuantidade() + " " + gpu.getUnidade());
        linhaFina();
    }

    // ======================================================
    // Loop principal
    // ======================================================

    public void iniciar() {
        while (true) {
            exibirIntroducao();

            switch (lerEntrada()) {
                case "1":
                    iniciarMenuInventario();
                    break;

                case "2":
<<<<<<< Updated upstream
                    iniciarMenuCompra();
                    break;

                case "3":
                    iniciarMenuProducao();
                    break;

=======
                    iniciarMenuCategoria(CategoriaProduto.PROCESSADOR);
                    break;
                case "3":
                    iniciarMenuCategoria(CategoriaProduto.PLACA_MAE);
                    break;
                case "4":
                    iniciarProducaoAutomatica();
                    break;
                case "5":
                    exibirTelaArmazem();
                    break;
                case "6":
                    exibirTelaEstoque();
                    break;
                case "7":
                    exibirTelaDemandas();
                    break;
                case "8":
                    iniciarMenuCompra();
                    break;
                case "9":
                    iniciarMenuEstrategia();
                    break;
                case "10":
                    iniciarAuditoria();
                    break;
>>>>>>> Stashed changes
                case "0":
                    System.out.println();
                    titulo("Encerrando a linha de produção. Até logo!");
                    System.exit(0);
                    break;

                default:
                    erro("Opção inválida. Escolha um número do menu");
            }
        }
    }
}