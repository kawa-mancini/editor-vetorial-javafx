package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;

/**
 * Ferramenta "Excluir": clicar numa forma apaga essa forma.
 * (Classe extra, não está no diagrama: o enunciado tem o botão Excluir
 * mas o diagrama não tem uma ferramenta para ele.)
 */
public class FerramentaExcluir implements Ferramenta {

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        // não usado
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        // não usado
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        // não usado
    }

    @Override
    public void aoClicar(MouseEvent evento, EditorContext contexto) {
        FormaGeometrica alvo = contexto.encontrarFormaEm(evento.getX(), evento.getY());
        if (alvo != null) {
            contexto.removerForma(alvo);
            contexto.requestRedraw();
            contexto.requestStatsUpdate();
        }
    }
}
