package org.example.Models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Testes do padrão Prototype: clonar() precisa fazer cópia PROFUNDA,
 * ou seja, original e cópia nunca podem compartilhar objetos Ponto.
 */
class ClonarTest {

    private static final double DELTA = 1e-9;

    private Poligono retangulo() {
        return new Poligono(List.of(new Ponto(0, 0), new Ponto(4, 0), new Ponto(4, 3), new Ponto(0, 3)));
    }

    @Test
    void cloneDoPontoEhOutroObjeto() {
        Ponto p = new Ponto(1, 2);
        Ponto copia = p.clonar();
        assertNotSame(p, copia);
        copia.mover(5, 5);
        assertEquals(1, p.getX(), DELTA);
    }

    @Test
    void cloneDoPoligonoNaoCompartilhaPontos() {
        Poligono original = retangulo();
        FormaGeometrica copia = original.clonar();

        assertInstanceOf(Poligono.class, copia);
        for (int i = 0; i < original.getQuantidadePontos(); i++) {
            assertNotSame(original.getPontos().get(i), copia.getPontos().get(i));
        }

        copia.transladar(100, 100);
        assertEquals(0, original.getPontos().get(0).getX(), DELTA);
    }

    @Test
    void cloneCopiaEstiloEGrupo() {
        Poligono original = retangulo();
        original.setCorPreenchimentoHex("#FF0000");
        original.setCorBordaHex("#00FF00");
        original.setEspessuraBorda(4);
        original.setGroupId("grupo-1");

        FormaGeometrica copia = original.clonar();
        assertEquals("#FF0000", copia.getCorPreenchimentoHex());
        assertEquals("#00FF00", copia.getCorBordaHex());
        assertEquals(4, copia.getEspessuraBorda(), DELTA);
        assertEquals("grupo-1", copia.getGroupId());
    }

    @Test
    void cloneDoQuadradoMantemTipoEPontosAtuais() {
        Quadrado q = new Quadrado(new Ponto(10, 10), Math.sqrt(2));
        q.rotacionar(0.3);
        FormaGeometrica copia = q.clonar();

        assertInstanceOf(Quadrado.class, copia);
        // A cópia tem os pontos rotacionados, não os recalculados do zero
        assertEquals(q.getPontos().get(0).getX(), copia.getPontos().get(0).getX(), DELTA);

        copia.rotacionar(1);
        assertNotEquals(q.getPontos().get(0).getX(), copia.getPontos().get(0).getX(), DELTA);
    }

    @Test
    void cloneDoHexagonoMantemTipo() {
        assertInstanceOf(Hexagono.class, new Hexagono(new Ponto(0, 0), 3).clonar());
    }

    @Test
    void cloneDoCirculoCopiaRaioENaoCompartilhaCentro() {
        Circulo original = new Circulo(new Ponto(5, 5), 3);
        Circulo copia = (Circulo) original.clonar();

        assertNotSame(original.getCentro(), copia.getCentro());
        assertEquals(3, copia.getRaio(), DELTA);

        copia.transladar(1, 1);
        copia.escalar(2);
        assertEquals(5, original.getCentro().getX(), DELTA);
        assertEquals(3, original.getRaio(), DELTA);
    }
}
