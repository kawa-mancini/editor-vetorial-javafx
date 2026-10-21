package org.example.Views;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
import org.example.Controller.Ferramenta;
import org.example.Controller.FerramentaDesenhar;
import org.example.Controller.FerramentaEditarVertice;
import org.example.Controller.FerramentaExcluir;
import org.example.Controller.FerramentaInserirForma;
import org.example.Controller.FerramentaSelecionar;
import org.example.Controller.FerramentaTransformar;
import org.example.Controller.ModoFerramenta;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Quadrado;

/**
 * Barra lateral esquerda com os controles do editor (figuras 4 a 10 do enunciado).
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

    // Ligado enquanto atualizarCoresInterface() mexe nos controles, para que essa
    // mudança feita pelo programa não seja confundida com uma escolha do usuário
    private boolean atualizandoInterface;

    public BarraLateralView(EditorContext contexto) {
        this.contexto = contexto;
        this.grupoFerramentas = new ToggleGroup();
        this.atualizandoInterface = false;

        setSpacing(4);
        setPadding(new Insets(8));
        setPrefWidth(LARGURA);
        setStyle("-fx-background-color: #F2F2F2;");

        construirSecaoEstilos();
        construirSecaoFerramentas();
        construirSecaoCamadas();
        construirSecaoSistema();
    }

    /**
     * Mostra nos controles de estilo as cores e a espessura da forma selecionada,
     * para o usuário ver (e poder mudar) o estilo dela.
     */
    public void atualizarCoresInterface() {
        List<FormaGeometrica> selecionados = contexto.getSelecionados();
        if (selecionados.isEmpty()) {
            return;
        }
        FormaGeometrica forma = selecionados.get(0);
        atualizandoInterface = true;
        seletorCorPreenchimento.setValue(Color.web(forma.getCorPreenchimentoHex()));
        seletorCorBorda.setValue(Color.web(forma.getCorBordaHex()));
        sliderEspessura.setValue(forma.getEspessuraBorda());
        atualizandoInterface = false;
    }

    private void construirSecaoEstilos() {
        seletorCorPreenchimento = new ColorPicker(Color.LIGHTGRAY);
        seletorCorPreenchimento.valueProperty().addListener((obs, antiga, nova) -> {
            String hex = paraHex(nova);
            contexto.setCorPreenchimentoAtual(hex);
            aplicarNosSelecionados(forma -> forma.setCorPreenchimentoHex(hex), true);
        });

        seletorCorBorda = new ColorPicker(Color.BLACK);
        seletorCorBorda.valueProperty().addListener((obs, antiga, nova) -> {
            String hex = paraHex(nova);
            contexto.setCorBordaAtual(hex);
            aplicarNosSelecionados(forma -> forma.setCorBordaHex(hex), true);
        });

        // Slider de 1 a 10 mostrando só os números das pontas, como na figura
        sliderEspessura = new Slider(1, 10, 1);
        sliderEspessura.setMajorTickUnit(9);
        sliderEspessura.setShowTickLabels(true);
        sliderEspessura.valueProperty().addListener((obs, antiga, nova) -> {
            double espessura = nova.doubleValue();
            contexto.setEspessuraBordaAtual(espessura);
            // Arrastando o slider o valor muda dezenas de vezes; o histórico é
            // registrado só uma vez, no início do arrasto (listener abaixo)
            aplicarNosSelecionados(forma -> forma.setEspessuraBorda(espessura), !sliderEspessura.isValueChanging());
        });
        sliderEspessura.valueChangingProperty().addListener((obs, antes, comecouArrastar) -> {
            if (comecouArrastar && !atualizandoInterface && !contexto.getSelecionados().isEmpty()) {
                contexto.registrarEstado();
            }
        });

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
        ToggleButton botaoSelecionar = criarBotaoFerramenta("Selecionar", FerramentaSelecionar::new);
        // A ferramenta inicial do EditorContext é a de seleção
        botaoSelecionar.setSelected(true);

        Button botaoExcluirSelecionados = criarBotao("Excluir Selecionados");
        botaoExcluirSelecionados.setOnAction(e -> contexto.excluirSelecionados());

        getChildren().addAll(
                criarTitulo("FERRAMENTAS"),
                criarBotaoFerramenta("Linha Livre", FerramentaDesenhar::new),
                botaoSelecionar,
                // As três transformações são a mesma classe com modos diferentes
                criarBotaoFerramenta("Escalar", () -> new FerramentaTransformar(ModoFerramenta.ESCALAR)),
                criarBotaoFerramenta("Rotacionar", () -> new FerramentaTransformar(ModoFerramenta.ROTACIONAR)),
                criarBotaoFerramenta("Cisalhar", () -> new FerramentaTransformar(ModoFerramenta.CISALHAR)),
                criarBotaoFerramenta("Excluir", FerramentaExcluir::new),
                criarBotaoFerramenta("Editar Vértice", FerramentaEditarVertice::new),
                botaoExcluirSelecionados,

                // Inserir forma também é uma ferramenta (arrasto no canvas),
                // então participa do mesmo grupo de toggle
                criarTitulo("FORMAS"),
                criarBotaoFerramenta("Inserir Quadrado", () -> new FerramentaInserirForma(Quadrado::new)),
                criarBotaoFerramenta("Inserir Círculo", () -> new FerramentaInserirForma(Circulo::new)),
                criarBotaoFerramenta("Inserir Hexágono", () -> new FerramentaInserirForma(Hexagono::new)));
    }

    private void construirSecaoCamadas() {
        Button botaoFrente = criarBotao("Trazer p/ Frente");
        botaoFrente.setOnAction(e -> contexto.trazerParaFrente());

        Button botaoTras = criarBotao("Enviar p/ Trás");
        botaoTras.setOnAction(e -> contexto.enviarParaTras());

        Button botaoAgrupar = criarBotao("Agrupar");
        botaoAgrupar.setOnAction(e -> contexto.agruparSelecionados());

        Button botaoDesagrupar = criarBotao("Desagrupar");
        botaoDesagrupar.setOnAction(e -> contexto.desagruparSelecionados());

        getChildren().addAll(criarTitulo("CAMADAS/GRUPO"), botaoFrente, botaoTras, botaoAgrupar, botaoDesagrupar);
    }

    private void construirSecaoSistema() {
        Button botaoDesfazer = criarBotao("Desfazer");
        botaoDesfazer.setOnAction(e -> contexto.desfazer());

        Button botaoRefazer = criarBotao("Refazer");
        botaoRefazer.setOnAction(e -> contexto.refazer());

        getChildren().addAll(
                criarTitulo("SISTEMA"),
                botaoDesfazer,
                botaoRefazer,
                criarBotao("Salvar"),
                criarBotao("Carregar"));
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /**
     * Aplica uma mudança de estilo nas formas selecionadas, se foi o usuário que mudou.
     * Com registrarHistorico = true, guarda o estado antes (para poder desfazer).
     */
    private void aplicarNosSelecionados(Consumer<FormaGeometrica> alteracao, boolean registrarHistorico) {
        if (atualizandoInterface || contexto.getSelecionados().isEmpty()) {
            return;
        }
        if (registrarHistorico) {
            contexto.registrarEstado();
        }
        for (FormaGeometrica forma : contexto.getSelecionados()) {
            alteracao.accept(forma);
        }
        contexto.requestRedraw();
    }

    /** Converte uma cor do JavaFX para o texto "#RRGGBB" guardado nas formas. */
    private String paraHex(Color cor) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(cor.getRed() * 255),
                (int) Math.round(cor.getGreen() * 255),
                (int) Math.round(cor.getBlue() * 255));
    }

    private Label criarTitulo(String texto) {
        Label titulo = new Label("— " + texto + " —");
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setAlignment(Pos.CENTER);
        titulo.setPadding(new Insets(8, 0, 2, 0));
        titulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");
        return titulo;
    }

    /**
     * Cria um botão de ferramenta. Ao clicar, o contexto recebe uma ferramenta
     * NOVA criada pela fábrica (Supplier). Se a fábrica for null, a ferramenta
     * ainda não foi implementada e o botão fica desabilitado.
     */
    private ToggleButton criarBotaoFerramenta(String texto, Supplier<Ferramenta> fabrica) {
        ToggleButton botao = new ToggleButton(texto);
        botao.setToggleGroup(grupoFerramentas);
        botao.setMaxWidth(Double.MAX_VALUE);
        if (fabrica == null) {
            botao.setDisable(true);
        } else {
            botao.setOnAction(e -> {
                // Clicar de novo no botão ativo não pode deixar o editor sem ferramenta
                botao.setSelected(true);
                contexto.setFerramentaAtual(fabrica.get());
            });
        }
        return botao;
    }

    private Button criarBotao(String texto) {
        Button botao = new Button(texto);
        botao.setMaxWidth(Double.MAX_VALUE);
        return botao;
    }
}
