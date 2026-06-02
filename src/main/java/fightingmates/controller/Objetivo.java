package fightingmates.controller;

import fightingmates.Juego;
import fightingmates.Jugador;
import fightingmates.Unidad;

/**
 * Referencia de objetivo para acciones solicitadas desde la capa de controlador.
 * Evita que la interfaz exponga directamente cómo se buscan unidades en el tablero.
 */
public final class Objetivo {
    public enum Tipo {
        ALIADO,
        ENEMIGO,
        JUGADOR_RIVAL
    }

    private final Tipo tipo;
    private final int posicion;

    private Objetivo(Tipo tipo, int posicion) {
        this.tipo = tipo;
        this.posicion = posicion;
    }

    public static Objetivo aliado(int posicion) {
        return new Objetivo(Tipo.ALIADO, posicion);
    }

    public static Objetivo enemigo(int posicion) {
        return new Objetivo(Tipo.ENEMIGO, posicion);
    }

    public static Objetivo jugadorRival() {
        return new Objetivo(Tipo.JUGADOR_RIVAL, -1);
    }

    public Unidad resolverUnidad(Juego juego) {
        if (juego == null || tipo == Tipo.JUGADOR_RIVAL) {
            return null;
        }

        Jugador jugadorObjetivo = tipo == Tipo.ALIADO
                ? juego.getJugadorActual()
                : juego.getJugadorRival(juego.getJugadorActual());

        return juego.getTablero().obtenerUnidad(jugadorObjetivo, posicion);
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getPosicion() {
        return posicion;
    }
}
