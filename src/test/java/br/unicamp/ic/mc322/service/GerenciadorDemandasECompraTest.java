package br.unicamp.ic.mc322.service;

import static br.unicamp.ic.mc322.suporte.PlantaDeTeste.ENTRADA;
import static br.unicamp.ic.mc322.suporte.PlantaDeTeste.FLAGSHIP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.QuantidadeInvalidaException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.strategy.EstrategiaFilaDaFundicao;
import br.unicamp.ic.mc322.strategy.EstrategiaMaiorLote;
import br.unicamp.ic.mc322.suporte.PlantaDeTeste;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GerenciadorProducao: criação, cenário, demandas, estratégia e compra")
class GerenciadorDemandasECompraTest {
    private GerenciadorProducao planta;

    @BeforeEach
    void criarPlanta() {
        planta = PlantaDeTeste.plantaSemMaquinas(20);
    }

    private Demanda demandaDe(String produto) {
        return planta.getDemandas().stream()
                .filter(d -> d.getTipoProduto().equals(produto))
                .findFirst()
                .orElseThrow();
    }

    // ---------- criação e cenário ----------

    @Test
    @DisplayName("construtor recusa objetos nulos e esteira inválida")
    void construtorRecusaInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new GerenciadorProducao(null, Cenario.IDEAL, 5.0,
                PlantaDeTeste.catalogo(), new EstrategiaFilaDaFundicao()));
        assertThrows(IllegalArgumentException.class, () -> new GerenciadorProducao(PlantaDeTeste.materiaPrima(1),
                null, 5.0, PlantaDeTeste.catalogo(), new EstrategiaFilaDaFundicao()));
        assertThrows(IllegalArgumentException.class, () -> new GerenciadorProducao(PlantaDeTeste.materiaPrima(1),
                Cenario.IDEAL, 5.0, null, new EstrategiaFilaDaFundicao()));
        assertThrows(IllegalArgumentException.class, () -> new GerenciadorProducao(PlantaDeTeste.materiaPrima(1),
                Cenario.IDEAL, 5.0, PlantaDeTeste.catalogo(), null));
        assertThrows(IllegalArgumentException.class, () -> new GerenciadorProducao(PlantaDeTeste.materiaPrima(1),
                Cenario.IDEAL, 0.0, PlantaDeTeste.catalogo(), new EstrategiaFilaDaFundicao()));
    }

    @Test
    @DisplayName("o budget inicial vem do cenário")
    void budgetDoCenario() {
        assertEquals(Cenario.IDEAL.getBudgetInicial(), planta.getBudget(), 1e-9);
        assertEquals(Cenario.APOCALIPTICO.getBudgetInicial(),
                PlantaDeTeste.plantaSemMaquinas(Cenario.APOCALIPTICO, 10).getBudget(), 1e-9);
        assertSame(Cenario.IDEAL, planta.getCenarioAtual());
    }

    @Test
    @DisplayName("adicionar máquina aplica o fator de falha e o desgaste do cenário")
    void cenarioAfetaMaquina() {
        Maquina ideal = PlantaDeTeste.insersora();
        ideal.setProbabilidadeFalha(0.2);
        planta.adicionarMaquina(ideal);

        GerenciadorProducao apocaliptica = PlantaDeTeste.plantaSemMaquinas(Cenario.APOCALIPTICO, 10);
        Maquina dura = PlantaDeTeste.insersora();
        dura.setProbabilidadeFalha(0.2);
        apocaliptica.adicionarMaquina(dura);

        assertEquals(0.2 * Cenario.IDEAL.getFatorProbabilidadeFalha(), ideal.getProbabilidadeFalha(), 1e-9);
        assertEquals(0.2 * Cenario.APOCALIPTICO.getFatorProbabilidadeFalha(), dura.getProbabilidadeFalha(), 1e-9);
        assertTrue(dura.getProbabilidadeFalha() > ideal.getProbabilidadeFalha());
    }

    @Test
    @DisplayName("adicionar máquina nula ou repetida é recusado")
    void adicionarMaquinaInvalida() {
        Maquina maquina = PlantaDeTeste.insersora();
        planta.adicionarMaquina(maquina);

        assertThrows(IllegalArgumentException.class, () -> planta.adicionarMaquina(null));
        assertThrows(IllegalArgumentException.class, () -> planta.adicionarMaquina(maquina));
        assertEquals(1, planta.getMaquinas().size());
    }

    @Test
    @DisplayName("as listas expostas não permitem alterar a planta por fora")
    void listasProtegidas() {
        assertThrows(UnsupportedOperationException.class, () -> planta.getDemandas().clear());
        assertThrows(UnsupportedOperationException.class, () -> planta.getArmazem().clear());
        assertThrows(UnsupportedOperationException.class, () -> planta.getCatalogoProdutos().clear());
    }

    // ---------- demandas ----------

    @Test
    @DisplayName("registrar demanda duplicada, vazia ou nula é recusado")
    void registrarDemandaInvalida() {
        int antes = planta.getDemandas().size();

        assertThrows(DemandaInvalidaException.class, () -> planta.registrarDemanda(ENTRADA));
        assertThrows(DemandaInvalidaException.class, () -> planta.registrarDemanda(""));
        assertThrows(DemandaInvalidaException.class, () -> planta.registrarDemanda(null));

        assertEquals(antes, planta.getDemandas().size());
    }

    @Test
    @DisplayName("atualizar a quantidade de uma demanda")
    void atualizarDemanda() {
        planta.atualizarDemanda(ENTRADA, 7);

        assertEquals(7, demandaDe(ENTRADA).getQuantidadeProdutos());
        assertEquals(StatusDemanda.PENDENTE, demandaDe(ENTRADA).getStatus());
    }

    @Test
    @DisplayName("atualizar ou cancelar demanda inexistente, ou quantidade negativa, é recusado")
    void atualizarCancelarInvalido() {
        assertThrows(DemandaInvalidaException.class, () -> planta.atualizarDemanda("Inexistente", 3));
        assertThrows(DemandaInvalidaException.class, () -> planta.cancelarDemanda("Inexistente"));
        assertThrows(DemandaInvalidaException.class, () -> planta.atualizarDemanda(ENTRADA, -5));
        assertEquals(0, demandaDe(ENTRADA).getQuantidadeProdutos());
    }

    @Test
    @DisplayName("cancelar e reabrir uma demanda")
    void cancelarEReabrir() {
        planta.atualizarDemanda(ENTRADA, 3);

        planta.cancelarDemanda(ENTRADA);
        assertEquals(StatusDemanda.CANCELADA, demandaDe(ENTRADA).getStatus());

        planta.atualizarDemanda(ENTRADA, 4);
        assertEquals(StatusDemanda.PENDENTE, demandaDe(ENTRADA).getStatus());
    }

    // ---------- estratégia ----------

    @Test
    @DisplayName("trocar a estratégia em tempo de execução, e recusar estratégia nula")
    void trocarEstrategia() {
        assertEquals("Fila da Fundição (FIFO)", planta.getEstrategiaAtual().getNomeEstrategia());

        planta.setEstrategia(new EstrategiaMaiorLote());

        assertEquals("Maior Lote", planta.getEstrategiaAtual().getNomeEstrategia());
        assertThrows(IllegalArgumentException.class, () -> planta.setEstrategia(null));
        assertEquals("Maior Lote", planta.getEstrategiaAtual().getNomeEstrategia());
    }

    @Test
    @DisplayName("a estratégia ativa decide qual demanda é produzida primeiro")
    void estrategiaDecideProximaProducao() {
        GerenciadorProducao fifo = PlantaDeTeste.plantaCompleta(50);
        fifo.atualizarDemanda(ENTRADA, 1);
        fifo.atualizarDemanda(FLAGSHIP, 5);
        fifo.executarProximaProducao();
        assertEquals(StatusDemanda.CONCLUIDA, fifo.getDemandas().get(0).getStatus());
        assertEquals(StatusDemanda.PENDENTE, fifo.getDemandas().get(2).getStatus());

        GerenciadorProducao maior = PlantaDeTeste.plantaCompleta(50);
        maior.setEstrategia(new EstrategiaMaiorLote());
        maior.atualizarDemanda(ENTRADA, 1);
        maior.atualizarDemanda(FLAGSHIP, 5);
        maior.executarProximaProducao();
        assertEquals(StatusDemanda.PENDENTE, maior.getDemandas().get(0).getStatus());
        assertEquals(StatusDemanda.CONCLUIDA, maior.getDemandas().get(2).getStatus());
    }

    @Test
    @DisplayName("sem demanda elegível, executarProximaProducao lança DemandaInvalidaException")
    void semDemandaElegivel() {
        GerenciadorProducao completa = PlantaDeTeste.plantaCompleta(50);

        assertThrows(DemandaInvalidaException.class, completa::executarProximaProducao);
        assertEquals(Cenario.IDEAL.getBudgetInicial(), completa.getBudget(), 1e-9);
    }

    // ---------- compra de matéria-prima ----------

    @Test
    @DisplayName("comprar matéria-prima soma ao estoque e debita o budget")
    void comprar() {
        planta.comprarMateriaPrima(10);

        assertEquals(30.0, planta.getMateriaPrima().getQuantidade(), 1e-9);
        assertEquals(Cenario.IDEAL.getBudgetInicial() - 10 * PlantaDeTeste.CUSTO_MATERIA_PRIMA,
                planta.getBudget(), 1e-9);
    }

    @Test
    @DisplayName("quantidade não positiva ou abaixo do lote mínimo é recusada sem alterar nada")
    void comprarQuantidadeInvalida() {
        assertThrows(QuantidadeInvalidaException.class, () -> planta.comprarMateriaPrima(0));
        assertThrows(QuantidadeInvalidaException.class, () -> planta.comprarMateriaPrima(-4));
        assertThrows(QuantidadeInvalidaException.class, () -> planta.comprarMateriaPrima(0.5));

        assertEquals(20.0, planta.getMateriaPrima().getQuantidade(), 1e-9);
        assertEquals(Cenario.IDEAL.getBudgetInicial(), planta.getBudget(), 1e-9);
    }

    @Test
    @DisplayName("budget insuficiente: lança RecursoInsuficienteException e nada muda")
    void comprarSemBudget() {
        // 100 unidades custam R$ 5000, mais que os R$ 3000 do cenário Ideal
        RecursoInsuficienteException erro = assertThrows(RecursoInsuficienteException.class,
                () -> planta.comprarMateriaPrima(100));

        assertEquals("Budget", erro.getRecurso());
        assertEquals(5000.0, erro.getNecessario(), 1e-9);
        assertEquals(3000.0, erro.getDisponivel(), 1e-9);
        assertEquals(20.0, planta.getMateriaPrima().getQuantidade(), 1e-9);
        assertEquals(3000.0, planta.getBudget(), 1e-9);
    }

    // ---------- API de restauração (usada pela persistência) ----------

    @Test
    @DisplayName("restaurarBudget aceita zero ou mais e recusa negativo e NaN")
    void restaurarBudget() {
        planta.restaurarBudget(1234.5);
        assertEquals(1234.5, planta.getBudget(), 1e-9);

        assertThrows(IllegalArgumentException.class, () -> planta.restaurarBudget(-1));
        assertThrows(IllegalArgumentException.class, () -> planta.restaurarBudget(Double.NaN));
        assertEquals(1234.5, planta.getBudget(), 1e-9);
    }

    @Test
    @DisplayName("restaurarDemanda valida catálogo, duplicidade e regras da demanda")
    void restaurarDemanda() {
        GerenciadorProducao vazia = new GerenciadorProducao(PlantaDeTeste.materiaPrima(5), Cenario.IDEAL, 5.5,
                PlantaDeTeste.catalogo(), new EstrategiaFilaDaFundicao());

        vazia.restaurarDemanda(ENTRADA, 3, StatusDemanda.CONCLUIDA);
        assertEquals(StatusDemanda.CONCLUIDA, vazia.getDemandas().get(0).getStatus());

        assertThrows(DemandaInvalidaException.class, () -> vazia.restaurarDemanda(ENTRADA, 3, StatusDemanda.PENDENTE));
        assertThrows(DemandaInvalidaException.class,
                () -> vazia.restaurarDemanda("Fora do catalogo", 3, StatusDemanda.PENDENTE));
        assertThrows(DemandaInvalidaException.class, () -> vazia.restaurarDemanda(FLAGSHIP, 3, StatusDemanda.EM_PRODUCAO));
        assertEquals(1, vazia.getDemandas().size());
    }

    @Test
    @DisplayName("restaurarProdutoNoArmazem valida produto, id e duplicidade")
    void restaurarArmazem() {
        planta.restaurarProdutoNoArmazem(ENTRADA, "PROD-T-001-1", 0.2);

        assertEquals(1, planta.getArmazem().size());
        assertEquals(0.2, planta.getArmazem().get(0).getProbabilidadeFalhaAcumulada(), 1e-9);
        assertThrows(IllegalArgumentException.class, () -> planta.restaurarProdutoNoArmazem(ENTRADA, "PROD-T-001-1", 0));
        assertThrows(IllegalArgumentException.class, () -> planta.restaurarProdutoNoArmazem(ENTRADA, " ", 0));
        assertThrows(IllegalArgumentException.class, () -> planta.restaurarProdutoNoArmazem("Fora", "X-1", 0));
        assertThrows(IllegalArgumentException.class, () -> planta.restaurarProdutoNoArmazem(ENTRADA, "PROD-T-001-2", 2));
        assertEquals(1, planta.getArmazem().size());
    }

    @Test
    @DisplayName("restaurarContadorUnidades recusa negativo e a numeração continua dali")
    void restaurarContador() {
        assertThrows(IllegalArgumentException.class, () -> planta.restaurarContadorUnidades(-1));

        GerenciadorProducao completa = PlantaDeTeste.plantaCompleta(20);
        completa.restaurarContadorUnidades(5);
        completa.atualizarDemanda(ENTRADA, 1);
        completa.fabricarDemanda(ENTRADA);

        assertEquals(6, completa.getContadorUnidadesCriadas());
    }
}
