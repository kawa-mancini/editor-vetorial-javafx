package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Poligono;
import org.example.Models.Ponto;

/**
 * Ferramenta "Linha Livre": cada clique adiciona um vértice ao polígono.
 * O polígono é fechado com duplo clique, clique direito ou clicando
 * perto do primeiro ponto.
 */
public class FerramentaDesenhar implements Ferramenta {

    // Clicar a menos disso do primeiro ponto fecha o polígono
    private static final double DISTANCIA_PARA_FECHAR = 10;

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        // não usado: esta ferramenta trabalha só com cliques
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
        if (evento.getButton() == MouseButton.SECONDARY || evento.getClickCount() == 2) {
            finalizarPoligono(contexto);
            return;
        }
        if (evento.getButton() != MouseButton.PRIMARY) {
            return;
        }

        Ponto novo = new Ponto(contexto.aplicarSnap(evento.getX()), contexto.aplicarSnap(evento.getY()));
        FormaGeometrica emAndamento = contexto.getFormaEmAndamento();

        if (emAndamento == null) {
            // Primeiro clique: começa um polígono novo com o estilo da barra lateral
            emAndamento = new Poligono();
            contexto.aplicarEstiloAtual(emAndamento);
            contexto.setFormaEmAndamento(emAndamento);
        } else if (emAndamento.getQuantidadePontos() >= Poligono.MINIMO_PONTOS
                && novo.distancia(emAndamento.getPontos().get(0)) <= DISTANCIA_PARA_FECHAR) {
            finalizarPoligono(contexto);
            return;
        }

        emAndamento.adicionarPonto(novo);
        contexto.requestRedraw();
    }

    /**
     * Transforma os pontos clicados num Poligono de verdade. O construtor do
     * Poligono confere a regra dos 3 pontos; se faltar ponto, ele lança
     * IllegalArgumentException e o usuário pode continuar clicando.
     */
    private void finalizarPoligono(EditorContext contexto) {
        FormaGeometrica emAndamento = contexto.getFormaEmAndamento();
        if (emAndamento == null) {
            return;
        }
        try {
            Poligono poligono = new Poligono(emAndamento.getPontos());
            contexto.aplicarEstiloAtual(poligono);
            contexto.registrarEstado();
            contexto.adicionarForma(poligono);
            contexto.setFormaEmAndamento(null);
        } catch (IllegalArgumentException e) {
            contexto.mostrarAviso(e.getMessage() + " Continue clicando para adicionar pontos.");
        }
        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }
}
