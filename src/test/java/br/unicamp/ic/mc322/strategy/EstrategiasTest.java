package br.unicamp.ic.mc322.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import br.unicamp.ic.mc322.model.Demanda;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Estratégias de produção (Strategy): escolha da próxima demanda")
class EstrategiasTest {
    private Demanda semQuantidade;
    private Demanda pequena;
    private Demanda grande;
    private Demanda media;
    private List<Demanda> demandas;

    @BeforeEach
    void criarDemandas() {
        semQuantidade = new Demanda("Zero");
        pequena = demanda("Pequena", 2);
        grande = demanda("Grande", 50);
        media = demanda("Media", 10);
        demandas = new ArrayList<>(List.of(semQuantidade, pequena, grande, media));
    }

    private static Demanda demanda(String nome, int quantidade) {
        Demanda demanda = new Demanda(nome);
        demanda.atualizarQuantidade(quantidade);
        return demanda;
    }

    @Test
    @DisplayName("Fila da Fundição: primeira demanda pendente com quantidade, na ordem de cadastro")
    void filaDaFundicao() {
        EstrategiaProducao estrategia = new EstrategiaFilaDaFundicao();

        assertSame(pequena, estrategia.selecionarDemanda(demandas, 1000));
        assertEquals("Fila da Fundição (FIFO)", estrategia.getNomeEstrategia());
    }

    @Test
    @DisplayName("Maior Lote: demanda pendente com mais unidades, ignorando o orçamento")
    void maiorLote() {
        EstrategiaProducao estrategia = new EstrategiaMaiorLote();

        assertSame(grande, estrategia.selecionarDemanda(demandas, 1));
        assertEquals("Maior Lote", estrategia.getNomeEstrategia());
    }

    @Test
    @DisplayName("Máximo Rendimento: maior lote que cabe no orçamento (em unidades)")
    void maximoRendimento() {
        EstrategiaProducao estrategia = new EstrategiaMaximoRendimento();

        assertSame(grande, estrategia.selecionarDemanda(demandas, 100));
        assertSame(media, estrategia.selecionarDemanda(demandas, 20));
        assertSame(pequena, estrategia.selecionarDemanda(demandas, 5));
        assertNull(estrategia.selecionarDemanda(demandas, 1));
        assertEquals("Máximo Rendimento", estrategia.getNomeEstrategia());
    }

    @Test
    @DisplayName("as estratégias ignoram demandas que não estão pendentes")
    void ignoramNaoPendentes() {
        grande.cancelar();
        pequena.iniciarProducao();

        assertSame(media, new EstrategiaMaiorLote().selecionarDemanda(demandas, 1000));
        assertSame(media, new EstrategiaFilaDaFundicao().selecionarDemanda(demandas, 1000));
        assertSame(media, new EstrategiaMaximoRendimento().selecionarDemanda(demandas, 1000));
    }

    @Test
    @DisplayName("sem demanda elegível, todas devolvem null")
    void semElegivel() {
        List<Demanda> vazias = List.of(new Demanda("A"), new Demanda("B"));

        assertNull(new EstrategiaFilaDaFundicao().selecionarDemanda(vazias, 1000));
        assertNull(new EstrategiaMaiorLote().selecionarDemanda(vazias, 1000));
        assertNull(new EstrategiaMaximoRendimento().selecionarDemanda(vazias, 1000));
        assertNull(new EstrategiaMaiorLote().selecionarDemanda(List.of(), 1000));
    }

    @Test
    @DisplayName("estratégias diferentes escolhem demandas diferentes sobre os mesmos dados")
    void comportamentoPolimorfico() {
        List<EstrategiaProducao> estrategias = List.of(new EstrategiaFilaDaFundicao(), new EstrategiaMaiorLote());

        assertEquals("Pequena", estrategias.get(0).selecionarDemanda(demandas, 1000).getTipoProduto());
        assertEquals("Grande", estrategias.get(1).selecionarDemanda(demandas, 1000).getTipoProduto());
    }
}
