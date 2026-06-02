package fightingmates.controller;

import fightingmates.Carta;
import fightingmates.Juego;
import fightingmates.Jugador;
import fightingmates.Objeto;
import fightingmates.Tablero;
import fightingmates.Unidad;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Fachada pública para la vista Swing. Centraliza todas las modificaciones del
 * modelo y publica snapshots inmutables para mantener separadas vista y lógica.
 */
public class GameController {
    public enum BoardSide { CURRENT_PLAYER, RIVAL_PLAYER }

    private final Juego juego;
    private final List<String> logMessages;
    private GameView view;

    public GameController(Juego juego) {
        this.juego = Objects.requireNonNull(juego, "juego no puede ser null");
        this.logMessages = new ArrayList<>();
        addLog("Partida lista. Turno de " + juego.getJugadorActual().getNombre() + ".");
    }

    public void setView(GameView view) {
        this.view = view;
        refreshView();
    }

    public GameState getState() {
        Jugador actual = juego.getJugadorActual();
        Jugador rival = juego.getJugadorRival(actual);
        Jugador ganador = juego.comprobarGanador();

        return new GameState(
                toPlayerState(actual),
                toPlayerState(rival),
                toHandState(actual),
                toBoardState(actual),
                toBoardState(rival),
                logMessages,
                ganador != null ? ganador.getNombre() : null,
                juego.getTurnosJugados()
        );
    }

    public void playUnit(int handIndex, int boardPosition) {
        if (isGameOver()) {
            reject("La partida ya ha terminado.");
            return;
        }

        Jugador actual = juego.getJugadorActual();
        Carta card = actual.obtenerCartaMano(handIndex);
        if (!(card instanceof Unidad)) {
            reject("Selecciona una carta de unidad válida.");
            return;
        }

        if (!juego.getTablero().esPosicionValida(boardPosition)) {
            reject("Selecciona una posición de tablero válida.");
            return;
        }

        if (actual.jugarUnidad(handIndex, boardPosition)) {
            addLog(actual.getNombre() + " juega " + card.getNombre() + " en la posición " + boardPosition + ".");
        } else {
            addLog("No se pudo jugar la unidad en esa posición.");
        }
        afterAction();
    }

    public void useObject(int handIndex, BoardSide targetSide, int boardPosition) {
        if (isGameOver()) {
            reject("La partida ya ha terminado.");
            return;
        }

        Jugador actual = juego.getJugadorActual();
        Jugador rival = juego.getJugadorRival(actual);
        Carta card = actual.obtenerCartaMano(handIndex);
        if (!(card instanceof Objeto)) {
            reject("Selecciona una carta de objeto válida.");
            return;
        }

        Objeto object = (Objeto) card;
        String effectType = object.getTipoEfecto() != null
                ? object.getTipoEfecto().toUpperCase(Locale.ROOT)
                : "";
        Unidad target = resolveObjectTarget(effectType, targetSide, boardPosition, actual, rival);
        if (target == null && !"DANIO_JUGADOR".equals(effectType)) {
            reject("Selecciona un objetivo válido para " + object.getNombre() + ".");
            return;
        }

        if (actual.usarObjeto(handIndex, target)) {
            addLog(actual.getNombre() + " usa " + object.getNombre() + describeTarget(target, rival, effectType) + ".");
        } else {
            addLog("No se pudo usar el objeto seleccionado.");
        }
        afterAction();
    }

    public void attack(int attackerPosition, int targetPosition) {
        if (isGameOver()) {
            reject("La partida ya ha terminado.");
            return;
        }

        Jugador actual = juego.getJugadorActual();
        Jugador rival = juego.getJugadorRival(actual);
        Unidad attacker = juego.getTablero().obtenerUnidad(actual, attackerPosition);
        if (attacker == null || !attacker.estaViva()) {
            reject("Selecciona una unidad atacante viva.");
            return;
        }

        if (!attacker.esActiva()) {
            reject("La unidad seleccionada ya ha actuado este turno.");
            return;
        }

        if (juego.getTablero().hayUnidadesVivas(rival)) {
            Unidad target = juego.getTablero().obtenerUnidad(rival, targetPosition);
            if (target == null || !target.estaViva()) {
                reject("Selecciona una unidad enemiga viva como objetivo.");
                return;
            }
            juego.ejecutarAtaque(attacker, target);
            addLog(attacker.getNombre() + " ataca a " + target.getNombre() + ".");
        } else {
            juego.atacarJugador(attacker, rival);
            addLog(attacker.getNombre() + " ataca directamente a " + rival.getNombre() + ".");
        }
        afterAction();
    }

    public void endTurn() {
        if (isGameOver()) {
            reject("La partida ya ha terminado.");
            return;
        }

        String endingPlayer = juego.getJugadorActual().getNombre();
        juego.finalizarTurno();
        addLog(endingPlayer + " finaliza el turno. Ahora juega " + juego.getJugadorActual().getNombre() + ".");
        afterAction();
    }

    private Unidad resolveObjectTarget(
            String effectType,
            BoardSide targetSide,
            int boardPosition,
            Jugador actual,
            Jugador rival
    ) {
        if ("DANIO_JUGADOR".equals(effectType)) {
            return null;
        }

        BoardSide expectedSide = switch (effectType) {
            case "DANIO" -> BoardSide.RIVAL_PLAYER;
            case "CURA", "BONUS_ATAQUE" -> BoardSide.CURRENT_PLAYER;
            default -> targetSide;
        };

        Jugador targetPlayer = expectedSide == BoardSide.RIVAL_PLAYER ? rival : actual;
        return juego.getTablero().obtenerUnidad(targetPlayer, boardPosition);
    }

    private String describeTarget(Unidad target, Jugador rival, String effectType) {
        if ("DANIO_JUGADOR".equals(effectType)) {
            return " sobre " + rival.getNombre();
        }
        return target != null ? " sobre " + target.getNombre() : "";
    }

    private boolean isGameOver() {
        return juego.comprobarGanador() != null;
    }

    private void reject(String message) {
        addLog(message);
        refreshView();
    }

    private void afterAction() {
        cleanDeadUnits();
        Jugador winner = juego.comprobarGanador();
        if (winner != null) {
            addLog("Ha ganado " + winner.getNombre() + ".");
        }
        refreshView();
    }

    private void cleanDeadUnits() {
        Tablero tablero = juego.getTablero();
        tablero.limpiarUnidadesMuertas(juego.getJugador1());
        tablero.limpiarUnidadesMuertas(juego.getJugador2());
    }

    private void refreshView() {
        if (view != null) {
            view.refresh(getState());
        }
    }

    private void addLog(String message) {
        logMessages.add(message);
    }

    private GameState.PlayerState toPlayerState(Jugador player) {
        return new GameState.PlayerState(
                player.getNombre(),
                player.getVida(),
                player.getMazo() != null ? player.getMazo().getNumCartas() : 0,
                player.getNumCartasDescarte()
        );
    }

    private List<GameState.CardState> toHandState(Jugador player) {
        List<GameState.CardState> cards = new ArrayList<>();
        for (int i = 0; i < player.getNumCartasMano(); i++) {
            Carta card = player.obtenerCartaMano(i);
            if (card != null) {
                cards.add(new GameState.CardState(i, card.getNombre(), card.getDescripcion(), cardKind(card)));
            }
        }
        return cards;
    }

    private String cardKind(Carta card) {
        if (card instanceof Unidad) {
            return "Unidad";
        }
        if (card instanceof Objeto) {
            return "Objeto";
        }
        return card.getTipo() == null || card.getTipo().isBlank() ? "Carta" : card.getTipo();
    }

    private List<GameState.UnitState> toBoardState(Jugador player) {
        List<GameState.UnitState> units = new ArrayList<>();
        Unidad[] board = juego.getTablero().getCampo(player);
        if (board == null) {
            return units;
        }

        for (int position = 0; position < board.length; position++) {
            Unidad unit = board[position];
            if (unit == null) {
                units.add(GameState.UnitState.empty(position));
            } else {
                units.add(GameState.UnitState.occupied(
                        position,
                        unit.getNombre(),
                        unit.getAtaque(),
                        unit.getAtaqueEfectivo(),
                        unit.getVida(),
                        unit.getVidaMaxima(),
                        unit.esActiva(),
                        formatStatus(unit)
                ));
            }
        }
        return units;
    }

    private String formatStatus(Unidad unit) {
        String state = unit.getEstadoActual();
        if (state == null || state.isBlank()) {
            return "";
        }
        return state + " (" + unit.getDuracionEstado() + "t)";
    }
}
