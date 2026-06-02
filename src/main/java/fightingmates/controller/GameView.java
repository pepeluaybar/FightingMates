package fightingmates.controller;

/**
 * Contrato mínimo que permite al controlador notificar a cualquier vista
 * que debe repintarse con un estado de juego ya actualizado.
 */
public interface GameView {
    void refresh(GameState state);
}
