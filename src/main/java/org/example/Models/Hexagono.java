package org.example.Models;

/**
 * Hexágono regular definido por centro e raio (distância do centro até cada vértice).
 * Os 6 vértices ficam a cada 60°, começando em 0°, o que deixa os lados de cima
 * e de baixo retos, como na figura 8 do enunciado.
 */
public class Hexagono extends FormaBase {

    private static final int LADOS = 6;
    private static final double ANGULO_INICIAL = 0;

    public Hexagono(Ponto centro, double raio) {
        super();
        gerarVerticesRegulares(centro, raio, LADOS, ANGULO_INICIAL);
    }

    // Usado só pelo clonar() (mesmo motivo do Quadrado)
    private Hexagono() {
        super();
    }

    @Override
    public FormaGeometrica clonar() {
        Hexagono copia = new Hexagono();
        copiarAtributosPara(copia);
        return copia;
    }
}
