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
        System.out.println("Quantidade mínima para compra: " + gpu1.getQuantidadeMinima());

        System.out.println("O que você deseja fazer?");
        System.out.println("1. Comprar " + gpu1.getNome());
        System.out.println("0. Voltar");

    }

    private void exibirMenuFabricar() {

        System.out.println("==================================================");
        System.out.println("                    FABRICAR                      ");
        System.out.println("==================================================");
        System.out.println("==================================================\n");

        System.out.println("Produtos:");
        System.out.println("- " + placaVideo1.getNome());
        System.out.println("Status: " + placaVideo1.getStatus());

        System.out.println("O que você deseja fazer?");
        System.out.println("1. Fabricar " + gpu1.getNome());
        System.out.println("0. Voltar");


    }

    public String lerEntrada() {
        System.out.print("Escolha uma opção: ");
        String entrada = scanner.nextLine();
        return entrada;
    }

    
    public void iniciar() {
        exibirIntroducao();
        
        switch (lerEntrada()) {
            case "1":
                exibirInventario();
                if (lerEntrada().equals("0")) {exibirIntroducao();}
                break;

            case "2":
                exibirMenuCompra();
                switch (lerEntrada()) {
                    case "1":
                    // logica de compra
                        break;
                    case "0":
                        exibirIntroducao();
                        break;
                    default:
                        System.out.println("Opção inválida.");
            }

                break;

            case "3":
                exibirMenuFabricar();
                switch (lerEntrada()) {
                    case "1":
                    // logica de producao
                        break;
                    case "0":
                        exibirIntroducao();
                        break;
                    default:
                        System.out.println("Opção inválida.");
            }
            break;

            case "0":
                System.exit(0);
                break;

            default:
            System.out.println("Opção inválida.");
        }
        

    }


}
