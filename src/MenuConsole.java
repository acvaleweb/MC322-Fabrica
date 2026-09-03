import java.util.Scanner;

public class MenuConsole {
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
                10);

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

        maquinaSMT = new Maquina("INS-SMT-001", 5);

        esteiraProducao = new Esteira(5.5);

        estacaoInspecao = new EstacaoInspecao();

    }

    private void pausar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void exibirIntroducao() {
        System.out.println("==================================================");
        System.out.println("                FÁBRICA DE HARDWARE               ");
        System.out.println("==================================================");
        System.out.println("Bem-vindos à nossa planta industrial automatizada!");
        System.out.println("==================================================\n");
        System.out.println("O que você deseja fazer?");
        System.out.println("[1] Consultar estoque");
        System.out.println("[2] Comprar matéria-prima");
        System.out.println("[3] Fabricar produtos");
        System.out.println("[0] Fechar programa");
    }

    private void exibirInventario() {
        System.out.println("==================================================");
        System.out.println("                    INVENTÁRIO                    ");
        System.out.println("==================================================");
        System.out.println("Produtos:");
        System.out.println("- " + placaVideoHigh.getNome());
        System.out.println("Status: " + placaVideoHigh.getStatus());
        System.out.println("- " + placaVideoMid.getNome());
        System.out.println("Status: " + placaVideoMid.getStatus());
        System.out.println("- " + placaVideoLow.getNome());
        System.out.println("Status: " + placaVideoLow.getStatus());
        System.out.println("Matérias-Primas em estoque:");
        System.out.println("- " + gpu.getNome());
        System.out.println("Quantidade: " + gpu.getQuantidade());
        System.out.println("[0] Para voltar");
    }

    private void exibirMenuCompra() {
        System.out.println("==================================================");
        System.out.println("              COMPRAR MATÉRIA-PRIMA               ");
        System.out.println("==================================================");
        System.out.println("Em estoque:");
        System.out.println("- " + gpu.getNome());
        System.out.println("Quantidade em estoque: " + gpu.getQuantidade());
        System.out.println("Quantidade mínima para compra: " + gpu.getQuantidadeMinima() + "\n");
    }

    private void exibirMenuFabricar() {
        System.out.println("==================================================");
        System.out.println("                    FABRICAR                      ");
        System.out.println("==================================================");
        System.out.println("Produtos:");
        System.out.println("- " + placaVideoHigh.getNome());
        System.out.println("Status: " + placaVideoHigh.getStatus());
        System.out.println("- " + placaVideoMid.getNome());
        System.out.println("Status: " + placaVideoMid.getStatus());
        System.out.println("- " + placaVideoLow.getNome());
        System.out.println("Status: " + placaVideoLow.getStatus());
    }

    public String lerEntrada() {
        System.out.print("Escolha uma opção: ");
        String entrada = scanner.nextLine();
        return entrada;
    }

    public double lerEntradaDouble() {
        System.out.print("Escolha um valor: ");
        double entrada = Double.parseDouble(scanner.nextLine());
        return entrada;
    }

    public void iniciarMenuInventario() {
        exibirInventario();
        lerEntrada(); // aguarda "0" para voltar
    }

    public void iniciarMenuCompra() {
        exibirMenuCompra();
        System.out.println("O que você deseja fazer?");
        System.out.println("[1] Comprar " + gpu.getNome());
        System.out.println("[0] Voltar");

        switch (lerEntrada()) {

            case "1":
                processarCompra();
                break;

            case "0":
                return;

            default:
                System.out.println("Opção inválida");
        }
    }

    private void processarCompra() {
        exibirMenuCompra();

        System.out.println("Quantidade mínima para compra: " + gpu.getQuantidadeMinima() + gpu.getUnidade() + "(s)");
        System.out.println("Qual quantidade deseja comprar?");
        System.out.println("Insira [0] para cancelar a compra");

        while (true) {
            double quantidade = lerEntradaDouble();

            if (quantidade == 0) {
                return;
            }

            if (quantidade < gpu.getQuantidadeMinima()) {
                System.out.println("Solicite uma quantidade maior. Mínimo: " + gpu.getQuantidadeMinima());
                continue;
            }

            gpu.adicionarEstoque(quantidade);

            System.out.println("Compra realizada com sucesso");
            System.out.println("Estoque atual: " + gpu.getQuantidade());
            System.out.println("O que você deseja fazer?");
            System.out.println("[1] Comprar novamente");
            System.out.println("[0] Voltar");

            String opcao = lerEntrada();

            if (opcao.equals("0")) {
                return;
            } else if (opcao.equals("1")) {
                exibirMenuCompra();

                System.out.println(
                        "Quantidade mínima para compra: " + gpu.getQuantidadeMinima() + gpu.getUnidade() + "(s)");
                System.out.println("Qual quantidade deseja comprar?");
                System.out.println("Insira [0] para cancelar a compra");
                // loop continua
            }
        }
    }

    public void iniciarMenuProducao() {
        exibirMenuFabricar();

        System.out.println("O que você deseja fazer?");
        System.out.println("[1] Fabricar " + placaVideoLow.getNome());
        System.out.println("[2] Fabricar " + placaVideoMid.getNome());
        System.out.println("[3] Fabricar " + placaVideoHigh.getNome());
        System.out.println("[0] Voltar");

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
                System.out.println("Opção inválida.");
                return;

        }

        fabricarProduto(produtoEscolhido);
    }

    private void fabricarProduto(Produto produto) {
        System.out.println("Checando estoque de " + gpu.getNome() + ": " + gpu.getQuantidade());

        if (gpu.getQuantidade() < produto.getDemandaMateriaPrima()) {
            System.out.println("Estoque insuficiente. Cancelando manufatura.");
            return;
        }

        pausar(100);

        System.out.println("Estoque suficiente. Diminuindo estoque de " + gpu.getNome() + " em "
                + produto.getDemandaMateriaPrima() + " e iniciando manufatura.");

        gpu.consumir(produto.getDemandaMateriaPrima());

        pausar(100);

        System.out.println("Checando estado da esteira");

        pausar(100);

        if (esteiraProducao.estaEmMovimento()) {
            System.out.println("Esteira em movimento. Desligando...");
            esteiraProducao.desligar();

            pausar(100);
        }

        System.out.println("Ligando esteira...");
        esteiraProducao.ligar();

        pausar(100);

        System.out.println(gpu.getNome() + " colocado na esteira");

        pausar(100);

        System.out.println(gpu.getNome() + " chegou na " + maquinaSMT.getNome() + ". Desligando esteira...");

        pausar(100);

        esteiraProducao.desligar();
        System.out.println("Checando estado da " + maquinaSMT.getNome());

        pausar(100);

        if (maquinaSMT.estaLigada()) {

            System.out.println(maquinaSMT.getNome() + " ligada. Desligando...");
            maquinaSMT.desligar();

            pausar(100);

        }

        System.out.println("Inserindo " + gpu.getNome() + " em " + maquinaSMT.getNome() + "...");

        pausar(100);

        maquinaSMT.ligar();
        System.out.println("Ligando " + maquinaSMT.getNome() + "...");

        pausar(100);

        System.out.println("Realizando manufatura...");
        produto.processar();
        System.out.println(produto.getNome() + " " + produto.getStatus() + ".");

        pausar(100);

        System.out.println(
                "Concluído. Desligando " + maquinaSMT.getNome() + " e Iniciando processo de inspeção.");

        maquinaSMT.desligar();

        pausar(100);

        System.out.println("Ativando estação...");
        estacaoInspecao.ativar();

        pausar(100);

        System.out.println("Realizando inspeção...");
        estacaoInspecao.inspecionar(produto);

        pausar(100);

        System.out.println("Inspeção Realizada. Desativando estação...");
        estacaoInspecao.desativar();

        pausar(100);

        System.out.println("Estação desativada. Processo concluído e " + produto.getNome() + " manufaturado.");
    }

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
                    System.exit(0);
                    break;

                default:
                    System.out.println("Opção inválida.");
            }
        }
    }
}