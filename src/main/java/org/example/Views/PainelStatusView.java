package org.example.Views;

import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.example.Controller.EditorContext;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

/**
 * Barra de status na parte de baixo da janela: coordenadas do mouse,
 * total de formas e área/perímetro da seleção, atualizados em tempo real.
 */
public class PainelStatusView extends HBox {

    private final EditorContext contexto;
    private final Label rotuloCoordenadas;
    private final Label rotuloTotalFormas;
    private final Label rotuloSelecao;

    public PainelStatusView(EditorContext contexto) {
        this.contexto = contexto;
        this.rotuloCoordenadas = new Label();
        this.rotuloTotalFormas = new Label();
        this.rotuloSelecao = new Label();

        // Largura fixa para o texto não "pular" enquanto o mouse se move
        rotuloCoordenadas.setMinWidth(170);

        setSpacing(24);
        setPadding(new Insets(4, 10, 4, 10));
        setStyle("-fx-background-color: #E8E8E8; -fx-border-color: #C0C0C0; -fx-border-width: 1 0 0 0;");
        getChildren().addAll(rotuloCoordenadas, rotuloTotalFormas, rotuloSelecao);
    }

    /** Lê o estado atual do contexto e reescreve os textos. */
    public void atualizar() {
        Ponto mouse = contexto.getPosicaoMouse();
        if (mouse == null) {
            rotuloCoordenadas.setText("X: —  Y: —");
        } else if (contexto.isGridSnappingHabilitado()) {
            // Com a grade ligada mostra onde o ponto realmente vai cair
            rotuloCoordenadas.setText(String.format("X: %.0f  Y: %.0f  (grade)",
                    contexto.aplicarSnap(mouse.getX()), contexto.aplicarSnap(mouse.getY())));
        } else {
            rotuloCoordenadas.setText(String.format("X: %.0f  Y: %.0f", mouse.getX(), mouse.getY()));
        }

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
