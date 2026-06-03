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

/** Vista principal JavaFX. Solo gestiona selección, renderizado y delega reglas al controlador. */
public class MainGameView extends BorderPane implements GameController.GameView {
    private final GameController controller;
    private final Label statusLabel;
    private final Label playerOneLabel;
    private final Label playerTwoLabel;
    private final GridPane rivalBoard;
    private final GridPane ownBoard;
    private final HBox handBox;
    private final TextArea logArea;
    private final Button startButton;
    private final Button playUnitButton;
    private final Button useObjectButton;
    private final Button attackUnitButton;
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
        this.statusLabel = new Label("Pulsa Iniciar partida para comenzar.");
        this.playerOneLabel = new Label();
        this.playerTwoLabel = new Label();
        this.rivalBoard = new GridPane();
        this.ownBoard = new GridPane();
        this.handBox = new HBox(10);
        this.logArea = new TextArea();
        this.startButton = new Button("Iniciar partida");
        this.playUnitButton = new Button("Jugar carta/unidad");
        this.useObjectButton = new Button("Usar objeto");
        this.attackUnitButton = new Button("Atacar unidad");
        this.attackPlayerButton = new Button("Atacar jugador");
        this.endTurnButton = new Button("Finalizar turno");
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

        VBox center = new VBox(14,
                playerTwoLabel,
                rivalBoard,
                new Label("Mano del jugador actual"),
                handBox,
                ownBoard,
                playerOneLabel);
        center.setAlignment(Pos.CENTER);
        center.getStyleClass().add("center-panel");
        setCenter(center);

        VBox actions = new VBox(10, startButton, playUnitButton, useObjectButton, attackUnitButton,
                attackPlayerButton, endTurnButton, surrenderButton);
        actions.getStyleClass().add("actions-panel");
        actions.setPrefWidth(220);
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
            logArea.appendText("• " + mensaje + System.lineSeparator());
        }
    }

    private void render(Juego juego) {
        Jugador actual = juego.getJugadorActual();
        Jugador rival = juego.getJugadorRival(actual);
        statusLabel.setText("Turno " + (juego.getTurnosJugados() + 1)
                + " · Jugador actual: " + actual.getNombre()
                + " · Mano: " + actual.getNumCartasMano() + "/" + Jugador.MANO_MAXIMA
                + " · Mazo: " + actual.getMazo().getNumCartas());

        playerOneLabel.setText(textoJugador(actual, "Jugador actual"));
        playerTwoLabel.setText(textoJugador(rival, "Rival"));
        renderBoard(ownBoard, actual, true);
        renderBoard(rivalBoard, rival, false);
        renderHand(actual);
        actualizarBotones(actual);
    }

    private String textoJugador(Jugador jugador, String rol) {
        return rol + ": " + jugador.getNombre() + " · Vida " + jugador.getVida() + "/" + Jugador.VIDA_MAXIMA
                + " · Mano " + jugador.getNumCartasMano()
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
            selectedOwnPosition = selectedOwnPosition != null && selectedOwnPosition == posicion ? null : posicion;
            selectedTargetPosition = selectedOwnPosition;
            selectedTargetAlly = true;
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

        startButton.setDisable(iniciada);
        playUnitButton.setDisable(!iniciada || terminada || !(carta instanceof Unidad) || selectedOwnPosition == null);
        useObjectButton.setDisable(!iniciada || terminada || !(carta instanceof Objeto) || selectedTargetPosition == null);
        attackUnitButton.setDisable(!iniciada || terminada || selectedOwnPosition == null || selectedEnemyPosition == null);
        attackPlayerButton.setDisable(!iniciada || terminada || selectedOwnPosition == null);
        endTurnButton.setDisable(!iniciada || terminada);
        surrenderButton.setDisable(!iniciada || terminada);
    }

    private int indiceMano() {
        return selectedHandIndex == null ? -1 : selectedHandIndex;
    }

    private int posicionPropia() {
        return selectedOwnPosition == null ? -1 : selectedOwnPosition;
    }

    private int posicionEnemiga() {
        return selectedEnemyPosition == null ? -1 : selectedEnemyPosition;
    }

    private int posicionObjetivo() {
        return selectedTargetPosition == null ? -1 : selectedTargetPosition;
    }

    private void limpiarSeleccion() {
        selectedHandIndex = null;
        selectedOwnPosition = null;
        selectedEnemyPosition = null;
        selectedTargetPosition = null;
        selectedTargetAlly = false;
    }

    @FunctionalInterface
    private interface Accion {
        void ejecutar();
    }

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
                card.setTargetSelected(!aliado && selectedEnemyPosition != null && selectedEnemyPosition == posicion);
                getChildren().add(card);
            }

            boolean seleccionado = aliado
                    ? selectedOwnPosition != null && selectedOwnPosition == posicion
                    : selectedEnemyPosition != null && selectedEnemyPosition == posicion;
            if (seleccionado) {
                getStyleClass().add(aliado ? "selected" : "target-selected");
            }
        }
    }
}
