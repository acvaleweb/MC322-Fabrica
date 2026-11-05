package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Demanda: ciclo de vida e regras de negócio")
class DemandaTest {

    @Test
    @DisplayName("nasce pendente e com quantidade zero")
    void nascePendenteComQuantidadeZero() {
        Demanda demanda = new Demanda("Andesite A7000");

        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
        assertEquals(0, demanda.getQuantidadeProdutos());
        assertEquals("Andesite A7000", demanda.getTipoProduto());
    }

    @Test
    @DisplayName("nome do produto vazio ou nulo é recusado")
    void recusaNomeVazio() {
        assertThrows(DemandaInvalidaException.class, () -> new Demanda(""));
        assertThrows(DemandaInvalidaException.class, () -> new Demanda("   "));
        assertThrows(DemandaInvalidaException.class, () -> new Demanda(null));
    }

    @Test
    @DisplayName("fluxo normal: pendente, em produção, concluída")
    void fluxoNormal() {
        Demanda demanda = new Demanda("X");
        demanda.atualizarQuantidade(5);

        demanda.iniciarProducao();
        assertEquals(StatusDemanda.EM_PRODUCAO, demanda.getStatus());

        demanda.concluir();
        assertEquals(StatusDemanda.CONCLUIDA, demanda.getStatus());
    }

    @Test
    @DisplayName("quantidade negativa é recusada e não altera a demanda")
    void recusaQuantidadeNegativa() {
        Demanda demanda = new Demanda("X");
        demanda.atualizarQuantidade(4);

        assertThrows(DemandaInvalidaException.class, () -> demanda.atualizarQuantidade(-1));

        assertEquals(4, demanda.getQuantidadeProdutos());
        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
    }

    @Test
    @DisplayName("não altera a quantidade de uma demanda em produção")
    void naoAlteraEmProducao() {
        Demanda demanda = new Demanda("X");
        demanda.atualizarQuantidade(4);
        demanda.iniciarProducao();

        assertThrows(DemandaInvalidaException.class, () -> demanda.atualizarQuantidade(9));

        assertEquals(4, demanda.getQuantidadeProdutos());
    }

    @Test
    @DisplayName("só inicia a produção de uma demanda pendente")
    void soIniciaPendente() {
        Demanda demanda = new Demanda("X");
        demanda.atualizarQuantidade(1);
        demanda.iniciarProducao();

        assertThrows(DemandaInvalidaException.class, demanda::iniciarProducao);

        demanda.concluir();
        assertThrows(DemandaInvalidaException.class, demanda::iniciarProducao);
    }

    @Test
    @DisplayName("só conclui uma demanda em produção")
    void soConcluiEmProducao() {
        Demanda pendente = new Demanda("X");
        assertThrows(DemandaInvalidaException.class, pendente::concluir);

        Demanda cancelada = new Demanda("X");
        cancelada.cancelar();
        assertThrows(DemandaInvalidaException.class, cancelada::concluir);
    }

    @Test
    @DisplayName("cancela pendente, mas não uma demanda já concluída")
    void cancelamento() {
        Demanda demanda = new Demanda("X");
        demanda.cancelar();
        assertEquals(StatusDemanda.CANCELADA, demanda.getStatus());

        Demanda concluida = new Demanda("X");
        concluida.atualizarQuantidade(1);
        concluida.iniciarProducao();
        concluida.concluir();
        assertThrows(DemandaInvalidaException.class, concluida::cancelar);
        assertEquals(StatusDemanda.CONCLUIDA, concluida.getStatus());
    }

    @Test
    @DisplayName("demanda concluída ou cancelada é reaberta como pendente ao mudar a quantidade")
    void reabreAoAtualizarQuantidade() {
        Demanda demanda = new Demanda("X");
        demanda.cancelar();

        demanda.atualizarQuantidade(3);

        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
        assertEquals(3, demanda.getQuantidadeProdutos());
    }

    @Test
    @DisplayName("calcula matéria-prima necessária e custo de operação do lote")
    void calculosDoLote() {
        Demanda demanda = new Demanda("Andesite A8000");
        demanda.atualizarQuantidade(4);
        Produto molde = new ComponentePerformance("P", "Andesite A8000", CategoriaProduto.PLACA_DE_VIDEO, 2, 2.5);

        assertEquals(8.0, demanda.calcularMateriaPrimaNecessaria(molde), 1e-9);
        assertEquals(120.0, demanda.calcularCustoOperacaoEstimado(30.0), 1e-9);
    }

    @Test
    @DisplayName("restaurar: reconstrói pendente e concluída válidas")
    void restauraEstadosValidos() {
        Demanda pendente = Demanda.restaurar("X", 7, StatusDemanda.PENDENTE);
        Demanda concluida = Demanda.restaurar("X", 2, StatusDemanda.CONCLUIDA);
        Demanda cancelada = Demanda.restaurar("X", 0, StatusDemanda.CANCELADA);

        assertEquals(7, pendente.getQuantidadeProdutos());
        assertEquals(StatusDemanda.CONCLUIDA, concluida.getStatus());
        assertEquals(StatusDemanda.CANCELADA, cancelada.getStatus());
    }

    @Test
    @DisplayName("restaurar: recusa estados que violam as regras da demanda")
    void restauraRecusaInvalidos() {
        assertThrows(DemandaInvalidaException.class, () -> Demanda.restaurar("X", -1, StatusDemanda.PENDENTE));
        assertThrows(DemandaInvalidaException.class, () -> Demanda.restaurar("X", 3, StatusDemanda.EM_PRODUCAO));
        assertThrows(DemandaInvalidaException.class, () -> Demanda.restaurar("X", 0, StatusDemanda.CONCLUIDA));
        assertThrows(DemandaInvalidaException.class, () -> Demanda.restaurar("X", 3, null));
        assertThrows(DemandaInvalidaException.class, () -> Demanda.restaurar(" ", 3, StatusDemanda.PENDENTE));
    }
}
