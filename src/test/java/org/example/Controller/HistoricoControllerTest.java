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

/** Testes do padrão Memento (HistoricoController) e do desfazer/refazer no editor. */
class HistoricoControllerTest {

    private static final double DELTA = 1e-9;

    // ---------------- HistoricoController sozinho ----------------

    @Test
    void desfazerDevolveOEstadoRegistrado() {
        HistoricoController historico = new HistoricoController();
        List<FormaGeometrica> formas = new ArrayList<>();
        formas.add(new Circulo(new Ponto(0, 0), 5));

        historico.registrarEstado(formas);
        formas.add(new Circulo(new Ponto(50, 50), 5));

        List<FormaGeometrica> anterior = historico.desfazer(formas);
        assertEquals(1, anterior.size());
    }

    @Test
    void estadoGuardadoEhCopiaProfunda() {
        HistoricoController historico = new HistoricoController();
        Circulo c = new Circulo(new Ponto(0, 0), 5);
        List<FormaGeometrica> formas = new ArrayList<>(List.of(c));

        historico.registrarEstado(formas);
        // Mexer na forma DEPOIS de registrar não pode alterar a foto guardada
        c.transladar(100, 100);
        c.setRaio(30);

        Circulo guardado = (Circulo) historico.desfazer(formas).get(0);
        assertNotSame(c, guardado);
        assertEquals(0, guardado.getCentro().getX(), DELTA);
        assertEquals(5, guardado.getRaio(), DELTA);
    }

    @Test
    void refazerDevolveOQueFoiDesfeito() {
        HistoricoController historico = new HistoricoController();
        List<FormaGeometrica> vazio = new ArrayList<>();
        historico.registrarEstado(vazio);
        List<FormaGeometrica> comUma = List.of(new Circulo(new Ponto(0, 0), 5));

        List<FormaGeometrica> desfeito = historico.desfazer(comUma);
        assertTrue(desfeito.isEmpty());
        List<FormaGeometrica> refeito = historico.refazer(desfeito);
        assertEquals(1, refeito.size());
    }

    @Test
    void alteracaoNovaLimpaORefazer() {
        HistoricoController historico = new HistoricoController();
        historico.registrarEstado(new ArrayList<>());
        historico.desfazer(new ArrayList<>());
        assertTrue(historico.podeRefazer());

        historico.registrarEstado(new ArrayList<>());
        assertFalse(historico.podeRefazer());
    }

    @Test
    void semHistoricoDevolveAListaAtual() {
        HistoricoController historico = new HistoricoController();
        List<FormaGeometrica> atual = new ArrayList<>();
        assertSame(atual, historico.desfazer(atual));
        assertSame(atual, historico.refazer(atual));
    }

    @Test
    void guardaNoMaximo100Estados() {
        HistoricoController historico = new HistoricoController();
        for (int i = 0; i < 150; i++) {
            historico.registrarEstado(new ArrayList<>());
        }
        int desfeitos = 0;
        while (historico.podeDesfazer()) {
            historico.desfazer(new ArrayList<>());
            desfeitos++;
        }
        assertEquals(100, desfeitos);
    }

    // ---------------- Desfazer/refazer no editor ----------------

    private EditorContext contexto;

    @BeforeEach
    void preparar() {
        contexto = new EditorContext();
    }

    @Test
    void desfazerInsercaoDeForma() {
        contexto.setFerramentaAtual(new FerramentaInserirForma(Quadrado::new));
        contexto.aoPressionarMouse(pressionar(100, 100));
        contexto.aoArrastarMouse(arrastar(150, 100));
        contexto.aoSoltarMouse(soltar(150, 100));
        assertEquals(1, contexto.getFormas().size());

        contexto.desfazer();
        assertTrue(contexto.getFormas().isEmpty());

        contexto.refazer();
        assertEquals(1, contexto.getFormas().size());
    }

    @Test
    void desfazerMovimentoVoltaAFormaParaOLugar() {
        contexto.adicionarForma(new Quadrado(new Ponto(100, 100), 30));
        contexto.aoPressionarMouse(pressionar(100, 100));
        contexto.aoArrastarMouse(arrastar(120, 100));
        contexto.aoArrastarMouse(arrastar(200, 150));
        contexto.aoSoltarMouse(soltar(200, 150));

        contexto.desfazer();
        // Um arrasto inteiro = um único passo de desfazer
        Ponto c = contexto.getFormas().get(0).getCentroide();
        assertEquals(100, c.getX(), DELTA);
        assertEquals(100, c.getY(), DELTA);
        assertFalse(contexto.getHistorico().podeDesfazer());
    }

    @Test
    void cliqueSemMoverNaoGeraHistorico() {
        contexto.adicionarForma(new Quadrado(new Ponto(100, 100), 30));
        contexto.aoPressionarMouse(pressionar(100, 100));
        contexto.aoSoltarMouse(soltar(100, 100));
        assertFalse(contexto.getHistorico().podeDesfazer());
    }

    @Test
    void desfazerTransformacaoEExclusao() {
        Quadrado q = new Quadrado(new Ponto(100, 100), 50);
        contexto.adicionarForma(q);
        contexto.selecionar(q);

        contexto.setFerramentaAtual(new FerramentaTransformar(ModoFerramenta.ESCALAR));
        contexto.aoPressionarMouse(pressionar(150, 100));
        contexto.aoArrastarMouse(arrastar(200, 100));
        contexto.aoSoltarMouse(soltar(200, 100));
        contexto.selecionar(contexto.getFormas().get(0));
        contexto.excluirSelecionados();
        assertTrue(contexto.getFormas().isEmpty());

        contexto.desfazer(); // volta a exclusão
        assertEquals(20000, contexto.getFormas().get(0).calcularArea(), 1e-6);
        contexto.desfazer(); // volta a escala
        assertEquals(5000, contexto.getFormas().get(0).calcularArea(), 1e-6);
    }

    @Test
    void desfazerSemHistoricoNaoApagaODesenho() {
        contexto.adicionarForma(new Quadrado(new Ponto(100, 100), 30));
        contexto.desfazer();
        assertEquals(1, contexto.getFormas().size());
    }
}
