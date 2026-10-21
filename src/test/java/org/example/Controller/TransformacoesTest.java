package org.example.Controller;

import static org.example.Controller.EventosFalsos.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.example.Models.Circulo;
import org.example.Models.Poligono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Testes da FerramentaTransformar (3 modos) e da FerramentaEditarVertice. */
class TransformacoesTest {

    private static final double DELTA = 1e-6;

    private EditorContext contexto;

    @BeforeEach
    void preparar() {
        contexto = new EditorContext();
    }

    // ---------------- Uma forma ----------------

    @Test
    void escalarDobraAoArrastarParaODobroDaDistancia() {
        Quadrado q = new Quadrado(new Ponto(100, 100), 50); // área 5000
        contexto.adicionarForma(q);
        contexto.selecionar(q);
        contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.ESCALAR));

        contexto.aoPressionarMouse(pressionar(150, 100)); // 50 px do centro
        contexto.aoArrastarMouse(arrastar(200, 100));     // 100 px do centro
        contexto.aoSoltarMouse(soltar(200, 100));

        assertEquals(20000, q.calcularArea(), DELTA);
        assertEquals(100, q.getCentroide().getX(), DELTA);
        assertEquals(100, q.getCentroide().getY(), DELTA);
    }

    @Test
    void rotacionarAcompanhaOAnguloDoMouse() {
        Quadrado q = new Quadrado(new Ponto(100, 100), 50);
        contexto.adicionarForma(q);
        contexto.selecionar(q);
        contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.ROTACIONAR));

        contexto.aoPressionarMouse(pressionar(150, 100)); // 0°
        contexto.aoArrastarMouse(arrastar(100, 150));     // 90°
        contexto.aoSoltarMouse(soltar(100, 150));

        // Vértice 0 estava a 45° (relativo +a, +a); girado 90° vai para (-a, +a)
        double a = 50 / Math.sqrt(2);
        assertEquals(100 - a, q.getPontos().get(0).getX(), DELTA);
        assertEquals(100 + a, q.getPontos().get(0).getY(), DELTA);
        assertEquals(5000, q.calcularArea(), DELTA);
    }

    @Test
    void cisalharPeloArrastoHorizontal() {
        Poligono r = new Poligono(List.of(new Ponto(0, 0), new Ponto(100, 0), new Ponto(100, 60), new Ponto(0, 60)));
        contexto.adicionarForma(r);
        contexto.selecionar(r);
        contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.CISALHAR));

        contexto.aoPressionarMouse(pressionar(200, 200));
        contexto.aoArrastarMouse(arrastar(250, 200)); // 50 px => shx = 0,5
        contexto.aoSoltarMouse(soltar(250, 200));

        // (0,60) relativo ao centroide (50,30) é (-50,30); x' = -50 + 0,5*30 = -35 => 15
        assertEquals(15, r.getPontos().get(3).getX(), DELTA);
        assertEquals(60, r.getPontos().get(3).getY(), DELTA);
        assertEquals(6000, r.calcularArea(), DELTA);
    }

    @Test
    void pressionarNumaFormaNaoSelecionadaPassaASelecionarEla() {
        Quadrado q = new Quadrado(new Ponto(100, 100), 50);
        contexto.adicionarForma(q);
        contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.ESCALAR));

        contexto.aoPressionarMouse(pressionar(110, 100));
        assertEquals(List.of(q), contexto.getSelecionados());
    }

    @Test
    void semSelecaoNadaAcontece() {
        Quadrado q = new Quadrado(new Ponto(100, 100), 50);
        contexto.adicionarForma(q);
        contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.ROTACIONAR));

        contexto.aoPressionarMouse(pressionar(500, 500));
        contexto.aoArrastarMouse(arrastar(500, 600));
        contexto.aoSoltarMouse(soltar(500, 600));

        assertEquals(100 + 50 / Math.sqrt(2), q.getPontos().get(0).getX(), DELTA);
    }

    @Test
    void escalarArrastandoAteOCentroNaoQuebra() {
        Quadrado q = new Quadrado(new Ponto(100, 100), 50);
        contexto.adicionarForma(q);
        contexto.selecionar(q);
        contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.ESCALAR));

        contexto.aoPressionarMouse(pressionar(150, 100));
        assertDoesNotThrow(() -> contexto.aoArrastarMouse(arrastar(100, 100)));
        assertEquals(5000, q.calcularArea(), DELTA);
    }

    // ---------------- Várias formas: transformam juntas em torno do centro comum ----------------

    @Test
    void escalarDuasFormasAfastaUmaDaOutra() {
        Quadrado a = new Quadrado(new Ponto(100, 100), 20);
        Quadrado b = new Quadrado(new Ponto(300, 100), 20);
        contexto.adicionarForma(a);
        contexto.adicionarForma(b);
        contexto.selecionar(a);
        contexto.selecionar(b);
        contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.ESCALAR));

        // Centro comum (200,100); arrasto de 50 px para 100 px => fator 2
        contexto.aoPressionarMouse(pressionar(250, 100));
        contexto.aoArrastarMouse(arrastar(300, 100));

        assertEquals(0, a.getCentroide().getX(), DELTA);
        assertEquals(400, b.getCentroide().getX(), DELTA);
    }

    @Test
    void rotacionar180GrausTrocaAsFormasDeLugar() {
        Circulo c = new Circulo(new Ponto(100, 100), 10);
        Quadrado q = new Quadrado(new Ponto(300, 100), 20);
        contexto.adicionarForma(c);
        contexto.adicionarForma(q);
        contexto.selecionar(c);
        contexto.selecionar(q);
        contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.ROTACIONAR));

        contexto.aoPressionarMouse(pressionar(250, 100)); // 0°
        contexto.aoArrastarMouse(arrastar(200, 150));     // 90°
        contexto.aoArrastarMouse(arrastar(150, 100));     // 180°

        assertEquals(300, c.getCentro().getX(), DELTA);
        assertEquals(100, c.getCentro().getY(), DELTA);
        assertEquals(10, c.getRaio(), DELTA);
        assertEquals(100, q.getCentroide().getX(), DELTA);
        assertEquals(100, q.getCentroide().getY(), DELTA);
    }

    // ---------------- Editar vértice ----------------

    @Test
    void arrastarUmVerticeMoveSoEle() {
        Poligono p = new Poligono(List.of(new Ponto(0, 0), new Ponto(100, 0), new Ponto(100, 100), new Ponto(0, 100)));
        contexto.adicionarForma(p);
        contexto.setFerramentaAtual(new FerramentaEditarVertice());

        contexto.aoPressionarMouse(pressionar(98, 3)); // perto de (100,0)
        contexto.aoArrastarMouse(arrastar(150, -20));
        contexto.aoSoltarMouse(soltar(150, -20));

        assertEquals(150, p.getPontos().get(1).getX(), DELTA);
        assertEquals(-20, p.getPontos().get(1).getY(), DELTA);
        assertEquals(0, p.getPontos().get(0).getX(), DELTA);
        assertEquals(100, p.getPontos().get(2).getX(), DELTA);
        assertEquals(List.of(p), contexto.getSelecionados());
    }

    @Test
    void pressionarLongeDosVerticesNaoMoveNada() {
        Poligono p = new Poligono(List.of(new Ponto(0, 0), new Ponto(100, 0), new Ponto(100, 100), new Ponto(0, 100)));
        contexto.adicionarForma(p);
        contexto.setFerramentaAtual(new FerramentaEditarVertice());

        contexto.aoPressionarMouse(pressionar(50, 50));
        contexto.aoArrastarMouse(arrastar(80, 80));
        contexto.aoSoltarMouse(soltar(80, 80));

        assertEquals(10000, p.calcularArea(), DELTA);
        assertEquals(List.of(p), contexto.getSelecionados(), "clicar dentro da forma a seleciona");
    }
}
