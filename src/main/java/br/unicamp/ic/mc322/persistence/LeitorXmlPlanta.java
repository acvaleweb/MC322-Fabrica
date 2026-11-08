package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.exception.ArquivoCorrompidoException;
import br.unicamp.ic.mc322.exception.DadosInvalidosException;
import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.EstadoMaquina;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.Produto;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Valida um documento XML no formato de {@link FormatoXml} e reconstrói, a partir dele,
 * uma planta nova ({@link GerenciadorProducao}).
 *
 * <p>Duas famílias de problema são distinguidas:</p>
 * <ul>
 *   <li>{@link ArquivoCorrompidoException}: problema de <b>estrutura</b> (elemento ou
 *       atributo obrigatório ausente ou repetido, raiz errada, versão desconhecida);</li>
 *   <li>{@link DadosInvalidosException}: a estrutura está certa, mas um <b>valor</b>
 *       viola o domínio (número malformado, fora da faixa, estado inexistente, produto
 *       fora do catálogo, ids incoerentes com o contador...).</li>
 * </ul>
 *
 * <p>A validação das regras de cada objeto não é repetida aqui: a leitura apenas
 * converte o texto e entrega os valores aos construtores e às operações de restauração
 * do domínio, que são as mesmas que valem para objetos criados normalmente. Assim as
 * regras ficam em um lugar só. O que esta classe acrescenta são as verificações que
 * dependem do arquivo como um todo (uma demanda por produto do catálogo, ids do armazém
 * coerentes com o contador).</p>
 *
 * <p>A planta devolvida é sempre um objeto novo; nada do que existia é alterado. Se
 * qualquer verificação falhar, a exceção sobe e a planta parcialmente montada é
 * simplesmente descartada.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
final class LeitorXmlPlanta {
    // Double.toString so produz este formato. Recusa-se o resto que Double.parseDouble
    // aceitaria por tolerancia ("10d", "0x1p3", "NaN", "Infinity")
    private static final Pattern NUMERO_DECIMAL = Pattern.compile("[-+]?\\d+(\\.\\d+)?([eE][-+]?\\d+)?");
    private static final Pattern NUMERO_INTEIRO = Pattern.compile("[-+]?\\d+");
    private static final Pattern SUFIXO_ID_UNIDADE = Pattern.compile("[1-9]\\d{0,8}");

    private final List<Produto> catalogo;

    /**
     * Cria um leitor que valida o arquivo contra o catálogo de produtos informado.
     *
     * @param catalogo produtos que a fábrica sabe fabricar
     */
    LeitorXmlPlanta(List<Produto> catalogo) {
        this.catalogo = new ArrayList<>(catalogo);
    }

    /**
     * Valida o documento e reconstrói a planta que ele descreve.
     *
     * @param documento documento XML já lido do arquivo
     * @return uma planta nova, com o estado do arquivo
     * @throws ArquivoCorrompidoException se a estrutura do documento não for a esperada
     * @throws DadosInvalidosException    se algum valor violar as regras do domínio
     */
    GerenciadorProducao ler(Document documento) {
        Element raiz = documento.getDocumentElement();

        if (!FormatoXml.PLANTA.equals(raiz.getTagName())) {
            throw new ArquivoCorrompidoException("O arquivo não é uma planta da Andesite: o elemento raiz é <"
                    + raiz.getTagName() + ">, mas deveria ser <" + FormatoXml.PLANTA + ">");
        }
        String versao = raiz.getAttribute(FormatoXml.ATRIBUTO_VERSAO);
        if (!FormatoXml.VERSAO_ATUAL.equals(versao)) {
            throw new ArquivoCorrompidoException("Versão do formato não suportada: \"" + versao
                    + "\" (esta aplicação lê a versão " + FormatoXml.VERSAO_ATUAL + ")");
        }

        Cenario cenario = lerCenario(raiz);
        EstrategiaProducao estrategia = FormatoXml.criarEstrategia(texto(raiz, FormatoXml.ESTRATEGIA).trim());
        double budget = numero(raiz, FormatoXml.BUDGET);
        double capacidadeEsteira = numero(raiz, FormatoXml.CAPACIDADE_ESTEIRA);
        int contador = inteiro(raiz, FormatoXml.CONTADOR_UNIDADES);
        MateriaPrima materiaPrima = lerMateriaPrima(raiz);

        GerenciadorProducao planta;
        try {
            planta = new GerenciadorProducao(materiaPrima, cenario, capacidadeEsteira, catalogo, estrategia);
            planta.restaurarBudget(budget);
            planta.restaurarContadorUnidades(contador);
        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException(e.getMessage(), e);
        }

        lerMaquinas(raiz, planta);
        lerDemandas(raiz, planta);
        lerArmazem(raiz, planta, contador);

        return planta;
    }

    // ----- Secoes do documento -----

    private Cenario lerCenario(Element raiz) {
        String codigo = texto(raiz, FormatoXml.CENARIO).trim();
        try {
            return Cenario.valueOf(codigo);
        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException("Cenário desconhecido no arquivo: \"" + codigo + "\"", e);
        }
    }

    private MateriaPrima lerMateriaPrima(Element raiz) {
        Element materiasPrimas = unico(raiz, FormatoXml.MATERIAS_PRIMAS);
        List<Element> elementos = filhos(materiasPrimas, FormatoXml.MATERIA_PRIMA);

        // A planta trabalha com um unico estoque de materia-prima
        if (elementos.size() != 1) {
            throw new ArquivoCorrompidoException("O arquivo deve ter exatamente uma <" + FormatoXml.MATERIA_PRIMA
                    + "> dentro de <" + FormatoXml.MATERIAS_PRIMAS + ">, mas tem " + elementos.size());
        }
        Element elemento = elementos.get(0);

        String id = atributo(elemento, FormatoXml.ATRIBUTO_ID);
        String nome = texto(elemento, FormatoXml.NOME);
        double quantidade = numero(elemento, FormatoXml.QUANTIDADE);
        String unidade = texto(elemento, FormatoXml.UNIDADE_MEDIDA);
        double custo = numero(elemento, FormatoXml.CUSTO_POR_UNIDADE);
        double quantidadeMinima = numero(elemento, FormatoXml.QUANTIDADE_MINIMA);

        try {
            return new MateriaPrima(id, nome, quantidade, unidade, custo, quantidadeMinima);
        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException("Matéria-prima inválida: " + e.getMessage(), e);
        }
    }

    private void lerMaquinas(Element raiz, GerenciadorProducao planta) {
        List<Element> maquinas = filhos(unico(raiz, FormatoXml.LINHA_DE_PRODUCAO), FormatoXml.MAQUINA);

        for (int i = 0; i < maquinas.size(); i++) {
            String local = "Máquina nº " + (i + 1) + ": ";
            try {
                lerMaquina(maquinas.get(i), planta);
            } catch (DadosInvalidosException e) {
                throw new DadosInvalidosException(local + e.getMessage(), e);
            } catch (ArquivoCorrompidoException e) {
                throw new ArquivoCorrompidoException(local + e.getMessage(), e);
            }
        }
    }

    private void lerMaquina(Element elemento, GerenciadorProducao planta) {
        String tipo = atributo(elemento, FormatoXml.ATRIBUTO_TIPO);
        String nome = texto(elemento, FormatoXml.NOME);
        double capacidade = numero(elemento, FormatoXml.CAPACIDADE_MAXIMA);
        double custo = numero(elemento, FormatoXml.CUSTO_OPERACAO);
        double probabilidadeFalha = numero(elemento, FormatoXml.PROBABILIDADE_FALHA);

        double temperatura = numero(unico(elemento, FormatoXml.MOTOR), FormatoXml.TEMPERATURA);
        double calibracao = numero(unico(elemento, FormatoXml.SENSOR), FormatoXml.CALIBRACAO);
        List<Double> integridades = lerAtuadores(unico(elemento, FormatoXml.ATUADORES));

        EstadoMaquina estado = new EstadoMaquina(
                numero(elemento, FormatoXml.SAUDE),
                numero(elemento, FormatoXml.LIMIAR_MANUTENCAO),
                numero(elemento, FormatoXml.DESGASTE_MINIMO_POR_USO),
                numero(elemento, FormatoXml.DESGASTE_MAXIMO_POR_USO),
                probabilidadeFalha, temperatura, calibracao, integridades);

        try {
            Maquina maquina = FormatoXml.criarMaquina(tipo, nome, capacidade, probabilidadeFalha, custo);
            planta.restaurarMaquina(maquina, estado);
        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException("\"" + nome + "\" inválida: " + e.getMessage(), e);
        }
    }

    private List<Double> lerAtuadores(Element atuadores) {
        List<Double> integridades = new ArrayList<>();
        List<Element> elementos = filhos(atuadores, FormatoXml.ATUADOR);

        for (int i = 0; i < elementos.size(); i++) {
            Element atuador = elementos.get(i);
            String numero = atributo(atuador, FormatoXml.ATRIBUTO_NUMERO);
            if (!numero.equals(Integer.toString(i + 1))) {
                throw new DadosInvalidosException("Os atuadores devem ser numerados em sequência a partir de 1: o "
                        + (i + 1) + "º atuador está numerado como \"" + numero + "\"");
            }
            integridades.add(numero(atuador, FormatoXml.INTEGRIDADE));
        }
        return integridades;
    }

    private void lerDemandas(Element raiz, GerenciadorProducao planta) {
        List<Element> demandas = filhos(unico(raiz, FormatoXml.DEMANDAS), FormatoXml.DEMANDA);

        for (int i = 0; i < demandas.size(); i++) {
            String local = "Demanda nº " + (i + 1) + ": ";
            try {
                lerDemanda(demandas.get(i), planta);
            } catch (DadosInvalidosException e) {
                throw new DadosInvalidosException(local + e.getMessage(), e);
            } catch (DemandaInvalidaException e) {
                throw new DadosInvalidosException(local + e.getMessage(), e);
            } catch (ArquivoCorrompidoException e) {
                throw new ArquivoCorrompidoException(local + e.getMessage(), e);
            }
        }

        // Na aplicacao toda demanda nasce junto com a planta (uma por produto do catalogo)
        // e nunca e removida. Sem uma delas, o produto ficaria sem poder ser demandado
        for (Produto produto : catalogo) {
            boolean existe = planta.getDemandas().stream()
                    .anyMatch(d -> d.getTipoProduto().equals(produto.getNome()));
            if (!existe) {
                throw new DadosInvalidosException("Falta a demanda do produto \"" + produto.getNome()
                        + "\": o arquivo precisa ter uma demanda para cada produto do catálogo");
            }
        }
    }

    private void lerDemanda(Element elemento, GerenciadorProducao planta) {
        String produto = atributo(elemento, FormatoXml.ATRIBUTO_PRODUTO);
        int quantidade = inteiro(elemento, FormatoXml.QUANTIDADE);
        String codigoStatus = texto(elemento, FormatoXml.STATUS).trim();

        StatusDemanda status;
        try {
            status = StatusDemanda.valueOf(codigoStatus);
        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException("Estado de demanda desconhecido no arquivo: \"" + codigoStatus + "\"", e);
        }

        planta.restaurarDemanda(produto, quantidade, status);
    }

    private void lerArmazem(Element raiz, GerenciadorProducao planta, int contador) {
        List<Element> itens = filhos(unico(raiz, FormatoXml.ARMAZEM), FormatoXml.ITEM_ARMAZEM);

        for (int i = 0; i < itens.size(); i++) {
            String local = "Item nº " + (i + 1) + " do armazém: ";
            try {
                lerItemArmazem(itens.get(i), planta, contador);
            } catch (DadosInvalidosException e) {
                throw new DadosInvalidosException(local + e.getMessage(), e);
            } catch (ArquivoCorrompidoException e) {
                throw new ArquivoCorrompidoException(local + e.getMessage(), e);
            }
        }
    }

    private void lerItemArmazem(Element elemento, GerenciadorProducao planta, int contador) {
        String id = atributo(elemento, FormatoXml.ATRIBUTO_ID);
        String nomeProduto = atributo(elemento, FormatoXml.ATRIBUTO_PRODUTO);
        double falha = numero(elemento, FormatoXml.PROBABILIDADE_FALHA_ACUMULADA);

        validarIdDaUnidade(id, nomeProduto, contador);

        try {
            planta.restaurarProdutoNoArmazem(nomeProduto, id, falha);
        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException(e.getMessage(), e);
        }
    }

    // O id de uma unidade e o id do produto do catalogo seguido de "-N", onde N e a posicao
    // dela na numeracao da fabrica. Se N passasse do contador, a proxima fabricacao
    // repetiria um id que ja esta no armazem
    private void validarIdDaUnidade(String id, String nomeProduto, int contador) {
        Produto molde = catalogo.stream().filter(p -> p.getNome().equals(nomeProduto)).findFirst().orElse(null);
        if (molde == null) {
            throw new DadosInvalidosException("O produto \"" + nomeProduto + "\" não está no catálogo");
        }

        String prefixo = molde.getId() + "-";
        String sufixo = id.startsWith(prefixo) ? id.substring(prefixo.length()) : "";
        if (!SUFIXO_ID_UNIDADE.matcher(sufixo).matches()) {
            throw new DadosInvalidosException("O id \"" + id + "\" não tem o formato esperado para "
                    + nomeProduto + " (" + prefixo + "N)");
        }
        if (Integer.parseInt(sufixo) > contador) {
            throw new DadosInvalidosException("O id \"" + id + "\" é maior que o contador de unidades ("
                    + contador + "), o que repetiria ids em futuras fabricações");
        }
    }

    // ----- Navegacao estrita no XML -----

    // Elementos filhos diretos com o nome dado (nao desce a netos, ao contrario de
    // getElementsByTagName, que confundiria <unidade> de lugares diferentes)
    private List<Element> filhos(Element pai, String nome) {
        List<Element> encontrados = new ArrayList<>();
        for (Node no = pai.getFirstChild(); no != null; no = no.getNextSibling()) {
            if (no instanceof Element elemento && elemento.getTagName().equals(nome)) {
                encontrados.add(elemento);
            }
        }
        return encontrados;
    }

    private Element unico(Element pai, String nome) {
        List<Element> encontrados = filhos(pai, nome);

        if (encontrados.isEmpty()) {
            throw new ArquivoCorrompidoException("Falta o elemento <" + nome + "> dentro de <" + pai.getTagName() + ">");
        }
        if (encontrados.size() > 1) {
            throw new ArquivoCorrompidoException("O elemento <" + nome + "> aparece " + encontrados.size()
                    + " vezes dentro de <" + pai.getTagName() + ">, mas deveria aparecer uma só");
        }
        return encontrados.get(0);
    }

    private String texto(Element pai, String nome) {
        return unico(pai, nome).getTextContent();
    }

    private String atributo(Element elemento, String nome) {
        String valor = elemento.getAttribute(nome);

        if (valor.isBlank()) {
            throw new ArquivoCorrompidoException("Falta o atributo \"" + nome + "\" no elemento <"
                    + elemento.getTagName() + ">");
        }
        return valor;
    }

    private double numero(Element pai, String nome) {
        String texto = texto(pai, nome).trim();

        if (!NUMERO_DECIMAL.matcher(texto).matches()) {
            throw new DadosInvalidosException("O valor \"" + texto + "\" de <" + nome + "> em <" + pai.getTagName()
                    + "> não é um número válido");
        }
        double valor = Double.parseDouble(texto);
        if (!Double.isFinite(valor)) {
            throw new DadosInvalidosException("O valor \"" + texto + "\" de <" + nome + "> em <" + pai.getTagName()
                    + "> está fora do limite dos números");
        }
        return valor;
    }

    private int inteiro(Element pai, String nome) {
        String texto = texto(pai, nome).trim();

        if (!NUMERO_INTEIRO.matcher(texto).matches()) {
            throw new DadosInvalidosException("O valor \"" + texto + "\" de <" + nome + "> em <" + pai.getTagName()
                    + "> não é um número inteiro");
        }
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("O valor \"" + texto + "\" de <" + nome + "> em <" + pai.getTagName()
                    + "> é grande demais", e);
        }
    }
}
