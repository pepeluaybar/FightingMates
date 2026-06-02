package fightingmates.viewfx;

import fightingmates.Carta;
import fightingmates.Juego;
import fightingmates.Jugador;
import fightingmates.Mazo;
import fightingmates.Objeto;
import fightingmates.Unidad;
import fightingmates.controller.GameController;
import fightingmates.controller.GameState;
import fightingmates.controller.GameView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Primer prototipo JavaFX de FightingMates con una escena única y estilos CSS.
 */
public class FightingMatesFxApp extends Application implements GameView {
    private final Label statusLabel = new Label();
    private final Label currentPlayerLabel = new Label();
    private final Label rivalPlayerLabel = new Label();
    private final ListView<GameState.CardState> handList = new ListView<>();
    private final ListView<String> logList = new ListView<>();
    private final GridPane currentBoard = new GridPane();
    private final GridPane rivalBoard = new GridPane();
    private final ComboBox<Integer> currentPositionSelector = new ComboBox<>();
    private final ComboBox<Integer> rivalPositionSelector = new ComboBox<>();

    private GameController controller;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Juego juego = createDemoGame();
        juego.iniciarPartida();
        controller = new GameController(juego);

        Scene scene = new Scene(createLayout(), 1180, 720);
        String stylesheet = getClass().getResource("/styles/fightingmates.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);

        stage.setTitle("FightingMates - Prototipo JavaFX");
        stage.setScene(scene);
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        controller.setView(this);
        stage.show();
    }

    @Override
    public void refresh(GameState state) {
        Runnable update = () -> render(state);
        if (Platform.isFxApplicationThread()) {
            update.run();
        } else {
            Platform.runLater(update);
        }
    }

    private BorderPane createLayout() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        root.setTop(createHeader());
        root.setCenter(createBoardArea());
        root.setBottom(createActionBar());
        root.setRight(createSidePanel());

        return root;
    }

    private VBox createHeader() {
        Label title = new Label("FightingMates");
        title.getStyleClass().add("app-title");

        statusLabel.getStyleClass().add("status-label");

        VBox header = new VBox(6, title, statusLabel);
        header.getStyleClass().add("header");
        return header;
    }

    private VBox createBoardArea() {
        currentBoard.setHgap(12);
        currentBoard.setVgap(12);
        rivalBoard.setHgap(12);
        rivalBoard.setVgap(12);

        VBox rivalSection = createBoardSection("Campo rival", rivalPlayerLabel, rivalBoard);
        VBox currentSection = createBoardSection("Tu campo", currentPlayerLabel, currentBoard);

        VBox boardArea = new VBox(18, rivalSection, currentSection);
        boardArea.getStyleClass().add("board-area");
        VBox.setVgrow(rivalSection, Priority.ALWAYS);
        VBox.setVgrow(currentSection, Priority.ALWAYS);
        return boardArea;
    }

    private VBox createBoardSection(String title, Label playerLabel, GridPane board) {
        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().add("section-title");
        playerLabel.getStyleClass().add("player-summary");

        VBox section = new VBox(10, sectionTitle, playerLabel, board);
        section.getStyleClass().add("board-section");
        VBox.setVgrow(board, Priority.ALWAYS);
        return section;
    }

    private VBox createSidePanel() {
        Label handTitle = new Label("Mano");
        handTitle.getStyleClass().add("section-title");
        configureHandList();

        Label logTitle = new Label("Registro");
        logTitle.getStyleClass().add("section-title");
        logList.getStyleClass().add("log-list");

        VBox side = new VBox(12, handTitle, handList, logTitle, logList);
        side.getStyleClass().add("side-panel");
        VBox.setVgrow(handList, Priority.ALWAYS);
        VBox.setVgrow(logList, Priority.ALWAYS);
        return side;
    }

    private HBox createActionBar() {
        currentPositionSelector.setItems(FXCollections.observableArrayList(0, 1, 2));
        rivalPositionSelector.setItems(FXCollections.observableArrayList(0, 1, 2));
        currentPositionSelector.getSelectionModel().selectFirst();
        rivalPositionSelector.getSelectionModel().selectFirst();

        Button playUnitButton = createActionButton("Jugar unidad", () -> controller.playUnit(selectedHandIndex(), selectedCurrentPosition()));
        Button useObjectOnCurrentButton = createActionButton("Objeto aliado", () -> controller.useObject(
                selectedHandIndex(),
                GameController.BoardSide.CURRENT_PLAYER,
                selectedCurrentPosition()
        ));
        Button useObjectOnRivalButton = createActionButton("Objeto rival", () -> controller.useObject(
                selectedHandIndex(),
                GameController.BoardSide.RIVAL_PLAYER,
                selectedRivalPosition()
        ));
        Button attackButton = createActionButton("Atacar", () -> controller.attack(selectedCurrentPosition(), selectedRivalPosition()));
        Button endTurnButton = createActionButton("Finalizar turno", () -> controller.endTurn());
        endTurnButton.getStyleClass().add("secondary-action");

        Label ownPositionLabel = new Label("Posición propia");
        Label rivalPositionLabel = new Label("Posición rival");
        ownPositionLabel.getStyleClass().add("field-label");
        rivalPositionLabel.getStyleClass().add("field-label");

        HBox actionBar = new HBox(
                10,
                ownPositionLabel,
                currentPositionSelector,
                rivalPositionLabel,
                rivalPositionSelector,
                spacer(),
                playUnitButton,
                useObjectOnCurrentButton,
                useObjectOnRivalButton,
                attackButton,
                endTurnButton
        );
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.getStyleClass().add("action-bar");
        return actionBar;
    }

    private Button createActionButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-action");
        button.setOnAction(event -> action.run());
        return button;
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private void configureHandList() {
        handList.getStyleClass().add("hand-list");
        handList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(GameState.CardState card, boolean empty) {
                super.updateItem(card, empty);
                if (empty || card == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label name = new Label(card.getName());
                name.getStyleClass().add("card-name");
                Label meta = new Label("#" + card.getHandIndex() + " · " + card.getType());
                meta.getStyleClass().add("card-meta");
                Label description = new Label(card.getDescription());
                description.getStyleClass().add("card-description");
                description.setWrapText(true);

                VBox content = new VBox(4, name, meta, description);
                content.setPadding(new Insets(4));
                setGraphic(content);
            }
        });
    }

    private void render(GameState state) {
        statusLabel.setText(formatStatus(state));
        currentPlayerLabel.setText(formatPlayer(state.getCurrentPlayer()));
        rivalPlayerLabel.setText(formatPlayer(state.getRivalPlayer()));
        handList.setItems(FXCollections.observableArrayList(state.getHand()));
        logList.setItems(FXCollections.observableArrayList(state.getLogMessages()));
        renderBoard(currentBoard, state.getCurrentBoard(), false);
        renderBoard(rivalBoard, state.getRivalBoard(), true);
    }

    private void renderBoard(GridPane board, List<GameState.UnitState> units, boolean rival) {
        board.getChildren().clear();
        for (GameState.UnitState unit : units) {
            VBox card = createUnitCard(unit, rival);
            board.add(card, unit.getPosition(), 0);
        }
    }

    private VBox createUnitCard(GameState.UnitState unit, boolean rival) {
        Label position = new Label("Pos " + unit.getPosition());
        position.getStyleClass().add("unit-position");

        Label name = new Label(unit.getName());
        name.getStyleClass().add("unit-name");
        name.setWrapText(true);

        Label stats = new Label(unit.isEmpty()
                ? "Espacio disponible"
                : "ATK " + unit.getEffectiveAttack() + " · VIDA " + unit.getLife() + "/" + unit.getMaxLife());
        stats.getStyleClass().add("unit-stats");

        Label status = new Label(unit.isEmpty() ? "" : (unit.isActive() ? "Activa" : "Agotada") + formatUnitStatus(unit));
        status.getStyleClass().add("unit-status");

        VBox card = new VBox(8, position, name, stats, status);
        card.getStyleClass().addAll("unit-card", unit.isEmpty() ? "unit-empty" : "unit-occupied");
        if (rival) {
            card.getStyleClass().add("rival-unit");
        }
        card.setMinWidth(150);
        card.setPrefWidth(180);
        card.setMinHeight(130);
        return card;
    }

    private String formatUnitStatus(GameState.UnitState unit) {
        return unit.getStatus() == null || unit.getStatus().isBlank() ? "" : " · " + unit.getStatus();
    }

    private String formatStatus(GameState state) {
        if (state.hasWinner()) {
            return "Ganador: " + state.getWinnerName();
        }
        return "Turno " + state.getTurnsPlayed() + " · Jugador actual: " + state.getCurrentPlayer().getName();
    }

    private String formatPlayer(GameState.PlayerState player) {
        return player.getName() + " · Vida " + player.getLife() + " · Mazo " + player.getDeckSize()
                + " · Descarte " + player.getDiscardSize();
    }

    private int selectedHandIndex() {
        GameState.CardState selected = handList.getSelectionModel().getSelectedItem();
        return selected != null ? selected.getHandIndex() : -1;
    }

    private int selectedCurrentPosition() {
        Integer selected = currentPositionSelector.getSelectionModel().getSelectedItem();
        return selected != null ? selected : 0;
    }

    private int selectedRivalPosition() {
        Integer selected = rivalPositionSelector.getSelectionModel().getSelectedItem();
        return selected != null ? selected : 0;
    }

    private Juego createDemoGame() {
        Jugador playerOne = new Jugador("Jugador 1", Jugador.VIDA_INICIAL, createDemoDeck(1));
        Jugador playerTwo = new Jugador("Jugador 2", Jugador.VIDA_INICIAL, createDemoDeck(100));
        return new Juego(playerOne, playerTwo);
    }

    private Mazo createDemoDeck(int idOffset) {
        Mazo deck = new Mazo();
        add(deck, new Unidad(idOffset, "Capibara Guardián", "Unidad defensiva equilibrada.", 3, 7, null, "", 0, true));
        add(deck, new Unidad(idOffset + 1, "Ajolote Místico", "Atacante ligero para abrir la partida.", 4, 5, null, "", 0, true));
        add(deck, new Objeto(idOffset + 2, "Mate Curativo", "Cura 3 puntos a una unidad aliada.", "CURA", 3));
        add(deck, new Unidad(idOffset + 3, "Cóndor Bravo", "Unidad ofensiva de alto impacto.", 6, 4, null, "", 0, true));
        add(deck, new Objeto(idOffset + 4, "Bombilla Ígnea", "Inflige 2 puntos de daño a una unidad rival.", "DANIO", 2));
        add(deck, new Unidad(idOffset + 5, "Ñandú Veloz", "Amenaza rápida con buen ataque.", 5, 5, null, "", 0, true));
        add(deck, new Objeto(idOffset + 6, "Cebada Furiosa", "Aumenta el ataque de una unidad aliada.", "BONUS_ATAQUE", 0));
        add(deck, new Objeto(idOffset + 7, "Termo Explosivo", "Daña directamente al jugador rival.", "DANIO_JUGADOR", 4));
        return deck;
    }

    private void add(Mazo deck, Carta card) {
        deck.anadirCarta(card);
    }
}
