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

        gerenciador = new GerenciadorProducao(materiaPrima, 1000.0, 5.5, (ArrayList<Produto>) catalogo);

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
    // Auxiliar: produtos de uma categoria, na ordem do catálogo
    // ======================================================

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
        System.out.printf("BUDGET ATUAL: R$%.2f%n", gerenciador.getBudget());
        linhaFina();

        System.out.println("LINHAS DE PRODUÇÃO");
        System.out.println(" 1 - Placas de Vídeo (Andesite)");
        System.out.println(" 2 - Processadores (Basalt)");
        System.out.println(" 3 - Placas-Mãe (Granite)");
        System.out.println();

        System.out.println("CONSULTAR");
        System.out.println(" 4 - Ver armazém");
        System.out.println(" 5 - Ver estoque de matéria-prima");
        System.out.println(" 6 - Ver demandas");
        System.out.println();

        System.out.println("COMPRAR MATÉRIA-PRIMA");
        System.out.println(" 7 - Comprar " + materiaPrima.getNome());
        System.out.println();

        System.out.println(" 0 - SAIR");
        linhaFina();
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
            String status = demanda.isAtendida() ? "Atendida" : "Pendente";
            System.out.println(" - " + demanda.getTipoProduto()
                    + " | quantidade: " + demanda.getQuantidadeProdutos()
                    + " | status: " + status);
        }
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
    // Loop principal
    // ======================================================

    public void iniciar() {
        while (true) {
            exibirMenuPrincipal();
            String opcao = lerEntrada();

            switch (opcao) {
                case "1":
                    iniciarMenuCategoria(CategoriaProduto.PLACA_DE_VIDEO);
                    break;
                case "2":
                    iniciarMenuCategoria(CategoriaProduto.PROCESSADOR);
                    break;
                case "3":
                    iniciarMenuCategoria(CategoriaProduto.PLACA_MAE);
                    break;
                case "4":
                    exibirTelaArmazem();
                    break;
                case "5":
                    exibirTelaEstoque();
                    break;
                case "6":
                    exibirTelaDemandas();
                    break;
                case "7":
                    iniciarMenuCompra();
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