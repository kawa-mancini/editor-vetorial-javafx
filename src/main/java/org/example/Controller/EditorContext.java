package org.example.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

/**
 * Contexto do padrão State e "estado" do editor: as formas desenhadas,
 * quais estão selecionadas, a forma sendo desenhada agora, a ferramenta
 * ativa e as opções ligadas.
 */
public class EditorContext {

    /** Distância entre as linhas da grade magnética, em pixels. */
    public static final double TAMANHO_GRADE = 20;

    // A ordem da lista é a ordem das camadas: a última forma fica na frente
    private final List<FormaGeometrica> formas;
    private final List<FormaGeometrica> selecionados;
    private FormaGeometrica formaEmAndamento;
    private Ferramenta ferramentaAtual;
    private boolean gridSnappingHabilitado;

    // Estilo usado nas formas novas (vem da barra lateral)
    private String corPreenchimentoAtual;
    private String corBordaAtual;
    private double espessuraBordaAtual;

    // Retângulo tracejado da seleção por caixa (null quando não há)
    private Rectangle2D caixaSelecao;

    // O que fazer quando o desenho ou a barra de status precisam ser atualizados,
    // ou quando é preciso avisar o usuário. Quem define é a View; assim o Controller
    // não precisa conhecer as classes da View (baixo acoplamento, seção 3.2).
    private Runnable acaoRedesenhar;
    private Runnable acaoAtualizarStats;
    private Consumer<String> acaoMostrarAviso;

    public EditorContext() {
        this.formas = new ArrayList<>();
        this.selecionados = new ArrayList<>();
        this.formaEmAndamento = null;
        this.ferramentaAtual = new FerramentaSelecionar();
        this.gridSnappingHabilitado = false;
        this.corPreenchimentoAtual = "#D3D3D3";
        this.corBordaAtual = "#000000";
        this.espessuraBordaAtual = 1.0;
        this.caixaSelecao = null;
        this.acaoRedesenhar = () -> { };
        this.acaoAtualizarStats = () -> { };
        this.acaoMostrarAviso = mensagem -> { };
    }

    // ------------------------------------------------------------------
    // Padrão State: os eventos do mouse só são repassados para a ferramenta ativa
    // ------------------------------------------------------------------

    public void aoPressionarMouse(MouseEvent evento) {
        ferramentaAtual.aoPressionarMouse(evento, this);
    }

    public void aoArrastarMouse(MouseEvent evento) {
        ferramentaAtual.aoArrastarMouse(evento, this);
    }

    public void aoSoltarMouse(MouseEvent evento) {
        ferramentaAtual.aoSoltarMouse(evento, this);
    }

    public void aoClicar(MouseEvent evento) {
        ferramentaAtual.aoClicar(evento, this);
    }

    public Ferramenta getFerramentaAtual() {
        return ferramentaAtual;
    }

    /** Troca o estado. O que estava pela metade na ferramenta anterior é descartado. */
    public void setFerramentaAtual(Ferramenta ferramenta) {
        if (ferramenta == null) {
            throw new IllegalArgumentException("A ferramenta não pode ser nula.");
        }
        this.ferramentaAtual = ferramenta;
        this.formaEmAndamento = null;
        this.caixaSelecao = null;
        requestRedraw();
    }

    // ------------------------------------------------------------------
    // Avisos para a View
    // ------------------------------------------------------------------

    public void requestRedraw() {
        acaoRedesenhar.run();
    }

    public void requestStatsUpdate() {
        acaoAtualizarStats.run();
    }

    public void mostrarAviso(String mensagem) {
        acaoMostrarAviso.accept(mensagem);
    }

    public void setAcaoRedesenhar(Runnable acao) {
        this.acaoRedesenhar = (acao != null) ? acao : () -> { };
    }

    public void setAcaoAtualizarStats(Runnable acao) {
        this.acaoAtualizarStats = (acao != null) ? acao : () -> { };
    }

    public void setAcaoMostrarAviso(Consumer<String> acao) {
        this.acaoMostrarAviso = (acao != null) ? acao : mensagem -> { };
    }

    // ------------------------------------------------------------------
    // Formas
    // ------------------------------------------------------------------

    /** Lista somente leitura: para alterar, use adicionarForma/removerForma/setFormas. */
    public List<FormaGeometrica> getFormas() {
        return Collections.unmodifiableList(formas);
    }

    /** Adiciona no fim da lista, ou seja, na camada da frente. */
    public void adicionarForma(FormaGeometrica forma) {
        formas.add(forma);
    }

    public void removerForma(FormaGeometrica forma) {
        formas.remove(forma);
        selecionados.remove(forma);
    }

    /** Troca todas as formas de uma vez (usado por desfazer/refazer e ao carregar arquivo). */
    public void setFormas(List<FormaGeometrica> novasFormas) {
        formas.clear();
        formas.addAll(novasFormas);
        selecionados.clear();
    }

    /**
     * Devolve a forma mais à frente que contém o ponto (x, y), ou null.
     * Percorre de trás para frente porque a última da lista é a camada de cima.
     */
    public FormaGeometrica encontrarFormaEm(double x, double y) {
        for (int i = formas.size() - 1; i >= 0; i--) {
            if (formas.get(i).contemPonto(x, y)) {
                return formas.get(i);
            }
        }
        return null;
    }

    /** Aplica na forma as cores e a espessura escolhidas na barra lateral. */
    public void aplicarEstiloAtual(FormaGeometrica forma) {
        forma.setCorPreenchimentoHex(corPreenchimentoAtual);
        forma.setCorBordaHex(corBordaAtual);
        forma.setEspessuraBorda(espessuraBordaAtual);
    }

    // ------------------------------------------------------------------
    // Seleção
    // ------------------------------------------------------------------

    public List<FormaGeometrica> getSelecionados() {
        return Collections.unmodifiableList(selecionados);
    }

    public void selecionar(FormaGeometrica forma) {
        if (!selecionados.contains(forma)) {
            selecionados.add(forma);
        }
    }

    public void desselecionar(FormaGeometrica forma) {
        selecionados.remove(forma);
    }

    public void limparSelecao() {
        selecionados.clear();
    }

    public boolean isSelecionado(FormaGeometrica forma) {
        return selecionados.contains(forma);
    }

    /**
     * Centro comum das formas selecionadas: média dos centroides de cada uma.
     * É o ponto em torno do qual a seleção é escalada, rotacionada e cisalhada.
     */
    public Ponto getCentroideSelecao() {
        if (selecionados.isEmpty()) {
            throw new IllegalStateException("Não há formas selecionadas.");
        }
        double somaX = 0;
        double somaY = 0;
        for (FormaGeometrica forma : selecionados) {
            Ponto c = forma.getCentroide();
            somaX += c.getX();
            somaY += c.getY();
        }
        return new Ponto(somaX / selecionados.size(), somaY / selecionados.size());
    }

    public void excluirSelecionados() {
        formas.removeAll(selecionados);
        selecionados.clear();
    }

    public Rectangle2D getCaixaSelecao() {
        return caixaSelecao;
    }

    public void setCaixaSelecao(Rectangle2D caixaSelecao) {
        this.caixaSelecao = caixaSelecao;
    }

    // ------------------------------------------------------------------
    // Grade magnética
    // ------------------------------------------------------------------

    /** Com a grade ligada, arredonda o valor para a linha da grade mais próxima. */
    public double aplicarSnap(double valor) {
        if (!gridSnappingHabilitado) {
            return valor;
        }
        return Math.round(valor / TAMANHO_GRADE) * TAMANHO_GRADE;
    }

    public boolean isGridSnappingHabilitado() {
        return gridSnappingHabilitado;
    }

    public void setGridSnappingHabilitado(boolean gridSnappingHabilitado) {
        this.gridSnappingHabilitado = gridSnappingHabilitado;
    }

    // ------------------------------------------------------------------
    // Demais estados
    // ------------------------------------------------------------------

    public FormaGeometrica getFormaEmAndamento() {
        return formaEmAndamento;
    }

    public void setFormaEmAndamento(FormaGeometrica formaEmAndamento) {
        this.formaEmAndamento = formaEmAndamento;
    }

    public String getCorPreenchimentoAtual() {
        return corPreenchimentoAtual;
    }

    public void setCorPreenchimentoAtual(String corPreenchimentoAtual) {
        this.corPreenchimentoAtual = corPreenchimentoAtual;
    }

    public String getCorBordaAtual() {
        return corBordaAtual;
    }

    public void setCorBordaAtual(String corBordaAtual) {
        this.corBordaAtual = corBordaAtual;
    }

    public double getEspessuraBordaAtual() {
        return espessuraBordaAtual;
    }

    public void setEspessuraBordaAtual(double espessuraBordaAtual) {
        this.espessuraBordaAtual = espessuraBordaAtual;
    }
}
