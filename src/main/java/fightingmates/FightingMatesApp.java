package fightingmates;

import fightingmates.controller.GameController;
import fightingmates.view.MainGameView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Arranque principal de la interfaz JavaFX de FightingMates. */
public class FightingMatesApp extends Application {
    private static final String MAZO_DEBUG_AGGRO = "Debug Aggro";
    private static final String MAZO_COMMIT_SEGURO = "Commit Seguro";
    private static final String MAZO_BUG_CONTROL = "Bug Control";
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

        stage.setTitle("FightingMates - Debug Battle");
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        mostrarPantallaInicio(stage, cartas);
        stage.show();
    }

    private void mostrarPantallaInicio(Stage stage, ArrayList<Carta> cartas) {
        Label title = new Label("FightingMates // Debug Battle");
        title.getStyleClass().add("setup-title");

        Label help = new Label("Cómo jugar\n"
                + "• Gana quien reduzca la vida rival a 0.\n"
                + "• En tu turno puedes jugar cartas y actuar con tus unidades.\n"
                + "• Una unidad puede Atacar o Usar habilidad, nunca ambas en el mismo turno.\n"
                + "• Algunos efectos aplican estados: Confundido, Presionado, Bloqueado y Protegido.\n"
                + "• Puedes descartar una carta y robar otra como máximo 1 vez por turno.");
        help.getStyleClass().add("setup-help");

        TextField nombreJ1 = new TextField();
        nombreJ1.setPromptText("Jugador 1");
        TextField nombreJ2 = new TextField();
        nombreJ2.setPromptText("Jugador 2");

        ComboBox<String> mazoJ1 = crearSelectorMazo();
        ComboBox<String> mazoJ2 = crearSelectorMazo();
        mazoJ2.setValue(MAZO_BUG_CONTROL);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setAlignment(Pos.CENTER);
        form.add(new Label("Nombre J1"), 0, 0);
        form.add(nombreJ1, 1, 0);
        form.add(new Label("Mazo J1"), 2, 0);
        form.add(mazoJ1, 3, 0);
        form.add(new Label("Nombre J2"), 0, 1);
        form.add(nombreJ2, 1, 1);
        form.add(new Label("Mazo J2"), 2, 1);
        form.add(mazoJ2, 3, 1);

        Button start = new Button("Iniciar debug battle");
        start.setOnAction(event -> iniciarPartida(stage, cartas, nombreJ1.getText(), nombreJ2.getText(), mazoJ1.getValue(), mazoJ2.getValue()));

        VBox root = new VBox(18, title, help, form, start);
        root.getStyleClass().addAll("app-root", "setup-root", "turn-player-one");
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 1280, 820);
        cargarCss(scene);
        stage.setScene(scene);
    }

    private ComboBox<String> crearSelectorMazo() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(MAZO_DEBUG_AGGRO, MAZO_COMMIT_SEGURO, MAZO_BUG_CONTROL);
        combo.setValue(MAZO_DEBUG_AGGRO);
        combo.setMinWidth(180);
        return combo;
    }

    private void iniciarPartida(Stage stage, ArrayList<Carta> cartas, String nombreJ1, String nombreJ2, String mazoJ1, String mazoJ2) {
        Jugador jugador1 = new Jugador(nombreOdefecto(nombreJ1, "Jugador 1"), Jugador.VIDA_INICIAL, crearMazoPredefinido(cartas, mazoJ1));
        Jugador jugador2 = new Jugador(nombreOdefecto(nombreJ2, "Jugador 2"), Jugador.VIDA_INICIAL, crearMazoPredefinido(cartas, mazoJ2));
        GameController controller = new GameController(new Juego(jugador1, jugador2));
        MainGameView root = new MainGameView(controller);

        Scene scene = new Scene(root, 1280, 820);
        cargarCss(scene);
        stage.setScene(scene);
        controller.iniciarPartida();
    }

    private String nombreOdefecto(String nombre, String defecto) {
        return nombre == null || nombre.trim().isEmpty() ? defecto : nombre.trim();
    }

    private Mazo crearMazoPredefinido(ArrayList<Carta> cartas, String preset) {
        Mazo mazo = new Mazo();
        anadirCartasPreferidas(mazo, cartas, preset);
        for (Carta carta : cartas) {
            if (mazo.estaLleno()) break;
            mazo.anadirCarta(Main.copiarCarta(carta));
        }
        return mazo;
    }

    private void anadirCartasPreferidas(Mazo mazo, ArrayList<Carta> cartas, String preset) {
        for (Carta carta : cartas) {
            if (mazo.estaLleno()) return;
            if (encajaEnPreset(carta, preset)) {
                mazo.anadirCarta(Main.copiarCarta(carta));
            }
        }
    }

    private boolean encajaEnPreset(Carta carta, String preset) {
        String texto = (carta.getNombre() + " " + carta.getDescripcion() + " " + carta.getTipo()).toLowerCase(Locale.ROOT);
        for (Effect efecto : carta.getEfectos()) {
            texto += " " + efecto.getType().toLowerCase(Locale.ROOT) + " " + efecto.getTarget().toLowerCase(Locale.ROOT)
                    + " " + efecto.getTextValue().toLowerCase(Locale.ROOT);
        }

        return switch (preset) {
            case MAZO_COMMIT_SEGURO -> texto.contains("heal") || texto.contains("cura") || texto.contains("defensa")
                    || texto.contains("protegido") || texto.contains("vida");
            case MAZO_BUG_CONTROL -> texto.contains("status") || texto.contains("conf") || texto.contains("pres")
                    || texto.contains("bloq") || texto.contains("estado");
            default -> texto.contains("damage") || texto.contains("danio") || texto.contains("ataque")
                    || texto.contains("daño") || carta instanceof Objeto;
        };
    }

    private void cargarCss(Scene scene) {
        String css = getClass().getResource("/fightingmates/styles/game.css").toExternalForm();
        scene.getStylesheets().add(css);
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
