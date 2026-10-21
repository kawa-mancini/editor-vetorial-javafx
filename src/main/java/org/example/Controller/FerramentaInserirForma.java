package org.example.Controller;

import java.util.function.BiFunction;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

/**
 * Ferramentas "Inserir Quadrado / Círculo / Hexágono": pressionar o mouse
 * marca o centro e arrastar define o raio.
 *
 * Uma única classe serve para as três formas: quem cria a ferramenta informa
 * uma "fábrica" que sabe construir a forma a partir de (centro, raio). Exemplo:
 *     new FerramentaInserirForma(Quadrado::new)
 * Assim não é preciso nenhum if/switch para saber qual forma criar.
 */
public class FerramentaInserirForma implements Ferramenta {

    // Arrastos menores que isso são tratados como clique acidental
    private static final double RAIO_MINIMO = 3;

    private final BiFunction<Ponto, Double, FormaGeometrica> fabrica;
    private Ponto centroInsercao;

    public FerramentaInserirForma(BiFunction<Ponto, Double, FormaGeometrica> fabrica) {
        if (fabrica == null) {
            throw new IllegalArgumentException("A fábrica de formas não pode ser nula.");
        }
        this.fabrica = fabrica;
        this.centroInsercao = null;
    }

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        centroInsercao = new Ponto(contexto.aplicarSnap(evento.getX()), contexto.aplicarSnap(evento.getY()));
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        if (centroInsercao == null) {
            return;
        }
        Ponto atual = new Ponto(contexto.aplicarSnap(evento.getX()), contexto.aplicarSnap(evento.getY()));
        double raio = centroInsercao.distancia(atual);

        if (raio < RAIO_MINIMO) {
            contexto.setFormaEmAndamento(null);
        } else {
            // A cada movimento cria uma prévia nova com o raio atual.
            // O centro é clonado para a prévia não compartilhar o Ponto da ferramenta.
            FormaGeometrica previa = fabrica.apply(centroInsercao.clonar(), raio);
            contexto.aplicarEstiloAtual(previa);
            contexto.setFormaEmAndamento(previa);
        }
        contexto.requestRedraw();
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        FormaGeometrica previa = contexto.getFormaEmAndamento();
        if (previa != null) {
            contexto.adicionarForma(previa);
            contexto.setFormaEmAndamento(null);
        }
        centroInsercao = null;
        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoClicar(MouseEvent evento, EditorContext contexto) {
        // não usado: a forma é criada pelo arrasto
    }
}
