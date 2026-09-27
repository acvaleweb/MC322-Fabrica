import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MenuConsole {
    private static final int LARGURA_TELA = 54;
    private final Scanner scanner;
    private final GerenciadorProducao gerenciador;
    private final List<Produto> catalogo;
    private final MateriaPrima materiaPrima;

    public MenuConsole() {
        scanner = new Scanner(System.in);

        materiaPrima = new MateriaPrima(
                "MP-WAF-001",
                "Wafer de Silício Andesite 5nm",
                10,
                "unidade",
                50.0,
                1);

        catalogo = new ArrayList<>();

        // GPU - Andesite
        catalogo.add(new ComponenteEntrada("PROD-VGA-001", "Andesite A7000", CategoriaProduto.PLACA_DE_VIDEO, 1, 1.5));
        catalogo.add(
                new ComponentePerformance("PROD-VGA-002", "Andesite A8000", CategoriaProduto.PLACA_DE_VIDEO, 2, 2.5));
        catalogo.add(new ComponenteFlagship("PROD-VGA-003", "Andesite A9000", CategoriaProduto.PLACA_DE_VIDEO, 3, 3.5));

        // CPU - Basalt
        catalogo.add(new ComponenteEntrada("PROD-CPU-001", "Basalt B7000", CategoriaProduto.PROCESSADOR, 1, 0.1));
        catalogo.add(new ComponentePerformance("PROD-CPU-002", "Basalt B8000", CategoriaProduto.PROCESSADOR, 1.5, 0.1));
        catalogo.add(new ComponenteFlagship("PROD-CPU-003", "Basalt B9000", CategoriaProduto.PROCESSADOR, 2, 0.1));

        // Placa-mãe - Granite
        catalogo.add(new ComponenteEntrada("PROD-MB-001", "Granite G7000", CategoriaProduto.PLACA_MAE, 2, 0.8));
        catalogo.add(new ComponentePerformance("PROD-MB-002", "Granite G8000", CategoriaProduto.PLACA_MAE, 3, 1.0));
        catalogo.add(new ComponenteFlagship("PROD-MB-003", "Granite G9000", CategoriaProduto.PLACA_MAE, 4, 1.2));

        Cenario cenario = escolherCenario();

        gerenciador = new GerenciadorProducao(materiaPrima, cenario, 5.5, (ArrayList<Produto>) catalogo,
                new EstrategiaFilaDaFundicao());

        // Ordem de montagem: insersora -> montadora -> inspetora
        gerenciador.adicionarMaquina(new InsersoraSMT("Insersora SMT-01", 5, 0.10, 15.0));
        gerenciador.adicionarMaquina(new Montadora("Montadora MT-01", 5, 0.15, 10.0));
        gerenciador.adicionarMaquina(new Inspetora("Inspetora INS-01", 5, 0.05, 8.0));

        for (Produto produto : catalogo) {
            gerenciador.registrarDemanda(produto.getNome());
        }
    }

    // ======================================================
    // Utilidades de formatação de tela
    // ======================================================

    private String repetir(String s, int vezes) {
        return s.repeat(Math.max(vezes, 0));
    }

    private void linha() {
        System.out.println(repetir("=", LARGURA_TELA));
    }

    private void linhaFina() {
        System.out.println(repetir("-", LARGURA_TELA));
    }

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

    private void info(String mensagem) {
        System.out.println("       " + mensagem);
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

    private int lerEntradaInt(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = scanner.nextLine().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                erro("Entrada inválida. Digite apenas números inteiros (ex: 10)");
            }
        }
    }

    // ======================================================
    // Escolha de cenário (início da execução)
    // ======================================================

    private Cenario escolherCenario() {
        cabecalho("ESCOLHA DE CENARIO");
        System.out.println(" 1 - " + Cenario.IDEAL.getNomeExibicao()
                + " (budget farto, baixa falha e desgaste reduzido)");
        System.out.println(" 2 - " + Cenario.APOCALIPTICO.getNomeExibicao()
                + " (budget apertado, alta falha e desgaste acelerado)");
        linhaFina();

        while (true) {
            System.out.print("\nEscolha um cenario: ");
            String opcao = scanner.nextLine().trim();

            if (opcao.equals("1")) {
                return Cenario.IDEAL;
            }
            if (opcao.equals("2")) {
                return Cenario.APOCALIPTICO;
            }
            erro("Opcao invalida. Digite 1 ou 2");
        }
    }

    // ======================================================
    // Auxiliar: escolher um produto do catálogo
    // ======================================================

    private void listarCatalogoNumerado() {
        for (int i = 0; i < catalogo.size(); i++) {
            Produto produto = catalogo.get(i);
            System.out.println(" " + (i + 1) + " - " + produto.getNome()
                    + " (" + produto.getCategoria().getNomeExibicao() + ")");
        }
    }

    private Produto escolherProdutoDoCatalogo() {
        cabecalho("ESCOLHER PRODUTO");
        listarCatalogoNumerado();
        linhaFina();

        int escolha = lerEntradaInt("\nNumero do produto (0 para cancelar): ");

        if (escolha == 0) {
            return null;
        }
        if (escolha < 1 || escolha > catalogo.size()) {
            erro("Produto invalido");
            return null;
        }
        return catalogo.get(escolha - 1);
    }

    // ======================================================
    // Tela principal
    // ======================================================

    private void exibirMenuPrincipal() {
        cabecalho("ANDESITE HARDWARE CO.");
        titulo("\"Silício, solda e ambição\"");
        linha();
        System.out.println("ESTRATEGIA ATUAL: " + gerenciador.getEstrategiaAtual().getNomeEstrategia());
        System.out.println("CENARIO ATIVO: " + gerenciador.getCenarioAtual().getNomeExibicao());
        System.out.printf("BUDGET ATUAL: R$%.2f%n", gerenciador.getBudget());
        linhaFina();

        System.out.println(" [1] - Demandas");
        System.out.println(" [2] - Fabricação");
        System.out.println(" [3] - Consultar");
        System.out.println(" [4] - Comprar matéria-prima");
        System.out.println(" [5] - Gerenciar estratégia de produção");
        System.out.println(" [6] - Auditoria");
        System.out.println();
        System.out.println(" [0] - SAIR");
        linhaFina();
    }

    // ======================================================
    // Submenu: Demandas
    // ======================================================

    private void iniciarMenuDemandas() {
        while (true) {
            cabecalho("DEMANDAS");
            System.out.println(" [1] - Atualizar demanda de um produto");
            System.out.println(" [2] - Listar demandas");
            System.out.println(" [3] - Cancelar demanda de um produto");
            System.out.println(" [0] - Voltar ao menu principal");
            linhaFina();

            String opcao = lerEntrada();

            switch (opcao) {
                case "1":
                    Produto produtoParaAtualizar = escolherProdutoDoCatalogo();
                    if (produtoParaAtualizar != null) {
                        iniciarMenuAtualizarDemanda(produtoParaAtualizar);
                    }
                    break;
                case "2":
                    exibirTelaDemandas();
                    break;
                case "3":
                    Produto produtoParaCancelar = escolherProdutoDoCatalogo();
                    if (produtoParaCancelar != null) {
                        iniciarMenuCancelarDemanda(produtoParaCancelar);
                    }
                    break;
                case "0":
                    return;
                default:
                    erro("Opção inválida. Escolha um número do menu");
            }
        }
    }

    private void iniciarMenuCancelarDemanda(Produto produto) {
        if (!gerenciador.cancelarDemanda(produto.getNome())) {
            erro("Não foi possível cancelar. A demanda já foi concluída ou não existe");
            return;
        }
        ok("Demanda de " + produto.getNome() + " cancelada");
    }

    private void iniciarMenuAtualizarDemanda(Produto produto) {
        cabecalho("ATUALIZAR DEMANDA: " + produto.getNome());
        int novaQuantidade = lerEntradaInt("Quantas unidades de " + produto.getNome() + " deseja demandar? ");

        if (!gerenciador.atualizarDemanda(produto.getNome(), novaQuantidade)) {
            erro("Não foi possível atualizar a demanda. Verifique se ela já está em produção/concluída");
            return;
        }

        ok("Demanda de " + produto.getNome() + " atualizada para " + novaQuantidade + " unidade(s)");
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
    }

    // ======================================================
    // Submenu: Fabricação
    // ======================================================

    private void iniciarMenuFabricacao() {
        while (true) {
            cabecalho("FABRICAÇÃO");
            System.out.println(" [1] - Processar próxima demanda (estratégia: "
                    + gerenciador.getEstrategiaAtual().getNomeEstrategia() + ")");
            System.out.println(" [2] - Fabricar produto específico");
            System.out.println(" [0] - Voltar ao menu principal");
            linhaFina();

            String opcao = lerEntrada();

            switch (opcao) {
                case "1":
                    cabecalho("PROCESSANDO PROXIMA DEMANDA");
                    gerenciador.executarProximaProducao();
                    info("Budget restante: R$" + String.format("%.2f", gerenciador.getBudget()));
                    break;
                case "2":
                    Produto produtoParaFabricar = escolherProdutoDoCatalogo();
                    if (produtoParaFabricar != null) {
                        iniciarMenuFabricar(produtoParaFabricar);
                    }
                    break;
                case "0":
                    return;
                default:
                    erro("Opção inválida. Escolha um número do menu");
            }
        }
    }

    private void iniciarMenuFabricar(Produto produto) {
        cabecalho("FABRICANDO: " + produto.getNome());
        // fabricarDemanda já imprime seus próprios status de [OK]/[ERRO]/[AVISO]
        gerenciador.fabricarDemanda(produto.getNome());
        info("Budget restante: R$" + String.format("%.2f", gerenciador.getBudget()));
    }

    // ======================================================
    // Submenu: Consultar
    // ======================================================

    private void iniciarMenuConsultar() {
        while (true) {
            cabecalho("CONSULTAR");
            System.out.println(" [1] - Ver armazém (produtos acabados)");
            System.out.println(" [2] - Ver estoque de matéria-prima");
            System.out.println(" [0] - Voltar ao menu principal");
            linhaFina();

            String opcao = lerEntrada();

            switch (opcao) {
                case "1":
                    exibirTelaArmazem();
                    break;
                case "2":
                    exibirTelaEstoque();
                    break;
                case "0":
                    return;
                default:
                    erro("Opção inválida. Escolha um número do menu");
            }
        }
    }

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

    // ======================================================
    // Fluxo: compra de matéria-prima
    // ======================================================

    private void iniciarMenuCompra() {
        cabecalho("COMPRAR MATÉRIA-PRIMA");
        System.out.println("Fornecedor: Andesite Foundries Ltda");
        linhaFina();
        System.out.println("- " + materiaPrima.getNome());
        System.out.println("  Estoque atual: " + materiaPrima.getQuantidade() + " " + materiaPrima.getUnidade()
                + "(s)");
        System.out.println("  Lote mínimo para compra: " + materiaPrima.getQuantidadeMinima() + " "
                + materiaPrima.getUnidade() + "(s)");
        System.out.println("  Custo por unidade: R$" + String.format("%.2f", materiaPrima.getCustoPorUnidade()));

        double quantidade = lerEntradaDouble("\nQuantas unidades deseja comprar? (0 para cancelar): ");

        if (quantidade == 0) {
            info("Compra cancelada");
            return;
        }

        if (!gerenciador.comprarMateriaPrima(quantidade)) {
            erro("Não foi possível concluir a compra. Verifique o lote mínimo e o budget disponível");
            return;
        }

        ok("Compra realizada com sucesso!");
        info("Novo estoque de " + materiaPrima.getNome() + ": " + materiaPrima.getQuantidade() + " "
                + materiaPrima.getUnidade());
        info("Budget restante: R$" + String.format("%.2f", gerenciador.getBudget()));
    }

    // ======================================================
    // Submenu: Estratégia de produção
    // ======================================================

    private void iniciarMenuEstrategia() {
        while (true) {
            cabecalho("ESTRATEGIA DE PRODUCAO");
            System.out.println("Estrategia ativa: " + gerenciador.getEstrategiaAtual().getNomeEstrategia());
            linhaFina();
            System.out.println(" [1] - Fila da Fundição (FIFO)");
            System.out.println(" [2] - Maior Lote");
            System.out.println(" [3] - Máximo Rendimento");
            System.out.println(" [0] - Voltar ao menu principal");
            linhaFina();

            String opcao = lerEntrada();

            switch (opcao) {
                case "1":
                    gerenciador.setEstrategia(new EstrategiaFilaDaFundicao());
                    ok("Estrategia alterada para Fila da Fundição");
                    break;
                case "2":
                    gerenciador.setEstrategia(new EstrategiaMaiorLote());
                    ok("Estrategia alterada para Maior Lote");
                    break;
                case "3":
                    gerenciador.setEstrategia(new EstrategiaMaximoRendimento());
                    ok("Estrategia alterada para Máximo Rendimento");
                    break;
                case "0":
                    return;
                default:
                    erro("Opção inválida. Escolha um número do menu");
            }
        }
    }

    // ======================================================
    // Submenu: Auditoria
    // ======================================================

    private void iniciarMenuAuditoria() {
        while (true) {
            cabecalho("AUDITORIA");
            System.out.println(" [1] - Relatorio geral (maquinas + produtos)");
            System.out.println(" [2] - Detalhar maquinas");
            System.out.println(" [3] - Detalhar produtos no armazem");
            System.out.println(" [0] - Voltar ao menu principal");
            linhaFina();

            String opcao = lerEntrada();

            switch (opcao) {
                case "1":
                    gerenciador.gerarAuditoriaGeral();
                    break;
                case "2":
                    exibirDetalheMaquinas();
                    break;
                case "3":
                    exibirDetalheProdutos();
                    break;
                case "0":
                    return;
                default:
                    erro("Opção inválida. Escolha um número do menu");
            }
        }
    }

    private void exibirDetalheMaquinas() {
        cabecalho("DIAGNOSTICO DAS MAQUINAS");
        List<Maquina> maquinas = gerenciador.getMaquinas();

        if (maquinas.isEmpty()) {
            System.out.println("Nenhuma maquina cadastrada.");
        }

        for (Maquina maquina : maquinas) {
            System.out.println(" - " + maquina.gerarRelatorioDiagnostico());
        }
        linhaFina();
    }

    private void exibirDetalheProdutos() {
        cabecalho("DIAGNOSTICO DOS PRODUTOS NO ARMAZEM");
        List<Produto> produtos = gerenciador.getArmazem();

        if (produtos.isEmpty()) {
            System.out.println("Armazem vazio.");
        }

        for (Produto produto : produtos) {
            System.out.println(" - " + produto.gerarRelatorioDiagnostico());
        }
        linhaFina();
    }

    // ======================================================
    // Loop principal
    // ======================================================

    public void iniciar() {
        while (true) {
            exibirMenuPrincipal();
            String opcao = lerEntrada();

            switch (opcao) {
                case "1":
                    iniciarMenuDemandas();
                    break;
                case "2":
                    iniciarMenuFabricacao();
                    break;
                case "3":
                    iniciarMenuConsultar();
                    break;
                case "4":
                    iniciarMenuCompra();
                    break;
                case "5":
                    iniciarMenuEstrategia();
                    break;
                case "6":
                    iniciarMenuAuditoria();
                    break;
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