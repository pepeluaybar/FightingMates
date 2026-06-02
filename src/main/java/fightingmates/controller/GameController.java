package fightingmates.controller;

import fightingmates.Carta;
import fightingmates.Juego;
import fightingmates.Jugador;
import fightingmates.Objeto;
import fightingmates.Tablero;
import fightingmates.Unidad;

import java.util.Locale;

/**
 * Controlador de aplicación para coordinar acciones de una partida.
 * Mantiene la lógica de flujo fuera de la interfaz y delega las reglas al modelo.
 */
public class GameController {
    private final Juego juego;

    public GameController(Juego juego) {
        if (juego == null) {
            throw new IllegalArgumentException("El juego no puede ser null.");
        }
        this.juego = juego;
    }

    public boolean jugarUnidad(int indiceMano, int posicion) {
        Jugador jugadorActual = juego.getJugadorActual();
        return jugadorActual != null && jugadorActual.jugarUnidad(indiceMano, posicion);
    }

    public boolean usarObjeto(int indiceMano, Objetivo objetivo) {
        Jugador jugadorActual = juego.getJugadorActual();
        if (jugadorActual == null) {
            return false;
        }

        Carta carta = jugadorActual.obtenerCartaMano(indiceMano);
        if (!(carta instanceof Objeto)) {
            return false;
        }

        Objeto objeto = (Objeto) carta;
        Unidad unidadObjetivo = resolverObjetivoObjeto(objeto, objetivo);
        if (unidadObjetivo == null && !esObjetoContraJugador(objeto)) {
            return false;
        }

        boolean usado = jugadorActual.usarObjeto(indiceMano, unidadObjetivo);
        if (usado) {
            limpiarUnidadesMuertas();
        }
        return usado;
    }

    public boolean atacar(int posicionAtacante, int posicionObjetivo) {
        Jugador atacante = juego.getJugadorActual();
        if (atacante == null) {
            return false;
        }

        Jugador defensor = juego.getJugadorRival(atacante);
        Unidad unidadAtacante = juego.getTablero().obtenerUnidad(atacante, posicionAtacante);
        if (unidadAtacante == null || !unidadAtacante.estaViva() || !unidadAtacante.esActiva()) {
            return false;
        }

        if (juego.getTablero().hayUnidadesVivas(defensor)) {
            Unidad unidadObjetivo = juego.getTablero().obtenerUnidad(defensor, posicionObjetivo);
            if (unidadObjetivo == null || !unidadObjetivo.estaViva()) {
                return false;
            }
            juego.ejecutarAtaque(unidadAtacante, unidadObjetivo);
        } else {
            juego.atacarJugador(unidadAtacante, defensor);
        }

        limpiarUnidadesMuertas();
        return true;
    }

    public void finalizarTurno() {
        juego.finalizarTurno();
    }

    public Juego getJuego() {
        return juego;
    }

    private Unidad resolverObjetivoObjeto(Objeto objeto, Objetivo objetivo) {
        if (esObjetoContraJugador(objeto)) {
            return null;
        }
        if (objetivo == null || objetivo.getTipo() == Objetivo.Tipo.JUGADOR_RIVAL) {
            return null;
        }
        if (!esTipoObjetivoCorrecto(objeto, objetivo)) {
            return null;
        }
        return objetivo.resolverUnidad(juego);
    }

    private boolean esTipoObjetivoCorrecto(Objeto objeto, Objetivo objetivo) {
        String tipoEfecto = normalizarTipoEfecto(objeto);
        switch (tipoEfecto) {
            case "DANIO":
                return objetivo.getTipo() == Objetivo.Tipo.ENEMIGO;
            case "CURA":
            case "BONUS_ATAQUE":
                return objetivo.getTipo() == Objetivo.Tipo.ALIADO;
            default:
                return false;
        }
    }

    private boolean esObjetoContraJugador(Objeto objeto) {
        return "DANIO_JUGADOR".equals(normalizarTipoEfecto(objeto));
    }

    private String normalizarTipoEfecto(Objeto objeto) {
        return objeto.getTipoEfecto() == null ? "" : objeto.getTipoEfecto().toUpperCase(Locale.ROOT);
    }

    private void limpiarUnidadesMuertas() {
        Tablero tablero = juego.getTablero();
        tablero.limpiarUnidadesMuertas(juego.getJugador1());
        tablero.limpiarUnidadesMuertas(juego.getJugador2());
    }
}
