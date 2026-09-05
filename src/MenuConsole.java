import java.util.Scanner;

public class MenuConsole {
    private static final int LARGURA_TELA = 54;
    private final Scanner scanner;
    private final MateriaPrima gpu;
    private final Produto placaVideoLow;
    private final Produto placaVideoMid;
    private final Produto placaVideoHigh;
    private final Maquina maquinaSMT;
    private final Esteira esteiraProducao;
    private final EstacaoInspecao estacaoInspecao;

    public MenuConsole() {

        scanner = new Scanner(System.in);

        gpu = new MateriaPrima(
                "MP-GPU-001",
                "Andesite 5nm 3840C 2.5GHz",
                10,
                "unidade",
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

        esteiraProducao = new Esteira(5.5);

        estacaoInspecao = new EstacaoInspecao();
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

    public void iniciarMenuInventario() {
        exibirInventario();
        lerEntrada(); // aguarda o usuário confirmar para voltar
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
                    iniciarMenuCompra();
                    break;

                case "3":
                    iniciarMenuProducao();
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