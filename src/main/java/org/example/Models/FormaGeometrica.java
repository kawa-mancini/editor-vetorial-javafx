package org.example.Models;

import java.util.List;

/**
 * Contrato que toda forma geométrica do editor precisa cumprir
 * (seção 6 do enunciado). O resto do sistema só conhece esta interface,
 * por isso pode tratar polígonos e círculos da mesma forma (polimorfismo).
 */
public interface FormaGeometrica {

    void adicionarPonto(Ponto p);

    List<Ponto> getPontos();

    int getQuantidadePontos();

    String getCorPreenchimentoHex();

    void setCorPreenchimentoHex(String hex);

    String getCorBordaHex();

    void setCorBordaHex(String hex);

    double getEspessuraBorda();

    void setEspessuraBorda(double espessura);

    String getGroupId();

    void setGroupId(String groupId);

    double calcularArea();

    double calcularPerimetro();

    Ponto getCentroide();

    boolean contemPonto(double px, double py);

    void transladar(double dx, double dy);

    void escalar(double fator);

    void rotacionar(double anguloRadianos);

    void cisalhar(double shx, double shy);

    /** Prototype: cria uma cópia profunda da forma (sem compartilhar pontos). */
    FormaGeometrica clonar();
}
