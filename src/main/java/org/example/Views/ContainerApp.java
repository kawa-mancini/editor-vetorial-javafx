package org.example.Views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.Controller.EditorContext;
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

    private Canvas areaDeDesenho;
    private EditorContext contexto;
    private BarraLateralView barraLateral;
    private PainelStatusView painelStatus;

    @Override
    public void start(Stage palco) {
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
        contexto.setAcaoRedesenhar(this::redesenharCanvas);
        contexto.setAcaoAtualizarStats(() -> {
            painelStatus.atualizar();
            barraLateral.atualizarCoresInterface();
        });
        contexto.setAcaoMostrarAviso(this::mostrarAviso);

        inicializarEventos();

        Scene cena = new Scene(raiz, LARGURA_INICIAL, ALTURA_INICIAL);
        configurarAtalhos(cena);
        palco.setTitle("Editor de Desenho Vetorial");
        palco.setScene(cena);
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
    }

    /** Atalhos de teclado: Ctrl+Z desfaz, Ctrl+Y (ou Ctrl+Shift+Z) refaz, Delete exclui a seleção. */
    private void configurarAtalhos(Scene cena) {
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
        alerta.setTitle("Aviso");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
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
