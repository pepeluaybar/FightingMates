package fightingmates;

import java.util.Random;

/**
 * Mazo de cartas de un jugador. Capacidad máxima: 17 cartas (reglamento v1).
 */
public class Mazo {
    public static final int CAPACIDAD_MAXIMA = 17;

    private Carta[] cartas;
    private int numCartas;

    // Constructor por defecto (capacidad estándar)
    public Mazo() {
        this(CAPACIDAD_MAXIMA);
    }

    // Constructor con capacidad personalizada (útil para pruebas)
    public Mazo(int capacidad) {
        this.cartas = new Carta[Math.max(1, capacidad)];
        this.numCartas = 0;
    }

    // Constructor de copia (copia las referencias, no clona las cartas)
    public Mazo(Mazo otro) {
        this(otro.cartas.length);
        for (int i = 0; i < otro.numCartas; i++) {
            this.cartas[i] = otro.cartas[i];
        }
        this.numCartas = otro.numCartas;
    }

    /** Baraja las cartas usando Fisher-Yates. */
    public void barajar() {
        Random random = new Random();
        for (int i = numCartas - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Carta aux = cartas[i];
            cartas[i] = cartas[j];
            cartas[j] = aux;
        }
    }

    /** Roba (extrae) la carta de la cima del mazo. Devuelve null si está vacío. */
    public Carta robar() {
        if (estaVacio()) return null;
        Carta carta = cartas[numCartas - 1];
        cartas[numCartas - 1] = null;
        numCartas--;
        return carta;
    }

    /** Añade una carta al mazo. Devuelve false si está lleno o la carta es null. */
    public boolean anadirCarta(Carta carta) {
        if (carta == null || estaLleno()) return false;
        cartas[numCartas] = carta;
        numCartas++;
        return true;
    }

    /** Elimina la carta en el índice indicado. Devuelve null si el índice es inválido. */
    public Carta eliminarCarta(int indice) {
        if (indice < 0 || indice >= numCartas) return null;
        Carta eliminada = cartas[indice];
        for (int i = indice; i < numCartas - 1; i++) {
            cartas[i] = cartas[i + 1];
        }
        cartas[numCartas - 1] = null;
        numCartas--;
        return eliminada;
    }

    public boolean estaVacio() { return numCartas == 0; }
    public boolean estaLleno() { return numCartas >= cartas.length; }
    public int getNumCartas() { return numCartas; }
    public Carta[] getCartas() { return cartas; }

    @Override
    public String toString() {
        return "Mazo{cartas=" + numCartas + "/" + cartas.length + "}";
    }
}
