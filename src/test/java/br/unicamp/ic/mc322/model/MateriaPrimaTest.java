package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.unicamp.ic.mc322.exception.QuantidadeInvalidaException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MateriaPrima: estoque, consumo e validações")
class MateriaPrimaTest {
    private MateriaPrima estoque;

    @BeforeEach
    void criarEstoque() {
        estoque = new MateriaPrima("MP-1", "Wafer", 10, "unidade", 50.0, 2);
    }

    @Test
    @DisplayName("consumir desconta do estoque")
    void consumirDesconta() {
        estoque.consumir(4);

        assertEquals(6.0, estoque.getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("consumir acima do estoque lança RecursoInsuficienteException e não altera nada")
    void consumirAcimaDoEstoque() {
        RecursoInsuficienteException erro = assertThrows(RecursoInsuficienteException.class,
                () -> estoque.consumir(11));

        assertEquals(11.0, erro.getNecessario(), 1e-9);
        assertEquals(10.0, erro.getDisponivel(), 1e-9);
        assertEquals(10.0, estoque.getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("consumir abaixo do lote mínimo ou quantidade não positiva é recusado")
    void consumirQuantidadeInvalida() {
        assertThrows(QuantidadeInvalidaException.class, () -> estoque.consumir(1));
        assertThrows(QuantidadeInvalidaException.class, () -> estoque.consumir(0));
        assertThrows(QuantidadeInvalidaException.class, () -> estoque.consumir(-3));

        assertEquals(10.0, estoque.getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("adicionarEstoque soma, e recusa quantidade não positiva")
    void adicionarEstoque() {
        estoque.adicionarEstoque(5);
        assertEquals(15.0, estoque.getQuantidade(), 1e-9);

        assertThrows(QuantidadeInvalidaException.class, () -> estoque.adicionarEstoque(0));
        assertThrows(QuantidadeInvalidaException.class, () -> estoque.adicionarEstoque(-1));
        assertEquals(15.0, estoque.getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("consultas de disponibilidade e lote mínimo")
    void consultas() {
        assertTrue(estoque.verificarDisponibilidade(10));
        assertFalse(estoque.verificarDisponibilidade(10.5));
        assertTrue(estoque.atendeLoteMinimo(2));
        assertFalse(estoque.atendeLoteMinimo(1.9));
    }

    @Test
    @DisplayName("construtor recusa dados inválidos")
    void construtorRecusaInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("", "W", 1, "u", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("I", " ", 1, "u", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("I", "W", 1, null, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("I", "W", -1, "u", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("I", "W", 1, "u", -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("I", "W", 1, "u", 1, -1));
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("I", "W", Double.NaN, "u", 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new MateriaPrima("I", "W", Double.POSITIVE_INFINITY, "u", 1, 1));
    }
}
