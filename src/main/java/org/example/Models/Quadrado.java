package org.example.Models;

import java.util.List;

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

    // Usado pelo clonar() e por aPartirDosVertices(): os pontos são copiados em vez
    // de recalculados, porque o quadrado pode ter sido rotacionado ou cisalhado
    private Quadrado() {
        super();
    }

    /**
     * Recria um quadrado a partir dos vértices já calculados (usado ao carregar
     * um arquivo, quando o quadrado pode ter sido transformado ou editado).
     */
    public static Quadrado aPartirDosVertices(List<Ponto> vertices) {
        if (vertices == null || vertices.size() != LADOS) {
            throw new IllegalArgumentException("Um quadrado precisa de exatamente " + LADOS + " vértices.");
        }
        Quadrado quadrado = new Quadrado();
        for (Ponto p : vertices) {
            quadrado.adicionarPonto(p.clonar());
        }
        return quadrado;
    }

    @Override
    public FormaGeometrica clonar() {
        Quadrado copia = new Quadrado();
        copiarAtributosPara(copia);
        return copia;
    }
}
