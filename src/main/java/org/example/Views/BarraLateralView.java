package org.example.Views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.Controller.EditorContext;

/**
 * Barra lateral esquerda com os controles do editor (figuras 4 a 10 do enunciado).
 *
 * Nesta etapa ela só monta os controles na tela. As ações dos botões
 * são ligadas nas próximas etapas, junto com as ferramentas.
 */
public class BarraLateralView extends VBox {

    private static final double LARGURA = 215;

    private final EditorContext contexto;

    // Todos os botões de ferramenta ficam no mesmo grupo: ligar um desliga o anterior
    private final ToggleGroup grupoFerramentas;

    private ColorPicker seletorCorPreenchimento;
    private ColorPicker seletorCorBorda;
    private Slider sliderEspessura;
    private CheckBox checkSnapping;

    public BarraLateralView(EditorContext contexto) {
        this.contexto = contexto;
        this.grupoFerramentas = new ToggleGroup();

        setSpacing(4);
        setPadding(new Insets(8));
        setPrefWidth(LARGURA);
        setStyle("-fx-background-color: #F2F2F2;");

        construirSecaoEstilos();
        construirSecaoFerramentas();
        construirSecaoCamadas();
        construirSecaoSistema();
    }

    private void construirSecaoEstilos() {
        seletorCorPreenchimento = new ColorPicker(Color.LIGHTGRAY);
        seletorCorBorda = new ColorPicker(Color.BLACK);

        // Slider de 1 a 10 mostrando só os números das pontas, como na figura
        sliderEspessura = new Slider(1, 10, 1);
        sliderEspessura.setMajorTickUnit(9);
        sliderEspessura.setShowTickLabels(true);

        checkSnapping = new CheckBox("Grade Magnética (Snapping)");
        checkSnapping.setWrapText(true);
        checkSnapping.setOnAction(e -> {
            contexto.setGridSnappingHabilitado(checkSnapping.isSelected());
            contexto.requestRedraw();
        });

        getChildren().addAll(
                criarTitulo("ESTILOS"),
                new Label("Cor Preenchimento:"), seletorCorPreenchimento,
                new Label("Cor Borda:"), seletorCorBorda,
                new Label("Espessura da Borda:"), sliderEspessura,
                checkSnapping);
    }

    private void construirSecaoFerramentas() {
        getChildren().addAll(
                criarTitulo("FERRAMENTAS"),
                criarBotaoFerramenta("Linha Livre"),
                criarBotaoFerramenta("Selecionar"),
                criarBotaoFerramenta("Escalar"),
                criarBotaoFerramenta("Rotacionar"),
                criarBotaoFerramenta("Cisalhar"),
                criarBotaoFerramenta("Excluir"),
                criarBotaoFerramenta("Editar Vértice"),
                criarBotao("Excluir Selecionados"),

                // Inserir forma também é uma ferramenta (arrasto no canvas),
                // então participa do mesmo grupo de toggle
                criarTitulo("FORMAS"),
                criarBotaoFerramenta("Inserir Quadrado"),
                criarBotaoFerramenta("Inserir Círculo"),
                criarBotaoFerramenta("Inserir Hexágono"));
    }

    private void construirSecaoCamadas() {
        getChildren().addAll(
                criarTitulo("CAMADAS/GRUPO"),
                criarBotao("Trazer p/ Frente"),
                criarBotao("Enviar p/ Trás"),
                criarBotao("Agrupar"),
                criarBotao("Desagrupar"));
    }

    private void construirSecaoSistema() {
        getChildren().addAll(
                criarTitulo("SISTEMA"),
                criarBotao("Desfazer"),
                criarBotao("Refazer"),
                criarBotao("Salvar"),
                criarBotao("Carregar"));
    }

    // ------------------------------------------------------------------
    // Auxiliares para criar controles com a mesma aparência
    // ------------------------------------------------------------------

    private Label criarTitulo(String texto) {
        Label titulo = new Label("— " + texto + " —");
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setAlignment(Pos.CENTER);
        titulo.setPadding(new Insets(8, 0, 2, 0));
        titulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");
        return titulo;
    }

    private ToggleButton criarBotaoFerramenta(String texto) {
        ToggleButton botao = new ToggleButton(texto);
        botao.setToggleGroup(grupoFerramentas);
        botao.setMaxWidth(Double.MAX_VALUE);
        return botao;
    }

    private Button criarBotao(String texto) {
        Button botao = new Button(texto);
        botao.setMaxWidth(Double.MAX_VALUE);
        return botao;
    }
}
