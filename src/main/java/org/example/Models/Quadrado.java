package org.example.Models;

/**
 * Quadrado definido por centro e raio (distância do centro até cada vértice).
 * Os 4 vértices são calculados com seno/cosseno a 45°, 135°, 225° e 315°,
 * o que deixa os lados alinhados com a tela. O lado mede raio * raiz(2).
 */
public class Quadrado extends FormaBase {

    private static final int LADOS = 4;
    private static final double ANGULO_INICIAL = Math.PI / 4; // 45°

    public Quadrado(Ponto centro, double raio) {
        super();
        gerarVerticesRegulares(centro, raio, LADOS, ANGULO_INICIAL);
    }

    // Usado só pelo clonar(): os pontos são copiados do original em vez de
    // recalculados, porque o quadrado pode ter sido rotacionado ou cisalhado
    private Quadrado() {
        super();
    }

    @Override
    public FormaGeometrica clonar() {
        Quadrado copia = new Quadrado();
        copiarAtributosPara(copia);
        return copia;
    }
}
