package org.example.Models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Testes da matemática de polígono (implementada na FormaBase).
 * Os valores esperados foram calculados à mão.
 */
class PoligonoTest {

    // Tolerância para comparar doubles (contas com seno/cosseno nunca dão exato)
    private static final double DELTA = 1e-9;

    /** Retângulo 4 x 3 com canto em (0,0): área 12, perímetro 14, centroide (2; 1,5). */
    private Poligono retangulo() {
        return new Poligono(List.of(new Ponto(0, 0), new Ponto(4, 0), new Ponto(4, 3), new Ponto(0, 3)));
    }

    // ---------------- Cálculos ----------------

    @Test
    void areaDoRetanguloPelaFormulaDeGauss() {
        assertEquals(12, retangulo().calcularArea(), DELTA);
    }

    @Test
    void areaContinuaPositivaComVerticesEmSentidoHorario() {
        Poligono triangulo = new Poligono(List.of(new Ponto(0, 0), new Ponto(0, 3), new Ponto(4, 0)));
        assertEquals(6, triangulo.calcularArea(), DELTA);
    }

    @Test
    void perimetroIncluiArestaDoUltimoAoPrimeiroPonto() {
        assertEquals(14, retangulo().calcularPerimetro(), DELTA);
        // Triângulo 3-4-5
        Poligono triangulo = new Poligono(List.of(new Ponto(0, 0), new Ponto(4, 0), new Ponto(0, 3)));
        assertEquals(12, triangulo.calcularPerimetro(), DELTA);
    }

    @Test
    void centroideDoRetanguloEhOCentro() {
        Ponto c = retangulo().getCentroide();
        assertEquals(2, c.getX(), DELTA);
        assertEquals(1.5, c.getY(), DELTA);
    }

    @Test
    void centroideDoTrianguloEhAMediaDosVertices() {
        Poligono triangulo = new Poligono(List.of(new Ponto(0, 0), new Ponto(4, 0), new Ponto(0, 3)));
        Ponto c = triangulo.getCentroide();
        assertEquals(4.0 / 3, c.getX(), DELTA);
        assertEquals(1, c.getY(), DELTA);
    }

    @Test
    void contemPontoPorRayCasting() {
        Poligono r = retangulo();
        assertTrue(r.contemPonto(1, 1));
        assertFalse(r.contemPonto(5, 1));
        assertFalse(r.contemPonto(2, -1));
    }

    @Test
    void contemPontoEmPoligonoConcavo() {
        // Formato de "U": o ponto (2, 3) fica no vão, fora do polígono
        Poligono u = new Poligono(List.of(
                new Ponto(0, 0), new Ponto(4, 0), new Ponto(4, 4), new Ponto(3, 4),
                new Ponto(3, 1), new Ponto(1, 1), new Ponto(1, 4), new Ponto(0, 4)));
        assertFalse(u.contemPonto(2, 3));
        assertTrue(u.contemPonto(0.5, 3));
    }

    // ---------------- Transformações ----------------

    @Test
    void translacaoSomaDxDyEmTodosOsPontos() {
        Poligono r = retangulo();
        r.transladar(10, -2);
        assertEquals(10, r.getPontos().get(0).getX(), DELTA);
        assertEquals(-2, r.getPontos().get(0).getY(), DELTA);
        assertEquals(14, r.getPontos().get(2).getX(), DELTA);
        assertEquals(1, r.getPontos().get(2).getY(), DELTA);
    }

    @Test
    void escalaMultiplicaAreaPeloQuadradoDoFatorEMantemCentroide() {
        Poligono r = retangulo();
        r.escalar(2);
        assertEquals(48, r.calcularArea(), DELTA);
        assertEquals(2, r.getCentroide().getX(), DELTA);
        assertEquals(1.5, r.getCentroide().getY(), DELTA);
    }

    @Test
    void rotacaoDe90GrausEmTornoDoCentroide() {
        Poligono r = retangulo();
        r.rotacionar(Math.PI / 2);
        // (0,0) relativo ao centro é (-2; -1,5); girado 90° vira (1,5; -2); absoluto (3,5; -0,5)
        assertEquals(3.5, r.getPontos().get(0).getX(), DELTA);
        assertEquals(-0.5, r.getPontos().get(0).getY(), DELTA);
        // Girar não muda tamanho nem posição do centro
        assertEquals(12, r.calcularArea(), DELTA);
        assertEquals(14, r.calcularPerimetro(), DELTA);
        assertEquals(2, r.getCentroide().getX(), DELTA);
    }

    @Test
    void cisalhamentoRelativoAoCentroide() {
        Poligono r = retangulo();
        r.cisalhar(0.5, 0);
        // (0,3) relativo ao centro é (-2; 1,5); x' = -2 + 0,5*1,5 = -1,25; absoluto 0,75
        assertEquals(0.75, r.getPontos().get(3).getX(), DELTA);
        assertEquals(3, r.getPontos().get(3).getY(), DELTA);
        // Cisalhamento só em x não muda a área
        assertEquals(12, r.calcularArea(), DELTA);
    }

    // ---------------- Validações ----------------

    @Test
    void poligonoComMenosDe3PontosLancaExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> new Poligono(List.of(new Ponto(0, 0), new Ponto(1, 1))));
    }

    @Test
    void validarPoligonoMontadoPorCliquesComPoucosPontos() {
        Poligono p = new Poligono();
        p.adicionarPonto(new Ponto(0, 0));
        p.adicionarPonto(new Ponto(1, 0));
        assertThrows(IllegalArgumentException.class, p::validar);

        p.adicionarPonto(new Ponto(1, 1));
        assertDoesNotThrow(p::validar);
    }

    @Test
    void escalaComFatorZeroOuNegativoLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> retangulo().escalar(0));
        assertThrows(IllegalArgumentException.class, () -> retangulo().escalar(-2));
    }

    @Test
    void corForaDoFormatoHexLancaExcecao() {
        Poligono r = retangulo();
        assertThrows(IllegalArgumentException.class, () -> r.setCorBordaHex("vermelho"));
        assertDoesNotThrow(() -> r.setCorBordaHex("#FF0000"));
    }

    @Test
    void espessuraZeroLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> retangulo().setEspessuraBorda(0));
    }

    @Test
    void listaDePontosEhSomenteLeitura() {
        assertThrows(UnsupportedOperationException.class,
                () -> retangulo().getPontos().add(new Ponto(1, 1)));
    }
}
