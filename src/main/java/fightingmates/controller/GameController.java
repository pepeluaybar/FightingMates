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
 * Fachada de alto nivel para coordinar las acciones de una partida.
 * La vista debe usar este controlador en lugar de invocar directamente
 * operaciones internas del modelo.
 */
public class GameController {
    private final Juego juego;
    private final GameView vista;
    private boolean partidaIniciada;
    private int unidadesJugadasEsteTurno;

    public GameController(Juego juego) {
        this(juego, null);
    }

    public GameController(Juego juego, GameView vista) {
        this.juego = Objects.requireNonNull(juego, "El juego no puede ser null.");
        this.vista = vista;
        this.partidaIniciada = false;
        this.unidadesJugadasEsteTurno = 0;
    }

    public String iniciarPartida() {
        if (partidaIniciada) {
            return notificar("La partida ya está iniciada.");
        }

        juego.iniciarPartida();
        partidaIniciada = true;
        unidadesJugadasEsteTurno = 0;
        return notificar("Partida iniciada. Turno de " + jugadorActual().getNombre() + ".");
    }

    public String jugarUnidad(int indiceCarta, int posicionTablero) {
        String error = validarPartidaActiva();
        if (error != null) return notificar(error);

        Jugador actual = jugadorActual();
        Tablero tablero = juego.getTablero();
        Carta carta = actual.obtenerCartaMano(indiceCarta);

        if (carta == null) {
            return notificar("Índice de carta no válido.");
        }
        if (!(carta instanceof Unidad)) {
            return notificar("La carta seleccionada no es una unidad.");
        }
        if (!tablero.esPosicionValida(posicionTablero)) {
            return notificar("Posición de tablero no válida.");
        }
        if (tablero.obtenerUnidad(actual, posicionTablero) != null) {
            return notificar("La posición seleccionada ya está ocupada.");
        }
        if (!puedeJugarUnidad(actual)) {
            return notificar("No puedes jugar más unidades en este turno.");
        }

        boolean colocada = actual.jugarUnidad(indiceCarta, posicionTablero);
        if (!colocada) {
            return notificar("No se pudo colocar la unidad.");
        }

        unidadesJugadasEsteTurno++;
        return notificar("Unidad " + carta.getNombre() + " colocada en la posición " + posicionTablero + ".");
    }

    public String usarObjeto(int indiceCarta, int posicionObjetivo, boolean objetivoAliado) {
        String error = validarPartidaActiva();
        if (error != null) return notificar(error);

        Jugador actual = jugadorActual();
        Carta carta = actual.obtenerCartaMano(indiceCarta);

        if (carta == null) {
            return notificar("Índice de carta no válido.");
        }
        if (!(carta instanceof Objeto)) {
            return notificar("La carta seleccionada no es un objeto.");
        }
        if (!puedeUsarObjeto(actual)) {
            return notificar("No puedes usar un objeto en este momento del turno.");
        }

        Objeto objeto = (Objeto) carta;
        String tipo = tipoObjeto(objeto);
        Unidad objetivo = null;

        if ("DANIO_JUGADOR".equals(tipo)) {
            if (objetivoAliado) {
                return notificar("Este objeto solo puede afectar al jugador rival.");
            }
        } else {
            Boolean objetivoDebeSerAliado = objetivoAliadoEsperado(tipo);
            if (objetivoDebeSerAliado == null) {
                return notificar("Tipo de objeto no soportado: " + objeto.getTipoEfecto() + ".");
            }
            if (objetivoDebeSerAliado != objetivoAliado) {
                return notificar(objetivoDebeSerAliado
                        ? "Este objeto debe usarse sobre una unidad aliada."
                        : "Este objeto debe usarse sobre una unidad enemiga.");
            }
            if (!juego.getTablero().esPosicionValida(posicionObjetivo)) {
                return notificar("Posición objetivo no válida.");
            }

            Jugador propietarioObjetivo = objetivoAliado ? actual : rivalActual();
            objetivo = juego.getTablero().obtenerUnidad(propietarioObjetivo, posicionObjetivo);
            if (objetivo == null || !objetivo.estaViva()) {
                return notificar("No hay una unidad viva en la posición objetivo.");
            }
        }

        boolean usado = actual.usarObjeto(indiceCarta, objetivo);
        if (!usado) {
            return notificar("No se pudo usar el objeto.");
        }

        limpiarUnidadesMuertas();
        return notificar(resultadoConGanador("Objeto " + objeto.getNombre() + " usado correctamente."));
    }

    public String atacar(int posicionAtacante, int posicionDefensor) {
        String error = validarPartidaActiva();
        if (error != null) return notificar(error);

        Jugador actual = jugadorActual();
        Jugador rival = rivalActual();
        Tablero tablero = juego.getTablero();

        if (!tablero.esPosicionValida(posicionAtacante) || !tablero.esPosicionValida(posicionDefensor)) {
            return notificar("Posición de atacante o defensor no válida.");
        }

        Unidad atacante = tablero.obtenerUnidad(actual, posicionAtacante);
        Unidad defensor = tablero.obtenerUnidad(rival, posicionDefensor);
        String validacion = validarAtacante(atacante);
        if (validacion != null) return notificar(validacion);
        if (defensor == null || !defensor.estaViva()) {
            return notificar("No hay una unidad enemiga viva en la posición defensora.");
        }

        juego.ejecutarAtaque(atacante, defensor);
        limpiarUnidadesMuertas();
        return notificar(resultadoConGanador(atacante.getNombre() + " atacó a " + defensor.getNombre() + "."));
    }

    public String atacarJugador(int posicionAtacante) {
        String error = validarPartidaActiva();
        if (error != null) return notificar(error);

        Jugador actual = jugadorActual();
        Jugador rival = rivalActual();
        Tablero tablero = juego.getTablero();

        if (!tablero.esPosicionValida(posicionAtacante)) {
            return notificar("Posición de atacante no válida.");
        }
        if (tablero.hayUnidadesVivas(rival)) {
            return notificar("No puedes atacar al jugador mientras tenga unidades vivas en el tablero.");
        }

        Unidad atacante = tablero.obtenerUnidad(actual, posicionAtacante);
        String validacion = validarAtacante(atacante);
        if (validacion != null) return notificar(validacion);

        juego.atacarJugador(atacante, rival);
        return notificar(resultadoConGanador(atacante.getNombre() + " atacó directamente a " + rival.getNombre() + "."));
    }

    public String finalizarTurno() {
        String error = validarPartidaActiva();
        if (error != null) return notificar(error);

        juego.finalizarTurno();
        unidadesJugadasEsteTurno = 0;
        return notificar("Turno finalizado. Ahora juega " + jugadorActual().getNombre() + ".");
    }

    public Juego getJuego() {
        return juego;
    }

    private boolean puedeJugarUnidad(Jugador jugador) {
        if (jugador.esPrimerTurno()) {
            return unidadesJugadasEsteTurno < 2;
        }
        return unidadesJugadasEsteTurno == 0 && !jugador.haUsadoObjetoEsteTurno();
    }

    private boolean puedeUsarObjeto(Jugador jugador) {
        if (jugador.esPrimerTurno()) {
            return !jugador.haUsadoObjetoEsteTurno();
        }
        return unidadesJugadasEsteTurno == 0 && !jugador.haUsadoObjetoEsteTurno();
    }

    private String validarAtacante(Unidad atacante) {
        if (atacante == null || !atacante.estaViva()) {
            return "No hay una unidad atacante viva en esa posición.";
        }
        if (!atacante.esActiva()) {
            return "La unidad atacante ya ha actuado este turno.";
        }
        return null;
    }

    private String validarPartidaActiva() {
        if (!partidaIniciada) {
            return "La partida aún no está iniciada.";
        }
        Jugador ganador = juego.comprobarGanador();
        if (ganador != null) {
            return "La partida ya terminó. Ganó " + ganador.getNombre() + ".";
        }
        return null;
    }

    private String resultadoConGanador(String mensajeBase) {
        Jugador ganador = juego.comprobarGanador();
        if (ganador == null) {
            return mensajeBase;
        }
        return mensajeBase + " Ganó " + ganador.getNombre() + ".";
    }

    private void limpiarUnidadesMuertas() {
        Tablero tablero = juego.getTablero();
        tablero.limpiarUnidadesMuertas(juego.getJugador1());
        tablero.limpiarUnidadesMuertas(juego.getJugador2());
    }

    private Jugador jugadorActual() {
        return juego.getJugadorActual();
    }

    private Jugador rivalActual() {
        return juego.getJugadorRival(jugadorActual());
    }

    private String tipoObjeto(Objeto objeto) {
        return objeto.getTipoEfecto() == null ? "" : objeto.getTipoEfecto().toUpperCase(Locale.ROOT);
    }

    private Boolean objetivoAliadoEsperado(String tipoObjeto) {
        switch (tipoObjeto) {
            case "DANIO":
                return Boolean.FALSE;
            case "CURA":
            case "BONUS_ATAQUE":
                return Boolean.TRUE;
            default:
                return null;
        }
    }

    private String notificar(String mensaje) {
        if (vista != null) {
            vista.mostrarMensaje(mensaje);
            vista.actualizar(juego);
        }
        return mensaje;
    }

    public interface GameView {
        void mostrarMensaje(String mensaje);

        void actualizar(Juego juego);
    }
}
