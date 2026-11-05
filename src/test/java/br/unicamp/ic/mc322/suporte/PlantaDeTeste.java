package br.unicamp.ic.mc322.suporte;

import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.CategoriaProduto;
import br.unicamp.ic.mc322.model.ComponenteEntrada;
import br.unicamp.ic.mc322.model.ComponenteFlagship;
import br.unicamp.ic.mc322.model.ComponentePerformance;
import br.unicamp.ic.mc322.model.InsersoraSMT;
import br.unicamp.ic.mc322.model.Inspetora;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.Montadora;
import br.unicamp.ic.mc322.model.Produto;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.strategy.EstrategiaFilaDaFundicao;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fábrica de objetos usada pelos testes automatizados.
 *
 * <p>Centraliza a montagem de uma planta pequena e previsível, para que cada teste
 * comece de um estado conhecido e independente dos demais. Nenhum teste usa teclado,
 * pausas de tempo ou o arquivo de estado real da aplicação.</p>
 */
public final class PlantaDeTeste {
    /** Nome dos produtos do catálogo de teste. */
    public static final String ENTRADA = "Andesite A7000";
    public static final String PERFORMANCE = "Andesite A8000";
    public static final String FLAGSHIP = "Andesite A9000";

    /** Custo de operação de cada máquina da linha de teste (3 máquinas, R$ 30 por unidade). */
    public static final double CUSTO_POR_MAQUINA = 10.0;
    public static final double CUSTO_POR_UNIDADE = 3 * CUSTO_POR_MAQUINA;
    public static final double CAPACIDADE_ESTEIRA = 5.5;
    public static final double CAPACIDADE_MAQUINA = 10.0;
    public static final double CUSTO_MATERIA_PRIMA = 50.0;

    private PlantaDeTeste() {
        // classe utilitaria
    }

    /** Catálogo com um produto de cada tier (consomem 1, 2 e 3 de matéria-prima). */
    public static List<Produto> catalogo() {
        List<Produto> catalogo = new ArrayList<>();
        catalogo.add(new ComponenteEntrada("PROD-T-001", ENTRADA, CategoriaProduto.PLACA_DE_VIDEO, 1, 1.5));
        catalogo.add(new ComponentePerformance("PROD-T-002", PERFORMANCE, CategoriaProduto.PLACA_DE_VIDEO, 2, 2.5));
        catalogo.add(new ComponenteFlagship("PROD-T-003", FLAGSHIP, CategoriaProduto.PLACA_DE_VIDEO, 3, 3.5));
        return catalogo;
    }

    /** Estoque de matéria-prima com a quantidade informada, lote mínimo 1 e custo R$ 50. */
    public static MateriaPrima materiaPrima(double quantidade) {
        return new MateriaPrima("MP-T-001", "Wafer de Teste", quantidade, "unidade", CUSTO_MATERIA_PRIMA, 1);
    }

    /** Máquina sem chance de falha, com sorteio fixo, para resultados repetíveis. */
    public static Maquina insersora() {
        return preparar(new InsersoraSMT("Insersora T", CAPACIDADE_MAQUINA, 0.0, CUSTO_POR_MAQUINA));
    }

    public static Maquina montadora() {
        return preparar(new Montadora("Montadora T", CAPACIDADE_MAQUINA, 0.0, CUSTO_POR_MAQUINA));
    }

    public static Maquina inspetora() {
        return preparar(new Inspetora("Inspetora T", CAPACIDADE_MAQUINA, 0.0, CUSTO_POR_MAQUINA));
    }

    private static Maquina preparar(Maquina maquina) {
        maquina.setRandom(new Random(42));
        return maquina;
    }

    /**
     * Planta sem máquinas, no cenário Ideal, com uma demanda (quantidade zero) por produto
     * do catálogo e pausas de tempo desligadas.
     */
    public static GerenciadorProducao plantaSemMaquinas(double estoqueMateriaPrima) {
        return plantaSemMaquinas(Cenario.IDEAL, estoqueMateriaPrima);
    }

    public static GerenciadorProducao plantaSemMaquinas(Cenario cenario, double estoqueMateriaPrima) {
        GerenciadorProducao planta = new GerenciadorProducao(materiaPrima(estoqueMateriaPrima), cenario,
                CAPACIDADE_ESTEIRA, catalogo(), new EstrategiaFilaDaFundicao());
        planta.setPausasAtivas(false);
        for (Produto produto : planta.getCatalogoProdutos()) {
            planta.registrarDemanda(produto.getNome());
        }
        return planta;
    }

    /** Planta completa: linha com Insersora, Montadora e Inspetora. */
    public static GerenciadorProducao plantaCompleta(double estoqueMateriaPrima) {
        return plantaCompleta(Cenario.IDEAL, estoqueMateriaPrima);
    }

    public static GerenciadorProducao plantaCompleta(Cenario cenario, double estoqueMateriaPrima) {
        GerenciadorProducao planta = plantaSemMaquinas(cenario, estoqueMateriaPrima);
        planta.adicionarMaquina(insersora());
        planta.adicionarMaquina(montadora());
        planta.adicionarMaquina(inspetora());
        return planta;
    }
}
