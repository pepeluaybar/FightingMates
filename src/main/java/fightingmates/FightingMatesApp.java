package fightingmates;

import fightingmates.controller.GameController;
import fightingmates.view.MainGameView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/** Arranque principal de la interfaz JavaFX de FightingMates. */
public class FightingMatesApp extends Application {
    private static ArrayList<Carta> cartasIniciales;

    public static void main(String[] args) {
        launch(args);
    }

    public static void launchApp(String[] args) {
        launch(args);
    }

    public static void setCartasIniciales(ArrayList<Carta> cartas) {
        cartasIniciales = cartas;
    }

    @Override
    public void start(Stage stage) {
        ArrayList<Carta> cartas = obtenerCartas();
        if (cartas.isEmpty()) {
            throw new IllegalStateException("No se han podido cargar cartas para iniciar FightingMates.");
        }

        Mazo mazo1 = Main.crearMazo(cartas);
        Mazo mazo2 = Main.crearMazo(cartas);
        Jugador jugador1 = new Jugador("Jugador 1", Jugador.VIDA_INICIAL, mazo1);
        Jugador jugador2 = new Jugador("Jugador 2", Jugador.VIDA_INICIAL, mazo2);
        GameController controller = new GameController(new Juego(jugador1, jugador2));
        MainGameView root = new MainGameView(controller);

        Scene scene = new Scene(root, 1280, 820);
        String css = getClass().getResource("/fightingmates/styles/game.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("FightingMates - JavaFX");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        stage.show();
        controller.iniciarPartida();
    }

    private ArrayList<Carta> obtenerCartas() {
        if (cartasIniciales != null && !cartasIniciales.isEmpty()) {
            return cartasIniciales;
        }

        Parameters parameters = getParameters();
        List<String> args = parameters.getRaw();
        String ruta = Main.DEFAULT_CARDS_PATH;
        for (int i = 0; i < args.size(); i++) {
            if ("--cards".equals(args.get(i)) && i + 1 < args.size()) {
                ruta = args.get(i + 1);
            }
        }
        return Main.cargarCartasDesdeJson(ruta);
    }
}
