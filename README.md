# MC322
Repositório para a submissão de tarefas da disciplina MC322.

# Andesite Hardware Co.

*"Silício, solda e ambição"*

## A fábrica

A Andesite Hardware Co. é uma fábrica de hardware. **Andesite** é a nossa linha de
produtos: placas de vídeo, processadores e placas-mãe. Cada uma dessas categorias é
fabricada em três tiers de qualidade — Entrada, Performance e Flagship —, que se
diferenciam pela qualidade do componente e pela quantidade de matéria-prima
consumida por unidade.

A produção é guiada por demandas: o usuário informa quantas unidades de cada
produto deseja, e o `GerenciadorProducao` só inicia a fabricação se houver estoque
de matéria-prima e budget suficientes, debitando o budget tanto na compra de
matéria-prima quanto no custo de operação de cada máquina utilizada.

## Fluxo de produção

Cada unidade fabricada passa pela esteira entre uma máquina e outra, respeitando o
estado de cada equipamento (ligado/desligado, em movimento/parado):

```
Estoque de Matéria-Prima → Esteira → Insersora SMT → Esteira → Montadora → Esteira → Inspetora → Armazém
```

1. O estoque de matéria-prima e o budget são verificados antes de qualquer lote começar.
2. A matéria-prima necessária é consumida e o custo de operação do lote é debitado do budget.
3. Cada unidade é transportada pela esteira até a Insersora SMT, que solda o die
   (GPU/CPU/chipset) no substrato e muda o status do produto para PROCESSADO.
4. Em seguida, a Montadora realiza o encaixe final (cooler/heatsink em GPUs e CPUs,
   ou conectores/soquetes em placas-mãe).
5. Insersora e Montadora não falham diretamente: elas podem aumentar a
   probabilidade de falha acumulada do produto.
6. Por fim, a Inspetora avalia o produto. A chance de rejeição cresce com a
   probabilidade de falha acumulada e com a própria qualidade do produto (quanto
   mais rigoroso o tier, maior a exigência da inspeção).
7. Produtos aprovados vão para o armazém; produtos rejeitados ou que travam na
   esteira são descartados do lote.

## Estrutura das classes

| Classe | Responsabilidade |
|--------|------------------|
| `Produto` (abstrata) | Estado comum a todo produto: id, nome, categoria, status, qualidade e probabilidade de falha acumulada |
| `ComponenteEntrada`, `ComponentePerformance`, `ComponenteFlagship` | Subclasses de `Produto` com qualidade (0.5 / 0.7 / 0.9) e tempo de produção próprios |
| `CategoriaProduto` | Enum das linhas de produto (Placa de Vídeo, Processador, Placa-Mãe) e seu tempo base de produção |
| `StatusProduto` | Enum dos estados de um produto (aguardando, processado, inspecionado, rejeitado) |
| `Maquina` (abstrata) | Estado comum a todo equipamento: ligado/desligado, capacidade, custo de operação e probabilidade de falha |
| `InsersoraSMT`, `Montadora`, `Inspetora` | Subclasses de `Maquina`; as duas primeiras só aumentam a falha acumulada do produto, a Inspetora é a única que falha (rejeita) diretamente |
| `MateriaPrima` | Controle de estoque, consumo e reposições |
| `Demanda` | Quantidade de produtos finais solicitada pelo usuário e status de atendimento |
| `Esteira` | Transporta um item por vez entre as etapas da linha, respeitando capacidade |
| `GerenciadorProducao` | Orquestra demandas, budget, matéria-prima, máquinas e o armazém de produtos fabricados |
| `MenuConsole` | Interface via terminal para consultar estoque/armazém, atualizar demandas, fabricar produtos e comprar matéria-prima |
| `Main` | Ponto de entrada do programa |

## Como compilar e executar

```bash
javac -d bin $(find src -name "*.java")
java -cp bin Main
```