package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Maquina e suas Partes (composição): operação, desgaste e estado")
class MaquinaTest {
    private InsersoraSMT insersora;
    private Produto produto;

    @BeforeEach
    void criar() {
        insersora = new InsersoraSMT("Insersora T", 10.0, 0.0, 10.0);
        insersora.setRandom(new Random(1));
        produto = new ComponenteEntrada("E-1", "Entrada", CategoriaProduto.PLACA_DE_VIDEO, 1, 1.5);
    }

    @Test
    @DisplayName("nasce desligada, saudável e com motor, sensor e 2 atuadores")
    void estadoInicial() {
        assertFalse(insersora.estaLigada());
        assertEquals(100.0, insersora.getSaude(), 1e-9);
        assertFalse(insersora.estaQuebrada());
        assertFalse(insersora.precisaManutencao());
        assertEquals(2, insersora.getQuantidadeAtuadores());
        assertEquals(2, insersora.contarAtuadoresOperacionais());
        assertEquals(25.0, insersora.lerTemperaturaMotor(), 1e-9);
        assertEquals(100.0, insersora.lerCalibracaoSensor(), 1e-9);
    }

    @Test
    @DisplayName("construtor recusa dados inválidos")
    void construtorRecusaInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new InsersoraSMT("", 10, 0.1, 1));
        assertThrows(IllegalArgumentException.class, () -> new InsersoraSMT("M", 0, 0.1, 1));
        assertThrows(IllegalArgumentException.class, () -> new InsersoraSMT("M", -5, 0.1, 1));
        assertThrows(IllegalArgumentException.class, () -> new InsersoraSMT("M", 10, 1.5, 1));
        assertThrows(IllegalArgumentException.class, () -> new InsersoraSMT("M", 10, -0.1, 1));
        assertThrows(IllegalArgumentException.class, () -> new InsersoraSMT("M", 10, Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> new InsersoraSMT("M", 10, 0.1, -1));
    }

    @Test
    @DisplayName("ligada processa o produto; a insersora muda o status para PROCESSADO")
    void processaQuandoLigada() {
        insersora.ligar();

        insersora.processar(produto);

        assertEquals(StatusProduto.PROCESSADO, produto.getStatus());
    }

    @Test
    @DisplayName("desligada não opera")
    void desligadaNaoOpera() {
        assertThrows(MaquinaIndisponivelException.class, () -> insersora.processar(produto));
        assertEquals(StatusProduto.AGUARDANDO_PROCESSAMENTO, produto.getStatus());
    }

    @Test
    @DisplayName("produto nulo é recusado")
    void produtoNulo() {
        insersora.ligar();

        assertThrows(IllegalArgumentException.class, () -> insersora.processar(null));
    }

    @Test
    @DisplayName("produto mais pesado que a capacidade da máquina é recusado")
    void produtoPesadoDemais() {
        insersora.ligar();
        Produto pesado = new ComponenteEntrada("E-2", "Pesado", CategoriaProduto.PLACA_DE_VIDEO, 1, 11.0);

        assertThrows(MaquinaIndisponivelException.class, () -> insersora.processar(pesado));
    }

    @Test
    @DisplayName("máquina quebrada (saúde zero) não opera até ser reparada")
    void quebradaNaoOpera() {
        insersora.setDesgastePorUso(100.0, 100.0);
        insersora.desgastar();
        insersora.ligar();

        assertTrue(insersora.estaQuebrada());
        assertThrows(MaquinaIndisponivelException.class, () -> insersora.processar(produto));

        insersora.reparar();
        assertFalse(insersora.estaQuebrada());
        assertEquals(100.0, insersora.getSaude(), 1e-9);
        insersora.processar(produto);
        assertEquals(StatusProduto.PROCESSADO, produto.getStatus());
    }

    @Test
    @DisplayName("desgaste reduz a saúde, nunca abaixo de zero, e propaga para as Partes")
    void desgaste() {
        insersora.setDesgastePorUso(20.0, 20.0);

        insersora.desgastar();
        assertEquals(80.0, insersora.getSaude(), 1e-9);
        assertTrue(insersora.lerTemperaturaMotor() > 25.0);
        assertTrue(insersora.lerCalibracaoSensor() < 100.0);

        for (int i = 0; i < 10; i++) {
            insersora.desgastar();
        }
        assertEquals(0.0, insersora.getSaude(), 1e-9);
    }

    @Test
    @DisplayName("abaixo do limiar a máquina precisa de manutenção; reparar restaura tudo")
    void manutencaoERepare() {
        insersora.setLimiarManutencao(90.0);
        insersora.setDesgastePorUso(15.0, 15.0);
        insersora.desgastar();

        assertTrue(insersora.precisaManutencao());
        assertTrue(insersora.gerarRelatorioDiagnostico().contains("MANUTENCAO NECESSARIA"));

        insersora.reparar();

        assertFalse(insersora.precisaManutencao());
        assertEquals(25.0, insersora.lerTemperaturaMotor(), 1e-9);
        assertEquals(100.0, insersora.lerCalibracaoSensor(), 1e-9);
        assertEquals(2, insersora.contarAtuadoresOperacionais());
    }

    @Test
    @DisplayName("desligar esfria o motor (mas nunca abaixo da temperatura ambiente)")
    void desligarEsfria() {
        insersora.setDesgastePorUso(10.0, 10.0);
        insersora.ligar();
        insersora.desgastar();
        double quente = insersora.lerTemperaturaMotor();

        insersora.desligar();

        assertTrue(insersora.lerTemperaturaMotor() < quente);
        assertTrue(insersora.lerTemperaturaMotor() >= 25.0);
    }

    @Test
    @DisplayName("relatório de diagnóstico inclui as Partes da máquina")
    void relatorioComPartes() {
        String relatorio = insersora.gerarRelatorioDiagnostico();

        assertTrue(relatorio.contains("Insersora SMT Insersora T"));
        assertTrue(relatorio.contains("Motor"));
        assertTrue(relatorio.contains("Sensor"));
        assertTrue(relatorio.contains("Atuador 1"));
        assertTrue(relatorio.contains("Atuador 2"));
    }

    @Test
    @DisplayName("a inspetora só inspeciona produto já processado")
    void inspetoraExigeProcessado() {
        Inspetora inspetora = new Inspetora("Inspetora T", 10.0, 0.0, 10.0);
        inspetora.setRandom(new Random(1));
        inspetora.ligar();

        assertThrows(MaquinaIndisponivelException.class, () -> inspetora.processar(produto));

        produto.processar();
        inspetora.processar(produto);
        StatusProduto resultado = produto.getStatus();
        assertTrue(resultado == StatusProduto.INSPECIONADO || resultado == StatusProduto.REJEITADO);
    }

    @Test
    @DisplayName("tipos das máquinas")
    void tipos() {
        assertEquals("Insersora SMT", insersora.getTipo());
        assertEquals("Montadora", new Montadora("M", 10, 0.0, 1).getTipo());
        assertEquals("Inspetora", new Inspetora("I", 10, 0.0, 1).getTipo());
    }

    @Test
    @DisplayName("obterEstado devolve um retrato fiel e restaurarEstado o reaplica")
    void estadoIdaEVolta() {
        insersora.setDesgastePorUso(12.0, 12.0);
        insersora.setLimiarManutencao(40.0);
        insersora.desgastar();
        EstadoMaquina estado = insersora.obterEstado();

        InsersoraSMT outra = new InsersoraSMT("Outra", 10.0, 0.3, 10.0);
        outra.restaurarEstado(estado);

        assertEquals(insersora.getSaude(), outra.getSaude(), 1e-9);
        assertEquals(insersora.getLimiarManutencao(), outra.getLimiarManutencao(), 1e-9);
        assertEquals(insersora.getProbabilidadeFalha(), outra.getProbabilidadeFalha(), 1e-9);
        assertEquals(insersora.lerTemperaturaMotor(), outra.lerTemperaturaMotor(), 1e-9);
        assertEquals(insersora.lerCalibracaoSensor(), outra.lerCalibracaoSensor(), 1e-9);
        assertEquals(insersora.contarAtuadoresOperacionais(), outra.contarAtuadoresOperacionais());
        assertEquals(insersora.obterEstado(), outra.obterEstado());
    }

    @Test
    @DisplayName("o estado devolvido é um retrato: alterar a máquina depois não o modifica")
    void estadoEUmRetrato() {
        EstadoMaquina antes = insersora.obterEstado();
        insersora.setDesgastePorUso(30.0, 30.0);
        insersora.desgastar();

        assertEquals(100.0, antes.saude(), 1e-9);
        assertNotNull(antes.integridadeAtuadores());
    }

    @Test
    @DisplayName("restaurarEstado recusa estado inválido sem alterar a máquina")
    void restaurarRecusaInvalido() {
        EstadoMaquina valido = insersora.obterEstado();
        EstadoMaquina saudeAcima = new EstadoMaquina(120.0, valido.limiarManutencao(),
                valido.desgasteMinimoPorUso(), valido.desgasteMaximoPorUso(), valido.probabilidadeFalha(),
                valido.temperaturaMotor(), valido.calibracaoSensor(), valido.integridadeAtuadores());
        EstadoMaquina poucosAtuadores = new EstadoMaquina(50.0, valido.limiarManutencao(),
                valido.desgasteMinimoPorUso(), valido.desgasteMaximoPorUso(), valido.probabilidadeFalha(),
                valido.temperaturaMotor(), valido.calibracaoSensor(), List.of(100.0));
        EstadoMaquina motorFrio = new EstadoMaquina(50.0, valido.limiarManutencao(),
                valido.desgasteMinimoPorUso(), valido.desgasteMaximoPorUso(), valido.probabilidadeFalha(),
                10.0, valido.calibracaoSensor(), valido.integridadeAtuadores());
        EstadoMaquina falhaNan = new EstadoMaquina(50.0, valido.limiarManutencao(),
                valido.desgasteMinimoPorUso(), valido.desgasteMaximoPorUso(), Double.NaN,
                valido.temperaturaMotor(), valido.calibracaoSensor(), valido.integridadeAtuadores());

        assertThrows(IllegalArgumentException.class, () -> insersora.restaurarEstado(null));
        assertThrows(IllegalArgumentException.class, () -> insersora.restaurarEstado(saudeAcima));
        assertThrows(IllegalArgumentException.class, () -> insersora.restaurarEstado(poucosAtuadores));
        assertThrows(IllegalArgumentException.class, () -> insersora.restaurarEstado(motorFrio));
        assertThrows(IllegalArgumentException.class, () -> insersora.restaurarEstado(falhaNan));

        assertEquals(valido, insersora.obterEstado());
    }
}
