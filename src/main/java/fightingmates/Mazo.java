package fightingmates;

import java.util.Random;

public class Mazo {
    public static final int CAPACIDAD_MAXIMA = 17;

    private Carta[] cartas;
    private int numCartas;

    public Mazo() {
        this(CAPACIDAD_MAXIMA);
    }

    public Mazo(int capacidad) {
        this.cartas = new Carta[Math.max(1, capacidad)];
        this.numCartas = 0;
    }

    public Mazo(Mazo otro) {
        this(otro.cartas.length);
        for (int i = 0; i < otro.numCartas; i++) {
            this.cartas[i] = otro.cartas[i];
        }
        this.numCartas = otro.numCartas;
    }

    public void barajar() {
        Random random = new Random();
        for (int i = numCartas - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Carta aux = cartas[i];
            cartas[i] = cartas[j];
            cartas[j] = aux;
        }
    }

    public Carta robar() {
        if (estaVacio()) {
            return null;
        }
        Carta carta = cartas[numCartas - 1];
        cartas[numCartas - 1] = null;
        numCartas--;
        return carta;
    }

    public boolean anadirCarta(Carta carta) {
        if (carta == null || estaLleno()) {
            return false;
        }
        cartas[numCartas] = carta;
        numCartas++;
        return true;
    }

    public Carta eliminarCarta(int indice) {
        if (indice < 0 || indice >= numCartas) {
            return null;
        }
        Carta eliminada = cartas[indice];
        for (int i = indice; i < numCartas - 1; i++) {
            cartas[i] = cartas[i + 1];
        }
        cartas[numCartas - 1] = null;
        numCartas--;
        return eliminada;
    }

    public boolean estaVacio() {
        return numCartas == 0;
    }

    public boolean estaLleno() {
        return numCartas == cartas.length;
    }

    public int getNumCartas() {
        return numCartas;
    }

    public Carta[] getCartas() {
        return cartas;
    }

    @Override
    public String toString() {
        return "Mazo{" +
                "numCartas=" + numCartas +
                ", capacidad=" + cartas.length +
                '}';
    }
}
