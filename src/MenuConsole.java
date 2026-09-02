import java.util.Scanner;

public class MenuConsole {

    private Scanner scanner;
    private MateriaPrima gpu1;
    private Produto placaVideo1;
    private Maquina SMT;
    private Esteira esteira1;
    private EstacaoInspecao est1;

    public MenuConsole() {
        scanner = new Scanner(System.in);
        gpu1 = new MateriaPrima("g0001a", "gpu-5nm-2.5Ghz-3840c-andesite-classA", 10, "unitário", 50);
        placaVideo1 = new Produto("p0001a", "flagship-vcard-A9000-andesite", 1, 2);
        SMT = new Maquina("insersora-SMT", 5);
        esteira1 = new Esteira(5.5); // capacidade maxima 5.5 kg
        est1 = new EstacaoInspecao();
    }

    private void exibirIntroducao() {
        System.out.println("==================================================");
        System.out.println("                FÁBRICA DE HARDWARE               ");
        System.out.println("==================================================");
        System.out.println("Bem-vindos à nossa planta industrial automatizada!");
        System.out.println("==================================================\n");
        System.out.println("O que você deseja fazer?");
        System.out.println("1. Consultar estoque");
        System.out.println("2. Comprar matéria-prima");
        System.out.println("3. Fabricar produtos");
        System.out.println("0. Fechar programa");
    }

    private void exibirInventario() {
        System.out.println("==================================================");
        System.out.println("                    INVENTÁRIO                    ");
        System.out.println("==================================================");
        System.out.println("==================================================\n");

        System.out.println("Produtos:");
        System.out.println("- " + placaVideo1.getNome());
        System.out.println("Status: " + placaVideo1.getStatus());

        System.out.println("Matérias-Primas em estoque:");
        System.out.println("- " + gpu1.getNome());
        System.out.println("Quantidade: " + gpu1.getQuantidade());

        System.out.println("Digite 0 para voltar.");
    }

    private void exibirMenuCompra() {
        System.out.println("==================================================");
        System.out.println("              COMPRAR MATÉRIA-PRIMA               ");
        System.out.println("==================================================");
        System.out.println("==================================================\n");

        System.out.println("Em estoque:");
        System.out.println("- " + gpu1.getNome());
        System.out.println("Quantidade em estoque: " + gpu1.getQuantidade());
        System.out.println("Quantidade mínima para compra: " + gpu1.getQuantidadeMinima() + "\n");
    }

    private void exibirMenuFabricar() {
        System.out.println("==================================================");
        System.out.println("                    FABRICAR                      ");
        System.out.println("==================================================");
        System.out.println("==================================================\n");

        System.out.println("Produtos:");
        System.out.println("- " + placaVideo1.getNome());
        System.out.println("Status: " + placaVideo1.getStatus() + "\n");
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
        System.out.println("1. Comprar " + gpu1.getNome());
        System.out.println("0. Voltar");

        switch (lerEntrada()) {
            case "1":
                processarCompra();
                break;
            case "0":
                return;
            default:
                System.out.println("Opção inválida.");
        }
    }

    private void processarCompra() {
        exibirMenuCompra();

        System.out.println("Quantidade mínima para compra: " + gpu1.getQuantidadeMinima() + gpu1.getUnidade() + "(s)");
        System.out.println("Qual quantidade deseja comprar?");
        System.out.println("Insira 0 para cancelar a compra.");

        while (true) {
            double quantidade = lerEntradaDouble();

            if (quantidade == 0) {
                return;
            }

            if (quantidade < gpu1.getQuantidadeMinima()) {
                System.out.println("Por favor, solicite uma quantidade maior.");
                continue;
            }

            gpu1.adicionarEstoque(quantidade);
            System.out.println("Compra realizada com sucesso");
            System.out.println("Estoque atual: " + gpu1.getQuantidade());

            System.out.println("O que você deseja fazer?");
            System.out.println("1. Comprar novamente");
            System.out.println("0. Voltar");

            String opcao = lerEntrada();
            if (opcao.equals("0")) {
                return;
            } else if (opcao.equals("1")) {
                exibirMenuCompra();
                System.out.println("Quantidade mínima para compra: " + gpu1.getQuantidadeMinima() + " "+ gpu1.getUnidade() + "(s)");
                System.out.println("Qual quantidade deseja comprar?");
                System.out.println("Insira 0 para cancelar a compra.");
                // loop continua, pede nova quantidade
            }
        }
    }

    public void iniciarMenuProducao() {
        exibirMenuFabricar();

        System.out.println("O que você deseja fazer?");
        System.out.println("1. Fabricar " + gpu1.getNome());
        System.out.println("0. Voltar");

        switch (lerEntrada()) {
            case "1":
                // logica de producao
                break;
            case "0":
                return;
            default:
                System.out.println("Opção inválida.");
        }
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