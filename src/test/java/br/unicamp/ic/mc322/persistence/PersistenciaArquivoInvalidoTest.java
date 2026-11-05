package br.unicamp.ic.mc322.persistence;

import static br.unicamp.ic.mc322.suporte.PlantaPersistivel.catalogo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.unicamp.ic.mc322.exception.ArquivoCorrompidoException;
import br.unicamp.ic.mc322.exception.ArquivoNaoEncontradoException;
import br.unicamp.ic.mc322.exception.DadosInvalidosException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.CategoriaProduto;
import br.unicamp.ic.mc322.model.ComponenteEntrada;
import br.unicamp.ic.mc322.model.Produto;
import br.unicamp.ic.mc322.suporte.PlantaPersistivel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Carregar um arquivo que não existe, está vazio, está corrompido ou tem dados que
 * violam as regras do domínio. Em todos os casos o programa precisa receber uma
 * {@link PersistenciaException} com mensagem útil, e nunca uma falha inesperada.
 */
@DisplayName("Persistência: arquivo ausente, vazio, corrompido ou incompatível")
class PersistenciaArquivoInvalidoTest {
    @TempDir
    Path pasta;

    private Path arquivo;
    private PersistenciaPlanta persistencia;
    private String xml;

    @BeforeEach
    void preparar() throws IOException {
        arquivo = pasta.resolve("planta.xml");
        persistencia = new PersistenciaPlanta(arquivo);
        persistencia.salvar(PlantaPersistivel.criar());
        xml = Files.readString(arquivo, StandardCharsets.UTF_8);
    }

    private Class<?> tipoDoErro() {
        try {
            persistencia.carregar(catalogo());
            return null;
        } catch (Throwable erro) {
            return erro.getClass();
        }
    }

    private String mensagemDoErro() {
        try {
            persistencia.carregar(catalogo());
            return null;
        } catch (Throwable erro) {
            return erro.getMessage();
        }
    }

    /**
     * Confere cada variação e junta todas as que não se comportaram como esperado, para o
     * relatório de falha mostrar tudo de uma vez.
     */
    private List<String> variacoesComComportamentoErrado(Map<String, String> variacoes, Class<?> esperada)
            throws IOException {
        List<String> erradas = new ArrayList<>();
        for (Map.Entry<String, String> variacao : variacoes.entrySet()) {
            if (variacao.getValue().equals(xml)) {
                erradas.add(variacao.getKey() + " (a alteração do teste não mudou o arquivo)");
                continue;
            }
            Files.writeString(arquivo, variacao.getValue(), StandardCharsets.UTF_8);
            Class<?> obtida = tipoDoErro();
            String mensagem = mensagemDoErro();
            if (obtida != esperada || mensagem == null || mensagem.isBlank()) {
                erradas.add(variacao.getKey() + " (obtido: " + (obtida == null ? "nenhum erro" : obtida.getSimpleName())
                        + ")");
            }
        }
        return erradas;
    }

    private String campo(String tag, String valor) {
        return xml.replaceFirst("<" + tag + ">[^<]*</" + tag + ">", "<" + tag + ">" + valor + "</" + tag + ">");
    }

    // ---------- ausente, vazio, ilegível ----------

    @Test
    @DisplayName("arquivo inexistente: ArquivoNaoEncontradoException com o caminho")
    void inexistente() throws IOException {
        Files.delete(arquivo);

        ArquivoNaoEncontradoException erro = assertThrows(ArquivoNaoEncontradoException.class,
                () -> persistencia.carregar(catalogo()));

        assertTrue(erro.getCaminho().contains("planta.xml"));
        assertFalse(persistencia.existe());
    }

    @Test
    @DisplayName("arquivo com 0 bytes ou só com espaços: ArquivoCorrompidoException (vazio)")
    void vazio() throws IOException {
        Files.write(arquivo, new byte[0]);
        assertEquals(ArquivoCorrompidoException.class, tipoDoErro());

        Files.writeString(arquivo, " \n\t \r\n ");
        assertEquals(ArquivoCorrompidoException.class, tipoDoErro());
        assertTrue(mensagemDoErro().contains("vazio"));
    }

    @Test
    @DisplayName("caminho que é uma pasta: PersistenciaException")
    void caminhoEUmaPasta() throws IOException {
        Path umaPasta = pasta.resolve("eh-pasta");
        Files.createDirectory(umaPasta);

        assertThrows(PersistenciaException.class, () -> new PersistenciaPlanta(umaPasta).carregar(catalogo()));
    }

    @Test
    @DisplayName("catálogo nulo é recusado")
    void catalogoNulo() {
        assertThrows(IllegalArgumentException.class, () -> persistencia.carregar(null));
    }

    // ---------- XML malformado ou com estrutura errada ----------

    @Test
    @DisplayName("XML malformado ou com estrutura errada: ArquivoCorrompidoException")
    void corrompidos() throws IOException {
        Map<String, String> variacoes = new LinkedHashMap<>();
        variacoes.put("texto qualquer", "isto nao e xml");
        variacoes.put("XML truncado no meio", xml.substring(0, xml.length() / 2));
        variacoes.put("bytes binários", new String(new byte[] {1, 2, 3, 4}, StandardCharsets.ISO_8859_1));
        variacoes.put("tag não fechada", xml.replace("</planta>", ""));
        variacoes.put("raiz errada", xml.replace("<planta ", "<fabrica ").replace("</planta>", "</fabrica>"));
        variacoes.put("versão desconhecida", xml.replace("versao=\"1\"", "versao=\"99\""));
        variacoes.put("sem versão", xml.replace(" versao=\"1\"", ""));
        variacoes.put("falta <budget>", xml.replaceFirst("<budget>[^<]*</budget>", ""));
        variacoes.put("<budget> duplicado", xml.replaceFirst("(<budget>[^<]*</budget>)", "$1$1"));
        variacoes.put("falta <materiasPrimas>", xml.replaceFirst("(?s)<materiasPrimas>.*</materiasPrimas>", ""));
        variacoes.put("matéria-prima sem id", xml.replace("<materiaPrima id=\"MP-01\">", "<materiaPrima>"));
        variacoes.put("duas matérias-primas", xml.replaceFirst("(?s)(<materiaPrima .*?</materiaPrima>)", "$1$1"));
        variacoes.put("nenhuma matéria-prima", xml.replaceFirst("(?s)<materiaPrima .*?</materiaPrima>", ""));
        variacoes.put("máquina sem <motor>", xml.replaceFirst("(?s)<motor>.*?</motor>", ""));
        variacoes.put("máquina sem tipo", xml.replaceFirst("<maquina tipo=\"[A-Z_]+\">", "<maquina>"));
        variacoes.put("demanda sem produto", xml.replaceFirst("<demanda produto=\"[^\"]*\">", "<demanda>"));
        variacoes.put("falta <armazem>", xml.replaceFirst("(?s)<armazem>.*</armazem>", ""));

        assertEquals(List.of(), variacoesComComportamentoErrado(variacoes, ArquivoCorrompidoException.class));
    }

    @Test
    @DisplayName("DOCTYPE (XXE e 'billion laughs') é recusado, sem ler arquivos nem estourar a memória")
    void doctypeRecusado() throws IOException {
        Map<String, String> variacoes = new LinkedHashMap<>();
        variacoes.put("XXE", "<?xml version=\"1.0\"?><!DOCTYPE planta [<!ENTITY x SYSTEM \"file:///etc/passwd\">]>"
                + "<planta versao=\"1\">&x;</planta>");
        variacoes.put("billion laughs", "<?xml version=\"1.0\"?><!DOCTYPE l [<!ENTITY a \"aaaa\">"
                + "<!ENTITY b \"&a;&a;&a;&a;\">]><planta versao=\"1\">&b;</planta>");

        assertEquals(List.of(), variacoesComComportamentoErrado(variacoes, ArquivoCorrompidoException.class));
    }

    @Test
    @DisplayName("o leitor de XML não escreve lixo em System.err")
    void semRuidoNoErr() throws IOException {
        Files.writeString(arquivo, "isto nao e xml");
        ByteArrayOutputStream captura = new ByteArrayOutputStream();
        PrintStream original = System.err;
        System.setErr(new PrintStream(captura));
        try {
            tipoDoErro();
        } finally {
            System.setErr(original);
        }

        assertEquals(0, captura.size());
    }

    // ---------- valores que violam o domínio ----------

    @Test
    @DisplayName("números inválidos (não numérico, NaN, Infinity, sufixo, hexa, vírgula, vazio, negativo)")
    void numerosInvalidos() throws IOException {
        Map<String, String> variacoes = new LinkedHashMap<>();
        variacoes.put("budget não numérico", campo("budget", "abc"));
        variacoes.put("budget NaN", campo("budget", "NaN"));
        variacoes.put("budget Infinity", campo("budget", "Infinity"));
        variacoes.put("budget com sufixo d", campo("budget", "10d"));
        variacoes.put("budget hexadecimal", campo("budget", "0x10"));
        variacoes.put("budget com vírgula", campo("budget", "10,5"));
        variacoes.put("budget vazio", campo("budget", ""));
        variacoes.put("budget negativo", campo("budget", "-5.0"));
        variacoes.put("esteira com capacidade zero", campo("capacidadeEsteira", "0.0"));
        variacoes.put("contador negativo", campo("contadorUnidades", "-1"));
        variacoes.put("contador gigante", campo("contadorUnidades", "99999999999"));
        variacoes.put("contador decimal", campo("contadorUnidades", "1.5"));
        variacoes.put("estoque negativo", xml.replaceFirst("<quantidade>96.0<", "<quantidade>-1.0<"));

        assertEquals(List.of(), variacoesComComportamentoErrado(variacoes, DadosInvalidosException.class));
    }

    @Test
    @DisplayName("valores fora das regras do domínio: cenário, estratégia, máquinas e Partes")
    void valoresForaDasRegras() throws IOException {
        Map<String, String> variacoes = new LinkedHashMap<>();
        variacoes.put("cenário desconhecido", xml.replace("<cenario>APOCALIPTICO</cenario>", "<cenario>XYZ</cenario>"));
        variacoes.put("estratégia desconhecida", xml.replace("MAIOR_LOTE", "XYZ"));
        variacoes.put("nome da matéria-prima vazio", xml.replaceFirst("<nome>Wafer[^<]*</nome>", "<nome></nome>"));
        variacoes.put("tipo de máquina desconhecido", xml.replace("tipo=\"MONTADORA\"", "tipo=\"BETONEIRA\""));
        variacoes.put("saúde 150", xml.replaceFirst("<saude>[^<]*<", "<saude>150.0<"));
        variacoes.put("saúde NaN", xml.replaceFirst("<saude>[^<]*<", "<saude>NaN<"));
        variacoes.put("probabilidade de falha 2",
                xml.replaceFirst("<probabilidadeFalha>0.2<", "<probabilidadeFalha>2.0<"));
        variacoes.put("desgaste máximo menor que o mínimo",
                xml.replaceFirst("<desgasteMaximoPorUso>[^<]*<", "<desgasteMaximoPorUso>0.5<"));
        variacoes.put("temperatura do motor 500", xml.replaceFirst("<temperatura>[^<]*<", "<temperatura>500.0<"));
        variacoes.put("temperatura abaixo da ambiente", xml.replaceFirst("<temperatura>[^<]*<", "<temperatura>0.0<"));
        variacoes.put("calibração 101", xml.replaceFirst("<calibracao>[^<]*<", "<calibracao>101.0<"));
        variacoes.put("integridade de atuador negativa",
                xml.replaceFirst("<integridade>[^<]*<", "<integridade>-1.0<"));
        variacoes.put("atuador a menos", xml.replaceFirst("(?s)<atuador numero=\"2\">.*?</atuador>", ""));
        variacoes.put("atuador fora de ordem", xml.replaceFirst("<atuador numero=\"1\">", "<atuador numero=\"7\">"));
        variacoes.put("máquina sem nome", xml.replaceFirst("<nome>Insersora SMT-01</nome>", "<nome></nome>"));
        variacoes.put("capacidade da máquina negativa",
                xml.replaceFirst("<capacidadeMaxima>5.0<", "<capacidadeMaxima>-5.0<"));

        assertEquals(List.of(), variacoesComComportamentoErrado(variacoes, DadosInvalidosException.class));
    }

    @Test
    @DisplayName("demandas e armazém que violam as regras: status, catálogo, duplicidade, ids")
    void demandasEArmazemInvalidos() throws IOException {
        Map<String, String> variacoes = new LinkedHashMap<>();
        variacoes.put("demanda EM_PRODUCAO", xml.replace("<status>PENDENTE</status>", "<status>EM_PRODUCAO</status>"));
        variacoes.put("status desconhecido", xml.replace("<status>PENDENTE</status>", "<status>FOO</status>"));
        variacoes.put("quantidade negativa",
                xml.replaceFirst("(?s)(<demanda produto=\"Andesite A8000\">\\s*<quantidade>)2<", "$1-2<"));
        variacoes.put("concluída com quantidade 0", xml.replaceFirst(
                "(?s)(<demanda produto=\"Andesite A8000\">\\s*<quantidade>)2(</quantidade>\\s*<status>)PENDENTE",
                "$10$2CONCLUIDA"));
        variacoes.put("demanda de produto fora do catálogo",
                xml.replace("<demanda produto=\"Andesite A8000\">", "<demanda produto=\"Fantasma\">"));
        variacoes.put("demanda duplicada",
                xml.replace("<demanda produto=\"Andesite A8000\">", "<demanda produto=\"Andesite A7000\">"));
        variacoes.put("falta a demanda de um produto do catálogo",
                xml.replaceFirst("(?s)<demanda produto=\"Andesite A8000\">.*?</demanda>", ""));
        variacoes.put("item do armazém fora do catálogo", xml.replaceFirst(
                "<item id=\"PROD-VGA-001-1\" produto=\"Andesite A7000\">",
                "<item id=\"PROD-VGA-001-1\" produto=\"Fantasma\">"));
        variacoes.put("id do armazém duplicado", xml.replace("id=\"PROD-VGA-001-3\"", "id=\"PROD-VGA-001-1\""));
        variacoes.put("id com formato errado", xml.replace("id=\"PROD-VGA-001-3\"", "id=\"XYZ-3\""));
        variacoes.put("id de outro produto", xml.replace("id=\"PROD-VGA-001-3\"", "id=\"PROD-VGA-002-3\""));
        variacoes.put("id sem número", xml.replace("id=\"PROD-VGA-001-3\"", "id=\"PROD-VGA-001-\""));
        variacoes.put("id acima do contador", xml.replace("id=\"PROD-VGA-001-3\"", "id=\"PROD-VGA-001-9\""));
        variacoes.put("falha acumulada 1.5", xml.replaceFirst(
                "<probabilidadeFalhaAcumulada>[^<]*<", "<probabilidadeFalhaAcumulada>1.5<"));

        assertEquals(List.of(), variacoesComComportamentoErrado(variacoes, DadosInvalidosException.class));
    }

    @Test
    @DisplayName("catálogo diferente do usado ao salvar: DadosInvalidosException")
    void catalogoDiferente() {
        List<Produto> outro = new ArrayList<>();
        outro.add(new ComponenteEntrada("PROD-CPU-001", "Basalt B7000", CategoriaProduto.PROCESSADOR, 1, 0.1));

        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar(outro));
        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar(new ArrayList<>()));
    }

    @Test
    @DisplayName("as mensagens de erro dizem o que está errado")
    void mensagensUteis() throws IOException {
        Files.writeString(arquivo, campo("budget", "abc"), StandardCharsets.UTF_8);
        assertTrue(mensagemDoErro().toLowerCase().contains("budget"));

        Files.writeString(arquivo, xml.substring(0, xml.length() / 2), StandardCharsets.UTF_8);
        assertTrue(mensagemDoErro().contains("planta.xml"));
    }
}
