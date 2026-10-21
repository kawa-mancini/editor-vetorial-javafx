# Editor de Desenho Vetorial Interativo (JavaFX) - Trabalho de POO 1

Trabalho acadêmico da UniRV (Engenharia de Software, Prof. Sergio Souza Novak).
O enunciado completo está em `docs/enunciado.pdf` e o diagrama de classes em
`docs/diagrama.pdf`. Em caso de dúvida, o enunciado prevalece sobre este arquivo.

## Como trabalhar neste projeto

- Somos estudantes: ao criar ou alterar código, explique em português o que foi feito
  e por quê, principalmente quando envolver um padrão de projeto.
- Comentários no código e mensagens de commit em português.
- Avance em etapas pequenas. Ao fim de cada etapa: `mvn compile` sem erros e um commit.
- Siga os nomes de classes, métodos e pacotes do diagrama. Não renomeie.
- Atributos sempre `private` ou `protected`, com getters/setters.
- Não adicione dependências além de JavaFX e uma biblioteca de JSON (Gson ou Jackson)
  sem perguntar. JUnit 5 (scope test) já foi aprovado.
- Testes ficam em `src/test/java` e rodam com `mvn test`; mantenha-os passando.

## Requisitos técnicos

- Java 17+, JavaFX 17+ (código programático ou FXML), Apache Maven (`pom.xml`).
- Rodar com `mvn javafx:run` (plugin `javafx-maven-plugin`).
- Persistência em JSON (preferência) ou XML.

## Pacotes e classes (conforme o diagrama)

`org.example.Models`
- `FormaGeometrica` (interface, contrato completo na seção 6 do enunciado)
- `FormaBase` (abstrata): `# pontos: List<Ponto>`, `# corPreenchimentoHex`,
  `# corBordaHex`, `# espessuraBorda`, `# groupId` + implementações comuns
- `Ponto`: `- x`, `- y`, `mover(dx, dy)`, `distancia(outro)`, `clonar()`
- `Poligono()`, `Circulo(centro, raio)`, `Quadrado(centro, raio)`,
  `Hexagono(centro, raio)` - todas herdam de `FormaBase` e sobrescrevem `clonar()`

`org.example.Controller`
- `EditorContext`: `formas`, `selecionados`, `formaEmAndamento`, `ferramentaAtual`,
  `gridSnappingHabilitado`; métodos `requestRedraw()`, `requestStatsUpdate()`,
  `agruparSelecionados()`, `desagruparSelecionados()`, `trazerParaFrente()`,
  `enviarParaTras()`, `aplicarSnap(valor)`
- `HistoricoController`: `undoStack` e `redoStack` (`Stack<List<FormaGeometrica>>`);
  `registrarEstado(lista)`, `desfazer(atual)`, `refazer(atual)`
- `Ferramenta` (interface): `aoPressionarMouse`, `aoArrastarMouse`, `aoSoltarMouse`,
  `aoClicar` - todos recebem `(MouseEvent, EditorContext)`
- `FerramentaDesenhar`, `FerramentaSelecionar` (`arrastandoCaixa`, `selecaoStartX`,
  `selecaoStartY`), `FerramentaTransformar` (`modo: ModoFerramenta`),
  `FerramentaInserirForma` (`centroInsercao`), `FerramentaEditarVertice`
  (`verticeSelecionado`)
- `ModoFerramenta` (enum): ESCALAR, ROTACIONAR, CISALHAR

`org.example.Views`
- `ContainerApp` (Application): `areaDeDesenho: Canvas`, `contexto`, `barraLateral`,
  `painelStatus`; `inicializarEventos()`, `redesenharCanvas()`,
  `desenharForma(forma, selecionada)`, `salvarArquivo()`, `carregarArquivo()`
- `BarraLateralView`: `atualizarCoresInterface()`, `construirSecaoEstilos()`,
  `construirSecaoFerramentas()`, `construirSecaoCamadas()`
- `PainelStatusView`: `atualizar()`

## Padrões de projeto (35% da nota - prioridade máxima)

- **State**: o `EditorContext` só repassa eventos do mouse para `ferramentaAtual`.
  Proibido `if/switch` por tipo de ferramenta nos handlers do canvas.
- **Prototype**: `clonar()` faz cópia PROFUNDA (clona cada `Ponto`). Original e cópia
  nunca podem compartilhar objetos `Ponto`.
- **Memento**: antes de cada alteração, `registrarEstado` empilha uma cópia profunda da
  lista de formas. Nova alteração limpa o `redoStack`.
- **Composite**: formas com o mesmo `groupId` (não nulo) se movem e se transformam
  juntas, como uma forma só (transformações em torno do centroide do grupo).

## Regras de negócio (Models)

- Polígono precisa de no mínimo 3 pontos; círculo precisa de raio > 0.
  Violação lança `IllegalArgumentException`.
- Quadrado e Hexágono calculam os vértices a partir de centro e raio (4 e 6 pontos
  distribuídos com seno/cosseno).
- Área de polígono: fórmula de Gauss (shoelace), valor absoluto.
- Perímetro: soma das distâncias euclidianas, incluindo último -> primeiro ponto.
- Centroide: centro de massa do polígono.
- Translação: soma dx, dy a cada ponto.
- Escala: multiplica a distância de cada ponto ao centroide pelo fator.
- Rotação: em torno do centroide, ângulo em radianos.
- Cisalhamento: x' = x + shx*y, y' = y + shy*x, relativo ao centroide.
- `contemPonto`: ray casting para polígonos; distância <= raio para círculo.
- **Círculo**: guardar o centro em `pontos` e o raio em atributo próprio; sobrescrever
  área (pi*r^2), perímetro (2*pi*r), escala (multiplica o raio), `contemPonto` e
  `getCentroide`. Rotação pode ser no-op.

## Interface (ver figuras 4 a 10 do enunciado)

Barra lateral à esquerda, canvas no centro, barra de status embaixo.

- Estilos: cor de preenchimento, cor da borda, espessura da borda (slider 1-10),
  checkbox "Grade Magnética (Snapping)"
- Ferramentas: Linha Livre (polígono por cliques), Selecionar, Escalar, Rotacionar,
  Cisalhar, Excluir, Editar Vértice, Excluir Selecionados
- Formas: Inserir Quadrado, Inserir Círculo, Inserir Hexágono (por arrasto)
- Camadas/Grupo: Trazer p/ Frente, Enviar p/ Trás, Agrupar, Desagrupar
- Sistema: Desfazer, Refazer, Salvar, Carregar
- Botões de ferramenta funcionam como toggle (só uma ativa).
- Seleção: borda verde na forma e vértices destacados em vermelho.
- Status: coordenadas do mouse, total de formas, área e perímetro da seleção.
- Snap to grid: arredonda coordenadas para a grade quando habilitado.
- A ordem da lista `formas` é a ordem das camadas (última = na frente).

## Persistência e erros (20% da nota)

- Salvar em JSON: configurações do canvas + lista de formas (tipo, pontos, raio quando
  houver, cores, espessura, groupId).
- Carregar reconstrói as formas pelo campo "tipo".
- Arquivo corrompido: `try-catch` + `Alert` explicativo, sem encerrar a aplicação.
- Ao fechar com alterações não salvas: perguntar se deseja salvar.

## Critérios de avaliação

Modelagem geométrica e validações 25% | Padrões (State, Prototype, Memento) 35% |
Usabilidade da GUI e mouse 20% | Persistência JSON/XML e tratamento de erros 20%

## Entrega

Repositório Git com projeto Maven funcional, `README.md` (descrição e como executar) e
o diagrama de classes UML em PDF (atualizado se o código tiver classes extras).

## Ordem de implementação sugerida

1. Projeto Maven + JavaFX abrindo janela vazia
2. Models com toda a matemática (testar cálculos antes da interface)
3. `ContainerApp` com canvas e barra lateral desenhando formas fixas
4. State: `EditorContext`, `Ferramenta` e as ferramentas, uma por vez
5. Transformações (escalar, rotacionar, cisalhar, editar vértice)
6. Desfazer/refazer com `HistoricoController`
7. Agrupar/desagrupar e camadas
8. Salvar/carregar JSON + tratamento de erros
9. Barra de status, snap to grid, README e diagrama final
