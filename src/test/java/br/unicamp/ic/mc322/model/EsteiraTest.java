package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Esteira: transporte de um item por vez")
class EsteiraTest {
    private Esteira esteira;
    private Produto leve;

    @BeforeEach
    void criar() {
        esteira = new Esteira(5.0);
        leve = new ComponenteEntrada("P-1", "Leve", CategoriaProduto.PROCESSADOR, 1, 0.5);
    }

    @Test
    @DisplayName("capacidade precisa ser positiva e finita")
    void capacidadeInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new Esteira(0));
        assertThrows(IllegalArgumentException.class, () -> new Esteira(-2));
        assertThrows(IllegalArgumentException.class, () -> new Esteira(Double.NaN));
    }

    @Test
    @DisplayName("transporta um item: adicionar e remover devolvem o mesmo produto")
    void adicionarERemover() {
        esteira.ligar();
        esteira.adicionarItem(leve);

        assertFalse(esteira.estaVazia());
        assertEquals(0.5, esteira.getQuantidade(), 1e-9);
        assertSame(leve, esteira.removerProduto());
        assertTrue(esteira.estaVazia());
    }

    @Test
    @DisplayName("esteira parada não aceita nem entrega item")
    void esteiraParada() {
        assertThrows(MaquinaIndisponivelException.class, () -> esteira.adicionarItem(leve));

        esteira.ligar();
        esteira.adicionarItem(leve);
        esteira.desligar();
        assertThrows(MaquinaIndisponivelException.class, esteira::removerProduto);
    }

    @Test
    @DisplayName("não aceita segundo item enquanto estiver ocupada")
    void esteiraOcupada() {
        esteira.ligar();
        esteira.adicionarItem(leve);
        Produto outro = new ComponenteEntrada("P-2", "Outro", CategoriaProduto.PROCESSADOR, 1, 0.5);

        assertThrows(MaquinaIndisponivelException.class, () -> esteira.adicionarItem(outro));
        assertSame(leve, esteira.getProduto());
    }

    @Test
    @DisplayName("recusa produto mais pesado que a capacidade")
    void excedeCapacidade() {
        esteira.ligar();
        Produto pesado = new ComponenteEntrada("P-3", "Pesado", CategoriaProduto.PLACA_DE_VIDEO, 1, 9.0);

        assertThrows(MaquinaIndisponivelException.class, () -> esteira.adicionarItem(pesado));
        assertTrue(esteira.estaVazia());
        assertTrue(esteira.verificarCapacidade(5.0));
        assertFalse(esteira.verificarCapacidade(5.1));
        assertFalse(esteira.verificarCapacidade(0));
    }

    @Test
    @DisplayName("produto nulo e remoção com a esteira vazia são recusados")
    void nuloEVazia() {
        esteira.ligar();

        assertThrows(IllegalArgumentException.class, () -> esteira.adicionarItem(null));
        assertThrows(MaquinaIndisponivelException.class, esteira::removerProduto);
    }
}
