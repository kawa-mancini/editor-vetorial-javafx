package org.example.Controller;

import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

/**
 * Os três modos da FerramentaTransformar.
 *
 * Cada constante do enum tem a sua própria versão do método transformar()
 * (enum com comportamento). Assim a ferramenta só chama modo.transformar(...)
 * e não precisa de if/switch para saber qual transformação fazer.
 *
 * Todas as transformações acontecem em torno de um centro comum (o centroide
 * da seleção). Com uma forma só, esse centro é o próprio centroide dela.
 * Com várias formas (ou um grupo), elas se transformam juntas, como uma só.
 */
public enum ModoFerramenta {

    /** Arrastar para longe do centro aumenta; para perto, diminui. */
    ESCALAR {
        @Override
        public void transformar(FormaGeometrica forma, Ponto centro, Ponto anterior, Ponto atual) {
            double distanciaAnterior = centro.distancia(anterior);
            double distanciaAtual = centro.distancia(atual);
            // Muito perto do centro o fator fica instável (divisão por quase zero)
            if (distanciaAnterior < DISTANCIA_MINIMA || distanciaAtual < DISTANCIA_MINIMA) {
                return;
            }
            double fator = distanciaAtual / distanciaAnterior;

            Ponto c = forma.getCentroide();
            forma.escalar(fator);
            // O centroide da forma também se afasta/aproxima do centro comum
            reposicionar(forma, c,
                    centro.getX() + (c.getX() - centro.getX()) * fator,
                    centro.getY() + (c.getY() - centro.getY()) * fator);
        }
    },

    /** Girar o mouse em volta do centro gira as formas pelo mesmo ângulo. */
    ROTACIONAR {
        @Override
        public void transformar(FormaGeometrica forma, Ponto centro, Ponto anterior, Ponto atual) {
            double anguloAnterior = Math.atan2(anterior.getY() - centro.getY(), anterior.getX() - centro.getX());
            double anguloAtual = Math.atan2(atual.getY() - centro.getY(), atual.getX() - centro.getX());
            double angulo = anguloAtual - anguloAnterior;
            double cos = Math.cos(angulo);
            double sin = Math.sin(angulo);

            Ponto c = forma.getCentroide();
            forma.rotacionar(angulo);
            double rx = c.getX() - centro.getX();
            double ry = c.getY() - centro.getY();
            reposicionar(forma, c,
                    centro.getX() + rx * cos - ry * sin,
                    centro.getY() + rx * sin + ry * cos);
        }
    },

    /** Arrastar na horizontal cisalha em x (shx); na vertical, em y (shy). */
    CISALHAR {
        @Override
        public void transformar(FormaGeometrica forma, Ponto centro, Ponto anterior, Ponto atual) {
            double shx = (atual.getX() - anterior.getX()) * SENSIBILIDADE_CISALHAMENTO;
            double shy = (atual.getY() - anterior.getY()) * SENSIBILIDADE_CISALHAMENTO;

            Ponto c = forma.getCentroide();
            forma.cisalhar(shx, shy);
            double rx = c.getX() - centro.getX();
            double ry = c.getY() - centro.getY();
            reposicionar(forma, c,
                    centro.getX() + rx + shx * ry,
                    centro.getY() + ry + shy * rx);
        }
    };

    private static final double DISTANCIA_MINIMA = 1;

    // Quanto de cisalhamento cada pixel de arrasto produz (100 px = 1,0)
    private static final double SENSIBILIDADE_CISALHAMENTO = 0.01;

    /**
     * Aplica um passo da transformação na forma.
     *
     * @param forma    forma a transformar
     * @param centro   centro comum da transformação (centroide da seleção)
     * @param anterior posição do mouse no evento anterior
     * @param atual    posição do mouse agora
     */
    public abstract void transformar(FormaGeometrica forma, Ponto centro, Ponto anterior, Ponto atual);

    /**
     * Cada forma se transforma em torno do PRÓPRIO centroide; depois, este método
     * a desloca para que o centroide vá para onde deveria ir se a transformação
     * tivesse sido feita em torno do centro comum.
     */
    private static void reposicionar(FormaGeometrica forma, Ponto centroideAntigo, double novoX, double novoY) {
        forma.transladar(novoX - centroideAntigo.getX(), novoY - centroideAntigo.getY());
    }
}
