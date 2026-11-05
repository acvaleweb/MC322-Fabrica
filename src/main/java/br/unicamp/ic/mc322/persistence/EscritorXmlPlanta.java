package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.EstadoMaquina;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.Produto;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Converte o estado de uma planta ({@link GerenciadorProducao}) em um documento XML na
 * memória, seguindo o formato descrito em {@link FormatoXml}.
 *
 * <p>Só <b>lê</b> a planta, pelos métodos públicos que ela já oferece: não acessa
 * atributos internos nem as Partes das máquinas, que chegam como um
 * {@link EstadoMaquina}. Também não toca em disco: gravar o arquivo é papel da
 * {@link PersistenciaPlanta}. Assim, qualquer problema que impeça de salvar (como um
 * tipo de máquina desconhecido) aparece antes de qualquer arquivo ser alterado.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
final class EscritorXmlPlanta {

    /**
     * Monta o documento XML com o estado atual da planta.
     *
     * @param planta planta a converter
     * @return o documento XML, pronto para ser gravado
     * @throws PersistenciaException se alguma demanda estiver em produção, se algum
     *                               texto tiver caracteres que o XML não aceita ou se
     *                               houver máquina ou estratégia de tipo desconhecido
     */
    Document escrever(GerenciadorProducao planta) {
        validarSalvavel(planta);

        Document documento = criarDocumentoVazio();
        Element raiz = documento.createElement(FormatoXml.PLANTA);
        raiz.setAttribute(FormatoXml.ATRIBUTO_VERSAO, FormatoXml.VERSAO_ATUAL);
        documento.appendChild(raiz);

        adicionarTexto(raiz, FormatoXml.CENARIO, planta.getCenarioAtual().name());
        adicionarTexto(raiz, FormatoXml.ESTRATEGIA, FormatoXml.codigoEstrategia(planta.getEstrategiaAtual()));
        adicionarNumero(raiz, FormatoXml.BUDGET, planta.getBudget());
        adicionarNumero(raiz, FormatoXml.CAPACIDADE_ESTEIRA, planta.getCapacidadeEsteira());
        adicionarTexto(raiz, FormatoXml.CONTADOR_UNIDADES, Integer.toString(planta.getContadorUnidadesCriadas()));

        escreverMateriasPrimas(raiz, planta.getMateriaPrima());
        escreverMaquinas(raiz, planta.getMaquinas());
        escreverDemandas(raiz, planta.getDemandas());
        escreverArmazem(raiz, planta.getArmazem());

        return documento;
    }

    // Uma demanda em producao so existe durante um lote, e uma demanda salva nesse estado
    // ficaria travada ao carregar. Em vez de gravar um arquivo que nao da para carregar,
    // a gravacao e recusada
    private void validarSalvavel(GerenciadorProducao planta) {
        for (Demanda demanda : planta.getDemandas()) {
            if (demanda.getStatus() == StatusDemanda.EM_PRODUCAO) {
                throw new PersistenciaException("Não é possível salvar a planta enquanto a demanda de "
                        + demanda.getTipoProduto() + " está em produção");
            }
        }
    }

    private Document criarDocumentoVazio() {
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder construtor = fabrica.newDocumentBuilder();
            return construtor.newDocument();
        } catch (ParserConfigurationException e) {
            throw new PersistenciaException("Não foi possível preparar o documento XML da planta", e);
        }
    }

    private void escreverMateriasPrimas(Element raiz, MateriaPrima materiaPrima) {
        Element materiasPrimas = adicionarElemento(raiz, FormatoXml.MATERIAS_PRIMAS);

        Element elemento = adicionarElemento(materiasPrimas, FormatoXml.MATERIA_PRIMA);
        definirAtributo(elemento, FormatoXml.ATRIBUTO_ID, materiaPrima.getId());
        adicionarTexto(elemento, FormatoXml.NOME, materiaPrima.getNome());
        adicionarNumero(elemento, FormatoXml.QUANTIDADE, materiaPrima.getQuantidade());
        adicionarTexto(elemento, FormatoXml.UNIDADE_MEDIDA, materiaPrima.getUnidade());
        adicionarNumero(elemento, FormatoXml.CUSTO_POR_UNIDADE, materiaPrima.getCustoPorUnidade());
        adicionarNumero(elemento, FormatoXml.QUANTIDADE_MINIMA, materiaPrima.getQuantidadeMinima());
    }

    private void escreverMaquinas(Element raiz, List<Maquina> maquinas) {
        Element linha = adicionarElemento(raiz, FormatoXml.LINHA_DE_PRODUCAO);

        for (Maquina maquina : maquinas) {
            EstadoMaquina estado = maquina.obterEstado();

            Element elemento = adicionarElemento(linha, FormatoXml.MAQUINA);
            definirAtributo(elemento, FormatoXml.ATRIBUTO_TIPO, FormatoXml.codigoTipoMaquina(maquina));

            // Configuracao: o que e preciso para criar a maquina
            adicionarTexto(elemento, FormatoXml.NOME, maquina.getNome());
            adicionarNumero(elemento, FormatoXml.CAPACIDADE_MAXIMA, maquina.getCapacidadeMaxima());
            adicionarNumero(elemento, FormatoXml.CUSTO_OPERACAO, maquina.getCustoOperacao());

            // Estado: o que mudou desde que a maquina foi criada
            adicionarNumero(elemento, FormatoXml.SAUDE, estado.saude());
            adicionarNumero(elemento, FormatoXml.LIMIAR_MANUTENCAO, estado.limiarManutencao());
            adicionarNumero(elemento, FormatoXml.DESGASTE_MINIMO_POR_USO, estado.desgasteMinimoPorUso());
            adicionarNumero(elemento, FormatoXml.DESGASTE_MAXIMO_POR_USO, estado.desgasteMaximoPorUso());
            adicionarNumero(elemento, FormatoXml.PROBABILIDADE_FALHA, estado.probabilidadeFalha());

            // Partes da composicao
            Element motor = adicionarElemento(elemento, FormatoXml.MOTOR);
            adicionarNumero(motor, FormatoXml.TEMPERATURA, estado.temperaturaMotor());

            Element sensor = adicionarElemento(elemento, FormatoXml.SENSOR);
            adicionarNumero(sensor, FormatoXml.CALIBRACAO, estado.calibracaoSensor());

            Element atuadores = adicionarElemento(elemento, FormatoXml.ATUADORES);
            List<Double> integridades = estado.integridadeAtuadores();
            for (int i = 0; i < integridades.size(); i++) {
                Element atuador = adicionarElemento(atuadores, FormatoXml.ATUADOR);
                definirAtributo(atuador, FormatoXml.ATRIBUTO_NUMERO, Integer.toString(i + 1));
                adicionarNumero(atuador, FormatoXml.INTEGRIDADE, integridades.get(i));
            }
        }
    }

    private void escreverDemandas(Element raiz, List<Demanda> demandas) {
        Element elementoDemandas = adicionarElemento(raiz, FormatoXml.DEMANDAS);

        for (Demanda demanda : demandas) {
            Element elemento = adicionarElemento(elementoDemandas, FormatoXml.DEMANDA);
            definirAtributo(elemento, FormatoXml.ATRIBUTO_PRODUTO, demanda.getTipoProduto());
            adicionarTexto(elemento, FormatoXml.QUANTIDADE, Integer.toString(demanda.getQuantidadeProdutos()));
            adicionarTexto(elemento, FormatoXml.STATUS, demanda.getStatus().name());
        }
    }

    private void escreverArmazem(Element raiz, List<Produto> armazem) {
        Element elementoArmazem = adicionarElemento(raiz, FormatoXml.ARMAZEM);

        for (Produto produto : armazem) {
            Element elemento = adicionarElemento(elementoArmazem, FormatoXml.ITEM_ARMAZEM);
            definirAtributo(elemento, FormatoXml.ATRIBUTO_ID, produto.getId());
            definirAtributo(elemento, FormatoXml.ATRIBUTO_PRODUTO, produto.getNome());
            adicionarNumero(elemento, FormatoXml.PROBABILIDADE_FALHA_ACUMULADA,
                    produto.getProbabilidadeFalhaAcumulada());
        }
    }

    // ----- Auxiliares de construcao do XML -----

    private Element adicionarElemento(Element pai, String nome) {
        Element filho = pai.getOwnerDocument().createElement(nome);
        pai.appendChild(filho);
        return filho;
    }

    private void adicionarTexto(Element pai, String nome, String texto) {
        validarTextoXml(texto);
        Element filho = adicionarElemento(pai, nome);
        filho.setTextContent(texto);
    }

    // Double.toString nao depende do idioma do computador e devolve um texto que
    // Double.parseDouble reconstroi com exatamente o mesmo valor
    private void adicionarNumero(Element pai, String nome, double valor) {
        adicionarTexto(pai, nome, Double.toString(valor));
    }

    private void definirAtributo(Element elemento, String nome, String valor) {
        validarTextoXml(valor);
        elemento.setAttribute(nome, valor);
    }

    // O XML 1.0 nao aceita certos caracteres de controle. Gravar um texto assim criaria
    // um arquivo que nem a propria aplicacao conseguiria ler, e a troca atomica ainda
    // apagaria o ultimo estado valido. Melhor recusar antes
    private void validarTextoXml(String texto) {
        texto.codePoints().forEach(c -> {
            boolean valido = c == 0x9 || c == 0xA || c == 0xD
                    || (c >= 0x20 && c <= 0xD7FF)
                    || (c >= 0xE000 && c <= 0xFFFD)
                    || (c >= 0x10000 && c <= 0x10FFFF);
            if (!valido) {
                throw new PersistenciaException("O texto \"" + texto + "\" tem um caractere (código "
                        + c + ") que o formato XML não aceita");
            }
        });
    }
}
