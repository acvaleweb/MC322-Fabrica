package br.unicamp.ic.mc322.persistence;

import static br.unicamp.ic.mc322.suporte.PlantaPersistivel.A7000;
import static br.unicamp.ic.mc322.suporte.PlantaPersistivel.catalogo;
import static br.unicamp.ic.mc322.suporte.PlantaPersistivel.descrever;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.suporte.PlantaPersistivel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Falhas ao salvar e a garantia de que uma falha nunca estraga o que já existe: nem o
 * arquivo salvo antes, nem a planta que está em uso.
 */
@DisplayName("Persistência: falhas ao salvar e preservação do estado")
class PersistenciaFalhasTest {
    @TempDir
    Path pasta;

    private Path arquivo;
    private PersistenciaPlanta persistencia;
    private GerenciadorProducao planta;

    @BeforeEach
    void preparar() {
        arquivo = pasta.resolve("planta.xml");
        persistencia = new PersistenciaPlanta(arquivo);
        planta = PlantaPersistivel.criar();
    }

    @Test
    @DisplayName("caminho nulo e planta nula são recusados")
    void argumentosNulos() {
        assertThrows(IllegalArgumentException.class, () -> new PersistenciaPlanta(null));
        assertThrows(IllegalArgumentException.class, () -> persistencia.salvar(null));
        assertFalse(persistencia.existe());
    }

    @Test
    @DisplayName("o caminho padrão da aplicação é dados/planta.xml")
    void caminhoPadrao() {
        assertEquals(Path.of("dados/planta.xml"), new PersistenciaPlanta().getCaminho());
        assertEquals(arquivo, persistencia.getCaminho());
    }

    @Test
    @DisplayName("demanda em produção não pode ser salva, e o arquivo anterior continua intacto")
    void recusaDemandaEmProducao() throws IOException {
        persistencia.salvar(planta);
        byte[] anterior = Files.readAllBytes(arquivo);
        planta.atualizarDemanda("Andesite A8000", 3);
        planta.getDemandas().get(1).iniciarProducao();
        assertEquals(StatusDemanda.EM_PRODUCAO, planta.getDemandas().get(1).getStatus());

        assertThrows(PersistenciaException.class, () -> persistencia.salvar(planta));

        assertArrayEquals(anterior, Files.readAllBytes(arquivo));
    }

    @Test
    @DisplayName("sem arquivo anterior, uma recusa de salvar não cria arquivo nenhum")
    void recusaNaoCriaArquivo() throws IOException {
        planta.atualizarDemanda("Andesite A8000", 3);
        planta.getDemandas().get(1).iniciarProducao();

        assertThrows(PersistenciaException.class, () -> persistencia.salvar(planta));

        assertFalse(persistencia.existe());
        try (Stream<Path> arquivos = Files.list(pasta)) {
            assertEquals(0L, arquivos.count());
        }
    }

    @Test
    @DisplayName("destino que é uma pasta: PersistenciaException")
    void destinoEUmaPasta() throws IOException {
        Path umaPasta = pasta.resolve("eh-pasta");
        Files.createDirectory(umaPasta);

        assertThrows(PersistenciaException.class, () -> new PersistenciaPlanta(umaPasta).salvar(planta));
        assertTrue(Files.isDirectory(umaPasta));
    }

    @Test
    @DisplayName("pasta de destino impossível de criar (existe um arquivo no lugar): PersistenciaException")
    void pastaPaiEUmArquivo() throws IOException {
        Path comoPasta = pasta.resolve("arquivo-comum");
        Files.writeString(comoPasta, "conteudo", StandardCharsets.UTF_8);

        assertThrows(PersistenciaException.class,
                () -> new PersistenciaPlanta(comoPasta.resolve("planta.xml")).salvar(planta));

        assertEquals("conteudo", Files.readString(comoPasta, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("carregamento que falha não altera a planta em uso nem o arquivo")
    void carregamentoQueFalhaPreservaTudo() throws IOException {
        persistencia.salvar(planta);
        Files.writeString(arquivo, "<planta versao=\"1\"><x", StandardCharsets.UTF_8);
        byte[] corrompido = Files.readAllBytes(arquivo);
        String antes = descrever(planta);

        GerenciadorProducao atual = planta;
        GerenciadorProducao carregada = null;
        try {
            carregada = persistencia.carregar(catalogo());
        } catch (PersistenciaException esperado) {
            // quem chama só troca a planta quando o carregamento termina sem exceção
        }

        assertEquals(null, carregada);
        assertEquals(antes, descrever(atual));
        assertArrayEquals(corrompido, Files.readAllBytes(arquivo)); // o arquivo não é apagado nem "consertado"
    }

    @Test
    @DisplayName("carregar não modifica o arquivo e pode ser repetido com o mesmo resultado")
    void carregarNaoModificaOArquivo() throws IOException {
        persistencia.salvar(planta);
        byte[] salvo = Files.readAllBytes(arquivo);

        GerenciadorProducao primeira = persistencia.carregar(catalogo());
        GerenciadorProducao segunda = persistencia.carregar(catalogo());

        assertArrayEquals(salvo, Files.readAllBytes(arquivo));
        assertEquals(descrever(primeira), descrever(segunda));
    }

    @Test
    @DisplayName("os dados sobrevivem ao fim do programa: um novo objeto de persistência os encontra")
    void dadosSobrevivem() {
        persistencia.salvar(planta);
        planta = null; // "encerra" a execução: só o arquivo resta

        PersistenciaPlanta novaExecucao = new PersistenciaPlanta(arquivo);

        assertTrue(novaExecucao.existe());
        GerenciadorProducao recuperada = novaExecucao.carregar(catalogo());
        assertEquals(List.of(A7000, "Andesite A8000"),
                recuperada.getDemandas().stream().map(d -> d.getTipoProduto()).toList());
    }
}
