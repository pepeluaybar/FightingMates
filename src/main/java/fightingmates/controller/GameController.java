package fightingmates.controller;

import fightingmates.Carta;
import fightingmates.Juego;
import fightingmates.Jugador;
import fightingmates.Objeto;
import fightingmates.Tablero;
import fightingmates.Unidad;

import java.util.Locale;
import java.util.Objects;

/**
 * Fachada de aplicación para la interfaz gráfica.
 * La vista debe invocar estas acciones de alto nivel en lugar de manipular el modelo directamente.
 */
public class GameController {
    private final Juego juego;
    private GameView view;

    public GameController(Juego juego) {
        this(juego, null);
    }

    public GameController(Juego juego, GameView view) {
        this.juego = Objects.requireNonNull(juego, "juego no puede ser null");
        this.view = view;
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

    public String iniciarPartida() {
        juego.iniciarPartida();
        return notificar("Partida iniciada. Turno de " + getJugadorActual().getNombre() + ".");
    }

    public String jugarUnidad(int indiceCarta, int posicionTablero) {
        Jugador actual = getJugadorActual();
        Carta carta = actual.obtenerCartaMano(indiceCarta);

        if (carta == null) {
            return notificar("Índice de carta no válido.");
        }
        if (!(carta instanceof Unidad)) {
            return notificar("La carta seleccionada no es una unidad.");
        }
        if (!juego.getTablero().esPosicionValida(posicionTablero)) {
            return notificar("Posición de tablero no válida.");
        }
        if (juego.getTablero().obtenerUnidad(actual, posicionTablero) != null) {
            return notificar("La posición seleccionada ya está ocupada.");
        }

        String nombreCarta = carta.getNombre();
        if (!actual.jugarUnidad(indiceCarta, posicionTablero)) {
            return notificar("No se pudo jugar la unidad.");
        }
        return notificar(actual.getNombre() + " jugó " + nombreCarta + " en la posición " + posicionTablero + ".");
    }

    public String usarObjeto(int indiceCarta, Objetivo objetivo) {
        if (objetivo == null) {
            return usarObjeto(indiceCarta, -1, false);
        }
        return usarObjeto(indiceCarta, objetivo.getPosicion(), objetivo.isAliado());
    }

    public String usarObjeto(int indiceCarta, int posicionObjetivo, boolean objetivoAliado) {
        Jugador actual = getJugadorActual();
        Carta carta = actual.obtenerCartaMano(indiceCarta);

        if (carta == null) {
            return notificar("Índice de carta no válido.");
        }
        if (!(carta instanceof Objeto)) {
            return notificar("La carta seleccionada no es un objeto.");
        }
        if (actual.haUsadoObjetoEsteTurno()) {
            return notificar("Ya se usó un objeto durante este turno.");
        }

        Objeto objeto = (Objeto) carta;
        String tipo = normalizarTipo(objeto);
        Jugador jugadorObjetivo = objetivoAliado ? actual : getJugadorRival();
        Unidad unidadObjetivo = null;

        if (!"DANIO_JUGADOR".equals(tipo)) {
            if (!juego.getTablero().esPosicionValida(posicionObjetivo)) {
                return notificar("Posición objetivo no válida.");
            }
            unidadObjetivo = juego.getTablero().obtenerUnidad(jugadorObjetivo, posicionObjetivo);
            if (unidadObjetivo == null || !unidadObjetivo.estaViva()) {
                return notificar("No hay una unidad viva en la posición objetivo.");
            }
        }

        if (!esObjetivoCompatible(tipo, objetivoAliado)) {
            return notificar("El tipo de objeto no es compatible con el objetivo seleccionado.");
        }

        String nombreObjeto = objeto.getNombre();
        if (!actual.usarObjeto(indiceCarta, unidadObjetivo)) {
            return notificar("No se pudo usar el objeto.");
        }
        limpiarUnidadesMuertas();
        return notificarConGanador(actual.getNombre() + " usó " + nombreObjeto + ".");
    }

    public String atacar(int posicionAtacante, int posicionDefensor) {
        Jugador actual = getJugadorActual();
        Jugador rival = getJugadorRival();
        Unidad atacante = obtenerUnidadValidada(actual, posicionAtacante, "atacante");
        if (atacante == null) {
            return notificar("No hay una unidad atacante válida en esa posición.");
        }
        if (!atacante.esActiva()) {
            return notificar("La unidad atacante ya actuó en este turno.");
        }
        Unidad defensor = obtenerUnidadValidada(rival, posicionDefensor, "defensor");
        if (defensor == null) {
            return notificar("No hay una unidad defensora válida en esa posición.");
        }

        String mensaje = atacante.getNombre() + " atacó a " + defensor.getNombre() + ".";
        juego.ejecutarAtaque(atacante, defensor);
        limpiarUnidadesMuertas();
        return notificarConGanador(mensaje);
    }

    public String atacarJugador(int posicionAtacante) {
        Jugador actual = getJugadorActual();
        Jugador rival = getJugadorRival();
        Unidad atacante = obtenerUnidadValidada(actual, posicionAtacante, "atacante");
        if (atacante == null) {
            return notificar("No hay una unidad atacante válida en esa posición.");
        }
        if (!atacante.esActiva()) {
            return notificar("La unidad atacante ya actuó en este turno.");
        }
        if (juego.getTablero().hayUnidadesVivas(rival)) {
            return notificar("No puedes atacar directamente mientras el rival tenga unidades vivas.");
        }

        String mensaje = atacante.getNombre() + " atacó directamente a " + rival.getNombre() + ".";
        juego.atacarJugador(atacante, rival);
        return notificarConGanador(mensaje);
    }

    public String finalizarTurno() {
        juego.finalizarTurno();
        return notificar("Turno finalizado. Ahora juega " + getJugadorActual().getNombre() + ".");
    }

    private Unidad obtenerUnidadValidada(Jugador jugador, int posicion, String rol) {
        if (!juego.getTablero().esPosicionValida(posicion)) {
            return null;
        }
        Unidad unidad = juego.getTablero().obtenerUnidad(jugador, posicion);
        return unidad != null && unidad.estaViva() ? unidad : null;
    }

    private boolean esObjetivoCompatible(String tipo, boolean objetivoAliado) {
        switch (tipo) {
            case "DANIO":
                return !objetivoAliado;
            case "CURA":
            case "BONUS_ATAQUE":
                return objetivoAliado;
            case "DANIO_JUGADOR":
                return true;
            default:
                return false;
        }
    }

    private String normalizarTipo(Objeto objeto) {
        return objeto.getTipoEfecto() == null ? "" : objeto.getTipoEfecto().toUpperCase(Locale.ROOT);
    }

    private void limpiarUnidadesMuertas() {
        Tablero tablero = juego.getTablero();
        tablero.limpiarUnidadesMuertas(juego.getJugador1());
        tablero.limpiarUnidadesMuertas(juego.getJugador2());
    }

    private String notificarConGanador(String mensaje) {
        Jugador ganador = juego.comprobarGanador();
        if (ganador != null) {
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
