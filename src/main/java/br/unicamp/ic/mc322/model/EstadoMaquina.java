package br.unicamp.ic.mc322.model;

import java.util.List;

/**
 * Instantâneo imutável do estado variável de uma {@link Maquina} e das suas Partes
 * (motor, sensor e atuadores).
 *
 * <p>É o que a {@link Maquina} entrega em {@link Maquina#obterEstado()} e aceita de
 * volta em {@link Maquina#restaurarEstado(EstadoMaquina)}. Assim, quem salva e carrega
 * a planta (a camada de persistência) não precisa tocar nas Partes, que continuam
 * escondidas dentro da máquina: o objeto apenas transporta números.</p>
 *
 * <p>Este objeto não valida os valores: quem valida é a própria máquina ao restaurar,
 * porque só ela conhece as faixas permitidas para cada Parte.</p>
 *
 * @param saude                  saúde da máquina, de 0 (quebrada) a 100
 * @param limiarManutencao       saúde abaixo da qual a máquina precisa de manutenção
 * @param desgasteMinimoPorUso   menor perda de saúde por uso
 * @param desgasteMaximoPorUso   maior perda de saúde por uso
 * @param probabilidadeFalha     probabilidade base de falha, já ajustada pelo cenário
 * @param temperaturaMotor       temperatura do motor, em °C
 * @param calibracaoSensor       calibração do sensor, de 0 a 100
 * @param integridadeAtuadores   integridade de cada atuador, na ordem dos atuadores
 *
 * @author Equipe Andesite Hardware Co.
 */
public record EstadoMaquina(
        double saude,
        double limiarManutencao,
        double desgasteMinimoPorUso,
        double desgasteMaximoPorUso,
        double probabilidadeFalha,
        double temperaturaMotor,
        double calibracaoSensor,
        List<Double> integridadeAtuadores) {

    /**
     * Cria o instantâneo guardando uma cópia imutável da lista de atuadores, para que
     * alterações posteriores na lista original não o modifiquem.
     *
     * @throws IllegalArgumentException se a lista de atuadores for nula ou tiver
     *                                  elementos nulos
     */
    public EstadoMaquina {
        if (integridadeAtuadores == null) {
            throw new IllegalArgumentException("A lista de integridade dos atuadores não pode ser nula");
        }
        for (Double integridade : integridadeAtuadores) {
            if (integridade == null) {
                throw new IllegalArgumentException("A integridade de um atuador não pode ser nula");
            }
        }
        integridadeAtuadores = List.copyOf(integridadeAtuadores);
    }
}
