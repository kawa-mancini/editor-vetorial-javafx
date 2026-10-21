package org.example.Controller;

import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Cria eventos de mouse "de mentira" para testar as ferramentas
 * sem precisar abrir a janela.
 */
final class EventosFalsos {

    private EventosFalsos() {
    }

    static MouseEvent evento(EventType<MouseEvent> tipo, double x, double y,
                             MouseButton botao, int cliques, boolean shift) {
        return new MouseEvent(tipo, x, y, x, y, botao, cliques,
                shift, false, false, false,
                botao == MouseButton.PRIMARY, false, botao == MouseButton.SECONDARY,
                false, false, true, null);
    }

    static MouseEvent pressionar(double x, double y) {
        return evento(MouseEvent.MOUSE_PRESSED, x, y, MouseButton.PRIMARY, 1, false);
    }

    static MouseEvent pressionarComShift(double x, double y) {
        return evento(MouseEvent.MOUSE_PRESSED, x, y, MouseButton.PRIMARY, 1, true);
    }

    static MouseEvent arrastar(double x, double y) {
        return evento(MouseEvent.MOUSE_DRAGGED, x, y, MouseButton.PRIMARY, 0, false);
    }

    static MouseEvent soltar(double x, double y) {
        return evento(MouseEvent.MOUSE_RELEASED, x, y, MouseButton.PRIMARY, 1, false);
    }

    static MouseEvent clicar(double x, double y) {
        return evento(MouseEvent.MOUSE_CLICKED, x, y, MouseButton.PRIMARY, 1, false);
    }

    static MouseEvent duploClique(double x, double y) {
        return evento(MouseEvent.MOUSE_CLICKED, x, y, MouseButton.PRIMARY, 2, false);
    }

    static MouseEvent cliqueDireito(double x, double y) {
        return evento(MouseEvent.MOUSE_CLICKED, x, y, MouseButton.SECONDARY, 1, false);
    }
}
