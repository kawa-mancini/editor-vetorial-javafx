package org.example.Views;

import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.Controller.EditorContext;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Poligono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;

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

        // A View ensina o contexto a pedir redesenho e atualização do status
        contexto.setAcaoRedesenhar(this::redesenharCanvas);
        contexto.setAcaoAtualizarStats(painelStatus::atualizar);

        criarFormasDeExemplo();

        Scene cena = new Scene(raiz, LARGURA_INICIAL, ALTURA_INICIAL);
        palco.setTitle("Editor de Desenho Vetorial");
        palco.setScene(cena);
        palco.show();

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    /**
     * TEMPORÁRIO (etapa 3): formas fixas só para testar o desenho.
     * Será removido na etapa 4, quando as formas forem criadas com o mouse.
     */
    private void criarFormasDeExemplo() {
        contexto.adicionarForma(new Circulo(new Ponto(220, 200), 90));
        contexto.adicionarForma(new Hexagono(new Ponto(420, 390), 100));
        contexto.adicionarForma(new Quadrado(new Ponto(760, 210), 110));

        Poligono triangulo = new Poligono(List.of(new Ponto(650, 420), new Ponto(820, 600), new Ponto(560, 640)));
        triangulo.setCorPreenchimentoHex("#8066CC");
        triangulo.setCorBordaHex("#3A2A6A");
        triangulo.setEspessuraBorda(3);
        contexto.adicionarForma(triangulo);

        // Um quadrado selecionado, para conferir o destaque verde/vermelho
        Quadrado selecionado = new Quadrado(new Ponto(230, 520), 80);
        selecionado.setCorPreenchimentoHex("#CCCC33");
        contexto.adicionarForma(selecionado);
        contexto.selecionar(selecionado);
    }

    // ------------------------------------------------------------------
    // Desenho
    // ------------------------------------------------------------------

    /** Apaga o canvas e desenha todas as formas, da camada de trás para a da frente. */
    private void redesenharCanvas() {
        GraphicsContext gc = areaDeDesenho.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, areaDeDesenho.getWidth(), areaDeDesenho.getHeight());

        for (FormaGeometrica forma : contexto.getFormas()) {
            desenharForma(forma, contexto.isSelecionado(forma));
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
            gc.fillPolygon(xs, ys, pontos.size());
            gc.strokePolygon(xs, ys, pontos.size());
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
