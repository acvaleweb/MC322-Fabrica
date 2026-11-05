package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.exception.DadosInvalidosException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.InsersoraSMT;
import br.unicamp.ic.mc322.model.Inspetora;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.Montadora;
import br.unicamp.ic.mc322.strategy.EstrategiaFilaDaFundicao;
import br.unicamp.ic.mc322.strategy.EstrategiaMaiorLote;
import br.unicamp.ic.mc322.strategy.EstrategiaMaximoRendimento;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;

/**
 * Vocabulário do arquivo XML da planta: nomes dos elementos e atributos, versão do
 * formato e códigos que identificam máquinas e estratégias no arquivo.
 *
 * <p>Fica separado para que a escrita e a leitura do XML usem exatamente os mesmos
 * nomes, sem repetir textos soltos em dois lugares (o que deixaria um lado fora de
 * sincronia com o outro ao primeiro erro de digitação).</p>
 *
 * <p>Estrutura do arquivo (valores de exemplo):</p>
 * <pre>
 * &lt;planta versao="1"&gt;
 *   &lt;cenario&gt;IDEAL&lt;/cenario&gt;
 *   &lt;estrategia&gt;FILA_DA_FUNDICAO&lt;/estrategia&gt;
 *   &lt;budget&gt;3000.0&lt;/budget&gt;
 *   &lt;capacidadeEsteira&gt;5.5&lt;/capacidadeEsteira&gt;
 *   &lt;contadorUnidades&gt;0&lt;/contadorUnidades&gt;
 *   &lt;materiasPrimas&gt;
 *     &lt;materiaPrima id="MP-01"&gt; nome, quantidade, unidade, custoPorUnidade, quantidadeMinima &lt;/materiaPrima&gt;
 *   &lt;/materiasPrimas&gt;
 *   &lt;linhaDeProducao&gt;
 *     &lt;maquina tipo="INSERSORA_SMT"&gt;
 *       nome, capacidadeMaxima, custoOperacao, saude, limiarManutencao,
 *       desgasteMinimoPorUso, desgasteMaximoPorUso, probabilidadeFalha,
 *       &lt;motor&gt;&lt;temperatura/&gt;&lt;/motor&gt;
 *       &lt;sensor&gt;&lt;calibracao/&gt;&lt;/sensor&gt;
 *       &lt;atuadores&gt;&lt;atuador numero="1"&gt;&lt;integridade/&gt;&lt;/atuador&gt; ...&lt;/atuadores&gt;
 *     &lt;/maquina&gt;
 *   &lt;/linhaDeProducao&gt;
 *   &lt;demandas&gt;
 *     &lt;demanda produto="Andesite A7000"&gt;&lt;quantidade/&gt;&lt;status/&gt;&lt;/demanda&gt;
 *   &lt;/demandas&gt;
 *   &lt;armazem&gt;
 *     &lt;item id="PROD-VGA-001-1" produto="Andesite A7000"&gt;&lt;probabilidadeFalhaAcumulada/&gt;&lt;/item&gt;
 *   &lt;/armazem&gt;
 * &lt;/planta&gt;
 * </pre>
 *
 * <p>A ordem das máquinas, das demandas e das unidades do armazém é a ordem de
 * processamento e é preservada ao carregar.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
final class FormatoXml {
    /** Versão do formato gravada no atributo {@code versao} da raiz. */
    static final String VERSAO_ATUAL = "1";

    // Raiz e dados gerais
    static final String PLANTA = "planta";
    static final String ATRIBUTO_VERSAO = "versao";
    static final String CENARIO = "cenario";
    static final String ESTRATEGIA = "estrategia";
    static final String BUDGET = "budget";
    static final String CAPACIDADE_ESTEIRA = "capacidadeEsteira";
    static final String CONTADOR_UNIDADES = "contadorUnidades";

    // Matéria-prima
    static final String MATERIAS_PRIMAS = "materiasPrimas";
    static final String MATERIA_PRIMA = "materiaPrima";
    static final String ATRIBUTO_ID = "id";
    static final String NOME = "nome";
    static final String QUANTIDADE = "quantidade";
    static final String UNIDADE_MEDIDA = "unidade";
    static final String CUSTO_POR_UNIDADE = "custoPorUnidade";
    static final String QUANTIDADE_MINIMA = "quantidadeMinima";

    // Linha de produção, máquinas e Partes
    static final String LINHA_DE_PRODUCAO = "linhaDeProducao";
    static final String MAQUINA = "maquina";
    static final String ATRIBUTO_TIPO = "tipo";
    static final String CAPACIDADE_MAXIMA = "capacidadeMaxima";
    static final String CUSTO_OPERACAO = "custoOperacao";
    static final String SAUDE = "saude";
    static final String LIMIAR_MANUTENCAO = "limiarManutencao";
    static final String DESGASTE_MINIMO_POR_USO = "desgasteMinimoPorUso";
    static final String DESGASTE_MAXIMO_POR_USO = "desgasteMaximoPorUso";
    static final String PROBABILIDADE_FALHA = "probabilidadeFalha";
    static final String MOTOR = "motor";
    static final String TEMPERATURA = "temperatura";
    static final String SENSOR = "sensor";
    static final String CALIBRACAO = "calibracao";
    static final String ATUADORES = "atuadores";
    static final String ATUADOR = "atuador";
    static final String ATRIBUTO_NUMERO = "numero";
    static final String INTEGRIDADE = "integridade";

    // Demandas
    static final String DEMANDAS = "demandas";
    static final String DEMANDA = "demanda";
    static final String ATRIBUTO_PRODUTO = "produto";
    static final String STATUS = "status";

    // Armazém
    static final String ARMAZEM = "armazem";
    // Nao pode se chamar "unidade": esse nome ja e o da unidade de medida da materia-prima
    static final String ITEM_ARMAZEM = "item";
    static final String PROBABILIDADE_FALHA_ACUMULADA = "probabilidadeFalhaAcumulada";

    // Códigos das máquinas no arquivo
    static final String TIPO_INSERSORA_SMT = "INSERSORA_SMT";
    static final String TIPO_MONTADORA = "MONTADORA";
    static final String TIPO_INSPETORA = "INSPETORA";

    // Códigos das estratégias no arquivo
    static final String ESTRATEGIA_FILA_DA_FUNDICAO = "FILA_DA_FUNDICAO";
    static final String ESTRATEGIA_MAIOR_LOTE = "MAIOR_LOTE";
    static final String ESTRATEGIA_MAXIMO_RENDIMENTO = "MAXIMO_RENDIMENTO";

    private FormatoXml() {
    }

    /**
     * Traduz o tipo de uma máquina para o código gravado no arquivo.
     *
     * @param maquina máquina a identificar
     * @return o código do tipo ({@code INSERSORA_SMT}, {@code MONTADORA} ou
     *         {@code INSPETORA})
     * @throws PersistenciaException se a máquina for de um tipo que o formato não conhece
     */
    static String codigoTipoMaquina(Maquina maquina) {
        if (maquina instanceof InsersoraSMT) {
            return TIPO_INSERSORA_SMT;
        }
        if (maquina instanceof Montadora) {
            return TIPO_MONTADORA;
        }
        if (maquina instanceof Inspetora) {
            return TIPO_INSPETORA;
        }
        throw new PersistenciaException("A " + maquina.getNome() + " é de um tipo que a persistência não suporta ("
                + maquina.getClass().getSimpleName() + ")");
    }

    /**
     * Cria uma máquina nova do tipo indicado pelo código lido do arquivo. É a operação
     * inversa de {@link #codigoTipoMaquina(Maquina)}; o estado da máquina (saúde, Partes
     * etc.) é restaurado depois, pelo gerenciador.
     *
     * @param tipo               código do tipo lido do arquivo
     * @param nome               nome da máquina
     * @param capacidadeMaxima   capacidade máxima, em kg
     * @param probabilidadeFalha probabilidade base de falha
     * @param custoOperacao      custo de operação por unidade
     * @return a máquina recém-criada
     * @throws DadosInvalidosException  se o código do tipo for desconhecido
     * @throws IllegalArgumentException se algum valor violar as regras da máquina
     */
    static Maquina criarMaquina(String tipo, String nome, double capacidadeMaxima, double probabilidadeFalha,
            double custoOperacao) {
        return switch (tipo) {
            case TIPO_INSERSORA_SMT -> new InsersoraSMT(nome, capacidadeMaxima, probabilidadeFalha, custoOperacao);
            case TIPO_MONTADORA -> new Montadora(nome, capacidadeMaxima, probabilidadeFalha, custoOperacao);
            case TIPO_INSPETORA -> new Inspetora(nome, capacidadeMaxima, probabilidadeFalha, custoOperacao);
            default -> throw new DadosInvalidosException("Tipo de máquina desconhecido no arquivo: \"" + tipo
                    + "\" (esperado: " + TIPO_INSERSORA_SMT + ", " + TIPO_MONTADORA + " ou " + TIPO_INSPETORA + ")");
        };
    }

    /**
     * Cria a estratégia de produção indicada pelo código lido do arquivo. É a operação
     * inversa de {@link #codigoEstrategia(EstrategiaProducao)}.
     *
     * @param codigo código da estratégia lido do arquivo
     * @return a estratégia
     * @throws DadosInvalidosException se o código for desconhecido
     */
    static EstrategiaProducao criarEstrategia(String codigo) {
        return switch (codigo) {
            case ESTRATEGIA_FILA_DA_FUNDICAO -> new EstrategiaFilaDaFundicao();
            case ESTRATEGIA_MAIOR_LOTE -> new EstrategiaMaiorLote();
            case ESTRATEGIA_MAXIMO_RENDIMENTO -> new EstrategiaMaximoRendimento();
            default -> throw new DadosInvalidosException("Estratégia de produção desconhecida no arquivo: \""
                    + codigo + "\" (esperado: " + ESTRATEGIA_FILA_DA_FUNDICAO + ", " + ESTRATEGIA_MAIOR_LOTE
                    + " ou " + ESTRATEGIA_MAXIMO_RENDIMENTO + ")");
        };
    }

    /**
     * Traduz uma estratégia de produção para o código gravado no arquivo.
     *
     * @param estrategia estratégia a identificar
     * @return o código da estratégia ({@code FILA_DA_FUNDICAO}, {@code MAIOR_LOTE} ou
     *         {@code MAXIMO_RENDIMENTO})
     * @throws PersistenciaException se a estratégia for de um tipo que o formato não
     *                               conhece
     */
    static String codigoEstrategia(EstrategiaProducao estrategia) {
        if (estrategia instanceof EstrategiaFilaDaFundicao) {
            return ESTRATEGIA_FILA_DA_FUNDICAO;
        }
        if (estrategia instanceof EstrategiaMaiorLote) {
            return ESTRATEGIA_MAIOR_LOTE;
        }
        if (estrategia instanceof EstrategiaMaximoRendimento) {
            return ESTRATEGIA_MAXIMO_RENDIMENTO;
        }
        throw new PersistenciaException("A estratégia de produção " + estrategia.getNomeEstrategia()
                + " não é suportada pela persistência");
    }
}
