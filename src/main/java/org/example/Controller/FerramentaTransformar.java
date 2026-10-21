package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

/**
 * Ferramentas "Escalar", "Rotacionar" e "Cisalhar": arrastar o mouse
 * transforma as formas selecionadas. O que acontece depende do modo
 * recebido no construtor (ver ModoFerramenta).
 *
 * Se o usuário pressionar sobre uma forma que não está selecionada,
 * ela passa a ser a seleção, para não ser preciso trocar de ferramenta.
 */
public class FerramentaTransformar implements Ferramenta {

    private final ModoFerramenta modo;

    // Durante o arrasto: centro comum da transformação e última posição do mouse
    private Ponto centroTransformacao;
    private Ponto ultimoMouse;

    public FerramentaTransformar(ModoFerramenta modo) {
        if (modo == null) {
            throw new IllegalArgumentException("O modo da ferramenta não pode ser nulo.");
        }
        this.modo = modo;
    }

    public ModoFerramenta getModo() {
        return modo;
    }

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        FormaGeometrica alvo = contexto.encontrarFormaEm(evento.getX(), evento.getY());
        if (alvo != null && !contexto.isSelecionado(alvo)) {
            contexto.limparSelecao();
            contexto.selecionar(alvo);
        }
        if (contexto.getSelecionados().isEmpty()) {
            return;
        }
        centroTransformacao = contexto.getCentroideSelecao();
        ultimoMouse = new Ponto(evento.getX(), evento.getY());
        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        if (ultimoMouse == null) {
            return;
        }
        Ponto atual = new Ponto(evento.getX(), evento.getY());
        for (FormaGeometrica forma : contexto.getSelecionados()) {
            modo.transformar(forma, centroTransformacao, ultimoMouse, atual);
        }
        ultimoMouse = atual;
        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        centroTransformacao = null;
        ultimoMouse = null;
    }

    @Override
    public void aoClicar(MouseEvent evento, EditorContext contexto) {
        // não usado: a transformação acontece no arrasto
    }
}
