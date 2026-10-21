package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

/**
 * Ferramenta "Editar Vértice": pressionar perto de um vértice e arrastar
 * move só aquele vértice, deformando a forma.
 *
 * Funciona porque getPontos() devolve os próprios objetos Ponto da forma:
 * a lista é somente leitura, mas cada ponto pode ter x/y alterados.
 * No círculo, o único "vértice" é o centro, então arrastá-lo move o círculo.
 */
public class FerramentaEditarVertice implements Ferramenta {

    // Distância máxima (em pixels) entre o clique e o vértice para "pegá-lo"
    private static final double TOLERANCIA = 8;

    private Ponto verticeSelecionado;

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        double x = evento.getX();
        double y = evento.getY();

        // Procura da forma da frente para a de trás
        for (int i = contexto.getFormas().size() - 1; i >= 0; i--) {
            FormaGeometrica forma = contexto.getFormas().get(i);
            for (Ponto vertice : forma.getPontos()) {
                if (vertice.distancia(new Ponto(x, y)) <= TOLERANCIA) {
                    verticeSelecionado = vertice;
                    selecionarSomente(forma, contexto);
                    return;
                }
            }
        }

        // Não pegou nenhum vértice: clicar numa forma só a seleciona, para mostrar os vértices
        FormaGeometrica alvo = contexto.encontrarFormaEm(x, y);
        if (alvo != null) {
            selecionarSomente(alvo, contexto);
        }
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        if (verticeSelecionado == null) {
            return;
        }
        verticeSelecionado.setX(contexto.aplicarSnap(evento.getX()));
        verticeSelecionado.setY(contexto.aplicarSnap(evento.getY()));
        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        verticeSelecionado = null;
    }

    @Override
    public void aoClicar(MouseEvent evento, EditorContext contexto) {
        // não usado
    }

    private void selecionarSomente(FormaGeometrica forma, EditorContext contexto) {
        contexto.limparSelecao();
        contexto.selecionar(forma);
        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }
}
