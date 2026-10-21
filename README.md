# Editor de Desenho Vetorial Interativo (JavaFX)

Trabalho da avaliação N3 da disciplina **Programação Orientada a Objetos 1**
(Engenharia de Software, UniRV, Prof. Me. Sergio Souza Novak).

Editor desktop para criar, editar e salvar formas geométricas 2D (polígonos livres,
quadrados, círculos e hexágonos) sobre uma tela interativa, aplicando os padrões de
projeto **State**, **Prototype**, **Memento** e **Composite** e a separação em camadas
**Models / Controller / Views**.

**Autor:** Kawã Mancini

---

## Requisitos

| Ferramenta | Versão |
|---|---|
| Java (JDK) | 17 ou superior |
| Apache Maven | 3.8 ou superior |
| JavaFX | 21 (baixado automaticamente pelo Maven) |
| Gson | 2.11 (baixado automaticamente pelo Maven) |

## Como executar

Na pasta do projeto:

```bash
mvn javafx:run
```

Outros comandos úteis:

```bash
mvn compile      # só compila
mvn test         # roda os 110 testes automáticos (JUnit 5)
```

**No IntelliJ IDEA:** abra a pasta como projeto Maven e, na janela *Maven*, execute
`Plugins > javafx > javafx:run`.

---

## Funcionalidades

A janela tem a **barra lateral** à esquerda, o **canvas** no centro e a **barra de status** embaixo.

### Ferramentas (só uma fica ativa por vez)

| Ferramenta | Como usar |
|---|---|
| **Linha Livre** | Cada clique adiciona um vértice. Fecha o polígono com duplo clique, clique direito ou clicando perto do primeiro ponto. |
| **Selecionar** | Clique seleciona; **Shift + clique** adiciona/remove da seleção; arrastar move; arrastar no vazio faz uma caixa de seleção. |
| **Escalar / Rotacionar / Cisalhar** | Arraste com a seleção ativa. As formas se transformam em torno do centroide da seleção. |
| **Excluir** | Clique numa forma para apagá-la (apaga o grupo inteiro, se ela estiver agrupada). |
| **Editar Vértice** | Arraste um vértice (ponto vermelho) para deformar a forma. |
| **Inserir Quadrado / Círculo / Hexágono** | Pressione no centro e arraste para definir o raio. |

### Demais controles

- **Estilos:** cor de preenchimento, cor da borda, espessura (1 a 10) e grade magnética (snap to grid).
  Mudar o estilo com formas selecionadas altera essas formas.
- **Camadas/Grupo:** Trazer p/ Frente, Enviar p/ Trás, Agrupar e Desagrupar.
- **Sistema:** Desfazer, Refazer, Salvar e Carregar.
- **Seleção visual:** borda verde, vértices em vermelho e contorno tracejado em volta dos grupos.
- **Barra de status:** coordenadas do mouse, total de formas e área/perímetro da seleção em tempo real.

### Atalhos de teclado

| Atalho | Ação |
|---|---|
| `Ctrl+Z` | Desfazer |
| `Ctrl+Y` ou `Ctrl+Shift+Z` | Refazer |
| `Delete` | Excluir selecionados |
| `Ctrl+S` | Salvar |
| `Ctrl+O` | Carregar |

---

## Arquitetura

```
src/main/java/org/example/
├── Models/       Formas e regras de negócio (não conhecem JavaFX)
│   ├── FormaGeometrica   interface (contrato da seção 6 do enunciado)
│   ├── FormaBase         classe abstrata com a matemática comum
│   ├── Ponto, Poligono, Circulo, Quadrado, Hexagono
├── Controller/   Estado do editor, ferramentas, histórico e persistência
│   ├── EditorContext, Ferramenta, Ferramenta*, ModoFerramenta
│   ├── HistoricoController
│   └── PersistenciaJson
└── Views/        Interface gráfica
    ├── ContainerApp      janela principal (Application)
    ├── BarraLateralView
    └── PainelStatusView
```

O Controller não importa nenhuma classe da View. A View entrega ao `EditorContext`
funções (*callbacks*) para redesenhar, atualizar o status e mostrar avisos, e o contexto
apenas as chama (`requestRedraw()`, `requestStatsUpdate()`, `mostrarAviso()`).

O diagrama de classes atualizado está em [`docs/diagrama_atualizado.pdf`](docs/diagrama_atualizado.pdf).
A última página lista tudo o que foi acrescentado em relação ao diagrama original
([`docs/diagrama.pdf`](docs/diagrama.pdf)) e o motivo de cada acréscimo.

---

## Padrões de projeto

### State: ferramentas
O canvas não sabe qual ferramenta está ativa. Os eventos do mouse vão para o
`EditorContext`, que só os repassa para `ferramentaAtual` (`Ferramenta`). Trocar de
ferramenta é trocar esse objeto; não existe `if/switch` por tipo de ferramenta.

- `ContainerApp.inicializarEventos()` liga o canvas ao contexto.
- `EditorContext.aoPressionarMouse(...)` → `ferramentaAtual.aoPressionarMouse(evento, this)`.
- As três transformações são a mesma classe `FerramentaTransformar` com um `ModoFerramenta`
  diferente. Cada constante do enum implementa o próprio método `transformar(...)`.
- Uma única `FerramentaInserirForma` cria quadrados, círculos ou hexágonos, conforme a
  fábrica recebida (`Quadrado::new`, `Circulo::new`, `Hexagono::new`).

### Prototype: `clonar()`
Toda forma implementa `clonar()` com **cópia profunda**: cada `Ponto` é clonado
(`FormaBase.copiarAtributosPara`), então original e cópia nunca compartilham objetos.
Isso é verificado em `ClonarTest`.

### Memento: desfazer/refazer
`HistoricoController` guarda "fotografias" da lista de formas em `undoStack` e `redoStack`.
Antes de cada alteração, `EditorContext.registrarEstado()` empilha uma cópia profunda
(feita com `clonar()`). Uma alteração nova limpa o `redoStack`. Um arrasto inteiro conta
como um único passo.

### Composite: grupos
Agrupar dá o mesmo `groupId` (um UUID) às formas selecionadas. `EditorContext.selecionar()`
sempre seleciona o grupo inteiro e, como todas as ferramentas trabalham sobre a seleção,
o grupo se move, se transforma (em torno do centroide do grupo) e é excluído como uma
forma só.

---

## Regras de negócio

- Polígono precisa de **no mínimo 3 pontos**; círculo precisa de **raio > 0**. A violação
  lança `IllegalArgumentException`.
- Quadrado e hexágono calculam os vértices a partir do centro e do raio com seno/cosseno.
- **Área:** fórmula de Gauss (*shoelace*), em valor absoluto. **Perímetro:** soma das
  distâncias euclidianas, incluindo a aresta do último ao primeiro ponto.
- **Centroide:** centro de massa do polígono.
- **Transformações:** translação (soma dx, dy), escala e rotação em torno do centroide,
  cisalhamento `x' = x + shx·y`, `y' = y + shy·x` relativo ao centroide.
- **Ponto dentro da forma:** *ray casting* para polígonos; distância ≤ raio para o círculo.
- O círculo guarda o centro em `pontos` e o raio num atributo próprio. Rotacioná-lo ou
  cisalhá-lo não o altera, pois o modelo centro + raio não representa uma elipse.

---

## Persistência (JSON)

O desenho é salvo em JSON com as configurações do canvas e a lista de formas, na ordem
das camadas:

```json
{
  "versao": 1,
  "canvas": { "largura": 985.0, "altura": 770.0, "gridSnappingHabilitado": false, "tamanhoGrade": 20.0 },
  "formas": [
    {
      "tipo": "Circulo",
      "pontos": [ { "x": 180.0, "y": 180.0 } ],
      "raio": 80.0,
      "corPreenchimentoHex": "#4A8080",
      "corBordaHex": "#000000",
      "espessuraBorda": 1.0,
      "groupId": null
    }
  ]
}
```

Ao carregar, cada forma é reconstruída pelo campo `"tipo"`.

### Tratamento de erros
- **Arquivo corrompido** (JSON inválido, campo faltando, tipo desconhecido, raio ≤ 0,
  polígono com menos de 3 pontos, cor inválida...): o programa mostra um alerta explicando
  o problema e indicando qual forma está errada. O desenho atual **não** é alterado, porque
  o arquivo só é aplicado se for lido por inteiro sem erros.
- **Alterações não salvas:** ao fechar a janela ou carregar outro arquivo, o editor pergunta
  se deseja salvar (Salvar / Não salvar / Cancelar). O título da janela mostra `*` quando
  há alterações pendentes.

A pasta [`exemplos/`](exemplos) tem um desenho válido (`desenho-exemplo.json`) e um
arquivo propositalmente corrompido (`arquivo-corrompido.json`) para testar esses casos.

---

## Testes

`mvn test` executa 110 testes JUnit 5:

| Classe de teste | O que verifica |
|---|---|
| `PoligonoTest`, `CirculoTest`, `FormasRegularesTest` | Área, perímetro, centroide, ray casting, transformações e validações |
| `ClonarTest` | Cópia profunda do Prototype |
| `FerramentasTest`, `TransformacoesTest` | Padrão State com eventos de mouse simulados |
| `HistoricoControllerTest` | Memento: desfazer, refazer e cópia profunda do histórico |
| `GruposECamadasTest` | Composite e ordem das camadas |
| `PersistenciaJsonTest` | Salvar/carregar e arquivos corrompidos |
