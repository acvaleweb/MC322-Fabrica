package br.unicamp.ic.mc322.model;

import br.unicamp.ic.mc322.exception.QuantidadeInvalidaException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;

/**
 * Matéria-prima da fábrica (o wafer de silício), com controle de estoque.
 *
 * <p>Regras de negócio: toda movimentação precisa ser maior que zero e, ao consumir ou
 * comprar, a quantidade precisa atingir o <b>lote mínimo</b>. As validações são feitas
 * antes de qualquer alteração, então, quando uma exceção é lançada, o estoque continua
 * intacto.</p>
 *
 * @author Equipe Andesite Hardware Co.
 */
public class MateriaPrima {
    private String id;
    private String nome;
    private double quantidade;
    private String unidade;
    private double custoPorUnidade;
    private double quantidadeMinima;

    /**
     * Cria uma matéria-prima com o estoque inicial informado.
     *
     * @param id               identificador do material
     * @param nome             nome do material
     * @param quantidade       estoque inicial
     * @param unidade          unidade de medida (ex.: "unidade")
     * @param custoPorUnidade  custo de compra por unidade, em reais
     * @param quantidadeMinima lote mínimo aceito em compras e consumos
     * @throws IllegalArgumentException se id, nome ou unidade forem vazios, ou se a
     *                                  quantidade, o custo ou o lote mínimo forem
     *                                  negativos ou não forem números finitos
     */
    public MateriaPrima(String id, String nome, double quantidade, String unidade, double custoPorUnidade, double quantidadeMinima) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O identificador da matéria-prima não pode ser vazio");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da matéria-prima não pode ser vazio");
        }
        if (unidade == null || unidade.isBlank()) {
            throw new IllegalArgumentException("A unidade de medida de " + nome + " não pode ser vazia");
        }
        if (!(Double.isFinite(quantidade) && quantidade >= 0)) {
            throw new IllegalArgumentException("A quantidade em estoque de " + nome
                    + " precisa ser um número maior ou igual a zero (informado: " + quantidade + ")");
        }
        if (!(Double.isFinite(custoPorUnidade) && custoPorUnidade >= 0)) {
            throw new IllegalArgumentException("O custo por unidade de " + nome
                    + " precisa ser um número maior ou igual a zero (informado: " + custoPorUnidade + ")");
        }
        if (!(Double.isFinite(quantidadeMinima) && quantidadeMinima >= 0)) {
            throw new IllegalArgumentException("O lote mínimo de " + nome
                    + " precisa ser um número maior ou igual a zero (informado: " + quantidadeMinima + ")");
        }

        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.custoPorUnidade = custoPorUnidade;
        this.quantidadeMinima = quantidadeMinima;
    }

    /**
     * Consome uma quantidade do estoque. Todas as validações acontecem antes de alterar
     * o estoque: se alguma falhar, a exceção é lançada e a quantidade permanece intacta.
     *
     * @param quantidadeDemandada quantidade a consumir
     * @throws QuantidadeInvalidaException  se a quantidade for menor ou igual a zero ou
     *                                      estiver abaixo do lote mínimo
     * @throws RecursoInsuficienteException se não houver estoque suficiente
     */
    public void consumir(double quantidadeDemandada) {
        if (quantidadeDemandada <= 0) {
            throw new QuantidadeInvalidaException(
                    "A quantidade de " + nome + " a consumir precisa ser maior que zero");
        }

        if (!atendeLoteMinimo(quantidadeDemandada)) {
            throw new QuantidadeInvalidaException("A quantidade de " + nome + " a consumir ("
                    + quantidadeDemandada + ") está abaixo do lote mínimo (" + quantidadeMinima + ")");
        }

        if (!verificarDisponibilidade(quantidadeDemandada)) {
            throw new RecursoInsuficienteException(nome, quantidadeDemandada, quantidade);
        }

        this.quantidade -= quantidadeDemandada;
    }


    /**
     * Verifica se a quantidade atinge o lote mínimo.
     *
     * @param quantidadeDemandada quantidade a verificar
     * @return {@code true} se a quantidade é maior ou igual ao lote mínimo
     */
    public boolean atendeLoteMinimo(double quantidadeDemandada) {
        return quantidadeDemandada >= this.quantidadeMinima;
    }

    /**
     * Adiciona uma quantidade ao estoque (compra já validada pelo gerenciador).
     *
     * @param quantidadeAdicional quantidade a adicionar
     * @throws QuantidadeInvalidaException se a quantidade for menor ou igual a zero
     */
    public void adicionarEstoque(double quantidadeAdicional) {
        if (quantidadeAdicional <= 0) {
            throw new QuantidadeInvalidaException(
                    "A quantidade de " + nome + " a adicionar precisa ser maior que zero");
        }

        this.quantidade += quantidadeAdicional;
    }

    /**
     * Verifica se há estoque suficiente para a quantidade pedida.
     *
     * @param quantidadeDemandada quantidade a verificar
     * @return {@code true} se o estoque cobre a quantidade
     */
    public boolean verificarDisponibilidade(double quantidadeDemandada) {
        return this.quantidade >= quantidadeDemandada;
    }

    // Getters

    /**
     * Retorna o identificador do material.
     *
     * @return identificador
     */
    public String getId() {
        return id;
    }

    /**
     * Retorna o nome do material.
     *
     * @return nome
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o estoque atual.
     *
     * @return quantidade em estoque
     */
    public double getQuantidade() {
        return quantidade;
    }

    /**
     * Retorna a unidade de medida.
     *
     * @return unidade de medida
     */
    public String getUnidade() {
        return unidade;
    }

    /**
     * Retorna o custo de compra por unidade.
     *
     * @return custo por unidade, em reais
     */
    public double getCustoPorUnidade() {
        return custoPorUnidade;
    }

    /**
     * Retorna o lote mínimo aceito em compras e consumos.
     *
     * @return lote mínimo
     */
    public double getQuantidadeMinima() {
        return quantidadeMinima;
    }
}
