# MC322
Repositório para a submissão de tarefas da disciplina MC322.

# Andesite Hardware Co.

*"Silício, solda e ambição"*

## A fábrica

A Andesite Hardware Co. é uma fábrica de hardware. **Andesite** é a nossa linha de
produtos: placas de vídeo, processadores, placas-mãe e outros componentes que vão
saindo da mesma planta industrial.

## Fluxo de produção

A simulação segue o fluxo completo pedido pelo enunciado, com cada equipamento
respeitando seus próprios estados (ligado/desligado, em movimento/parado,
ativo/desativado):

```
Estoque de Matéria-Prima → Esteira → Máquina → Esteira → Estação de Inspeção → Produto Final
```

1. O estoque de matéria-prima é verificado antes de qualquer produção começar.
2. A esteira é ligada e recebe a matéria-prima, respeitando sua capacidade máxima.
3. A máquina processa a matéria-prima, consumindo o estoque e liberando o produto.
4. A esteira transporta o produto pronto até a estação de inspeção.
5. A estação de inspeção ativa, inspeciona e aprova o produto final.

## Estrutura das classes

| Classe | Responsabilidade |
|--------|------------------|
| `MateriaPrima` | Controle de estoque, consumo e reposições |
| `Produto` | Estado de cada produto da linha Andesite (aguardando, processado, inspecionado) |
| `Maquina` | Transforma matéria-prima em produto, respeitando capacidade e estado |
| `Esteira` | Transporta um item por vez entre as etapas da linha |
| `EstacaoInspecao` | Inspeciona produtos processados e contabiliza o total inspecionado |
| `MenuConsole` | Interface via terminal para consultar estoque, comprar matéria-prima e fabricar produtos |
| `Main` | Ponto de entrada do programa |

## Como compilar e executar

```bash
javac -d bin $(find src -name "*.java")
java -cp bin Main
```