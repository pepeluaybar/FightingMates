package fightingmates.controller;

import fightingmates.Carta;
import fightingmates.Effect;
import fightingmates.Juego;
import fightingmates.Jugador;
import fightingmates.Objeto;
import fightingmates.Tablero;
import fightingmates.Unidad;

import java.util.Locale;
import java.util.Objects;

/**
 * Fachada de aplicación para cualquier interfaz gráfica.
 * La vista invoca acciones de alto nivel y la lógica del juego se mantiene en el modelo.
 */
public class GameController {
    private final Juego juego;
    private GameView view;
    private boolean partidaIniciada;
    private boolean partidaTerminada;
    private int desplieguesEsteTurno;

    public GameController(Juego juego) {
        this(juego, null);
    }

    public GameController(Juego juego, GameView view) {
        this.juego = Objects.requireNonNull(juego, "juego no puede ser null");
        this.view = view;
        this.partidaIniciada = false;
        this.partidaTerminada = false;
        this.desplieguesEsteTurno = 0;
    }

    public void setView(GameView view) {
        this.view = view;
    }

    public Juego getJuego() {
        return juego;
    }

    public Jugador getJugadorActual() {
        return juego.getJugadorActual();
    }

    public Jugador getJugadorRival() {
        return juego.getJugadorRival(juego.getJugadorActual());
    }

    public boolean isPartidaIniciada() {
        return partidaIniciada;
    }

    public boolean isPartidaTerminada() {
        return partidaTerminada;
    }

    public String iniciarPartida() {
        if (partidaIniciada) {
            return notificar("La partida ya está iniciada. Turno de " + getJugadorActual().getNombre() + ".");
        }

        juego.iniciarPartida();
        partidaIniciada = true;
        partidaTerminada = false;
        desplieguesEsteTurno = 0;
        return notificar("Partida iniciada. Turno de " + getJugadorActual().getNombre() + ".");
    }

    public String jugarUnidad(int indiceCarta, int posicionTablero) {
        if (!puedeActuar()) return notificar("Primero inicia una partida.");

        Jugador actual = getJugadorActual();
        Carta carta = actual.obtenerCartaMano(indiceCarta);

        if (carta == null) {
            return notificar("Selecciona una carta de la mano válida.");
        }
        if (!(carta instanceof Unidad)) {
            return notificar("La carta seleccionada no es una unidad.");
        }
        if (!puedeDesplegar(actual)) {
            return notificar("Límite de despliegues alcanzado: " + limiteDespliegues(actual) + " en este turno.");
        }
        if (!juego.getTablero().esPosicionValida(posicionTablero)) {
            return notificar("Selecciona un hueco propio válido del tablero.");
        }
        if (juego.getTablero().obtenerUnidad(actual, posicionTablero) != null) {
            return notificar("La posición seleccionada ya está ocupada.");
        }

        String nombreCarta = carta.getNombre();
        if (!actual.jugarUnidad(indiceCarta, posicionTablero)) {
            return notificar("No se pudo jugar la unidad.");
        }

        desplieguesEsteTurno++;
        return notificar(actual.getNombre() + " jugó " + nombreCarta + " en la posición " + (posicionTablero + 1) + ".");
    }

    public String usarObjeto(int indiceCarta, Objetivo objetivo) {
        if (objetivo == null) {
            return usarObjeto(indiceCarta, -1, false);
        }
        return usarObjeto(indiceCarta, objetivo.getPosicion(), objetivo.isAliado());
    }

    public String usarObjeto(int indiceCarta, int posicionObjetivo, boolean objetivoAliado) {
        if (!puedeActuar()) return notificar("Primero inicia una partida.");

        Jugador actual = getJugadorActual();
        Carta carta = actual.obtenerCartaMano(indiceCarta);

        if (carta == null) {
            return notificar("Selecciona una carta de la mano válida.");
        }
        if (!(carta instanceof Objeto objeto)) {
            return notificar("La carta seleccionada no es un objeto.");
        }
        if (actual.haUsadoObjetoEsteTurno()) {
            return notificar("Ya se usó un objeto durante este turno.");
        }

        String tipo = normalizarTipo(objeto);
        String mensajeNoImplementado = validarEfectosImplementados(carta);
        if (!mensajeNoImplementado.isBlank()) {
            return notificar(mensajeNoImplementado);
        }

        Jugador jugadorObjetivo = objetivoAliado ? actual : getJugadorRival();
        Unidad unidadObjetivo = null;

        if (!"DANIO_JUGADOR".equals(tipo)) {
            if (!juego.getTablero().esPosicionValida(posicionObjetivo)) {
                return notificar("Selecciona una unidad objetivo válida.");
            }
            unidadObjetivo = juego.getTablero().obtenerUnidad(jugadorObjetivo, posicionObjetivo);
            if (unidadObjetivo == null || !unidadObjetivo.estaViva()) {
                return notificar("No hay una unidad viva en la posición objetivo.");
            }
        }

        if (!esObjetivoCompatible(tipo, objetivoAliado)) {
            return notificar("El objeto " + objeto.getNombre() + " no puede usarse sobre ese objetivo.");
        }

        String nombreObjeto = objeto.getNombre();
        if (!actual.usarObjeto(indiceCarta, unidadObjetivo)) {
            return notificar("No se pudo usar el objeto.");
        }
        limpiarUnidadesMuertas();
        return notificarConGanador(actual.getNombre() + " usó " + nombreObjeto + ".");
    }

    public String atacar(int posicionAtacante, int posicionDefensor) {
        if (!puedeActuar()) return notificar("Primero inicia una partida.");

        Jugador actual = getJugadorActual();
        Jugador rival = getJugadorRival();
        Unidad atacante = obtenerUnidadValidada(actual, posicionAtacante);
        if (atacante == null) {
            return notificar("Selecciona una unidad atacante propia válida.");
        }
        if (!atacante.esActiva()) {
            return notificar("La unidad atacante ya actuó en este turno.");
        }
        Unidad defensor = obtenerUnidadValidada(rival, posicionDefensor);
        if (defensor == null) {
            return notificar("Selecciona una unidad defensora rival válida.");
        }

        String mensaje = atacante.getNombre() + " atacó a " + defensor.getNombre() + ".";
        juego.ejecutarAtaque(atacante, defensor);
        limpiarUnidadesMuertas();
        return notificarConGanador(mensaje);
    }

    public String atacarJugador(int posicionAtacante) {
        if (!puedeActuar()) return notificar("Primero inicia una partida.");

        Jugador actual = getJugadorActual();
        Jugador rival = getJugadorRival();
        Unidad atacante = obtenerUnidadValidada(actual, posicionAtacante);
        if (atacante == null) {
            return notificar("Selecciona una unidad atacante propia válida.");
        }
        if (!atacante.esActiva()) {
            return notificar("La unidad atacante ya actuó en este turno.");
        }
        if (juego.getTurnosJugados() < 2) {
            return notificar("No se puede atacar directamente al jugador durante la primera ronda.");
        }
        if (juego.getTablero().hayUnidadesVivas(rival)) {
            return notificar("No puedes atacar directamente mientras el rival tenga unidades vivas.");
        }

        String mensaje = atacante.getNombre() + " atacó directamente a " + rival.getNombre() + ".";
        juego.atacarJugador(atacante, rival);
        return notificarConGanador(mensaje);
    }

    public String finalizarTurno() {
        if (!puedeActuar()) return notificar("Primero inicia una partida.");

        Carta robada = juego.finalizarTurno();
        desplieguesEsteTurno = 0;
        Jugador actual = getJugadorActual();
        String robo = robada == null ? " No robó carta." : " Robó 1 carta.";
        return notificar("Turno finalizado. Ahora juega " + actual.getNombre() + "." + robo);
    }

    public String rendirse() {
        if (!puedeActuar()) return notificar("Primero inicia una partida.");

        partidaTerminada = true;
        return notificar(getJugadorActual().getNombre() + " se rindió. Gana " + getJugadorRival().getNombre() + ".");
    }

    private boolean puedeActuar() {
        return partidaIniciada && !partidaTerminada;
    }

    private boolean puedeDesplegar(Jugador jugador) {
        return desplieguesEsteTurno < limiteDespliegues(jugador);
    }

    private int limiteDespliegues(Jugador jugador) {
        return jugador.esPrimerTurno() ? 2 : 1;
    }

    private Unidad obtenerUnidadValidada(Jugador jugador, int posicion) {
        if (!juego.getTablero().esPosicionValida(posicion)) {
            return null;
        }
        Unidad unidad = juego.getTablero().obtenerUnidad(jugador, posicion);
        return unidad != null && unidad.estaViva() ? unidad : null;
    }

    private boolean esObjetivoCompatible(String tipo, boolean objetivoAliado) {
        return switch (tipo) {
            case "DANIO" -> !objetivoAliado;
            case "CURA", "BONUS_ATAQUE" -> objetivoAliado;
            case "DANIO_JUGADOR" -> true;
            default -> false;
        };
    }

    private String normalizarTipo(Objeto objeto) {
        return objeto.getTipoEfecto() == null ? "" : objeto.getTipoEfecto().toUpperCase(Locale.ROOT);
    }

    private String validarEfectosImplementados(Carta carta) {
        for (Effect efecto : carta.getEfectos()) {
            String tipo = efecto.getType() == null ? "" : efecto.getType().toLowerCase(Locale.ROOT);
            if (!tipo.isBlank() && !esEfectoImplementado(tipo)) {
                return "La carta " + carta.getNombre() + " define el efecto '" + efecto.getType()
                        + "', que todavía no está implementado en esta versión.";
            }
        }
        return "";
    }

    private boolean esEfectoImplementado(String tipo) {
        return switch (tipo) {
            case "damage", "heal", "player_damage", "bonus_attack", "buff_damage", "apply_status", "status" -> true;
            default -> false;
        };
    }

    private void limpiarUnidadesMuertas() {
        Tablero tablero = juego.getTablero();
        tablero.limpiarUnidadesMuertas(juego.getJugador1());
        tablero.limpiarUnidadesMuertas(juego.getJugador2());
    }

    private String notificarConGanador(String mensaje) {
        Jugador ganador = juego.comprobarGanador();
        if (ganador != null) {
            partidaTerminada = true;
            mensaje += " Gana " + ganador.getNombre() + ".";
        }
        return notificar(mensaje);
    }

    private String notificar(String mensaje) {
        if (view != null) {
            view.refresh(juego, mensaje);
        }
        return mensaje;
    }

    public interface GameView {
        void refresh(Juego juego, String mensaje);
    }
}
