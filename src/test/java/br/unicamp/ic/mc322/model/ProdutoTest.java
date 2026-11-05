package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Produto e seus tiers: polimorfismo e regras")
class ProdutoTest {

    @Test
    @DisplayName("cada tier tem seu tipo, qualidade e tempo de produção")
    void tiersPolimorficos() {
        Produto entrada = new ComponenteEntrada("E", "E", CategoriaProduto.PLACA_DE_VIDEO, 1, 1.0);
        Produto performance = new ComponentePerformance("P", "P", CategoriaProduto.PLACA_DE_VIDEO, 1, 1.0);
        Produto flagship = new ComponenteFlagship("F", "F", CategoriaProduto.PLACA_DE_VIDEO, 1, 1.0);

        assertEquals("Entrada", entrada.getTipo());
        assertEquals("Performance", performance.getTipo());
        assertEquals("Flagship", flagship.getTipo());

        // tempo base da placa de vídeo (10) vezes o fator do tier (0,8 / 1,0 / 1,3)
        assertEquals(8.0, entrada.calcularTempoProducao(), 1e-9);
        assertEquals(10.0, performance.calcularTempoProducao(), 1e-9);
        assertEquals(13.0, flagship.calcularTempoProducao(), 1e-9);

        assertTrue(entrada.getQualidade() < performance.getQualidade());
        assertTrue(performance.getQualidade() < flagship.getQualidade());
    }

    @Test
    @DisplayName("nasce aguardando processamento e processar muda o status")
    void processar() {
        Produto produto = new ComponenteEntrada("E", "E", CategoriaProduto.PROCESSADOR, 1, 0.1);
        assertEquals(StatusProduto.AGUARDANDO_PROCESSAMENTO, produto.getStatus());

        produto.processar();

        assertEquals(StatusProduto.PROCESSADO, produto.getStatus());
    }

    @Test
    @DisplayName("criarUnidade copia as características do molde com novo id")
    void criarUnidade() {
        ComponentePerformance molde = new ComponentePerformance("MOLDE", "Andesite A8000",
                CategoriaProduto.PLACA_DE_VIDEO, 2, 2.5);

        ComponentePerformance unidade = molde.criarUnidade("MOLDE-7");

        assertNotSame(molde, unidade);
        assertEquals("MOLDE-7", unidade.getId());
        assertEquals(molde.getNome(), unidade.getNome());
        assertEquals(molde.getCategoria(), unidade.getCategoria());
        assertEquals(molde.getMassa(), unidade.getMassa(), 1e-9);
        assertEquals(molde.getQuantidadeMateriaPrimaPorUnidade(), unidade.getQuantidadeMateriaPrimaPorUnidade(), 1e-9);
        assertEquals(StatusProduto.AGUARDANDO_PROCESSAMENTO, unidade.getStatus());
    }

    @Test
    @DisplayName("probabilidade de falha acumulada sobe, mas nunca passa de 1")
    void falhaAcumulada() {
        Produto produto = new ComponenteEntrada("E", "E", CategoriaProduto.PROCESSADOR, 1, 0.1);
        assertEquals(0.0, produto.getProbabilidadeFalhaAcumulada(), 1e-9);

        produto.aumentarProbabilidadeFalha();
        double aposUma = produto.getProbabilidadeFalhaAcumulada();
        assertTrue(aposUma > 0.0);

        for (int i = 0; i < 1000; i++) {
            produto.aumentarProbabilidadeFalha();
        }
        assertEquals(1.0, produto.getProbabilidadeFalhaAcumulada(), 1e-9);
    }

    @Test
    @DisplayName("restaurar a probabilidade valida a faixa de 0 a 1")
    void restaurarProbabilidade() {
        Produto produto = new ComponenteEntrada("E", "E", CategoriaProduto.PROCESSADOR, 1, 0.1);

        produto.restaurarProbabilidadeFalhaAcumulada(0.4);
        assertEquals(0.4, produto.getProbabilidadeFalhaAcumulada(), 1e-9);

        assertThrows(IllegalArgumentException.class, () -> produto.restaurarProbabilidadeFalhaAcumulada(-0.1));
        assertThrows(IllegalArgumentException.class, () -> produto.restaurarProbabilidadeFalhaAcumulada(1.1));
        assertThrows(IllegalArgumentException.class,
                () -> produto.restaurarProbabilidadeFalhaAcumulada(Double.NaN));
        assertEquals(0.4, produto.getProbabilidadeFalhaAcumulada(), 1e-9);
    }
}
