package br.unicamp.ic.mc322.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exceções da planta: hierarquia e dados carregados")
class ExcecoesTest {

    @Test
    @DisplayName("todas as exceções de regra de negócio são PlantaException não verificadas")
    void hierarquiaDeNegocio() {
        assertTrue(RuntimeException.class.isAssignableFrom(PlantaException.class));
        assertTrue(PlantaException.class.isAssignableFrom(DemandaInvalidaException.class));
        assertTrue(PlantaException.class.isAssignableFrom(MaquinaIndisponivelException.class));
        assertTrue(PlantaException.class.isAssignableFrom(QuantidadeInvalidaException.class));
        assertTrue(PlantaException.class.isAssignableFrom(RecursoInsuficienteException.class));
    }

    @Test
    @DisplayName("as exceções de persistência formam uma subárvore própria")
    void hierarquiaDePersistencia() {
        assertTrue(PlantaException.class.isAssignableFrom(PersistenciaException.class));
        assertTrue(PersistenciaException.class.isAssignableFrom(ArquivoNaoEncontradoException.class));
        assertTrue(PersistenciaException.class.isAssignableFrom(ArquivoCorrompidoException.class));
        assertTrue(PersistenciaException.class.isAssignableFrom(DadosInvalidosException.class));
    }

    @Test
    @DisplayName("RecursoInsuficienteException guarda recurso, necessário e disponível")
    void recursoInsuficiente() {
        RecursoInsuficienteException erro = new RecursoInsuficienteException("Budget", 120.5, 30.0);

        assertEquals("Budget", erro.getRecurso());
        assertEquals(120.5, erro.getNecessario(), 1e-9);
        assertEquals(30.0, erro.getDisponivel(), 1e-9);
        assertTrue(erro.getMessage().contains("Budget"));
    }

    @Test
    @DisplayName("ArquivoNaoEncontradoException guarda o caminho")
    void arquivoNaoEncontrado() {
        ArquivoNaoEncontradoException erro = new ArquivoNaoEncontradoException("dados/planta.xml");

        assertEquals("dados/planta.xml", erro.getCaminho());
        assertTrue(erro.getMessage().contains("dados/planta.xml"));
    }

    @Test
    @DisplayName("a causa original é preservada")
    void preservaCausa() {
        Exception causa = new IllegalStateException("original");

        PlantaException planta = new PlantaException("falhou", causa);
        PersistenciaException persistencia = new PersistenciaException("falhou", causa);
        ArquivoCorrompidoException corrompido = new ArquivoCorrompidoException("falhou", causa);
        DadosInvalidosException invalidos = new DadosInvalidosException("falhou", causa);

        assertSame(causa, planta.getCause());
        assertSame(causa, persistencia.getCause());
        assertInstanceOf(PersistenciaException.class, corrompido);
        assertSame(causa, corrompido.getCause());
        assertSame(causa, invalidos.getCause());
        assertEquals("falhou", invalidos.getMessage());
    }
}
