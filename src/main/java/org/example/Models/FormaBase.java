package org.example.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementação comum a todas as formas. A matemática aqui trata a forma como um
 * polígono (lista de vértices ligados em sequência, com o último ligado ao primeiro).
 * O Circulo sobrescreve o que for diferente para ele.
 *
 * É abstrata porque cada forma concreta precisa dizer como se clonar.
 */
public abstract class FormaBase implements FormaGeometrica {

    // Abaixo disso consideramos o valor como zero (evita divisão por quase-zero)
    private static final double EPSILON = 1e-9;

    // Aceita "#RRGGBB" ou "#RRGGBBAA" (com transparência)
    private static final String PADRAO_COR_HEX = "#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})";

    protected List<Ponto> pontos;
    protected String corPreenchimentoHex;
    protected String corBordaHex;
    protected double espessuraBorda;
    protected String groupId;

    protected FormaBase() {
        this.pontos = new ArrayList<>();
        this.corPreenchimentoHex = "#D3D3D3"; // cinza claro, como nas figuras do enunciado
        this.corBordaHex = "#000000";
        this.espessuraBorda = 1.0;
        this.groupId = null; // null = não pertence a nenhum grupo
    }

    // ------------------------------------------------------------------
    // Pontos
    // ------------------------------------------------------------------

    @Override
    public void adicionarPonto(Ponto p) {
        if (p == null) {
            throw new IllegalArgumentException("O ponto não pode ser nulo.");
        }
        pontos.add(p);
    }

    /**
     * Devolve a lista "somente leitura": quem está de fora não consegue
     * adicionar nem remover pontos sem passar por adicionarPonto (encapsulamento).
     * Os pontos em si continuam editáveis, o que a ferramenta Editar Vértice usa.
     */
    @Override
    public List<Ponto> getPontos() {
        return Collections.unmodifiableList(pontos);
    }

    @Override
    public int getQuantidadePontos() {
        return pontos.size();
    }

    // ------------------------------------------------------------------
    // Estilo e grupo
    // ------------------------------------------------------------------

    @Override
    public String getCorPreenchimentoHex() {
        return corPreenchimentoHex;
    }

    @Override
    public void setCorPreenchimentoHex(String hex) {
        validarCorHex(hex);
        this.corPreenchimentoHex = hex;
    }

    @Override
    public String getCorBordaHex() {
        return corBordaHex;
    }

    @Override
    public void setCorBordaHex(String hex) {
        validarCorHex(hex);
        this.corBordaHex = hex;
    }

    @Override
    public double getEspessuraBorda() {
        return espessuraBorda;
    }

    @Override
    public void setEspessuraBorda(double espessura) {
        if (espessura <= 0) {
            throw new IllegalArgumentException("A espessura da borda deve ser maior que zero.");
        }
        this.espessuraBorda = espessura;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    private void validarCorHex(String hex) {
        if (hex == null || !hex.matches(PADRAO_COR_HEX)) {
            throw new IllegalArgumentException("Cor inválida: " + hex + " (use o formato #RRGGBB).");
        }
    }

    // ------------------------------------------------------------------
    // Cálculos geométricos
    // ------------------------------------------------------------------

    /** Área pela fórmula de Gauss (shoelace), sempre positiva. */
    @Override
    public double calcularArea() {
        return Math.abs(areaComSinal());
    }

    /**
     * Fórmula de Gauss SEM o valor absoluto. O sinal só indica se os vértices
     * estão em sentido horário ou anti-horário; o centroide precisa dele.
     */
    private double areaComSinal() {
        int n = pontos.size();
        if (n < 3) {
            return 0;
        }
        double soma = 0;
        for (int i = 0; i < n; i++) {
            Ponto atual = pontos.get(i);
            Ponto proximo = pontos.get((i + 1) % n); // o % faz o último se ligar ao primeiro
            soma += atual.getX() * proximo.getY() - proximo.getX() * atual.getY();
        }
        return soma / 2;
    }

    /** Soma das distâncias entre vértices vizinhos, incluindo último -> primeiro. */
    @Override
    public double calcularPerimetro() {
        int n = pontos.size();
        if (n < 3) {
            return 0;
        }
        double perimetro = 0;
        for (int i = 0; i < n; i++) {
            perimetro += pontos.get(i).distancia(pontos.get((i + 1) % n));
        }
        return perimetro;
    }

    /**
     * Centro de massa do polígono:
     * Cx = 1/(6A) * soma((xi + xi+1) * (xi*yi+1 - xi+1*yi)), e o mesmo para Cy.
     * Se a área for zero (pontos alinhados), usa a média dos vértices.
     */
    @Override
    public Ponto getCentroide() {
        if (pontos.isEmpty()) {
            throw new IllegalStateException("A forma não tem pontos para calcular o centroide.");
        }
        double area = areaComSinal();
        if (Math.abs(area) < EPSILON) {
            return mediaDosPontos();
        }
        int n = pontos.size();
        double cx = 0;
        double cy = 0;
        for (int i = 0; i < n; i++) {
            Ponto atual = pontos.get(i);
            Ponto proximo = pontos.get((i + 1) % n);
            double produtoCruzado = atual.getX() * proximo.getY() - proximo.getX() * atual.getY();
            cx += (atual.getX() + proximo.getX()) * produtoCruzado;
            cy += (atual.getY() + proximo.getY()) * produtoCruzado;
        }
        return new Ponto(cx / (6 * area), cy / (6 * area));
    }

    private Ponto mediaDosPontos() {
        double somaX = 0;
        double somaY = 0;
        for (Ponto p : pontos) {
            somaX += p.getX();
            somaY += p.getY();
        }
        return new Ponto(somaX / pontos.size(), somaY / pontos.size());
    }

    /**
     * Ray casting: traça uma linha horizontal do ponto até a direita e conta
     * quantas arestas ela cruza. Número ímpar = dentro; par = fora.
     */
    @Override
    public boolean contemPonto(double px, double py) {
        int n = pontos.size();
        if (n < 3) {
            return false;
        }
        boolean dentro = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Ponto a = pontos.get(i);
            Ponto b = pontos.get(j);
            // A aresta a-b só pode ser cruzada se py estiver entre as alturas dos dois pontos
            boolean cruzaAltura = (a.getY() > py) != (b.getY() > py);
            if (cruzaAltura) {
                double xDoCruzamento = a.getX() + (py - a.getY()) * (b.getX() - a.getX()) / (b.getY() - a.getY());
                if (px < xDoCruzamento) {
                    dentro = !dentro;
                }
            }
        }
        return dentro;
    }

    // ------------------------------------------------------------------
    // Transformações (todas relativas ao centroide, exceto a translação)
    // ------------------------------------------------------------------

    @Override
    public void transladar(double dx, double dy) {
        for (Ponto p : pontos) {
            p.mover(dx, dy);
        }
    }

    /** Multiplica a distância de cada ponto ao centroide pelo fator (2 = dobra, 0.5 = metade). */
    @Override
    public void escalar(double fator) {
        validarFatorEscala(fator);
        Ponto c = getCentroide();
        for (Ponto p : pontos) {
            p.setX(c.getX() + (p.getX() - c.getX()) * fator);
            p.setY(c.getY() + (p.getY() - c.getY()) * fator);
        }
    }

    /**
     * Rotação em torno do centroide. Para cada ponto, calcula a posição relativa
     * ao centro (rx, ry) e aplica:
     * x' = rx*cos - ry*sin,  y' = rx*sin + ry*cos
     */
    @Override
    public void rotacionar(double anguloRadianos) {
        Ponto c = getCentroide();
        double cos = Math.cos(anguloRadianos);
        double sin = Math.sin(anguloRadianos);
        for (Ponto p : pontos) {
            double rx = p.getX() - c.getX();
            double ry = p.getY() - c.getY();
            p.setX(c.getX() + rx * cos - ry * sin);
            p.setY(c.getY() + rx * sin + ry * cos);
        }
    }

    /** Cisalhamento relativo ao centroide: x' = x + shx*y,  y' = y + shy*x. */
    @Override
    public void cisalhar(double shx, double shy) {
        Ponto c = getCentroide();
        for (Ponto p : pontos) {
            // Usa os valores ANTIGOS de rx e ry nas duas fórmulas
            double rx = p.getX() - c.getX();
            double ry = p.getY() - c.getY();
            p.setX(c.getX() + rx + shx * ry);
            p.setY(c.getY() + ry + shy * rx);
        }
    }

    protected void validarFatorEscala(double fator) {
        if (fator <= 0) {
            throw new IllegalArgumentException("O fator de escala deve ser maior que zero.");
        }
    }

    // ------------------------------------------------------------------
    // Apoio às subclasses
    // ------------------------------------------------------------------

    /**
     * Gera os vértices de um polígono regular (quadrado, hexágono...) distribuídos
     * num círculo imaginário de centro e raio dados:
     * x = cx + raio*cos(ângulo),  y = cy + raio*sin(ângulo)
     */
    protected void gerarVerticesRegulares(Ponto centro, double raio, int lados, double anguloInicial) {
        if (centro == null) {
            throw new IllegalArgumentException("O centro não pode ser nulo.");
        }
        if (raio <= 0) {
            throw new IllegalArgumentException("O raio deve ser maior que zero.");
        }
        double passo = 2 * Math.PI / lados;
        for (int i = 0; i < lados; i++) {
            double angulo = anguloInicial + i * passo;
            pontos.add(new Ponto(centro.getX() + raio * Math.cos(angulo),
                                 centro.getY() + raio * Math.sin(angulo)));
        }
    }

    /**
     * Base do Prototype: copia estilo, grupo e uma cópia de CADA ponto para outra forma.
     * Como os pontos são clonados um a um, original e cópia não compartilham nada.
     */
    protected void copiarAtributosPara(FormaBase destino) {
        destino.pontos = new ArrayList<>();
        for (Ponto p : this.pontos) {
            destino.pontos.add(p.clonar());
        }
        destino.corPreenchimentoHex = this.corPreenchimentoHex;
        destino.corBordaHex = this.corBordaHex;
        destino.espessuraBorda = this.espessuraBorda;
        destino.groupId = this.groupId;
    }
}
