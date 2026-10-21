package org.example.Controller;

import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

/**
 * Ferramenta "Selecionar":
 * - clicar numa forma seleciona (com Shift, adiciona/remove da seleção);
 * - arrastar uma forma selecionada move toda a seleção;
 * - arrastar no vazio desenha uma caixa e seleciona as formas que ficarem dentro dela.
 */
public class FerramentaSelecionar implements Ferramenta {

    private boolean arrastandoCaixa;
    private double selecaoStartX;
    private double selecaoStartY;

    // Para mover a seleção: posição do mouse no último evento de arrasto
    private boolean movendoSelecao;
    private double ultimoX;
    private double ultimoY;

    // O histórico só é registrado quando a forma realmente se move:
    // um simples clique para selecionar não deve gerar um "desfazer"
    private boolean estadoRegistrado;

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        double x = evento.getX();
        double y = evento.getY();
        FormaGeometrica alvo = contexto.encontrarFormaEm(x, y);

        if (alvo == null) {
            // Clicou no vazio: começa a caixa de seleção
            if (!evento.isShiftDown()) {
                contexto.limparSelecao();
            }
            arrastandoCaixa = true;
            selecaoStartX = x;
            selecaoStartY = y;
        } else if (evento.isShiftDown() && contexto.isSelecionado(alvo)) {
            // Shift + clique numa forma já selecionada: tira da seleção
            contexto.desselecionar(alvo);
        } else {
            if (!contexto.isSelecionado(alvo)) {
                if (!evento.isShiftDown()) {
                    contexto.limparSelecao();
                }
                contexto.selecionar(alvo);
            }
            movendoSelecao = true;
            estadoRegistrado = false;
            ultimoX = contexto.aplicarSnap(x);
            ultimoY = contexto.aplicarSnap(y);
        }

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        if (movendoSelecao) {
            double x = contexto.aplicarSnap(evento.getX());
            double y = contexto.aplicarSnap(evento.getY());
            double dx = x - ultimoX;
            double dy = y - ultimoY;
            if (dx != 0 || dy != 0) {
                if (!estadoRegistrado) {
                    contexto.registrarEstado();
                    estadoRegistrado = true;
                }
                for (FormaGeometrica forma : contexto.getSelecionados()) {
                    forma.transladar(dx, dy);
                }
                ultimoX = x;
                ultimoY = y;
                contexto.requestRedraw();
            }
        } else if (arrastandoCaixa) {
            contexto.setCaixaSelecao(criarCaixa(selecaoStartX, selecaoStartY, evento.getX(), evento.getY()));
            contexto.requestRedraw();
        }
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        if (arrastandoCaixa) {
            Rectangle2D caixa = criarCaixa(selecaoStartX, selecaoStartY, evento.getX(), evento.getY());
            for (FormaGeometrica forma : contexto.getFormas()) {
                if (estaDentroDaCaixa(forma, caixa)) {
                    contexto.selecionar(forma);
                }
            }
            contexto.setCaixaSelecao(null);
        }
        arrastandoCaixa = false;
        movendoSelecao = false;
        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoClicar(MouseEvent evento, EditorContext contexto) {
        // não usado: a seleção já acontece ao pressionar o botão
    }

    /** Monta o retângulo entre dois cantos, em qualquer direção de arrasto. */
    private Rectangle2D criarCaixa(double x1, double y1, double x2, double y2) {
        return new Rectangle2D(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    /** A forma está dentro se TODOS os seus pontos estão dentro da caixa. */
    private boolean estaDentroDaCaixa(FormaGeometrica forma, Rectangle2D caixa) {
        List<Ponto> pontos = forma.getPontos();
        if (pontos.isEmpty()) {
            return false;
        }
        for (Ponto p : pontos) {
            if (!caixa.contains(p.getX(), p.getY())) {
                return false;
            }
        }
        return true;
    }
}
