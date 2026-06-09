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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Vista principal JavaFX. Gestiona selección y renderizado; las reglas viven en el controlador/modelo. */
public class MainGameView extends BorderPane implements GameController.GameView {
    private final GameController controller;
    private final Runnable onSurrender;
    private final Label statusLabel;
    private final Label playerOneLabel;
    private final Label playerTwoLabel;
    private final Label handTitleLabel;
    private final GridPane rivalBoard;
    private final GridPane ownBoard;
    private final HBox handBox;
    private final ScrollPane handScroll;
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
        this(controller, null);
    }

    public MainGameView(GameController controller, Runnable onSurrender) {
        this.controller = controller;
        this.onSurrender = onSurrender;
        this.controller.setView(this);
        this.statusLabel = new Label("Preparando terminal de combate...");
        this.playerOneLabel = new Label();
        this.playerTwoLabel = new Label();
        this.handTitleLabel = new Label("Mano del jugador actual");
        this.rivalBoard = new GridPane();
        this.ownBoard = new GridPane();
        this.handBox = new HBox(14);
        this.handScroll = new ScrollPane(handBox);
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

        rivalBoard.setHgap(14);
        rivalBoard.setVgap(10);
        ownBoard.setHgap(14);
        ownBoard.setVgap(10);
        rivalBoard.setAlignment(Pos.CENTER);
        ownBoard.setAlignment(Pos.CENTER);
        handTitleLabel.getStyleClass().add("hand-title");
        handBox.setAlignment(Pos.CENTER);
        handBox.setFillHeight(false);
        handBox.getStyleClass().add("hand-box");

        handScroll.getStyleClass().add("hand-scroll");
        handScroll.setFitToHeight(true);
        handScroll.setFitToWidth(true);
        handScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        handScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        handScroll.setPannable(true);

        HBox playerInfo = new HBox(24, playerTwoLabel, playerOneLabel);
        playerInfo.setAlignment(Pos.CENTER);
        playerInfo.getStyleClass().add("player-info");

        VBox battlefield = new VBox(6, playerInfo, rivalBoard, ownBoard);
        battlefield.setAlignment(Pos.CENTER);
        battlefield.getStyleClass().add("battlefield");

        VBox center = new VBox(8, battlefield, handTitleLabel, handScroll);
        center.setAlignment(Pos.CENTER);
        center.getStyleClass().add("center-panel");
        setCenter(center);

        TextArea helpArea = new TextArea(textoAyuda());
        helpArea.setEditable(false);
        helpArea.setWrapText(true);
        helpArea.setPrefRowCount(13);
        helpArea.setMinHeight(210);
        helpArea.setMaxHeight(290);
        helpArea.getStyleClass().add("help-panel");

        TitledPane helpPane = new TitledPane("Como jugar", helpArea);
        helpPane.getStyleClass().add("help-pane");
        helpPane.setExpanded(true);
        helpPane.setAnimated(true);

        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(14);
        logArea.setMinHeight(180);
        logArea.setMaxHeight(Double.MAX_VALUE);
        logArea.getStyleClass().add("log-area");

        VBox actionButtons = new VBox(8, startButton, playUnitButton, useObjectButton, attackUnitButton,
                useAbilityButton, discardDrawButton, attackPlayerButton, endTurnButton, surrenderButton);
        actionButtons.getStyleClass().add("action-buttons");
        actionButtons.setFillWidth(true);
        for (Button button : new Button[]{startButton, playUnitButton, useObjectButton, attackUnitButton,
                useAbilityButton, discardDrawButton, attackPlayerButton, endTurnButton, surrenderButton}) {
            button.setMaxWidth(Double.MAX_VALUE);
        }

        Label logTitle = new Label("Registro");
        logTitle.getStyleClass().add("log-title");
        VBox logPanel = new VBox(8, logTitle, logArea);
        logPanel.getStyleClass().add("log-panel");
        VBox.setVgrow(logArea, Priority.ALWAYS);
        VBox.setVgrow(logPanel, Priority.ALWAYS);

        VBox actions = new VBox(12, helpPane, actionButtons, logPanel);
        actions.getStyleClass().add("actions-panel");
        actions.setPrefWidth(300);
        actions.setMinWidth(280);
        actions.setMaxWidth(320);
        actions.setFillWidth(true);
        setRight(actions);
    }

    private String textoAyuda() {
        return """
                // OBJETIVO
                Gana quien baje la vida del jugador rival a 0.

                // TURNO
                En tu turno puedes jugar cartas, atacar, usar habilidades o descartar y robar.
                Cuando termines, pulsa Terminar turno.

                // CAMPO
                Cada jugador tiene hasta 3 huecos para unidades.
                Solo puedes jugar una unidad si tienes un hueco libre.

                // MANO, MAZO Y DESCARTE
                Mano: cartas disponibles.
                Mazo: cartas pendientes de robar.
                Descarte: cartas usadas o descartadas.

                // ATAQUES
                Puedes atacar a una unidad rival si hay objetivo valido.
                Tambien puede existir la opcion de atacar directamente al jugador rival.

                // HABILIDADES
                Una carta puede atacar o usar habilidad en el turno, nunca ambas cosas.
                Si usa habilidad, ya no puede atacar en ese turno.
                Si ataca, ya no puede usar habilidad en ese turno.

                // ESTADOS
                CONF: Confundido, no puede atacar durante 1 turno.
                PRES: Presionado, hace menos dano en su proximo ataque.
                LOCK: Bloqueado, no puede usar habilidad durante 1 turno.
                SHIELD: Protegido, reduce el proximo dano recibido.

                // DESCARTAR Y ROBAR
                Puedes descartar una carta de la mano y robar una nueva.
                Solo puede hacerse 1 vez por turno.
                Si el mazo esta vacio, el registro mostrara aviso.

                // ACCIONES NO PERMITIDAS
                Si un boton esta desactivado, esa accion no se puede hacer ahora.
                Si intentas una accion invalida, el registro explica el motivo.
                """;
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
        surrenderButton.setOnAction(event -> {
            controller.rendirse();
            limpiarSeleccion();
            if (onSurrender != null) {
                onSurrender.run();
            } else if (currentGame != null) {
                render(currentGame);
            }
        });
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
            registrarMensaje(mensaje);
        }
    }

    private void registrarMensaje(String mensaje) {
        String linea = etiquetaLog(mensaje) + " " + mensaje;
        logArea.appendText(linea + System.lineSeparator());
        logArea.positionCaret(logArea.getText().length());
    }

    private String etiquetaLog(String mensaje) {
        String texto = mensaje.toLowerCase();

        if (texto.contains("no puedes") || texto.contains("no puede") || texto.contains("no se pudo")
                || texto.contains("selecciona") || texto.contains("primero inicia")
                || texto.contains("límite") || texto.contains("limite") || texto.contains("ya se us")) {
            return "[ERROR]";
        }
        if (texto.contains("turno") || texto.contains("partida iniciada")) {
            return "[TURN]";
        }
        if (texto.contains("descart") || texto.contains("rob")) {
            return "[DRAW]";
        }
        if (texto.contains("presionado") || texto.contains("confundido") || texto.contains("bloqueado")
                || texto.contains("protegido") || texto.contains("estado")) {
            return "[STATUS]";
        }
        if (texto.contains("atac")) {
            return "[DMG]";
        }
        if (texto.contains("jugó") || texto.contains("jugo") || texto.contains("usó") || texto.contains("uso")) {
            return "[PLAY]";
        }
        if (texto.contains("gana") || texto.contains("rindi")) {
            return "[END]";
        }

        return "[INFO]";
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
            setPrefSize(180, 266);

            Label title = new Label((aliado ? "Propio" : "Rival") + " " + (posicion + 1));
            title.getStyleClass().add("slot-title");
            getChildren().add(title);

            if (unidad == null) {
                Label empty = new Label("Hueco vacío");
                empty.getStyleClass().add("empty-slot");
                getChildren().add(empty);
            } else {
                CardView card = new CardView(unidad);
                card.usarTamanoCompacto();
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
