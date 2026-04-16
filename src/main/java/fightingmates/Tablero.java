package fightingmates;

public class Tablero {
    public static final int TAMANIO_CAMPO = 5;

    private Jugador jugador1;
    private Jugador jugador2;
    private Unidad[] campoJ1;
    private Unidad[] campoJ2;

    public Tablero() {
        this(null, null, TAMANIO_CAMPO);
    }

    public Tablero(Jugador jugador1, Jugador jugador2, int tamanioCampo) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        int capacidad = Math.max(1, tamanioCampo);
        this.campoJ1 = new Unidad[capacidad];
        this.campoJ2 = new Unidad[capacidad];
    }

    public Tablero(Tablero otro) {
        this(otro.jugador1, otro.jugador2, otro.campoJ1.length);
        System.arraycopy(otro.campoJ1, 0, this.campoJ1, 0, otro.campoJ1.length);
        System.arraycopy(otro.campoJ2, 0, this.campoJ2, 0, otro.campoJ2.length);
    }

    public boolean colocarUnidad(Jugador jugador, Unidad unidad, int posicion) {
        if (jugador == null || unidad == null || !esPosicionValida(posicion)) {
            return false;
        }

        Unidad[] campo = obtenerCampoPorJugador(jugador);
        if (campo == null || campo[posicion] != null) {
            return false;
        }
        campo[posicion] = unidad;
        return true;
    }

    public Unidad obtenerUnidad(Jugador jugador, int posicion) {
        if (!esPosicionValida(posicion)) {
            return null;
        }
        Unidad[] campo = obtenerCampoPorJugador(jugador);
        return campo == null ? null : campo[posicion];
    }

    public Unidad eliminarUnidad(Jugador jugador, int posicion) {
        if (!esPosicionValida(posicion)) {
            return null;
        }
        Unidad[] campo = obtenerCampoPorJugador(jugador);
        if (campo == null) {
            return null;
        }

        Unidad unidad = campo[posicion];
        campo[posicion] = null;
        return unidad;
    }

    public Unidad[] obtenerUnidadesVivas(Jugador jugador) {
        Unidad[] campo = obtenerCampoPorJugador(jugador);
        if (campo == null) {
            return new Unidad[0];
        }

        int contador = 0;
        for (Unidad unidad : campo) {
            if (unidad != null && unidad.estaViva()) {
                contador++;
            }
        }

        Unidad[] vivas = new Unidad[contador];
        int indice = 0;
        for (Unidad unidad : campo) {
            if (unidad != null && unidad.estaViva()) {
                vivas[indice++] = unidad;
            }
        }
        return vivas;
    }

    public boolean hayUnidadesVivas(Jugador jugador) {
        Unidad[] campo = obtenerCampoPorJugador(jugador);
        if (campo == null) {
            return false;
        }

        for (Unidad unidad : campo) {
            if (unidad != null && unidad.estaViva()) {
                return true;
            }
        }
        return false;
    }

    public boolean esPosicionValida(int posicion) {
        return posicion >= 0 && posicion < campoJ1.length;
    }

    private Unidad[] obtenerCampoPorJugador(Jugador jugador) {
        if (jugador == null) {
            return null;
        }
        if (jugador.equals(jugador1)) {
            return campoJ1;
        }
        if (jugador.equals(jugador2)) {
            return campoJ2;
        }
        return null;
    }

    public Jugador getJugador1() {
        return jugador1;
    }

    public void setJugador1(Jugador jugador1) {
        this.jugador1 = jugador1;
    }

    public Jugador getJugador2() {
        return jugador2;
    }

    public void setJugador2(Jugador jugador2) {
        this.jugador2 = jugador2;
    }

    public Unidad[] getCampoJ1() {
        return campoJ1;
    }

    public Unidad[] getCampoJ2() {
        return campoJ2;
    }

    @Override
    public String toString() {
        return "Tablero{" +
                "tamanioCampo=" + campoJ1.length +
                ", jugador1=" + (jugador1 != null ? jugador1.getNombre() : "null") +
                ", jugador2=" + (jugador2 != null ? jugador2.getNombre() : "null") +
                '}';
    }
}
