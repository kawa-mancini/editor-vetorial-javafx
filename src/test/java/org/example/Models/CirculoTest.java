package org.example.Models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Testes do Circulo, que sobrescreve os cálculos da FormaBase. */
class CirculoTest {

    private static final double DELTA = 1e-9;

    @Test
    void areaEPerimetro() {
        Circulo c = new Circulo(new Ponto(5, 5), 2);
        assertEquals(Math.PI * 4, c.calcularArea(), DELTA);
        assertEquals(2 * Math.PI * 2, c.calcularPerimetro(), DELTA);
    }

    @Test
    void guardaSoOCentroNaListaDePontos() {
        Circulo c = new Circulo(new Ponto(5, 5), 2);
        assertEquals(1, c.getQuantidadePontos());
        assertThrows(UnsupportedOperationException.class, () -> c.adicionarPonto(new Ponto(0, 0)));
    }

    @Test
    void contemPontoPelaDistanciaAoCentro() {
        Circulo c = new Circulo(new Ponto(5, 5), 2);
        assertTrue(c.contemPonto(5, 5));
        assertTrue(c.contemPonto(7, 5)); // exatamente na borda conta como dentro
        assertFalse(c.contemPonto(7.1, 5));
    }

    @Test
    void escalarMultiplicaORaio() {
        Circulo c = new Circulo(new Ponto(0, 0), 2);
        c.escalar(1.5);
        assertEquals(3, c.getRaio(), DELTA);
    }

    @Test
    void transladarMoveOCentro() {
        Circulo c = new Circulo(new Ponto(0, 0), 2);
        c.transladar(3, 4);
        assertEquals(3, c.getCentro().getX(), DELTA);
        assertEquals(4, c.getCentro().getY(), DELTA);
    }

    @Test
    void rotacionarECisalharNaoAlteramOCirculo() {
        Circulo c = new Circulo(new Ponto(1, 2), 3);
        c.rotacionar(1.2);
        c.cisalhar(0.5, 0.5);
        assertEquals(1, c.getCentro().getX(), DELTA);
        assertEquals(2, c.getCentro().getY(), DELTA);
        assertEquals(3, c.getRaio(), DELTA);
    }

    @Test
    void raioZeroOuNegativoLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> new Circulo(new Ponto(0, 0), 0));
        assertThrows(IllegalArgumentException.class, () -> new Circulo(new Ponto(0, 0), -3));
        Circulo c = new Circulo(new Ponto(0, 0), 1);
        assertThrows(IllegalArgumentException.class, () -> c.setRaio(0));
    }
}
