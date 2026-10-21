package org.example.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.example.Models.FormaGeometrica;

/**
 * Guarda o estado do editor: as formas desenhadas, quais estão selecionadas,
 * a forma que está sendo desenhada agora e as opções ligadas.
 *
 * Nesta etapa ainda não tem as ferramentas (padrão State), que entram na etapa 4.
 */
public class EditorContext {

    // A ordem da lista é a ordem das camadas: a última forma fica na frente
    private final List<FormaGeometrica> formas;
    private final List<FormaGeometrica> selecionados;
    private FormaGeometrica formaEmAndamento;
    private boolean gridSnappingHabilitado;

    // O que fazer quando o desenho ou a barra de status precisam ser atualizados.
    // Quem define é a View; assim o Controller não precisa conhecer as classes
    // da View (baixo acoplamento, seção 3.2 do enunciado).
    private Runnable acaoRedesenhar;
    private Runnable acaoAtualizarStats;

    public EditorContext() {
        this.formas = new ArrayList<>();
        this.selecionados = new ArrayList<>();
        this.formaEmAndamento = null;
        this.gridSnappingHabilitado = false;
        this.acaoRedesenhar = () -> { };
        this.acaoAtualizarStats = () -> { };
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

    public void setAcaoRedesenhar(Runnable acao) {
        this.acaoRedesenhar = (acao != null) ? acao : () -> { };
    }

    public void setAcaoAtualizarStats(Runnable acao) {
        this.acaoAtualizarStats = (acao != null) ? acao : () -> { };
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

    public void limparSelecao() {
        selecionados.clear();
    }

    public boolean isSelecionado(FormaGeometrica forma) {
        return selecionados.contains(forma);
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

    public boolean isGridSnappingHabilitado() {
        return gridSnappingHabilitado;
    }

    public void setGridSnappingHabilitado(boolean gridSnappingHabilitado) {
        this.gridSnappingHabilitado = gridSnappingHabilitado;
    }
}
