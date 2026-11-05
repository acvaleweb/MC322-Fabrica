package br.unicamp.ic.mc322.service;

import static br.unicamp.ic.mc322.suporte.PlantaDeTeste.ENTRADA;
import static br.unicamp.ic.mc322.suporte.PlantaDeTeste.FLAGSHIP;
import static br.unicamp.ic.mc322.suporte.PlantaDeTeste.PERFORMANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.exception.QuantidadeInvalidaException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import br.unicamp.ic.mc322.model.CategoriaProduto;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.ComponenteEntrada;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.InsersoraSMT;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.Produto;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.model.StatusProduto;
import br.unicamp.ic.mc322.strategy.EstrategiaFilaDaFundicao;
import br.unicamp.ic.mc322.suporte.PlantaDeTeste;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Fabricação de um lote e os erros de regra de negócio. Em todo erro, a planta precisa
 * continuar exatamente como estava (estoque, budget e status da demanda).
 */
@DisplayName("GerenciadorProducao: fabricação e erros de negócio")
class GerenciadorFabricacaoTest {

    private static Demanda demandaDe(GerenciadorProducao planta, String produto) {
        return planta.getDemandas().stream()
                .filter(d -> d.getTipoProduto().equals(produto))
                .findFirst()
                .orElseThrow();
    }

    private static void assertPlantaIntacta(GerenciadorProducao planta, double estoque, double budget) {
        assertEquals(estoque, planta.getMateriaPrima().getQuantidade(), 1e-9);
        assertEquals(budget, planta.getBudget(), 1e-9);
        assertTrue(planta.getArmazem().isEmpty());
    }

    // ---------- caminho feliz ----------

    @Test
    @DisplayName("lote bem-sucedido: consome matéria-prima, debita o custo e conclui a demanda")
    void loteBemSucedido() {
        GerenciadorProducao planta = PlantaDeTeste.plantaCompleta(20);
        planta.atualizarDemanda(PERFORMANCE, 3); // 3 x 2 = 6 de matéria-prima, 3 x R$ 30 de operação

        planta.fabricarDemanda(PERFORMANCE);

        assertEquals(14.0, planta.getMateriaPrima().getQuantidade(), 1e-9);
        assertEquals(Cenario.IDEAL.getBudgetInicial() - 3 * PlantaDeTeste.CUSTO_POR_UNIDADE,
                planta.getBudget(), 1e-9);
        assertEquals(StatusDemanda.CONCLUIDA, demandaDe(planta, PERFORMANCE).getStatus());
        assertEquals(3, planta.getContadorUnidadesCriadas());
    }

    @Test
    @DisplayName("só vão para o armazém unidades aprovadas na inspeção, com ids derivados do molde")
    void armazemSoComAprovadas() {
        GerenciadorProducao planta = PlantaDeTeste.plantaCompleta(20);
        planta.atualizarDemanda(ENTRADA, 5);

        planta.fabricarDemanda(ENTRADA);

        List<Produto> armazem = planta.getArmazem();
        assertTrue(armazem.size() <= 5);
        for (Produto produto : armazem) {
            assertEquals(StatusProduto.INSPECIONADO, produto.getStatus());
            assertEquals(ENTRADA, produto.getNome());
            assertTrue(produto.getId().startsWith("PROD-T-001-"));
        }
    }

    @Test
    @DisplayName("uma demanda concluída só volta a ser produzida depois de reaberta")
    void demandaConcluidaPrecisaReabrir() {
        GerenciadorProducao planta = PlantaDeTeste.plantaCompleta(20);
        planta.atualizarDemanda(ENTRADA, 1);
        planta.fabricarDemanda(ENTRADA);

        assertThrows(DemandaInvalidaException.class, () -> planta.fabricarDemanda(ENTRADA));

        planta.atualizarDemanda(ENTRADA, 1);
        planta.fabricarDemanda(ENTRADA);
        assertEquals(StatusDemanda.CONCLUIDA, demandaDe(planta, ENTRADA).getStatus());
    }

    @Test
    @DisplayName("máquina que quebra no meio do lote: unidades são perdidas e o lote termina")
    void quebraNoMeioDoLote() {
        GerenciadorProducao planta = PlantaDeTeste.plantaSemMaquinas(20);
        Maquina insersora = PlantaDeTeste.insersora();
        planta.adicionarMaquina(insersora);
        planta.adicionarMaquina(PlantaDeTeste.montadora());
        planta.adicionarMaquina(PlantaDeTeste.inspetora());
        insersora.setDesgastePorUso(100.0, 100.0); // a primeira unidade já zera a saúde
        planta.atualizarDemanda(ENTRADA, 3);

        planta.fabricarDemanda(ENTRADA);

        assertEquals(StatusDemanda.CONCLUIDA, demandaDe(planta, ENTRADA).getStatus());
        assertTrue(insersora.estaQuebrada());
        assertTrue(planta.getArmazem().size() <= 1); // só a primeira unidade pode ter passado
        assertEquals(3, planta.getContadorUnidadesCriadas());
    }

    // ---------- erros de demanda ----------

    @Test
    @DisplayName("fabricar sem demanda registrada, com quantidade zero, cancelada ou fora do catálogo")
    void erroDeDemanda() {
        GerenciadorProducao planta = PlantaDeTeste.plantaCompleta(20);

        assertThrows(DemandaInvalidaException.class, () -> planta.fabricarDemanda("Inexistente"));
        assertThrows(DemandaInvalidaException.class, () -> planta.fabricarDemanda(ENTRADA)); // quantidade zero

        planta.atualizarDemanda(ENTRADA, 2);
        planta.cancelarDemanda(ENTRADA);
        assertThrows(DemandaInvalidaException.class, () -> planta.fabricarDemanda(ENTRADA));

        planta.registrarDemanda("Fora do catalogo");
        planta.atualizarDemanda("Fora do catalogo", 1);
        assertThrows(DemandaInvalidaException.class, () -> planta.fabricarDemanda("Fora do catalogo"));

        assertPlantaIntacta(planta, 20, Cenario.IDEAL.getBudgetInicial());
    }

    // ---------- erros de linha e máquina ----------

    @Test
    @DisplayName("sem máquinas na linha: MaquinaIndisponivelException e nada é consumido")
    void semMaquinas() {
        GerenciadorProducao planta = PlantaDeTeste.plantaSemMaquinas(20);
        planta.atualizarDemanda(ENTRADA, 2);

        assertThrows(MaquinaIndisponivelException.class, () -> planta.fabricarDemanda(ENTRADA));

        assertPlantaIntacta(planta, 20, Cenario.IDEAL.getBudgetInicial());
        assertEquals(StatusDemanda.PENDENTE, demandaDe(planta, ENTRADA).getStatus());
    }

    @Test
    @DisplayName("máquina quebrada antes de começar: recusa o lote e nada é consumido")
    void maquinaQuebradaAntesDoLote() {
        GerenciadorProducao planta = PlantaDeTeste.plantaSemMaquinas(20);
        Maquina quebrada = PlantaDeTeste.insersora();
        quebrada.setDesgastePorUso(100.0, 100.0);
        quebrada.desgastar();
        planta.adicionarMaquina(quebrada);
        planta.atualizarDemanda(ENTRADA, 2);

        assertThrows(MaquinaIndisponivelException.class, () -> planta.fabricarDemanda(ENTRADA));

        assertPlantaIntacta(planta, 20, Cenario.IDEAL.getBudgetInicial());
        assertEquals(StatusDemanda.PENDENTE, demandaDe(planta, ENTRADA).getStatus());

        // depois do reparo a produção é liberada
        assertEquals(1, planta.repararMaquinas());
        planta.fabricarDemanda(ENTRADA);
        assertEquals(StatusDemanda.CONCLUIDA, demandaDe(planta, ENTRADA).getStatus());
    }

    @Test
    @DisplayName("produto mais pesado que a capacidade da máquina é recusado antes de produzir")
    void produtoExcedeMaquina() {
        GerenciadorProducao planta = PlantaDeTeste.plantaSemMaquinas(20);
        planta.adicionarMaquina(new InsersoraSMT("Fraca", 1.0, 0.0, 10.0)); // Entrada pesa 1,5 kg
        planta.atualizarDemanda(ENTRADA, 1);

        assertThrows(MaquinaIndisponivelException.class, () -> planta.fabricarDemanda(ENTRADA));

        assertPlantaIntacta(planta, 20, Cenario.IDEAL.getBudgetInicial());
    }

    @Test
    @DisplayName("produto mais pesado que a esteira é recusado antes de produzir")
    void produtoExcedeEsteira() {
        List<Produto> catalogo = List.of(
                new ComponenteEntrada("PESADO-1", "Placa Pesada", CategoriaProduto.PLACA_MAE, 1, 6.0));
        GerenciadorProducao planta = new GerenciadorProducao(PlantaDeTeste.materiaPrima(20), Cenario.IDEAL,
                PlantaDeTeste.CAPACIDADE_ESTEIRA, catalogo, new EstrategiaFilaDaFundicao());
        planta.setPausasAtivas(false);
        planta.registrarDemanda("Placa Pesada");
        planta.adicionarMaquina(PlantaDeTeste.insersora());
        planta.atualizarDemanda("Placa Pesada", 1);

        assertThrows(MaquinaIndisponivelException.class, () -> planta.fabricarDemanda("Placa Pesada"));

        assertPlantaIntacta(planta, 20, Cenario.IDEAL.getBudgetInicial());
    }

    // ---------- erros de recurso ----------

    @Test
    @DisplayName("matéria-prima insuficiente: RecursoInsuficienteException com os valores e nada muda")
    void materiaPrimaInsuficiente() {
        GerenciadorProducao planta = PlantaDeTeste.plantaCompleta(5);
        planta.atualizarDemanda(FLAGSHIP, 2); // 2 x 3 = 6, mas só há 5

        RecursoInsuficienteException erro = assertThrows(RecursoInsuficienteException.class,
                () -> planta.fabricarDemanda(FLAGSHIP));

        assertEquals("Wafer de Teste", erro.getRecurso());
        assertEquals(6.0, erro.getNecessario(), 1e-9);
        assertEquals(5.0, erro.getDisponivel(), 1e-9);
        assertPlantaIntacta(planta, 5, Cenario.IDEAL.getBudgetInicial());
        assertEquals(StatusDemanda.PENDENTE, demandaDe(planta, FLAGSHIP).getStatus());
    }

    @Test
    @DisplayName("budget insuficiente para o custo de operação: RecursoInsuficienteException")
    void budgetInsuficiente() {
        // Apocalíptico começa com R$ 1.500; 100 unidades custam R$ 3.000 de operação
        GerenciadorProducao planta = PlantaDeTeste.plantaCompleta(Cenario.APOCALIPTICO, 1000);
        planta.atualizarDemanda(ENTRADA, 100);

        RecursoInsuficienteException erro = assertThrows(RecursoInsuficienteException.class,
                () -> planta.fabricarDemanda(ENTRADA));

        assertEquals("Budget", erro.getRecurso());
        assertEquals(3000.0, erro.getNecessario(), 1e-9);
        assertEquals(1500.0, erro.getDisponivel(), 1e-9);
        assertPlantaIntacta(planta, 1000, 1500.0);
        assertEquals(StatusDemanda.PENDENTE, demandaDe(planta, ENTRADA).getStatus());
    }

    @Test
    @DisplayName("matéria-prima abaixo do lote mínimo de consumo: QuantidadeInvalidaException")
    void abaixoDoLoteMinimo() {
        MateriaPrima exigente = new MateriaPrima("MP-X", "Wafer Exigente", 50, "unidade", 10.0, 5);
        GerenciadorProducao planta = new GerenciadorProducao(exigente, Cenario.IDEAL,
                PlantaDeTeste.CAPACIDADE_ESTEIRA, PlantaDeTeste.catalogo(), new EstrategiaFilaDaFundicao());
        planta.setPausasAtivas(false);
        planta.registrarDemanda(ENTRADA);
        planta.adicionarMaquina(PlantaDeTeste.insersora());
        planta.atualizarDemanda(ENTRADA, 2); // precisa de 2, mas o lote mínimo é 5

        assertThrows(QuantidadeInvalidaException.class, () -> planta.fabricarDemanda(ENTRADA));

        assertPlantaIntacta(planta, 50, Cenario.IDEAL.getBudgetInicial());
    }

    @Test
    @DisplayName("um erro não impede a produção seguinte depois de corrigido")
    void recuperaDepoisDoErro() {
        GerenciadorProducao planta = PlantaDeTeste.plantaCompleta(2);
        planta.atualizarDemanda(FLAGSHIP, 2); // precisa de 6

        assertThrows(RecursoInsuficienteException.class, () -> planta.fabricarDemanda(FLAGSHIP));

        planta.comprarMateriaPrima(10);
        planta.fabricarDemanda(FLAGSHIP);

        assertEquals(StatusDemanda.CONCLUIDA, demandaDe(planta, FLAGSHIP).getStatus());
        assertEquals(6.0, planta.getMateriaPrima().getQuantidade(), 1e-9);
    }
}
