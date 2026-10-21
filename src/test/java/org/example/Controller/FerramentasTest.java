package org.example.Controller;

import static org.example.Controller.EventosFalsos.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Poligono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes do padrão State: o mesmo evento de mouse tem efeitos diferentes
 * dependendo da ferramenta ativa no EditorContext.
 */
class FerramentasTest {

    private static final double DELTA = 1e-9;

    private EditorContext contexto;
    private List<String> avisos;

    @BeforeEach
    void preparar() {
        contexto = new EditorContext();
        avisos = new ArrayList<>();
        contexto.setAcaoMostrarAviso(avisos::add);
    }

    // ---------------- State ----------------

    @Test
    void ferramentaInicialEhSelecionar() {
        assertInstanceOf(FerramentaSelecionar.class, contexto.getFerramentaAtual());
    }

    @Test
    void mesmoCliqueTemEfeitoDiferenteConformeAFerramenta() {
        Quadrado q = new Quadrado(new Ponto(100, 100), 50);
        contexto.adicionarForma(q);

        // Com Linha Livre, clicar sobre o quadrado começa um polígono
        contexto.setFerramentaAtual(new FerramentaDesenhar());
        contexto.aoClicar(clicar(100, 100));
        assertNotNull(contexto.getFormaEmAndamento());
        assertEquals(1, contexto.getFormas().size());

        // Com Excluir, o mesmo clique apaga o quadrado
        contexto.setFerramentaAtual(new FerramentaExcluir());
        contexto.aoClicar(clicar(100, 100));
        assertTrue(contexto.getFormas().isEmpty());
    }

    @Test
    void trocarDeFerramentaDescartaFormaEmAndamento() {
        contexto.setFerramentaAtual(new FerramentaDesenhar());
        contexto.aoClicar(clicar(10, 10));
        contexto.setFerramentaAtual(new FerramentaSelecionar());
        assertNull(contexto.getFormaEmAndamento());
    }

    // ---------------- Linha Livre ----------------

    @Test
    void desenharPoligonoFechandoComDuploClique() {
        contexto.setFerramentaAtual(new FerramentaDesenhar());
        contexto.aoClicar(clicar(0, 0));
        contexto.aoClicar(clicar(100, 0));
        contexto.aoClicar(clicar(100, 100));
        contexto.aoClicar(duploClique(100, 100));

        assertEquals(1, contexto.getFormas().size());
        assertInstanceOf(Poligono.class, contexto.getFormas().get(0));
        assertEquals(3, contexto.getFormas().get(0).getQuantidadePontos());
        assertNull(contexto.getFormaEmAndamento());
    }

    @Test
    void clicarPertoDoPrimeiroPontoFechaOPoligono() {
        contexto.setFerramentaAtual(new FerramentaDesenhar());
        contexto.aoClicar(clicar(0, 0));
        contexto.aoClicar(clicar(100, 0));
        contexto.aoClicar(clicar(100, 100));
        contexto.aoClicar(clicar(3, 4)); // a 5 px do primeiro ponto

        assertEquals(1, contexto.getFormas().size());
        assertEquals(3, contexto.getFormas().get(0).getQuantidadePontos());
    }

    @Test
    void fecharComMenosDe3PontosAvisaEContinuaDesenhando() {
        contexto.setFerramentaAtual(new FerramentaDesenhar());
        contexto.aoClicar(clicar(0, 0));
        contexto.aoClicar(clicar(100, 0));
        contexto.aoClicar(cliqueDireito(100, 0));

        assertTrue(contexto.getFormas().isEmpty());
        assertEquals(1, avisos.size());
        assertNotNull(contexto.getFormaEmAndamento(), "o desenho continua para o usuário adicionar pontos");
    }

    @Test
    void poligonoNovoUsaOEstiloDaBarraLateral() {
        contexto.setCorPreenchimentoAtual("#FF0000");
        contexto.setEspessuraBordaAtual(5);
        contexto.setFerramentaAtual(new FerramentaDesenhar());
        contexto.aoClicar(clicar(0, 0));
        contexto.aoClicar(clicar(100, 0));
        contexto.aoClicar(clicar(100, 100));
        contexto.aoClicar(cliqueDireito(100, 100));

        FormaGeometrica criada = contexto.getFormas().get(0);
        assertEquals("#FF0000", criada.getCorPreenchimentoHex());
        assertEquals(5, criada.getEspessuraBorda(), DELTA);
    }

    @Test
    void snapArredondaOsPontosParaAGrade() {
        contexto.setGridSnappingHabilitado(true);
        contexto.setFerramentaAtual(new FerramentaDesenhar());
        contexto.aoClicar(clicar(13, 28));
        Ponto p = contexto.getFormaEmAndamento().getPontos().get(0);
        assertEquals(20, p.getX(), DELTA);
        assertEquals(20, p.getY(), DELTA);
    }

    // ---------------- Selecionar ----------------

    @Test
    void clicarNumaFormaSelecionaSoEla() {
        Quadrado a = new Quadrado(new Ponto(100, 100), 30);
        Quadrado b = new Quadrado(new Ponto(300, 100), 30);
        contexto.adicionarForma(a);
        contexto.adicionarForma(b);

        contexto.aoPressionarMouse(pressionar(100, 100));
        contexto.aoSoltarMouse(soltar(100, 100));
        assertEquals(List.of(a), contexto.getSelecionados());

        contexto.aoPressionarMouse(pressionar(300, 100));
        contexto.aoSoltarMouse(soltar(300, 100));
        assertEquals(List.of(b), contexto.getSelecionados());
    }

    @Test
    void shiftAdicionaNaSelecao() {
        Quadrado a = new Quadrado(new Ponto(100, 100), 30);
        Quadrado b = new Quadrado(new Ponto(300, 100), 30);
        contexto.adicionarForma(a);
        contexto.adicionarForma(b);

        contexto.aoPressionarMouse(pressionar(100, 100));
        contexto.aoSoltarMouse(soltar(100, 100));
        contexto.aoPressionarMouse(pressionarComShift(300, 100));
        contexto.aoSoltarMouse(soltar(300, 100));
        assertEquals(2, contexto.getSelecionados().size());
    }

    @Test
    void clicarNaFormaDaFrenteQuandoHaSobreposicao() {
        Quadrado tras = new Quadrado(new Ponto(100, 100), 50);
        Quadrado frente = new Quadrado(new Ponto(120, 100), 50);
        contexto.adicionarForma(tras);
        contexto.adicionarForma(frente);

        contexto.aoPressionarMouse(pressionar(110, 100));
        assertEquals(List.of(frente), contexto.getSelecionados());
    }

    @Test
    void arrastarMoveAsFormasSelecionadas() {
        Quadrado q = new Quadrado(new Ponto(100, 100), 30);
        contexto.adicionarForma(q);

        contexto.aoPressionarMouse(pressionar(100, 100));
        contexto.aoArrastarMouse(arrastar(130, 110));
        contexto.aoArrastarMouse(arrastar(150, 140));
        contexto.aoSoltarMouse(soltar(150, 140));

        assertEquals(150, q.getCentroide().getX(), DELTA);
        assertEquals(140, q.getCentroide().getY(), DELTA);
    }

    @Test
    void caixaDeSelecaoPegaSoFormasInteiramenteDentro() {
        Quadrado dentro = new Quadrado(new Ponto(100, 100), 20);
        Quadrado metadeFora = new Quadrado(new Ponto(200, 100), 20);
        contexto.adicionarForma(dentro);
        contexto.adicionarForma(metadeFora);

        contexto.aoPressionarMouse(pressionar(50, 50));
        contexto.aoArrastarMouse(arrastar(200, 150));
        assertNotNull(contexto.getCaixaSelecao());
        contexto.aoSoltarMouse(soltar(200, 150));

        assertEquals(List.of(dentro), contexto.getSelecionados());
        assertNull(contexto.getCaixaSelecao());
    }

    @Test
    void clicarNoVazioLimpaASelecao() {
        Quadrado q = new Quadrado(new Ponto(100, 100), 30);
        contexto.adicionarForma(q);
        contexto.selecionar(q);

        contexto.aoPressionarMouse(pressionar(500, 500));
        contexto.aoSoltarMouse(soltar(500, 500));
        assertTrue(contexto.getSelecionados().isEmpty());
    }

    // ---------------- Inserir forma ----------------

    @Test
    void arrastarInsereFormaComCentroERaio() {
        contexto.setFerramentaAtual(new FerramentaInserirForma(Circulo::new));
        contexto.aoPressionarMouse(pressionar(100, 100));
        contexto.aoArrastarMouse(arrastar(130, 100));
        contexto.aoArrastarMouse(arrastar(100, 150)); // raio final = 50
        assertNotNull(contexto.getFormaEmAndamento(), "durante o arrasto aparece uma prévia");
        contexto.aoSoltarMouse(soltar(100, 150));

        assertEquals(1, contexto.getFormas().size());
        Circulo c = (Circulo) contexto.getFormas().get(0);
        assertEquals(50, c.getRaio(), DELTA);
        assertEquals(100, c.getCentro().getX(), DELTA);
        assertNull(contexto.getFormaEmAndamento());
    }

    @Test
    void mesmaFerramentaCriaTiposDiferentesConformeAFabrica() {
        contexto.setFerramentaAtual(new FerramentaInserirForma(Hexagono::new));
        contexto.aoPressionarMouse(pressionar(100, 100));
        contexto.aoArrastarMouse(arrastar(150, 100));
        contexto.aoSoltarMouse(soltar(150, 100));

        contexto.setFerramentaAtual(new FerramentaInserirForma(Quadrado::new));
        contexto.aoPressionarMouse(pressionar(300, 100));
        contexto.aoArrastarMouse(arrastar(350, 100));
        contexto.aoSoltarMouse(soltar(350, 100));

        assertInstanceOf(Hexagono.class, contexto.getFormas().get(0));
        assertInstanceOf(Quadrado.class, contexto.getFormas().get(1));
    }

    @Test
    void cliqueSemArrastarNaoInsereForma() {
        contexto.setFerramentaAtual(new FerramentaInserirForma(Quadrado::new));
        contexto.aoPressionarMouse(pressionar(100, 100));
        contexto.aoArrastarMouse(arrastar(101, 101));
        contexto.aoSoltarMouse(soltar(101, 101));
        assertTrue(contexto.getFormas().isEmpty());
    }

    // ---------------- Excluir ----------------

    @Test
    void excluirSelecionadosApagaSoAsSelecionadas() {
        Quadrado a = new Quadrado(new Ponto(100, 100), 30);
        Quadrado b = new Quadrado(new Ponto(300, 100), 30);
        contexto.adicionarForma(a);
        contexto.adicionarForma(b);
        contexto.selecionar(a);

        contexto.excluirSelecionados();
        assertEquals(List.of(b), contexto.getFormas());
        assertTrue(contexto.getSelecionados().isEmpty());
    }

    @Test
    void ferramentaExcluirIgnoraCliqueNoVazio() {
        contexto.adicionarForma(new Quadrado(new Ponto(100, 100), 30));
        contexto.setFerramentaAtual(new FerramentaExcluir());
        contexto.aoClicar(clicar(500, 500));
        assertEquals(1, contexto.getFormas().size());
    }
}
