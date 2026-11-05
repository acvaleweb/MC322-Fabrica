package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LinhaDeProducao: agregação de máquinas")
class LinhaDeProducaoTest {
    private LinhaDeProducao linha;
    private Maquina insersora;
    private Maquina montadora;

    @BeforeEach
    void criar() {
        linha = new LinhaDeProducao("Linha T");
        insersora = new InsersoraSMT("Insersora", 10, 0.0, 10.0);
        montadora = new Montadora("Montadora", 10, 0.0, 15.0);
    }

    @Test
    @DisplayName("linha nova está vazia e não precisa de manutenção")
    void linhaNova() {
        assertTrue(linha.estaVazia());
        assertFalse(linha.precisaManutencao());
        assertEquals(0.0, linha.calcularCustoOperacaoPorUnidade(), 1e-9);
    }

    @Test
    @DisplayName("mantém a ordem das máquinas e soma o custo de operação por unidade")
    void ordemECusto() {
        linha.adicionarMaquina(insersora);
        linha.adicionarMaquina(montadora);

        assertEquals(2, linha.getMaquinas().size());
        assertEquals(insersora, linha.getMaquinas().get(0));
        assertEquals(montadora, linha.getMaquinas().get(1));
        assertEquals(25.0, linha.calcularCustoOperacaoPorUnidade(), 1e-9);
    }

    @Test
    @DisplayName("recusa máquina nula ou repetida")
    void recusaNulaERepetida() {
        linha.adicionarMaquina(insersora);

        assertThrows(IllegalArgumentException.class, () -> linha.adicionarMaquina(null));
        assertThrows(IllegalArgumentException.class, () -> linha.adicionarMaquina(insersora));
        assertEquals(1, linha.getMaquinas().size());
    }

    @Test
    @DisplayName("a lista devolvida não permite alterar a linha por fora")
    void listaProtegida() {
        linha.adicionarMaquina(insersora);

        assertThrows(UnsupportedOperationException.class, () -> linha.getMaquinas().clear());
        assertEquals(1, linha.getMaquinas().size());
    }

    @Test
    @DisplayName("remover devolve se a máquina estava na linha")
    void remover() {
        linha.adicionarMaquina(insersora);

        assertTrue(linha.removerMaquina(insersora));
        assertFalse(linha.removerMaquina(insersora));
        assertTrue(linha.estaVazia());
    }

    @Test
    @DisplayName("conta máquinas em risco e repara só as que precisam")
    void riscoEReparo() {
        linha.adicionarMaquina(insersora);
        linha.adicionarMaquina(montadora);
        insersora.setLimiarManutencao(90.0);
        insersora.setDesgastePorUso(20.0, 20.0);
        insersora.desgastar();

        assertEquals(1, linha.contarMaquinasEmRisco());
        assertTrue(linha.precisaManutencao());

        assertEquals(1, linha.repararMaquinas());

        assertEquals(0, linha.contarMaquinasEmRisco());
        assertFalse(linha.precisaManutencao());
    }

    @Test
    @DisplayName("relatório lista a linha, as máquinas e as Partes")
    void relatorio() {
        linha.adicionarMaquina(insersora);

        String relatorio = linha.gerarRelatorioDiagnostico();

        assertTrue(relatorio.contains("Linha T"));
        assertTrue(relatorio.contains("Insersora"));
        assertTrue(relatorio.contains("Motor"));
    }
}
