# MC322
Repositório para a submissão de tarefas da disciplina MC322.

# Andesite Hardware Co.

*"Silício, solda e ambição"*

## A fábrica

A Andesite Hardware Co. é uma fábrica de hardware. **Andesite** é a nossa linha de
produtos: placas de vídeo, processadores e placas-mãe. Cada uma dessas categorias é
fabricada em três tiers de qualidade: Entrada, Performance e Flagship, que se
diferenciam pela qualidade do componente e pela quantidade de matéria-prima
consumida por unidade.

A produção é guiada por demandas: o usuário informa quantas unidades de cada
produto deseja, e o `GerenciadorProducao` só inicia a fabricação se houver estoque
de matéria-prima e budget suficientes, debitando o budget tanto na compra de
matéria-prima quanto no custo de operação de cada máquina utilizada. A escolha de
qual demanda atender a seguir é delegada a uma estratégia de produção plugável
(padrão Strategy), e o usuário escolhe entre dois cenários de operação — Ideal e
Apocalíptico — no início da execução.

## Fluxo de produção

Cada unidade fabricada passa pela esteira entre uma máquina e outra, respeitando o
estado de cada equipamento (ligado/desligado, em movimento/parado):

```
Estoque de Matéria-Prima → Esteira → Insersora SMT → Esteira → Montadora → Esteira → Inspetora → Armazém
```

1. O estoque de matéria-prima e o budget são verificados antes de qualquer lote começar.
2. A matéria-prima necessária é consumida e o custo de operação do lote é debitado do budget.
3. A demanda muda de `PENDENTE` para `EM_PRODUCAO` (enum `StatusDemanda`).
4. Cada unidade é transportada pela esteira até a Insersora SMT, que solda o die
   (GPU/CPU/chipset) no substrato e muda o status do produto para PROCESSADO.
5. Em seguida, a Montadora realiza o encaixe final (cooler/heatsink em GPUs e CPUs,
   ou conectores/soquetes em placas-mãe).
6. A cada uso, a máquina sofre desgaste: sua saúde (0 a 100) cai um pouco, e a
   probabilidade de falha efetiva cresce conforme a saúde diminui. Insersora e
   Montadora não falham diretamente: elas podem aumentar a probabilidade de falha
   acumulada do produto.
7. Por fim, a Inspetora avalia o produto. A chance de rejeição cresce com a
   probabilidade de falha acumulada, com a saúde da própria Inspetora e com a
   qualidade do produto (quanto mais rigoroso o tier, maior a exigência da
   inspeção).
8. Produtos aprovados vão para o armazém e a demanda é marcada como `CONCLUIDA`;
   produtos rejeitados ou que travam na esteira são descartados do lote.

## Cenários de operação

Escolhido no início da execução, o cenário (`Cenario.IDEAL` ou
`Cenario.APOCALIPTICO`) define o budget inicial da fábrica e escala a
probabilidade de falha e o desgaste por uso de todas as máquinas da linha,
simulando desde uma operação tranquila até uma sob forte estresse financeiro e
mecânico.

## Estrutura das classes

| Classe / Interface / Enum | Responsabilidade |
|--------|------------------|
| `Produto` (abstrata) | Estado comum a todo produto: id, nome, categoria, status, qualidade e probabilidade de falha acumulada. Implementa `Auditavel` |
| `ComponenteEntrada`, `ComponentePerformance`, `ComponenteFlagship` | Subclasses de `Produto` com qualidade (0.5 / 0.7 / 0.9) e tempo de produção próprios |
| `CategoriaProduto` | Enum das linhas de produto (Placa de Vídeo, Processador, Placa-Mãe) e seu tempo base de produção |
| `StatusProduto` | Enum dos estados de um produto (aguardando, processado, inspecionado, rejeitado) |
| `Maquina` (abstrata) | Estado comum a todo equipamento: ligado/desligado, capacidade, custo de operação, saúde e desgaste. Implementa `Auditavel` |
| `InsersoraSMT`, `Montadora`, `Inspetora` | Subclasses de `Maquina`; as duas primeiras só aumentam a falha acumulada do produto, a Inspetora é a única que falha (rejeita) diretamente |
| `Auditavel` | Interface que padroniza diagnóstico (`gerarRelatorioDiagnostico`) e necessidade de manutenção (`precisaManutencao`) entre `Maquina` e `Produto` |
| `MateriaPrima` | Controle de estoque, consumo e reposições |
| `Demanda` | Quantidade de produtos finais solicitada pelo usuário, controlada pelo enum `StatusDemanda` |
| `StatusDemanda` | Enum dos estados de uma demanda (pendente, em produção, concluída, cancelada) |
| `EstrategiaProducao` | Interface do padrão Strategy: seleciona a próxima demanda a ser atendida |
| `EstrategiaFilaDaFundicao`, `EstrategiaMaiorLote`, `EstrategiaMaximoRendimento` | Implementações concretas de `EstrategiaProducao` |
| `Cenario` | Enum dos cenários de operação (Ideal / Apocalíptico) e seus parâmetros de budget, falha e desgaste |
| `Esteira` | Transporta um item por vez entre as etapas da linha, respeitando capacidade |
| `GerenciadorProducao` | Contexto do Strategy; orquestra demandas, budget, matéria-prima, máquinas, cenário e o armazém de produtos fabricados |
| `MenuConsole` | Interface via terminal, organizada em submenus: Demandas, Fabricação, Consultar, Comprar matéria-prima, Gerenciar estratégia, Auditoria |
| `Main` | Ponto de entrada do programa |

## Como compilar e executar

```bash
javac -d bin $(find src -name "*.java")
java -cp bin Main
```