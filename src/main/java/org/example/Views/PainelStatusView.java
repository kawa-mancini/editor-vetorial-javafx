package org.example.Views;

import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.example.Controller.EditorContext;
import org.example.Models.FormaGeometrica;

/**
 * Barra de status na parte de baixo da janela: total de formas e
 * área/perímetro da seleção. As coordenadas do mouse entram na etapa 9.
 */
public class PainelStatusView extends HBox {

    private final EditorContext contexto;
    private final Label rotuloTotalFormas;
    private final Label rotuloSelecao;

    public PainelStatusView(EditorContext contexto) {
        this.contexto = contexto;
        this.rotuloTotalFormas = new Label();
        this.rotuloSelecao = new Label();

        setSpacing(24);
        setPadding(new Insets(4, 10, 4, 10));
        setStyle("-fx-background-color: #E8E8E8; -fx-border-color: #C0C0C0; -fx-border-width: 1 0 0 0;");
        getChildren().addAll(rotuloTotalFormas, rotuloSelecao);
    }

    /** Lê o estado atual do contexto e reescreve os textos. */
    public void atualizar() {
        rotuloTotalFormas.setText("Formas: " + contexto.getFormas().size());

        List<FormaGeometrica> selecionados = contexto.getSelecionados();
        if (selecionados.isEmpty()) {
            rotuloSelecao.setText("Nenhuma forma selecionada");
            return;
        }
        double area = 0;
        double perimetro = 0;
        for (FormaGeometrica forma : selecionados) {
            area += forma.calcularArea();
            perimetro += forma.calcularPerimetro();
        }
        rotuloSelecao.setText(String.format("Selecionadas: %d  |  Área: %.2f  |  Perímetro: %.2f",
                selecionados.size(), area, perimetro));
    }
}
