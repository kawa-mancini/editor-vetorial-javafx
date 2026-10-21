package org.example.Models;

/**
 * Círculo: guarda o centro como único ponto da lista e o raio num atributo próprio.
 * Como a matemática de polígono da FormaBase não serve para ele, os cálculos e
 * algumas transformações são sobrescritos aqui.
 */
public class Circulo extends FormaBase {

    private double raio;

    public Circulo(Ponto centro, double raio) {
        super();
        if (centro == null) {
            throw new IllegalArgumentException("O centro não pode ser nulo.");
        }
        validarRaio(raio);
        pontos.add(centro);
        this.raio = raio;
    }

    // Usado só pelo clonar()
    private Circulo() {
        super();
    }

    public Ponto getCentro() {
        return pontos.get(0);
    }

    public double getRaio() {
        return raio;
    }

    public void setRaio(double raio) {
        validarRaio(raio);
        this.raio = raio;
    }

    private void validarRaio(double raio) {
        if (raio <= 0) {
            throw new IllegalArgumentException("O raio do círculo deve ser maior que zero.");
        }
    }

    /** O círculo é definido só pelo centro, então não aceita pontos extras. */
    @Override
    public void adicionarPonto(Ponto p) {
        throw new UnsupportedOperationException("O círculo tem apenas o ponto central.");
    }

    @Override
    public double calcularArea() {
        return Math.PI * raio * raio;
    }

    @Override
    public double calcularPerimetro() {
        return 2 * Math.PI * raio;
    }

    @Override
    public Ponto getCentroide() {
        return getCentro().clonar();
    }

    @Override
    public boolean contemPonto(double px, double py) {
        return getCentro().distancia(new Ponto(px, py)) <= raio;
    }

    // transladar() não precisa ser sobrescrito: mover o centro já move o círculo

    @Override
    public void escalar(double fator) {
        validarFatorEscala(fator);
        raio *= fator;
    }

    /** Girar um círculo em torno do próprio centro não muda nada. */
    @Override
    public void rotacionar(double anguloRadianos) {
        // sem efeito
    }

    /**
     * Cisalhar um círculo o transformaria numa elipse, que este modelo
     * (centro + raio) não consegue representar. Por isso não tem efeito.
     */
    @Override
    public void cisalhar(double shx, double shy) {
        // sem efeito
    }

    @Override
    public FormaGeometrica clonar() {
        Circulo copia = new Circulo();
        copiarAtributosPara(copia);
        copia.raio = this.raio;
        return copia;
    }
}
