package org.example.Views;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Controller.EditorContext;
import org.example.Controller.PersistenciaJson;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

/**
 * Janela principal do editor: barra lateral à esquerda, canvas no centro
 * e barra de status embaixo.
 */
public class ContainerApp extends Application {

    private static final double LARGURA_INICIAL = 1200;
    private static final double ALTURA_INICIAL = 800;

    // Aparência da seleção: borda verde e vértices em vermelho
    private static final Color COR_SELECAO = Color.GREEN;
    private static final Color COR_VERTICE = Color.RED;
    private static final double ESPESSURA_MINIMA_SELECAO = 2;
    private static final double RAIO_VERTICE = 3;
    private static final double MARGEM_CONTORNO_GRUPO = 8;

    private static final Color COR_GRADE = Color.web("#E6E6E6");
    private static final Color COR_CAIXA_SELECAO = Color.web("#1E78D7");

    private static final String TITULO = "Editor de Desenho Vetorial";

    private Canvas areaDeDesenho;
    private EditorContext contexto;
    private BarraLateralView barraLateral;
    private PainelStatusView painelStatus;

    // Para salvar/carregar: a janela (dona das caixas de diálogo) e o arquivo aberto
    private Stage palco;
    private File arquivoAtual;
    private final PersistenciaJson persistencia = new PersistenciaJson();

    @Override
    public void start(Stage palco) {
        this.palco = palco;
        contexto = new EditorContext();
        barraLateral = new BarraLateralView(contexto);
        painelStatus = new PainelStatusView(contexto);
        areaDeDesenho = new Canvas();

        // O Canvas tem tamanho fixo. Para ele acompanhar a janela, fica dentro de um
        // Pane e tem a largura/altura "amarradas" (bind) às do Pane.
        Pane containerCanvas = new Pane(areaDeDesenho);
        containerCanvas.setMinSize(0, 0);
        areaDeDesenho.widthProperty().bind(containerCanvas.widthProperty());
        areaDeDesenho.heightProperty().bind(containerCanvas.heightProperty());
        // Mudou de tamanho, o Canvas é apagado: precisa redesenhar
        areaDeDesenho.widthProperty().addListener(obs -> redesenharCanvas());
        areaDeDesenho.heightProperty().addListener(obs -> redesenharCanvas());

        // Rolagem para a barra lateral caber em janelas mais baixas
        ScrollPane rolagemLateral = new ScrollPane(barraLateral);
        rolagemLateral.setFitToWidth(true);
        rolagemLateral.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        BorderPane raiz = new BorderPane();
        raiz.setLeft(rolagemLateral);
        raiz.setCenter(containerCanvas);
        raiz.setBottom(painelStatus);

        // A View ensina o contexto a redesenhar, atualizar o status e avisar o usuário
        contexto.setAcaoRedesenhar(() -> {
            redesenharCanvas();
            atualizarTitulo();
        });
        contexto.setAcaoAtualizarStats(() -> {
            painelStatus.atualizar();
            barraLateral.atualizarCoresInterface();
        });
        contexto.setAcaoMostrarAviso(this::mostrarAviso);
        barraLateral.setAcaoSalvar(this::salvarArquivo);
        barraLateral.setAcaoCarregar(this::carregarArquivo);

        inicializarEventos();

        Scene cena = new Scene(raiz, LARGURA_INICIAL, ALTURA_INICIAL);
        configurarAtalhos(cena);
        palco.setScene(cena);
        // Ao fechar a janela: se houver alterações não salvas, pergunta antes
        palco.setOnCloseRequest(evento -> {
            if (!confirmarDescarteDasAlteracoes()) {
                evento.consume(); // consumir o evento cancela o fechamento
            }
        });
        atualizarTitulo();
        palco.show();

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    /**
     * Liga os eventos do mouse no canvas ao contexto.
     * Repare que não há nenhum if/switch por ferramenta: o contexto repassa
     * o evento para a ferramenta ativa (padrão State).
     */
    public void inicializarEventos() {
        areaDeDesenho.setOnMousePressed(contexto::aoPressionarMouse);
        areaDeDesenho.setOnMouseDragged(contexto::aoArrastarMouse);
        areaDeDesenho.setOnMouseReleased(contexto::aoSoltarMouse);
        areaDeDesenho.setOnMouseClicked(contexto::aoClicar);
        // Só para as coordenadas da barra de status
        areaDeDesenho.setOnMouseMoved(contexto::aoMoverMouse);
        areaDeDesenho.setOnMouseExited(evento -> contexto.aoSairDoCanvas());
    }

    /**
     * Atalhos de teclado: Ctrl+Z desfaz, Ctrl+Y (ou Ctrl+Shift+Z) refaz,
     * Delete exclui a seleção, Ctrl+S salva e Ctrl+O carrega.
     */
    private void configurarAtalhos(Scene cena) {
        cena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), this::salvarArquivo);
        cena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN), this::carregarArquivo);
        cena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN), contexto::desfazer);
        cena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN), contexto::refazer);
        cena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN),
                contexto::refazer);
        cena.getAccelerators().put(new KeyCodeCombination(KeyCode.DELETE), contexto::excluirSelecionados);
    }

    private void mostrarAviso(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.initOwner(palco);
        alerta.setTitle("Aviso");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.initOwner(palco);
        alerta.setTitle("Erro");
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    /** Título da janela com o nome do arquivo e um "*" quando há alterações não salvas. */
    private void atualizarTitulo() {
        String nome = (arquivoAtual != null) ? arquivoAtual.getName() : "Sem título";
        String modificado = contexto.temAlteracoesNaoSalvas() ? "*" : "";
        palco.setTitle(nome + modificado + " - " + TITULO);
    }

    // ------------------------------------------------------------------
    // Salvar / carregar
    // ------------------------------------------------------------------

    /**
     * Pede o nome do arquivo e salva o desenho em JSON.
     * Devolve true se salvou; false se o usuário cancelou ou deu erro.
     */
    private boolean salvarArquivo() {
        FileChooser seletor = criarSeletorDeArquivo("Salvar desenho");
        if (arquivoAtual != null) {
            seletor.setInitialDirectory(arquivoAtual.getParentFile());
            seletor.setInitialFileName(arquivoAtual.getName());
        } else {
            seletor.setInitialFileName("desenho.json");
        }
        File arquivo = seletor.showSaveDialog(palco);
        if (arquivo == null) {
            return false; // usuário cancelou
        }
        if (!arquivo.getName().toLowerCase().endsWith(".json")) {
            arquivo = new File(arquivo.getParentFile(), arquivo.getName() + ".json");
        }

        try {
            persistencia.salvar(arquivo.toPath(), contexto, areaDeDesenho.getWidth(), areaDeDesenho.getHeight());
            arquivoAtual = arquivo;
            atualizarTitulo();
            return true;
        } catch (IOException e) {
            mostrarErro("Não foi possível salvar o arquivo.",
                    "Verifique se a pasta existe e se você tem permissão para gravar nela.\n\nDetalhe: " + e.getMessage());
            return false;
        }
    }

    /** Escolhe um arquivo JSON e substitui o desenho atual pelo conteúdo dele. */
    private void carregarArquivo() {
        if (!confirmarDescarteDasAlteracoes()) {
            return;
        }
        FileChooser seletor = criarSeletorDeArquivo("Carregar desenho");
        if (arquivoAtual != null) {
            seletor.setInitialDirectory(arquivoAtual.getParentFile());
        }
        File arquivo = seletor.showOpenDialog(palco);
        if (arquivo == null) {
            return;
        }

        try {
            persistencia.carregar(arquivo.toPath(), contexto);
            arquivoAtual = arquivo;
            barraLateral.atualizarSnapping();
            contexto.requestRedraw();
            contexto.requestStatsUpdate();
        } catch (PersistenciaJson.ArquivoInvalidoException e) {
            // Arquivo corrompido: explica o problema e o editor continua funcionando
            mostrarErro("Não foi possível carregar \"" + arquivo.getName() + "\".",
                    e.getMessage() + "\n\nO desenho atual não foi alterado.");
        }
    }

    private FileChooser criarSeletorDeArquivo(String titulo) {
        FileChooser seletor = new FileChooser();
        seletor.setTitle(titulo);
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Desenho vetorial (*.json)", "*.json"));
        return seletor;
    }

    /**
     * Se houver alterações não salvas, pergunta se quer salvar.
     * Devolve true se pode continuar (fechar, carregar outro arquivo...).
     */
    private boolean confirmarDescarteDasAlteracoes() {
        if (!contexto.temAlteracoesNaoSalvas()) {
            return true;
        }
        ButtonType salvar = new ButtonType("Salvar");
        ButtonType naoSalvar = new ButtonType("Não salvar");
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert pergunta = new Alert(Alert.AlertType.CONFIRMATION, "", salvar, naoSalvar, cancelar);
        pergunta.initOwner(palco);
        pergunta.setTitle("Alterações não salvas");
        pergunta.setHeaderText("O desenho tem alterações que não foram salvas.");
        pergunta.setContentText("Deseja salvar antes de continuar?");

        ButtonType resposta = pergunta.showAndWait().orElse(cancelar);
        if (resposta == salvar) {
            return salvarArquivo(); // se o salvamento for cancelado, não continua
        }
        return resposta == naoSalvar;
    }

    // ------------------------------------------------------------------
    // Desenho
    // ------------------------------------------------------------------

    /** Apaga o canvas e desenha tudo de novo, da camada de trás para a da frente. */
    private void redesenharCanvas() {
        GraphicsContext gc = areaDeDesenho.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, areaDeDesenho.getWidth(), areaDeDesenho.getHeight());

        if (contexto.isGridSnappingHabilitado()) {
            desenharGrade(gc);
        }

        for (FormaGeometrica forma : contexto.getFormas()) {
            desenharForma(forma, contexto.isSelecionado(forma));
        }
        desenharContornoDosGrupos(gc);

        // Forma ainda sendo criada (polígono pela metade, quadrado sendo arrastado...)
        FormaGeometrica emAndamento = contexto.getFormaEmAndamento();
        if (emAndamento != null) {
            desenharForma(emAndamento, false);
            desenharVertices(gc, emAndamento);
        }

        Rectangle2D caixa = contexto.getCaixaSelecao();
        if (caixa != null) {
            gc.setStroke(COR_CAIXA_SELECAO);
            gc.setLineWidth(1);
            gc.setLineDashes(6, 4);
            gc.strokeRect(caixa.getMinX(), caixa.getMinY(), caixa.getWidth(), caixa.getHeight());
            gc.setLineDashes(null);
        }
    }

    /**
     * Desenha um retângulo tracejado em volta de cada grupo selecionado,
     * para o usuário ver que aquelas formas estão juntas.
     */
    private void desenharContornoDosGrupos(GraphicsContext gc) {
        // groupId -> {minX, minY, maxX, maxY}
        Map<String, double[]> limitesPorGrupo = new HashMap<>();
        for (FormaGeometrica forma : contexto.getSelecionados()) {
            if (forma.getGroupId() == null) {
                continue;
            }
            double[] limites = limitesPorGrupo.computeIfAbsent(forma.getGroupId(),
                    id -> new double[] {Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE});
            // O círculo ocupa raio para cada lado do centro; os demais, só os vértices
            double folga = (forma instanceof Circulo) ? ((Circulo) forma).getRaio() : 0;
            for (Ponto p : forma.getPontos()) {
                limites[0] = Math.min(limites[0], p.getX() - folga);
                limites[1] = Math.min(limites[1], p.getY() - folga);
                limites[2] = Math.max(limites[2], p.getX() + folga);
                limites[3] = Math.max(limites[3], p.getY() + folga);
            }
        }

        gc.setStroke(COR_SELECAO);
        gc.setLineWidth(1);
        gc.setLineDashes(4, 4);
        for (double[] l : limitesPorGrupo.values()) {
            gc.strokeRect(l[0] - MARGEM_CONTORNO_GRUPO, l[1] - MARGEM_CONTORNO_GRUPO,
                    l[2] - l[0] + 2 * MARGEM_CONTORNO_GRUPO, l[3] - l[1] + 2 * MARGEM_CONTORNO_GRUPO);
        }
        gc.setLineDashes(null);
    }

    private void desenharGrade(GraphicsContext gc) {
        double passo = EditorContext.TAMANHO_GRADE;
        gc.setStroke(COR_GRADE);
        gc.setLineWidth(1);
        for (double x = 0; x <= areaDeDesenho.getWidth(); x += passo) {
            gc.strokeLine(x, 0, x, areaDeDesenho.getHeight());
        }
        for (double y = 0; y <= areaDeDesenho.getHeight(); y += passo) {
            gc.strokeLine(0, y, areaDeDesenho.getWidth(), y);
        }
    }

    private void desenharForma(FormaGeometrica forma, boolean selecionada) {
        GraphicsContext gc = areaDeDesenho.getGraphicsContext2D();
        gc.setFill(Color.web(forma.getCorPreenchimentoHex()));
        if (selecionada) {
            gc.setStroke(COR_SELECAO);
            gc.setLineWidth(Math.max(forma.getEspessuraBorda(), ESPESSURA_MINIMA_SELECAO));
        } else {
            gc.setStroke(Color.web(forma.getCorBordaHex()));
            gc.setLineWidth(forma.getEspessuraBorda());
        }

        // O círculo é o único que não é desenhado como polígono
        if (forma instanceof Circulo) {
            Circulo circulo = (Circulo) forma;
            Ponto centro = circulo.getCentro();
            double raio = circulo.getRaio();
            gc.fillOval(centro.getX() - raio, centro.getY() - raio, 2 * raio, 2 * raio);
            gc.strokeOval(centro.getX() - raio, centro.getY() - raio, 2 * raio, 2 * raio);
        } else {
            List<Ponto> pontos = forma.getPontos();
            double[] xs = new double[pontos.size()];
            double[] ys = new double[pontos.size()];
            for (int i = 0; i < pontos.size(); i++) {
                xs[i] = pontos.get(i).getX();
                ys[i] = pontos.get(i).getY();
            }
            if (pontos.size() < 3) {
                // Polígono ainda sendo desenhado: só a linha entre os pontos
                gc.strokePolyline(xs, ys, pontos.size());
            } else {
                gc.fillPolygon(xs, ys, pontos.size());
                gc.strokePolygon(xs, ys, pontos.size());
            }
        }

        if (selecionada) {
            desenharVertices(gc, forma);
        }
    }

    private void desenharVertices(GraphicsContext gc, FormaGeometrica forma) {
        gc.setFill(COR_VERTICE);
        for (Ponto p : forma.getPontos()) {
            gc.fillOval(p.getX() - RAIO_VERTICE, p.getY() - RAIO_VERTICE, 2 * RAIO_VERTICE, 2 * RAIO_VERTICE);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
