package org.example.Views;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Janela principal do editor vetorial.
 *
 * Por enquanto (etapa 1) só abre uma janela vazia. Nas próximas etapas ela vai
 * receber o canvas no centro, a barra lateral à esquerda e a barra de status embaixo.
 */
public class ContainerApp extends Application {

    private static final double LARGURA_INICIAL = 1200;
    private static final double ALTURA_INICIAL = 800;

    @Override
    public void start(Stage palco) {
        // BorderPane divide a tela em regiões (left, center, bottom...),
        // que é exatamente o layout pedido no enunciado
        BorderPane raiz = new BorderPane();

        Scene cena = new Scene(raiz, LARGURA_INICIAL, ALTURA_INICIAL);
        palco.setTitle("Editor de Desenho Vetorial");
        palco.setScene(cena);
        palco.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
