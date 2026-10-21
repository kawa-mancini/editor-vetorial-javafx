package org.example.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.example.Models.FormaGeometrica;

/**
 * Padrão Memento: guarda "fotografias" da lista de formas para desfazer e refazer.
 *
 * - A fotografia (o memento) é uma CÓPIA PROFUNDA da lista: cada forma é clonada
 *   com clonar() (Prototype). Se guardássemos as mesmas formas, mexer no desenho
 *   também mudaria o histórico.
 * - Esta classe é a "zeladora" (caretaker): só empilha e desempilha as fotos,
 *   sem olhar o que tem dentro.
 * - Quem cria e restaura as fotos é o EditorContext (o "originador").
 */
public class HistoricoController {

    // Para não gastar memória sem fim, guarda só os últimos estados
    private static final int LIMITE_ESTADOS = 100;

    private final Stack<List<FormaGeometrica>> undoStack;
    private final Stack<List<FormaGeometrica>> redoStack;

    public HistoricoController() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    /**
     * Chamado ANTES de cada alteração: guarda como o desenho estava.
     * Uma alteração nova invalida o que podia ser refeito.
     */
    public void registrarEstado(List<FormaGeometrica> lista) {
        undoStack.push(copiaProfunda(lista));
        if (undoStack.size() > LIMITE_ESTADOS) {
            undoStack.remove(0); // descarta o estado mais antigo (fundo da pilha)
        }
        redoStack.clear();
    }

    /**
     * Volta um passo. O estado atual vai para a pilha de refazer.
     * Se não houver o que desfazer, devolve a própria lista atual.
     */
    public List<FormaGeometrica> desfazer(List<FormaGeometrica> atual) {
        if (undoStack.isEmpty()) {
            return atual;
        }
        redoStack.push(copiaProfunda(atual));
        return undoStack.pop();
    }

    /** Avança um passo (o contrário de desfazer). */
    public List<FormaGeometrica> refazer(List<FormaGeometrica> atual) {
        if (redoStack.isEmpty()) {
            return atual;
        }
        undoStack.push(copiaProfunda(atual));
        return redoStack.pop();
    }

    public boolean podeDesfazer() {
        return !undoStack.isEmpty();
    }

    public boolean podeRefazer() {
        return !redoStack.isEmpty();
    }

    /** Esquece todo o histórico (ex.: ao carregar outro arquivo). */
    public void limpar() {
        undoStack.clear();
        redoStack.clear();
    }

    /** Clona cada forma da lista (Prototype); nenhum objeto é compartilhado. */
    private List<FormaGeometrica> copiaProfunda(List<FormaGeometrica> lista) {
        List<FormaGeometrica> copia = new ArrayList<>();
        for (FormaGeometrica forma : lista) {
            copia.add(forma.clonar());
        }
        return copia;
    }
}
