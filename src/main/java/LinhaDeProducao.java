import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Agrupador das máquinas da fábrica: a linha de produção.
 *
 * <p><b>Agregação, não composição:</b> as máquinas são criadas fora da linha e apenas
 * entram nela ({@link #adicionarMaquina(Maquina)}). Uma máquina pode ser retirada
 * ({@link #removerMaquina(Maquina)}) e colocada em outra linha, então o ciclo de vida
 * dela não depende da linha. Já as Partes internas de cada máquina (motor, sensor e
 * atuadores) pertencem a ela por composição.</p>
 *
 * <p>A coleção interna nunca é exposta: {@link #getMaquinas()} devolve uma visão
 * imutável. A auditoria desce em cascata: a linha chama o diagnóstico de cada máquina,
 * que chama o de cada Parte.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class LinhaDeProducao implements Auditavel {
    private static final String RECUO = "  ";

    private final String nome;
    private final List<Maquina> maquinas;

    /**
     * Cria uma linha vazia.
     *
     * @param nome nome da linha, usado nos relatórios
     */
    public LinhaDeProducao(String nome) {
        this.nome = nome;
        this.maquinas = new ArrayList<>();
    }

    /**
     * Adiciona uma máquina ao fim da linha; a ordem define a ordem de processamento.
     *
     * @param maquina máquina a incluir
     * @throws IllegalArgumentException se a máquina for nula ou já estiver na linha
     */
    public void adicionarMaquina(Maquina maquina) {
        if (maquina == null) {
            throw new IllegalArgumentException("Não é possível adicionar uma máquina nula à linha");
        }
        if (maquinas.contains(maquina)) {
            throw new IllegalArgumentException("A " + maquina.getNome() + " já está na linha " + nome);
        }
        maquinas.add(maquina);
    }

    /**
     * Retira uma máquina da linha. A máquina continua existindo e pode ir para outra linha.
     *
     * @param maquina máquina a retirar
     * @return {@code true} se ela estava na linha
     */
    public boolean removerMaquina(Maquina maquina) {
        return maquinas.remove(maquina);
    }

    /**
     * Repara todas as máquinas que precisam de manutenção.
     *
     * @return quantas máquinas foram reparadas
     */
    public int repararMaquinas() {
        int reparadas = 0;
        for (Maquina maquina : maquinas) {
            if (maquina.precisaManutencao()) {
                maquina.reparar();
                reparadas++;
            }
        }
        return reparadas;
    }

    /**
     * Soma o custo de operação de todas as máquinas para produzir uma unidade.
     *
     * @return custo por unidade, em reais
     */
    public double calcularCustoOperacaoPorUnidade() {
        double custo = 0;
        for (Maquina maquina : maquinas) {
            custo += maquina.getCustoOperacao();
        }
        return custo;
    }

    /**
     * Conta quantas máquinas precisam de manutenção (inclui falhas nas Partes).
     *
     * @return quantidade de máquinas em risco
     */
    public int contarMaquinasEmRisco() {
        int total = 0;
        for (Maquina maquina : maquinas) {
            if (maquina.precisaManutencao()) {
                total++;
            }
        }
        return total;
    }

    /**
     * Informa se a linha não tem máquinas.
     *
     * @return {@code true} se está vazia
     */
    public boolean estaVazia() {
        return maquinas.isEmpty();
    }

    /**
     * Retorna as máquinas, na ordem de processamento, em uma visão imutável.
     *
     * @return lista imutável de máquinas
     */
    public List<Maquina> getMaquinas() {
        return Collections.unmodifiableList(maquinas);
    }

    /**
     * Retorna o nome da linha.
     *
     * @return nome
     */
    public String getNome() {
        return nome;
    }

    /**
     * Uma linha precisa de atenção se alguma máquina dela precisa.
     *
     * @return {@code true} se há máquina em risco
     */
    @Override
    public boolean precisaManutencao() {
        return contarMaquinasEmRisco() > 0;
    }

    /**
     * Diagnóstico hierárquico e indentado (Linha, Máquinas e Partes). Cada máquina
     * monta o próprio trecho, chamando o diagnóstico das suas Partes.
     *
     * @return o relatório em várias linhas
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("Linha ").append(nome).append(" (").append(maquinas.size())
                .append(" máquina(s), ").append(contarMaquinasEmRisco()).append(" em risco)");

        for (Maquina maquina : maquinas) {
            relatorio.append("\n").append(RECUO).append("> ")
                    .append(maquina.gerarRelatorioDiagnostico().replace("\n", "\n" + RECUO));
        }
        return relatorio.toString();
    }
}
