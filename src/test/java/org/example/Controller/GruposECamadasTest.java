package org.example.Controller;

import static org.example.Controller.EventosFalsos.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Testes do Composite (agrupar/desagrupar) e da ordem das camadas. */
class GruposECamadasTest {

    private static final double DELTA = 1e-6;

    private EditorContext contexto;
    private List<String> avisos;
    private Quadrado a;
    private Quadrado b;
    private Circulo c;

    @BeforeEach
    void preparar() {
        contexto = new EditorContext();
        avisos = new ArrayList<>();
        contexto.setAcaoMostrarAviso(avisos::add);

        a = new Quadrado(new Ponto(100, 100), 20);
        b = new Quadrado(new Ponto(300, 100), 20);
        c = new Circulo(new Ponto(500, 100), 20);
        contexto.adicionarForma(a);
        contexto.adicionarForma(b);
        contexto.adicionarForma(c);
    }

    /** Agrupa A e B (C fica de fora). */
    private void agruparAeB() {
        contexto.selecionar(a);
        contexto.selecionar(b);
        contexto.agruparSelecionados();
        contexto.limparSelecao();
    }

    // ---------------- Agrupar / desagrupar ----------------

    @Test
    void agruparDaOMesmoGroupIdParaAsSelecionadas() {
        agruparAeB();
        assertNotNull(a.getGroupId());
        assertEquals(a.getGroupId(), b.getGroupId());
        assertNull(c.getGroupId());
    }

    @Test
    void agruparComMenosDeDuasFormasAvisa() {
        contexto.selecionar(a);
        contexto.agruparSelecionados();
        assertNull(a.getGroupId());
        assertEquals(1, avisos.size());
    }

    @Test
    void doisGruposTemIdsDiferentes() {
        agruparAeB();
        Circulo d = new Circulo(new Ponto(700, 100), 20);
        contexto.adicionarForma(d);
        contexto.selecionar(c);
        contexto.selecionar(d);
        contexto.agruparSelecionados();
        assertNotEquals(a.getGroupId(), c.getGroupId());
    }

    @Test
    void clicarNumMembroSelecionaOGrupoInteiro() {
        agruparAeB();
        contexto.aoPressionarMouse(pressionar(100, 100)); // clica só em A
        contexto.aoSoltarMouse(soltar(100, 100));

        assertTrue(contexto.isSelecionado(a));
        assertTrue(contexto.isSelecionado(b));
        assertFalse(contexto.isSelecionado(c));
    }

    @Test
    void arrastarUmMembroMoveOGrupoInteiro() {
        agruparAeB();
        contexto.aoPressionarMouse(pressionar(100, 100));
        contexto.aoArrastarMouse(arrastar(110, 150));
        contexto.aoSoltarMouse(soltar(110, 150));

        assertEquals(110, a.getCentroide().getX(), DELTA);
        assertEquals(310, b.getCentroide().getX(), DELTA);
        assertEquals(150, b.getCentroide().getY(), DELTA);
        assertEquals(500, c.getCentroide().getX(), DELTA, "a forma fora do grupo não se move");
    }

    @Test
    void grupoEscalaEmTornoDoCentroideDoGrupo() {
        agruparAeB();
        contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.ESCALAR));
        // Pressionar em A seleciona o grupo; centro do grupo = (200,100)
        contexto.aoPressionarMouse(pressionar(100, 100)); // 100 px do centro
        contexto.aoArrastarMouse(arrastar(50, 100));      // 150 px => fator 1,5

        assertEquals(50, a.getCentroide().getX(), DELTA);
        assertEquals(350, b.getCentroide().getX(), DELTA);
    }

    @Test
    void excluirUmMembroExcluiOGrupo() {
        agruparAeB();
        contexto.setFerramentaAtual(new FerramentaExcluir());
        contexto.aoClicar(clicar(300, 100)); // clica em B
        assertEquals(List.of(c), contexto.getFormas());
    }

    @Test
    void desagruparSeparaAsFormas() {
        agruparAeB();
        contexto.selecionar(a);
        contexto.desagruparSelecionados();
        assertNull(a.getGroupId());
        assertNull(b.getGroupId());

        contexto.limparSelecao();
        contexto.selecionar(a);
        assertEquals(List.of(a), contexto.getSelecionados());
    }

    @Test
    void desagruparSemGrupoAvisa() {
        contexto.selecionar(a);
        contexto.desagruparSelecionados();
        assertEquals(1, avisos.size());
    }

    @Test
    void agruparPodeSerDesfeito() {
        agruparAeB();
        contexto.desfazer();
        for (FormaGeometrica forma : contexto.getFormas()) {
            assertNull(forma.getGroupId());
        }
    }

    @Test
    void groupIdEhCopiadoNoClone() {
        agruparAeB();
        assertEquals(a.getGroupId(), a.clonar().getGroupId());
    }

    // ---------------- Camadas ----------------

    @Test
    void trazerParaFrenteLevaParaOFimDaLista() {
        contexto.selecionar(a);
        contexto.trazerParaFrente();
        assertEquals(List.of(b, c, a), contexto.getFormas());
    }

    @Test
    void enviarParaTrasLevaParaOInicioDaLista() {
        contexto.selecionar(c);
        contexto.enviarParaTras();
        assertEquals(List.of(c, a, b), contexto.getFormas());
    }

    @Test
    void moverVariasMantemAOrdemEntreElas() {
        contexto.selecionar(c);
        contexto.selecionar(a); // ordem de seleção diferente da ordem das camadas
        contexto.trazerParaFrente();
        assertEquals(List.of(b, a, c), contexto.getFormas());
    }

    @Test
    void camadaSemSelecaoAvisaENaoMudaNada() {
        contexto.trazerParaFrente();
        assertEquals(List.of(a, b, c), contexto.getFormas());
        assertEquals(1, avisos.size());
    }

    @Test
    void trazerParaFrenteMudaQuemRecebeOClique() {
        // Dois quadrados sobrepostos: o da frente recebe o clique
        Quadrado tras = new Quadrado(new Ponto(800, 100), 50);
        Quadrado frente = new Quadrado(new Ponto(820, 100), 50);
        contexto.adicionarForma(tras);
        contexto.adicionarForma(frente);
        assertSame(frente, contexto.encontrarFormaEm(810, 100));

        contexto.selecionar(tras);
        contexto.trazerParaFrente();
        assertSame(tras, contexto.encontrarFormaEm(810, 100));
    }

    @Test
    void mudarCamadaPodeSerDesfeito() {
        contexto.selecionar(a);
        contexto.trazerParaFrente();
        contexto.desfazer();
        // Depois de desfazer as formas são cópias; confere a ordem pelas posições
        List<FormaGeometrica> formas = contexto.getFormas();
        assertEquals(100, formas.get(0).getCentroide().getX(), DELTA);
        assertEquals(300, formas.get(1).getCentroide().getX(), DELTA);
        assertEquals(500, formas.get(2).getCentroide().getX(), DELTA);
    }
}
