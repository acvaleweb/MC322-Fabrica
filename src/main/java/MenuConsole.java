import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Interface de terminal da planta: exibe os menus, lê as opções do usuário e
 * apresenta os resultados e os erros.
 *
 * <p>Esta classe é a <b>fronteira de tratamento de erros</b> da aplicação:</p>
 * <ul>
 *   <li>entradas inválidas (texto no lugar de número, opção inexistente) repetem a
 *       pergunta com uma mensagem curta;</li>
 *   <li>erros de regra de negócio ({@link PlantaException}) aparecem em um bloco
 *       "ERRO" que aguarda o usuário pressionar ENTER, sem encerrar o programa;</li>
 *   <li>o fim da entrada de teclado (Ctrl+D ou Ctrl+Z) encerra o programa de forma
 *       limpa.</li>
 * </ul>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class MenuConsole {
    private static final int LARGURA_TELA = 54;
    private static final int LARGURA_BLOCO_ERRO = 40;
    private final Scanner scanner;
    private final List<Produto> catalogo;
    private final MateriaPrima materiaPrima;
    private GerenciadorProducao gerenciador;

    // Sinaliza que a entrada padrao foi fechada (Ctrl+D / Ctrl+Z): nao ha mais
    // como ler opcoes do usuario, entao o programa precisa encerrar com educacao
    private static class EntradaEncerradaException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    /**
     * Cria o menu e monta o catálogo de produtos (três GPUs, três CPUs e três
     * placas-mãe) e o estoque inicial de matéria-prima. A planta em si só é montada em
     * {@link #iniciar()}, depois que o usuário escolhe o cenário.
     */
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
    }

    // A planta so e montada depois da escolha do cenario, que ja depende de
    // leitura do teclado (por isso nao fica no construtor)
    private void montarPlanta(Cenario cenario) {
        gerenciador = new GerenciadorProducao(materiaPrima, cenario, 5.5, catalogo,
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

    // Erro curto, para entradas invalidas: a pergunta e repetida logo em seguida
    private void erro(String mensagem) {
        System.out.println("[ERRO] " + mensagem);
    }

    private void info(String mensagem) {
        System.out.println("       " + mensagem);
    }

    // Quebra o texto em linhas de no maximo "largura" caracteres, sem cortar palavras
    private List<String> quebrarLinhas(String texto, int largura) {
        List<String> linhas = new ArrayList<>();
        StringBuilder atual = new StringBuilder();

        for (String palavra : texto.split(" ")) {
            if (atual.length() > 0 && atual.length() + 1 + palavra.length() > largura) {
                linhas.add(atual.toString());
                atual.setLength(0);
            }
            if (atual.length() > 0) {
                atual.append(' ');
            }
            atual.append(palavra);
        }

        if (atual.length() > 0) {
            linhas.add(atual.toString());
        }
        return linhas;
    }

    // Erro de regra de negocio (PlantaException), no formato do enunciado: como as
    // validacoes acontecem antes de qualquer alteracao, a planta continua
    // exatamente como estava. O usuario volta ao menu depois de apertar ENTER
    private void exibirErro(String acao, PlantaException e) {
        String linhaErro = repetir("=", LARGURA_BLOCO_ERRO);

        System.out.println();
        System.out.println(linhaErro);
        System.out.println("ERRO");
        System.out.println(linhaErro);
        for (String linha : quebrarLinhas("Não foi possível " + acao + ":", LARGURA_BLOCO_ERRO)) {
            System.out.println(linha);
        }
        for (String linha : quebrarLinhas(e.getMessage(), LARGURA_BLOCO_ERRO)) {
            System.out.println(linha);
        }
        System.out.println();
        System.out.println("Nenhuma alteração foi realizada na planta.");
        System.out.println();
        aguardarEnter();
    }

    private void aguardarEnter() {
        System.out.print("Pressione ENTER para continuar...");
        lerLinha();
    }

    // ======================================================
    // Leitura de entrada (com validação)
    // ======================================================

    // Unico ponto que le o teclado. Se a entrada foi fechada (EOF), converte o
    // NoSuchElementException do Scanner no sinal de encerramento do programa
    private String lerLinha() {
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new EntradaEncerradaException();
        }
    }

    private String lerEntrada() {
        System.out.print("\nEscolha uma opção: ");
        return lerLinha();
    }

    private double lerEntradaDouble(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = lerLinha().replace(",", ".");
            try {
                double valor = Double.parseDouble(entrada);

                // "NaN" e "Infinity" sao aceitos pelo parseDouble, mas nao sao quantidades
                if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                    erro("Entrada inválida. Digite um número comum (ex: 2 ou 2.5)");
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                erro("Entrada inválida. Digite apenas números (ex: 2 ou 2.5)");
            }
        }
    }

    private int lerEntradaInt(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = lerLinha();
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
            String opcao = lerLinha();

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
            System.out.println(" [" + (i + 1) + "] - " + produto.getNome()
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
            erro("Produto inválido. Escolha um número entre 1 e " + catalogo.size());
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
        System.out.println(" [7] - Linha de produção (máquinas e partes)");
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
        try {
            gerenciador.cancelarDemanda(produto.getNome());
            ok("Demanda de " + produto.getNome() + " cancelada");
        } catch (PlantaException e) {
            exibirErro("cancelar a demanda de " + produto.getNome(), e);
        }
    }

    private void iniciarMenuAtualizarDemanda(Produto produto) {
        cabecalho("ATUALIZAR DEMANDA: " + produto.getNome());
        int novaQuantidade = lerEntradaInt("Quantas unidades de " + produto.getNome() + " deseja demandar? ");

        try {
            gerenciador.atualizarDemanda(produto.getNome(), novaQuantidade);
            ok("Demanda de " + produto.getNome() + " atualizada para " + novaQuantidade + " unidade(s)");
        } catch (PlantaException e) {
            exibirErro("atualizar a demanda de " + produto.getNome(), e);
        }
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
                    try {
                        gerenciador.executarProximaProducao();
                        info("Budget restante: R$" + String.format("%.2f", gerenciador.getBudget()));
                    } catch (PlantaException e) {
                        exibirErro("iniciar a produção", e);
                    }
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
        // fabricarDemanda imprime o andamento ([OK]/[AVISO]); erros chegam como exceção
        try {
            gerenciador.fabricarDemanda(produto.getNome());
            info("Budget restante: R$" + String.format("%.2f", gerenciador.getBudget()));
        } catch (PlantaException e) {
            exibirErro("iniciar a produção de " + produto.getNome(), e);
        }
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

        try {
            gerenciador.comprarMateriaPrima(quantidade);
        } catch (PlantaException e) {
            exibirErro("concluir a compra de matéria-prima", e);
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
            System.out.println(" [4] - Reparar maquinas");
            System.out.println(" [5] - Auditoria estrutural (linha > maquina > partes)");
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
                case "4":
                    repararMaquinas();
                    break;
                case "5":
                    exibirAuditoriaEstrutural();
                    break;
                case "0":
                    return;
                default:
                    erro("Opção inválida. Escolha um número do menu");
            }
        }
    }

    private void exibirAuditoriaEstrutural() {
        cabecalho("AUDITORIA ESTRUTURAL");
        gerenciador.gerarAuditoriaEstrutural();
        linhaFina();
    }

    // ======================================================
    // Submenu: Linha de produção (agrupador, máquinas e partes)
    // ======================================================

    private void iniciarMenuLinha() {
        while (true) {
            cabecalho("LINHA DE PRODUÇÃO");
            System.out.println(" [1] - Ver máquinas da linha");
            System.out.println(" [2] - Ler partes internas (motor, sensor, atuadores)");
            System.out.println(" [3] - Auditoria estrutural detalhada");
            System.out.println(" [4] - Reparar máquinas");
            System.out.println(" [0] - Voltar ao menu principal");
            linhaFina();

            String opcao = lerEntrada();

            switch (opcao) {
                case "1":
                    exibirMaquinasDaLinha();
                    break;
                case "2":
                    exibirLeituraDasPartes();
                    break;
                case "3":
                    exibirAuditoriaEstrutural();
                    break;
                case "4":
                    repararMaquinas();
                    break;
                case "0":
                    return;
                default:
                    erro("Opção inválida. Escolha um número do menu");
            }
        }
    }

    private void exibirMaquinasDaLinha() {
        cabecalho("LINHA " + gerenciador.getNomeLinha().toUpperCase());
        int posicao = 1;
        for (Maquina maquina : gerenciador.getMaquinas()) {
            System.out.println(" " + posicao + ". " + maquina.getTipo() + " " + maquina.getNome()
                    + " | custo: R$" + String.format("%.2f", maquina.getCustoOperacao()));
            posicao++;
        }
        info("Custo de operação por unidade: R$" + String.format("%.2f", gerenciador.getCustoOperacaoPorUnidade()));
        linhaFina();
    }

    // Usa so os metodos de delegacao da Maquina: o menu nunca enxerga as partes
    private void exibirLeituraDasPartes() {
        cabecalho("LEITURA DAS PARTES INTERNAS");
        for (Maquina maquina : gerenciador.getMaquinas()) {
            System.out.println(" - " + maquina.getNome());
            System.out.println("     Motor: " + String.format("%.0f", maquina.lerTemperaturaMotor()) + " °C");
            System.out.println("     Sensor: calibração " + String.format("%.0f", maquina.lerCalibracaoSensor()) + "%");
            System.out.println("     Atuadores operacionais: " + maquina.contarAtuadoresOperacionais()
                    + "/" + maquina.getQuantidadeAtuadores());
        }
        linhaFina();
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

    private void repararMaquinas() {
        cabecalho("REPARAR MAQUINAS");
        int reparadas = gerenciador.repararMaquinas();

        if (reparadas == 0) {
            info("Nenhuma máquina precisa de reparo no momento");
            return;
        }
        ok(reparadas + " máquina(s) reparada(s). Saúde restaurada ao máximo");
    }

    // ======================================================
    // Loop principal
    // ======================================================

    /**
     * Executa o programa: pede o cenário, monta a planta e mantém o menu principal até
     * o usuário escolher sair ou a entrada do teclado ser encerrada.
     */
    public void iniciar() {
        try {
            montarPlanta(escolherCenario());
            executarLoopPrincipal();
        } catch (EntradaEncerradaException e) {
            System.out.println();
            titulo("Entrada encerrada. Encerrando a linha de produção.");
        } finally {
            scanner.close();
        }
    }

    private void executarLoopPrincipal() {
        while (true) {
            exibirMenuPrincipal();
            String opcao = lerEntrada();

            if (opcao.equals("0")) {
                System.out.println();
                titulo("Encerrando a linha de produção. Até logo!");
                return;
            }

            // os erros de regra de negocio sao tratados em cada acao do menu
            processarOpcaoPrincipal(opcao);
        }
    }

    private void processarOpcaoPrincipal(String opcao) {
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
            case "7":
                iniciarMenuLinha();
                break;
            default:
                erro("Opção inválida. Escolha um número do menu");
        }
    }
}
