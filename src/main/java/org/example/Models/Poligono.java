package org.example.Models;

import java.util.List;

/**
 * Polígono livre, desenhado ponto a ponto pelo usuário (ferramenta Linha Livre).
 * Toda a matemática vem pronta da FormaBase.
 */
public class Poligono extends FormaBase {

    public static final int MINIMO_PONTOS = 3;

    /**
     * Cria um polígono vazio. Os pontos entram depois, um por clique,
     * por isso a regra do mínimo de 3 pontos é conferida em validar().
     */
    public Poligono() {
        super();
    }

    /** Cria um polígono já com todos os pontos (cada ponto é copiado). */
    public Poligono(List<Ponto> pontos) {
        super();
        if (pontos == null || pontos.size() < MINIMO_PONTOS) {
            throw new IllegalArgumentException("Um polígono precisa de no mínimo " + MINIMO_PONTOS + " pontos.");
        }
        for (Ponto p : pontos) {
            adicionarPonto(p.clonar());
        }
    }

    /** Lança exceção se o polígono tiver menos de 3 pontos. */
    public void validar() {
        if (pontos.size() < MINIMO_PONTOS) {
            throw new IllegalArgumentException("Um polígono precisa de no mínimo " + MINIMO_PONTOS
                    + " pontos (atual: " + pontos.size() + ").");
        }
    }

    @Override
    public FormaGeometrica clonar() {
        Poligono copia = new Poligono();
        copiarAtributosPara(copia);
        return copia;
    }
}
