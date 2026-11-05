package br.unicamp.ic.mc322.suporte;

import br.unicamp.ic.mc322.model.Auditavel;
import br.unicamp.ic.mc322.model.CategoriaProduto;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.ComponenteEntrada;
import br.unicamp.ic.mc322.model.ComponentePerformance;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.InsersoraSMT;
import br.unicamp.ic.mc322.model.Inspetora;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.Montadora;
import br.unicamp.ic.mc322.model.Produto;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.strategy.EstrategiaMaiorLote;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Planta usada pelos testes de persistência: tem estado "interessante" em todas as
 * camadas (cenário não padrão, estratégia não padrão, máquinas com sorteio fixo, demandas
 * em estados diferentes, estoque consumido e unidades no armazém).
 */
public final class PlantaPersistivel {
    public static final String A7000 = "Andesite A7000";
    public static final String A8000 = "Andesite A8000";

    private PlantaPersistivel() {
        // classe utilitaria
    }

    /** Catálogo de dois produtos. É o mesmo que deve ser informado ao carregar. */
    public static List<Produto> catalogo() {
        List<Produto> catalogo = new ArrayList<>();
        catalogo.add(new ComponenteEntrada("PROD-VGA-001", A7000, CategoriaProduto.PLACA_DE_VIDEO, 1, 1.5));
        catalogo.add(new ComponentePerformance("PROD-VGA-002", A8000, CategoriaProduto.PLACA_DE_VIDEO, 2, 2.5));
        return catalogo;
    }

    /**
     * Cenário Apocalíptico, estratégia Maior Lote, 3 máquinas, demandas A7000 (4,
     * concluída após produzir) e A8000 (2, pendente) e unidades de A7000 no armazém.
     */
    public static GerenciadorProducao criar() {
        GerenciadorProducao planta = new GerenciadorProducao(
                new MateriaPrima("MP-01", "Wafer de Silício Andesite 5nm", 100, "unidade", 50.0, 1),
                Cenario.APOCALIPTICO, 5.5, catalogo(), new EstrategiaMaiorLote());
        planta.setPausasAtivas(false);

        long semente = 11;
        for (Maquina maquina : List.of(
                new InsersoraSMT("Insersora SMT-01", 5, 0.10, 15.0),
                new Montadora("Montadora MT-01", 5, 0.15, 10.0),
                new Inspetora("Inspetora INS-01", 5, 0.05, 8.0))) {
            maquina.setRandom(new Random(semente++));
            planta.adicionarMaquina(maquina);
        }

        for (Produto produto : catalogo()) {
            planta.registrarDemanda(produto.getNome());
        }
        planta.atualizarDemanda(A7000, 4);
        planta.atualizarDemanda(A8000, 2);
        silenciar(() -> planta.fabricarDemanda(A7000));
        return planta;
    }

    /** Executa a ação sem poluir a saída dos testes com o log da fabricação. */
    public static void silenciar(Runnable acao) {
        PrintStream original = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        try {
            acao.run();
        } finally {
            System.setOut(original);
        }
    }

    /**
     * Descreve o estado completo da planta em texto, para comparar duas plantas campo a
     * campo (configuração, estoque, máquinas com as Partes, demandas e armazém).
     */
    public static String descrever(GerenciadorProducao planta) {
        StringBuilder texto = new StringBuilder();
        texto.append(planta.getCenarioAtual()).append('|')
                .append(planta.getEstrategiaAtual().getClass().getSimpleName()).append('|')
                .append(planta.getBudget()).append('|')
                .append(planta.getCapacidadeEsteira()).append('|')
                .append(planta.getContadorUnidadesCriadas()).append('\n');

        MateriaPrima estoque = planta.getMateriaPrima();
        texto.append(estoque.getId()).append('|').append(estoque.getNome()).append('|')
                .append(estoque.getQuantidade()).append('|').append(estoque.getUnidade()).append('|')
                .append(estoque.getCustoPorUnidade()).append('|').append(estoque.getQuantidadeMinima())
                .append('\n');

        for (Maquina maquina : planta.getMaquinas()) {
            texto.append(maquina.getClass().getSimpleName()).append('|').append(maquina.getNome()).append('|')
                    .append(maquina.getCapacidadeMaxima()).append('|').append(maquina.getCustoOperacao()).append('|')
                    .append(maquina.estaLigada()).append('|').append(maquina.obterEstado()).append('\n');
        }
        for (Demanda demanda : planta.getDemandas()) {
            texto.append(demanda.getTipoProduto()).append('|').append(demanda.getQuantidadeProdutos()).append('|')
                    .append(demanda.getStatus()).append('\n');
        }
        for (Produto produto : planta.getArmazem()) {
            texto.append(produto.getClass().getSimpleName()).append('|').append(produto.getId()).append('|')
                    .append(produto.getNome()).append('|').append(produto.getStatus()).append('|')
                    .append(produto.getQualidade()).append('|').append(produto.getMassa()).append('|')
                    .append(produto.getProbabilidadeFalhaAcumulada()).append('\n');
        }
        return texto.toString();
    }

    /** Relatório de diagnóstico da primeira máquina (inclui as Partes). */
    public static String diagnosticoDaPrimeiraMaquina(GerenciadorProducao planta) {
        return ((Auditavel) planta.getMaquinas().get(0)).gerarRelatorioDiagnostico();
    }
}
