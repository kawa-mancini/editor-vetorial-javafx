package org.example.Models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Testes de Quadrado e Hexagono (vértices calculados a partir de centro e raio). */
class FormasRegularesTest {

    private static final double DELTA = 1e-9;

    @Test
    void quadradoGera4VerticesAlinhadosComOsEixos() {
        // Raio raiz(2) => lado 2, vértices a 1 unidade do centro em x e em y
        Quadrado q = new Quadrado(new Ponto(10, 10), Math.sqrt(2));
        assertEquals(4, q.getQuantidadePontos());
        assertEquals(11, q.getPontos().get(0).getX(), DELTA);
        assertEquals(11, q.getPontos().get(0).getY(), DELTA);
        assertEquals(4, q.calcularArea(), DELTA);
        assertEquals(8, q.calcularPerimetro(), DELTA);
    }

    @Test
    void centroideDoQuadradoEhOCentroInformado() {
        Quadrado q = new Quadrado(new Ponto(7, -3), 5);
        assertEquals(7, q.getCentroide().getX(), DELTA);
        assertEquals(-3, q.getCentroide().getY(), DELTA);
    }

    @Test
    void hexagonoGera6VerticesComLadoIgualAoRaio() {
        Hexagono h = new Hexagono(new Ponto(0, 0), 2);
        assertEquals(6, h.getQuantidadePontos());
        // Área do hexágono regular: (3 * raiz(3) / 2) * r^2
        assertEquals(6 * Math.sqrt(3), h.calcularArea(), DELTA);
        assertEquals(12, h.calcularPerimetro(), DELTA);
        // Primeiro vértice em 0°: (raio, 0)
        assertEquals(2, h.getPontos().get(0).getX(), DELTA);
        assertEquals(0, h.getPontos().get(0).getY(), DELTA);
    }

    @Test
    void raioInvalidoLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> new Quadrado(new Ponto(0, 0), 0));
        assertThrows(IllegalArgumentException.class, () -> new Hexagono(new Ponto(0, 0), -1));
    }

    @Test
    void centroNuloLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> new Quadrado(null, 5));
    }
}
