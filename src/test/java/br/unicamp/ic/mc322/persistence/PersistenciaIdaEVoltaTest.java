package br.unicamp.ic.mc322.persistence;

import static br.unicamp.ic.mc322.suporte.PlantaPersistivel.A8000;
import static br.unicamp.ic.mc322.suporte.PlantaPersistivel.catalogo;
import static br.unicamp.ic.mc322.suporte.PlantaPersistivel.descrever;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.unicamp.ic.mc322.model.ComponenteEntrada;
import br.unicamp.ic.mc322.model.Inspetora;
import br.unicamp.ic.mc322.model.InsersoraSMT;
import br.unicamp.ic.mc322.model.Montadora;
import br.unicamp.ic.mc322.model.Produto;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.strategy.EstrategiaFilaDaFundicao;
import br.unicamp.ic.mc322.suporte.PlantaPersistivel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Salvar e carregar: o que sai do arquivo precisa ser igual ao que entrou. Todos os
 * testes usam uma pasta temporária própria, nunca o arquivo real da aplicação.
 */
@DisplayName("Persistência: salvar e carregar (ida e volta)")
class PersistenciaIdaEVoltaTest {
    @TempDir
    Path pasta;

    private Path arquivo;
    private PersistenciaPlanta persistencia;
    private GerenciadorProducao original;

    @BeforeEach
    void preparar() {
        arquivo = pasta.resolve("planta.xml");
        persistencia = new PersistenciaPlanta(arquivo);
        original = PlantaPersistivel.criar();
    }

    private GerenciadorProducao salvarECarregar() {
        persistencia.salvar(original);
        GerenciadorProducao carregada = persistencia.carregar(catalogo());
        carregada.setPausasAtivas(false);
        return carregada;
    }

    @Test
    @DisplayName("o estado completo é idêntico depois de salvar e carregar")
    void estadoIdentico() {
        String antes = descrever(original);

        GerenciadorProducao carregada = salvarECarregar();

        assertEquals(antes, descrever(carregada));
        assertNotSame(original, carregada);
    }

    @Test
    @DisplayName("as coleções e relacionamentos são reconstruídos: máquinas, demandas e armazém")
    void colecoesERelacionamentos() {
        GerenciadorProducao carregada = salvarECarregar();

        assertEquals(3, carregada.getMaquinas().size());
        assertInstanceOf(InsersoraSMT.class, carregada.getMaquinas().get(0));
        assertInstanceOf(Montadora.class, carregada.getMaquinas().get(1));
        assertInstanceOf(Inspetora.class, carregada.getMaquinas().get(2));

        assertEquals(2, carregada.getDemandas().size());
        assertEquals(StatusDemanda.CONCLUIDA, carregada.getDemandas().get(0).getStatus());
        assertEquals(StatusDemanda.PENDENTE, carregada.getDemandas().get(1).getStatus());

        assertFalse(carregada.getArmazem().isEmpty());
        assertEquals(original.getArmazem().size(), carregada.getArmazem().size());
        assertInstanceOf(ComponenteEntrada.class, carregada.getArmazem().get(0));
    }

    @Test
    @DisplayName("as Partes das máquinas (motor, sensor, atuadores) são preservadas")
    void partesPreservadas() {
        GerenciadorProducao carregada = salvarECarregar();

        assertEquals(PlantaPersistivel.diagnosticoDaPrimeiraMaquina(original),
                PlantaPersistivel.diagnosticoDaPrimeiraMaquina(carregada));
        assertEquals(original.getMaquinas().get(0).lerTemperaturaMotor(),
                carregada.getMaquinas().get(0).lerTemperaturaMotor(), 1e-9);
        assertEquals(original.getMaquinas().get(0).lerCalibracaoSensor(),
                carregada.getMaquinas().get(0).lerCalibracaoSensor(), 1e-9);
    }

    @Test
    @DisplayName("a chance de falha salva não é multiplicada de novo pelo cenário ao carregar")
    void naoReaplicaCenario() {
        double antes = original.getMaquinas().get(0).getProbabilidadeFalha();

        GerenciadorProducao carregada = salvarECarregar();

        assertEquals(antes, carregada.getMaquinas().get(0).getProbabilidadeFalha(), 1e-12);
    }

    @Test
    @DisplayName("salvar a planta carregada gera um arquivo idêntico ao original")
    void estavelAoSalvarDeNovo() throws IOException {
        GerenciadorProducao carregada = salvarECarregar();
        String primeiro = Files.readString(arquivo, StandardCharsets.UTF_8);

        Path outro = pasta.resolve("segunda.xml");
        new PersistenciaPlanta(outro).salvar(carregada);

        assertEquals(primeiro, Files.readString(outro, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("a planta carregada é independente da original")
    void objetosIndependentes() {
        GerenciadorProducao carregada = salvarECarregar();
        String antes = descrever(original);

        carregada.atualizarDemanda(A8000, 9);
        carregada.comprarMateriaPrima(5);

        assertEquals(antes, descrever(original));
    }

    @Test
    @DisplayName("a planta carregada continua produzindo, e a numeração das unidades continua")
    void continuaProduzindo() {
        GerenciadorProducao carregada = salvarECarregar();
        int contadorAntes = carregada.getContadorUnidadesCriadas();
        int armazemAntes = carregada.getArmazem().size();

        PlantaPersistivel.silenciar(() -> carregada.fabricarDemanda(A8000));

        assertEquals(StatusDemanda.CONCLUIDA, carregada.getDemandas().get(1).getStatus());
        assertEquals(contadorAntes + 2, carregada.getContadorUnidadesCriadas());
        Set<String> ids = new HashSet<>();
        for (Produto produto : carregada.getArmazem()) {
            ids.add(produto.getId());
        }
        assertEquals(carregada.getArmazem().size(), ids.size()); // nenhum id repetido
        assertTrue(carregada.getArmazem().size() >= armazemAntes);
    }

    @Test
    @DisplayName("a estratégia salva é a estratégia carregada, mesmo sendo a padrão ou não")
    void estrategiaPreservada() {
        original.setEstrategia(new EstrategiaFilaDaFundicao());

        GerenciadorProducao carregada = salvarECarregar();

        assertEquals("Fila da Fundição (FIFO)", carregada.getEstrategiaAtual().getNomeEstrategia());
    }

    @Test
    @DisplayName("salvar de novo atualiza o arquivo: vale o último estado salvo")
    void salvarAtualiza() {
        persistencia.salvar(original);
        original.atualizarDemanda(A8000, 7);
        original.comprarMateriaPrima(3);
        persistencia.salvar(original);

        GerenciadorProducao carregada = persistencia.carregar(catalogo());

        assertEquals(7, carregada.getDemandas().get(1).getQuantidadeProdutos());
        assertEquals(original.getMateriaPrima().getQuantidade(), carregada.getMateriaPrima().getQuantidade(), 1e-9);
        assertEquals(original.getBudget(), carregada.getBudget(), 1e-9);
    }

    @Test
    @DisplayName("cria as pastas que faltam no caminho")
    void criaPastas() {
        Path fundo = pasta.resolve("a").resolve("b").resolve("planta.xml");
        PersistenciaPlanta emPastaNova = new PersistenciaPlanta(fundo);

        emPastaNova.salvar(original);

        assertTrue(Files.isRegularFile(fundo));
        assertTrue(emPastaNova.existe());
        assertEquals(descrever(original), descrever(emPastaNova.carregar(catalogo())));
    }

    @Test
    @DisplayName("o arquivo é um XML em UTF-8 com a versão do formato e preserva acentos")
    void formatoDoArquivo() throws IOException {
        persistencia.salvar(original);

        String xml = Files.readString(arquivo, StandardCharsets.UTF_8);

        assertTrue(xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(xml.contains("<planta versao=\"1\">"));
        assertTrue(xml.contains("Wafer de Silício Andesite 5nm"));
    }

    @Test
    @DisplayName("não deixa arquivos temporários para trás")
    void semTemporarios() throws IOException {
        persistencia.salvar(original);
        persistencia.salvar(original);

        try (Stream<Path> arquivos = Files.list(pasta)) {
            List<String> nomes = arquivos.map(p -> p.getFileName().toString()).toList();
            assertEquals(List.of("planta.xml"), nomes);
        }
    }

    @Test
    @DisplayName("espaços, CRLF, BOM, comentários e elementos desconhecidos são tolerados")
    void toleraVariacoesInofensivas() throws IOException {
        persistencia.salvar(original);
        String xml = Files.readString(arquivo, StandardCharsets.UTF_8);
        String antes = descrever(original);

        Files.writeString(arquivo, xml.replace("<budget>", "<budget>  ").replace("</budget>", " \n</budget>"));
        assertEquals(antes, descrever(persistencia.carregar(catalogo())));

        Files.writeString(arquivo, xml.replace("\n", "\r\n"));
        assertEquals(antes, descrever(persistencia.carregar(catalogo())));

        Files.writeString(arquivo, xml.replace("<planta versao=\"1\">",
                "<planta versao=\"1\"><extra/><!-- comentario -->"));
        assertDoesNotThrow(() -> persistencia.carregar(catalogo()));

        byte[] corpo = xml.getBytes(StandardCharsets.UTF_8);
        byte[] comBom = new byte[corpo.length + 3];
        comBom[0] = (byte) 0xEF;
        comBom[1] = (byte) 0xBB;
        comBom[2] = (byte) 0xBF;
        System.arraycopy(corpo, 0, comBom, 3, corpo.length);
        Files.write(arquivo, comBom);
        assertEquals(antes, descrever(persistencia.carregar(catalogo())));
    }
}
