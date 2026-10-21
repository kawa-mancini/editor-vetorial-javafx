package org.example.Models;

/**
 * Um ponto 2D (x, y) no canvas. É a peça básica de todas as formas:
 * cada forma é uma lista de pontos.
 */
public class Ponto {

    private double x;
    private double y;

    public Ponto(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    /** Desloca o ponto somando dx e dy às coordenadas. */
    public void mover(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    /** Distância euclidiana até outro ponto: raiz((x2-x1)^2 + (y2-y1)^2). */
    public double distancia(Ponto outro) {
        double dx = outro.x - this.x;
        double dy = outro.y - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** Prototype: devolve um ponto NOVO com as mesmas coordenadas. */
    public Ponto clonar() {
        return new Ponto(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
