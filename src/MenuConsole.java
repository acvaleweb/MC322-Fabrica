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
        System.out.println("              FÁBRICA DE HARDWARE      ");
        System.out.println("==================================================");
        System.out.println("Bem-vindos à nossa planta industrial automatizada!");
        System.out.println("==================================================\n");
        System.out.println("O que você deseja fazer?");
        System.out.println("1. Consultar estoque");
        System.out.println("2. Comprar matéria-prima");
        System.out.println("3. Fabricar produtos");
    
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
                // consultar estoque
                break;
            case "2":
                // comprar matéria-prima
                break;
            case "3":
                // fabricar produtos
                break;
            default:
            System.out.println("Opção inválida.");
        }
        

    }


}
