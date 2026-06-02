package fightingmates.controller;

/**
 * Describe un objetivo seleccionado desde la vista sin exponer operaciones de bajo nivel del modelo.
 */
public class Objetivo {
    private final int posicion;
    private final boolean aliado;

    public Objetivo(int posicion, boolean aliado) {
        this.posicion = posicion;
        this.aliado = aliado;
    }

    public int getPosicion() {
        return posicion;
    }

    public boolean isAliado() {
        return aliado;
    }
}
