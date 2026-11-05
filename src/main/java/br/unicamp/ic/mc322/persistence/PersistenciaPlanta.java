package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.exception.ArquivoCorrompidoException;
import br.unicamp.ic.mc322.exception.ArquivoNaoEncontradoException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.Produto;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Salva e carrega o estado da planta em um arquivo XML.
 *
 * <p>É a única classe do sistema que sabe que existem arquivos: as entidades de domínio
 * e o {@link GerenciadorProducao} não leem nem escrevem nada em disco. A conversão entre
 * objetos e XML fica em classes auxiliares do pacote ({@link EscritorXmlPlanta} e
 * {@link FormatoXml}), e esta classe cuida do arquivo em si e da tradução das falhas de
 * entrada e saída em {@link PersistenciaException}.</p>
 *
 * <h2>Salvar</h2>
 * <ol>
 *   <li>O documento XML é montado inteiro na memória, sem tocar no disco. Se algo impedir
 *       o salvamento, nenhum arquivo é alterado.</li>
 *   <li>O conteúdo é gravado em um arquivo temporário na mesma pasta.</li>
 *   <li>O temporário substitui o arquivo de destino em uma única operação (troca
 *       atômica quando o sistema de arquivos permite). Por isso uma falha no meio da
 *       gravação, ou o fim abrupto do programa, nunca deixa um arquivo pela metade: ou
 *       o estado novo está completo, ou o estado salvo anteriormente continua intacto.</li>
 * </ol>
 *
 * <p>Salvar de novo no mesmo caminho é a operação de <b>atualizar</b>: o arquivo anterior
 * é substituído pelo estado atual.</p>
 *
 * <h2>Carregar</h2>
 * <ol>
 *   <li>O arquivo é lido e interpretado como XML, com o processamento de entidades
 *       externas desligado (um arquivo malicioso não consegue ler outros arquivos nem
 *       acessar a rede).</li>
 *   <li>O documento é validado e convertido em uma planta <b>nova</b> pelo
 *       {@link LeitorXmlPlanta}, que reaproveita as regras do próprio domínio.</li>
 *   <li>Só no fim a planta é devolvida a quem chamou.</li>
 * </ol>
 *
 * <p>Como {@link #carregar(List)} devolve um objeto novo e não altera nada que já exista,
 * <b>a planta atual só é trocada se o carregamento inteiro tiver dado certo</b>: qualquer
 * falha deixa o estado válido anterior exatamente como estava.</p>
 *
 * <p>Os dados ficam no arquivo depois que o programa termina; encerrar a aplicação não
 * apaga nem altera o arquivo.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class PersistenciaPlanta {
    /** Caminho usado pela aplicação, relativo à pasta onde o programa é executado. */
    public static final String CAMINHO_PADRAO = "dados/planta.xml";

    private static final String CABECALHO_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    private static final String PREFIXO_TEMPORARIO = "planta-";
    private static final String SUFIXO_TEMPORARIO = ".tmp";

    private final Path caminho;

    /**
     * Cria a persistência para o caminho padrão da aplicação ({@value #CAMINHO_PADRAO}).
     */
    public PersistenciaPlanta() {
        this(Path.of(CAMINHO_PADRAO));
    }

    /**
     * Cria a persistência para um arquivo específico. Os testes automatizados usam esta
     * forma, com arquivos temporários, para nunca tocar nos dados reais da aplicação.
     *
     * @param caminho caminho do arquivo XML
     * @throws IllegalArgumentException se o caminho for nulo
     */
    public PersistenciaPlanta(Path caminho) {
        if (caminho == null) {
            throw new IllegalArgumentException("O caminho do arquivo de persistência não pode ser nulo");
        }

        this.caminho = caminho;
    }

    /**
     * Grava o estado atual da planta no arquivo, criando as pastas que faltarem e
     * substituindo o conteúdo anterior.
     *
     * <p>Persistem: cenário, estratégia, budget, capacidade da esteira, contador de
     * unidades, estoque de matéria-prima, as máquinas com todas as suas Partes (na ordem
     * da linha), as demandas com o seu {@code StatusDemanda} e as unidades do armazém.
     * O catálogo de produtos não é gravado: faz parte da configuração da aplicação e é
     * informado ao carregar.</p>
     *
     * @param planta planta a salvar
     * @throws IllegalArgumentException se a planta for nula
     * @throws PersistenciaException    se o caminho for uma pasta, se alguma demanda
     *                                  estiver em produção, se a planta tiver algo que o
     *                                  formato não suporta ou se a gravação falhar (disco
     *                                  cheio, falta de permissão, pasta inacessível); o
     *                                  arquivo salvo anteriormente, se existir, continua
     *                                  intacto
     */
    public void salvar(GerenciadorProducao planta) {
        if (planta == null) {
            throw new IllegalArgumentException("Não há planta para salvar");
        }

        // Tudo que pode ser recusado acontece aqui, antes de qualquer alteracao em disco
        Document documento = new EscritorXmlPlanta().escrever(planta);

        gravarSubstituindo(documento);
    }

    /**
     * Lê o arquivo, valida o seu conteúdo e reconstrói a planta que ele descreve.
     *
     * <p>Devolve sempre uma planta <b>nova</b>: nenhum objeto existente é alterado, então
     * quem chama só precisa substituir a planta atual pela devolvida quando este método
     * terminar sem exceção. O catálogo de produtos não vem do arquivo (faz parte da
     * configuração da aplicação) e é usado para conferir as demandas e o armazém.</p>
     *
     * @param catalogo produtos que a fábrica sabe fabricar
     * @return a planta reconstruída
     * @throws IllegalArgumentException     se o catálogo for nulo
     * @throws ArquivoNaoEncontradoException se o arquivo não existir
     * @throws ArquivoCorrompidoException   se o arquivo estiver vazio, não for um XML
     *                                      válido ou não tiver a estrutura esperada
     * @throws br.unicamp.ic.mc322.exception.DadosInvalidosException
     *                                      se algum valor violar as regras do domínio
     * @throws PersistenciaException        se a leitura falhar (permissão, caminho que
     *                                      é uma pasta, erro de disco)
     */
    public GerenciadorProducao carregar(List<Produto> catalogo) {
        if (catalogo == null) {
            throw new IllegalArgumentException("O catálogo de produtos não pode ser nulo");
        }

        try {
            byte[] conteudo = lerArquivo();
            Document documento = interpretar(conteudo);
            return new LeitorXmlPlanta(catalogo).ler(documento);
        } catch (PersistenciaException e) {
            throw e;
        } catch (RuntimeException e) {
            // Rede de seguranca: nada que escape do leitor deve derrubar a aplicacao
            throw new PersistenciaException("Falha inesperada ao carregar " + caminho + descreverCausa(e), e);
        }
    }

    private byte[] lerArquivo() {
        if (!Files.exists(caminho)) {
            throw new ArquivoNaoEncontradoException(caminho.toString());
        }
        if (Files.isDirectory(caminho)) {
            throw new PersistenciaException("O caminho " + caminho + " é uma pasta, e não um arquivo");
        }

        byte[] conteudo;
        try {
            conteudo = Files.readAllBytes(caminho);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler o arquivo " + caminho + descreverCausa(e), e);
        }

        if (estaEmBranco(conteudo)) {
            throw new ArquivoCorrompidoException("O arquivo " + caminho + " está vazio");
        }
        return conteudo;
    }

    private boolean estaEmBranco(byte[] conteudo) {
        for (byte b : conteudo) {
            if (b != ' ' && b != '\t' && b != '\n' && b != '\r') {
                return false;
            }
        }
        return true;
    }

    private Document interpretar(byte[] conteudo) {
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // Sem DOCTYPE nao ha entidades externas, entao nao ha como ler arquivos
            // locais nem acessar a rede a partir do XML (ataque XXE)
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            fabrica.setXIncludeAware(false);
            fabrica.setExpandEntityReferences(false);

            DocumentBuilder construtor = fabrica.newDocumentBuilder();
            // O tratador padrao imprime mensagens em System.err; este so propaga o erro
            construtor.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) {
                    // avisos nao invalidam o arquivo
                }

                @Override
                public void error(SAXParseException e) throws SAXException {
                    throw e;
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    throw e;
                }
            });

            Document documento = construtor.parse(new ByteArrayInputStream(conteudo));
            documento.getDocumentElement().normalize();
            return documento;
        } catch (SAXParseException e) {
            throw new ArquivoCorrompidoException("O arquivo " + caminho + " não é um XML válido (linha "
                    + e.getLineNumber() + ", coluna " + e.getColumnNumber() + "): " + e.getMessage(), e);
        } catch (SAXException | IOException e) {
            throw new ArquivoCorrompidoException("O arquivo " + caminho + " não é um XML válido" + descreverCausa(e), e);
        } catch (ParserConfigurationException e) {
            throw new PersistenciaException("Não foi possível preparar a leitura do XML", e);
        }
    }

    /**
     * Informa se o arquivo de persistência existe.
     *
     * @return {@code true} se há um arquivo (ou pasta) no caminho
     */
    public boolean existe() {
        return Files.exists(caminho);
    }

    /**
     * Retorna o caminho do arquivo de persistência.
     *
     * @return o caminho
     */
    public Path getCaminho() {
        return caminho;
    }

    private void gravarSubstituindo(Document documento) {
        Path destino = caminho.toAbsolutePath();

        if (Files.isDirectory(destino)) {
            throw new PersistenciaException("O caminho " + caminho + " é uma pasta, e não um arquivo");
        }

        Path temporario = null;
        try {
            Files.createDirectories(destino.getParent());
            temporario = Files.createTempFile(destino.getParent(), PREFIXO_TEMPORARIO, SUFIXO_TEMPORARIO);

            try (OutputStream saida = Files.newOutputStream(temporario)) {
                serializar(documento, saida);
            }

            substituir(temporario, destino);
            temporario = null; // ja virou o arquivo de destino
        } catch (IOException | TransformerException e) {
            throw new PersistenciaException("Não foi possível salvar o estado da planta em " + caminho
                    + descreverCausa(e), e);
        } finally {
            apagarSilenciosamente(temporario);
        }
    }

    private void serializar(Document documento, OutputStream saida) throws IOException, TransformerException {
        TransformerFactory fabrica = TransformerFactory.newInstance();
        fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        Transformer transformador = fabrica.newTransformer();
        transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformador.setOutputProperty(OutputKeys.INDENT, "yes");
        transformador.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        // O cabecalho e escrito a mao: o do Transformer varia entre versoes do Java
        // (ora acrescenta standalone="no", ora cola a raiz na mesma linha)
        transformador.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        saida.write(CABECALHO_XML.getBytes(StandardCharsets.UTF_8));
        transformador.transform(new DOMSource(documento), new StreamResult(saida));
    }

    // Troca atomica quando possivel; se o sistema de arquivos nao a oferece, usa a troca comum
    private void substituir(Path temporario, Path destino) throws IOException {
        try {
            Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void apagarSilenciosamente(Path arquivo) {
        if (arquivo == null) {
            return;
        }

        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException ignorada) {
            // O arquivo temporario e so lixo; nao vale mascarar o erro original por causa dele
        }
    }

    // Traduz as falhas de disco mais comuns em uma frase que o usuario entende
    private String descreverCausa(Exception e) {
        if (e instanceof AccessDeniedException) {
            return ": sem permissão de acesso a " + e.getMessage();
        }
        if (e instanceof FileAlreadyExistsException) {
            return ": já existe um arquivo, e não uma pasta, em " + e.getMessage();
        }
        if (e.getMessage() == null || e.getMessage().isBlank()) {
            return " (" + e.getClass().getSimpleName() + ")";
        }
        return " (" + e.getMessage() + ")";
    }
}
