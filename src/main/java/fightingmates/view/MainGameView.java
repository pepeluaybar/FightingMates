package fightingmates.view;

import fightingmates.Carta;
import fightingmates.Juego;
import fightingmates.Jugador;
import fightingmates.Objeto;
import fightingmates.Tablero;
import fightingmates.Unidad;
import fightingmates.controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Vista principal JavaFX. Gestiona selección y renderizado; las reglas viven en el controlador/modelo. */
public class MainGameView extends BorderPane implements GameController.GameView {
    private final GameController controller;
    private final Label statusLabel;
    private final Label playerOneLabel;
    private final Label playerTwoLabel;
    private final Label handTitleLabel;
    private final GridPane rivalBoard;
    private final GridPane ownBoard;
    private final HBox handBox;
    private final TextArea logArea;
    private final Button startButton;
    private final Button playUnitButton;
    private final Button useObjectButton;
    private final Button attackUnitButton;
    private final Button useAbilityButton;
    private final Button discardDrawButton;
    private final Button attackPlayerButton;
    private final Button endTurnButton;
    private final Button surrenderButton;

    private Integer selectedHandIndex;
    private Integer selectedOwnPosition;
    private Integer selectedEnemyPosition;
    private Integer selectedTargetPosition;
    private boolean selectedTargetAlly;
    private Juego currentGame;

    public MainGameView(GameController controller) {
        this.controller = controller;
        this.controller.setView(this);
        this.statusLabel = new Label("Preparando terminal de combate...");
        this.playerOneLabel = new Label();
        this.playerTwoLabel = new Label();
        this.handTitleLabel = new Label("Mano del jugador actual");
        this.rivalBoard = new GridPane();
        this.ownBoard = new GridPane();
        this.handBox = new HBox(10);
        this.logArea = new TextArea();
        this.startButton = new Button("Iniciar partida");
        this.playUnitButton = new Button("Jugar unidad");
        this.useObjectButton = new Button("Usar objeto");
        this.attackUnitButton = new Button("Atacar unidad");
        this.useAbilityButton = new Button("Usar habilidad");
        this.discardDrawButton = new Button("Descartar y robar");
        this.attackPlayerButton = new Button("Atacar jugador");
        this.endTurnButton = new Button("Terminar turno");
        this.surrenderButton = new Button("Rendirse");
        configurarLayout();
        configurarAcciones();
        refresh(controller.getJuego(), "Interfaz JavaFX preparada.");
    }

    private void configurarLayout() {
        getStyleClass().add("app-root");
        setPadding(new Insets(16));

        statusLabel.getStyleClass().add("status-label");
        setTop(statusLabel);

        rivalBoard.setHgap(12);
        ownBoard.setHgap(12);
        rivalBoard.setAlignment(Pos.CENTER);
        ownBoard.setAlignment(Pos.CENTER);
        handTitleLabel.getStyleClass().add("hand-title");

        VBox center = new VBox(12, playerTwoLabel, rivalBoard, handTitleLabel, handBox, ownBoard, playerOneLabel);
        center.setAlignment(Pos.CENTER);
        center.getStyleClass().add("center-panel");
        setCenter(center);

        Label help = new Label("Cómo jugar\n"
                + "// Baja la vida rival a 0.\n"
                + "// Una unidad: atacar O habilidad.\n"
                + "// Estados: ?, !, 🔒, 🛡.\n"
                + "// Descartar y robar: 1 vez/turno.");
        help.getStyleClass().add("help-panel");

        VBox actions = new VBox(10, help, startButton, playUnitButton, useObjectButton, attackUnitButton,
                useAbilityButton, discardDrawButton, attackPlayerButton, endTurnButton, surrenderButton);
        actions.getStyleClass().add("actions-panel");
        actions.setPrefWidth(245);
        setRight(actions);

        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(7);
        logArea.getStyleClass().add("log-area");
        setBottom(logArea);
        BorderPane.setMargin(logArea, new Insets(12, 0, 0, 0));
    }

    private void configurarAcciones() {
        startButton.setOnAction(event -> ejecutar(controller::iniciarPartida));
        playUnitButton.setOnAction(event -> ejecutar(() -> controller.jugarUnidad(indiceMano(), posicionPropia())));
        useObjectButton.setOnAction(event -> ejecutar(() -> controller.usarObjeto(indiceMano(), posicionObjetivo(), selectedTargetAlly)));
        attackUnitButton.setOnAction(event -> ejecutar(() -> controller.atacar(posicionPropia(), posicionEnemiga())));
        useAbilityButton.setOnAction(event -> ejecutar(() -> controller.usarHabilidad(posicionPropia(), posicionObjetivo(), selectedTargetAlly)));
        discardDrawButton.setOnAction(event -> ejecutar(() -> controller.descartarYRobar(indiceMano())));
        attackPlayerButton.setOnAction(event -> ejecutar(() -> controller.atacarJugador(posicionPropia())));
        endTurnButton.setOnAction(event -> {
            limpiarSeleccion();
            controller.finalizarTurno();
        });
        surrenderButton.setOnAction(event -> ejecutar(controller::rendirse));
    }

    private void ejecutar(Accion accion) {
        accion.ejecutar();
        limpiarSeleccion();
        if (currentGame != null) {
            render(currentGame);
        }
    }

    @Override
    public void refresh(Juego juego, String mensaje) {
        this.currentGame = juego;
        render(juego);
        if (mensaje != null && !mensaje.isBlank()) {
            logArea.appendText("// " + mensaje + System.lineSeparator());
        }
    }

    private void render(Juego juego) {
        Jugador actual = juego.getJugadorActual();
        Jugador rival = juego.getJugadorRival(actual);
        aplicarTemaTurno(juego, actual);

        statusLabel.setText("> TURNO DE " + actual.getNombre().toUpperCase()
                + "  |  Mano " + actual.getNumCartasMano() + "/" + Jugador.MANO_MAXIMA
                + "  |  Mazo " + actual.getMazo().getNumCartas()
                + "  |  Descarte " + actual.getNumCartasDescarte());

        playerOneLabel.setText(textoJugador(actual, "Jugador actual"));
        playerTwoLabel.setText(textoJugador(rival, "Rival"));
        handTitleLabel.setText("Mano visible: " + actual.getNombre());
        renderBoard(ownBoard, actual, true);
        renderBoard(rivalBoard, rival, false);
        renderHand(actual);
        actualizarBotones(actual);
    }

    private void aplicarTemaTurno(Juego juego, Jugador actual) {
        getStyleClass().removeAll("turn-player-one", "turn-player-two");
        getStyleClass().add(actual == juego.getJugador1() ? "turn-player-one" : "turn-player-two");
    }

    private String textoJugador(Jugador jugador, String rol) {
        return rol + ": " + jugador.getNombre() + " · Vida " + jugador.getVida() + "/" + Jugador.VIDA_MAXIMA
                + " · Mano " + jugador.getNumCartasMano()
                + " · Mazo " + jugador.getMazo().getNumCartas()
                + " · Descarte " + jugador.getNumCartasDescarte()
                + " · Unidades " + jugador.getTablero().obtenerUnidadesVivas(jugador).length + "/" + Tablero.TAMANIO_CAMPO;
    }

    private void renderBoard(GridPane board, Jugador jugador, boolean aliado) {
        board.getChildren().clear();
        for (int pos = 0; pos < Tablero.TAMANIO_CAMPO; pos++) {
            Unidad unidad = currentGame.getTablero().obtenerUnidad(jugador, pos);
            BoardSlotView slot = new BoardSlotView(unidad, pos, aliado);
            int posicion = pos;
            slot.setOnMouseClicked(event -> seleccionarTablero(posicion, aliado));
            board.add(slot, pos, 0);
        }
    }

    private void renderHand(Jugador actual) {
        handBox.getChildren().clear();
        handBox.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < actual.getNumCartasMano(); i++) {
            Carta carta = actual.obtenerCartaMano(i);
            CardView cardView = new CardView(carta);
            cardView.setSelected(selectedHandIndex != null && selectedHandIndex == i);
            int indice = i;
            cardView.setOnMouseClicked(event -> seleccionarMano(indice));
            HBox.setHgrow(cardView, Priority.NEVER);
            handBox.getChildren().add(cardView);
        }
        if (actual.getNumCartasMano() == 0) {
            Label empty = new Label("No hay cartas en mano.");
            empty.getStyleClass().add("empty-label");
            handBox.getChildren().add(empty);
        }
    }

    private void seleccionarMano(int indice) {
        selectedHandIndex = selectedHandIndex != null && selectedHandIndex == indice ? null : indice;
        render(currentGame);
    }

    private void seleccionarTablero(int posicion, boolean aliado) {
        if (aliado) {
            if (selectedOwnPosition != null && selectedOwnPosition != posicion) {
                selectedTargetPosition = posicion;
                selectedTargetAlly = true;
            } else {
                selectedOwnPosition = selectedOwnPosition != null && selectedOwnPosition == posicion ? null : posicion;
                selectedTargetPosition = selectedOwnPosition;
                selectedTargetAlly = true;
            }
        } else {
            selectedEnemyPosition = selectedEnemyPosition != null && selectedEnemyPosition == posicion ? null : posicion;
            selectedTargetPosition = selectedEnemyPosition;
            selectedTargetAlly = false;
        }
        render(currentGame);
    }

    private void actualizarBotones(Jugador actual) {
        boolean iniciada = controller.isPartidaIniciada();
        boolean terminada = controller.isPartidaTerminada();
        Carta carta = selectedHandIndex == null ? null : actual.obtenerCartaMano(selectedHandIndex);
        Unidad unidadSeleccionada = selectedOwnPosition == null ? null
                : currentGame.getTablero().obtenerUnidad(actual, selectedOwnPosition);
        boolean unidadPuedeActuar = unidadSeleccionada != null && unidadSeleccionada.esActiva();

        startButton.setDisable(iniciada);
        playUnitButton.setDisable(!iniciada || terminada || !(carta instanceof Unidad) || selectedOwnPosition == null);
        useObjectButton.setDisable(!iniciada || terminada || !(carta instanceof Objeto) || selectedTargetPosition == null);
        attackUnitButton.setDisable(!iniciada || terminada || !unidadPuedeActuar || selectedEnemyPosition == null
                || !unidadSeleccionada.puedeAtacar());
        useAbilityButton.setDisable(!iniciada || terminada || !unidadPuedeActuar || unidadSeleccionada.getHabilidad() == null
                || selectedTargetPosition == null || !unidadSeleccionada.puedeUsarHabilidad());
        discardDrawButton.setDisable(!iniciada || terminada || carta == null || actual.haUsadoDescarteRoboEsteTurno());
        attackPlayerButton.setDisable(!iniciada || terminada || !unidadPuedeActuar || !unidadSeleccionada.puedeAtacar());
        endTurnButton.setDisable(!iniciada || terminada);
        surrenderButton.setDisable(!iniciada || terminada);
    }

    private int indiceMano() { return selectedHandIndex == null ? -1 : selectedHandIndex; }
    private int posicionPropia() { return selectedOwnPosition == null ? -1 : selectedOwnPosition; }
    private int posicionEnemiga() { return selectedEnemyPosition == null ? -1 : selectedEnemyPosition; }
    private int posicionObjetivo() { return selectedTargetPosition == null ? -1 : selectedTargetPosition; }

    private void limpiarSeleccion() {
        selectedHandIndex = null;
        selectedOwnPosition = null;
        selectedEnemyPosition = null;
        selectedTargetPosition = null;
        selectedTargetAlly = false;
    }

    @FunctionalInterface
    private interface Accion { void ejecutar(); }

    private final class BoardSlotView extends VBox {
        private BoardSlotView(Unidad unidad, int posicion, boolean aliado) {
            getStyleClass().add("board-slot");
            setAlignment(Pos.CENTER);
            setSpacing(6);
            setPrefSize(190, 275);

            Label title = new Label((aliado ? "Propio" : "Rival") + " " + (posicion + 1));
            title.getStyleClass().add("slot-title");
            getChildren().add(title);

            if (unidad == null) {
                Label empty = new Label("Hueco vacío");
                empty.getStyleClass().add("empty-slot");
                getChildren().add(empty);
            } else {
                CardView card = new CardView(unidad);
                card.setSelected(aliado && selectedOwnPosition != null && selectedOwnPosition == posicion);
                card.setTargetSelected(selectedTargetPosition != null && selectedTargetPosition == posicion
                        && selectedTargetAlly == aliado && !(aliado && selectedOwnPosition != null && selectedOwnPosition == posicion));
                getChildren().add(card);
            }

            boolean seleccionado = aliado
                    ? (selectedOwnPosition != null && selectedOwnPosition == posicion)
                    || (selectedTargetAlly && selectedTargetPosition != null && selectedTargetPosition == posicion)
                    : selectedEnemyPosition != null && selectedEnemyPosition == posicion;
            if (seleccionado) {
                getStyleClass().add(aliado ? "selected" : "target-selected");
            }
        }
    }
}
