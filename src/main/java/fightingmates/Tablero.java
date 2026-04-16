package fightingmates;

/**
 * Tablero de juego. Almacena las unidades en campo de ambos jugadores.
 * Cada jugador tiene un array de posiciones (campoJ1 / campoJ2).
 * No hay emparejamientos fijos: cualquier unidad puede atacar a cualquier enemiga.
 */
public class Tablero {
    public static final int TAMANIO_CAMPO = 5;

    private Jugador jugador1;
    private Jugador jugador2;
    private Unidad[] campoJ1;
    private Unidad[] campoJ2;

    // Constructor por defecto
    public Tablero() {
        this(null, null, TAMANIO_CAMPO);
    }

    // Constructor por parámetros
    public Tablero(Jugador jugador1, Jugador jugador2, int tamanioCampo) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        int cap = Math.max(1, tamanioCampo);
        this.campoJ1 = new Unidad[cap];
        this.campoJ2 = new Unidad[cap];
    }

    // Constructor de copia (copia las referencias a unidades, no las unidades en sí)
    public Tablero(Tablero otro) {
        this(otro.jugador1, otro.jugador2, otro.campoJ1.length);
        System.arraycopy(otro.campoJ1, 0, this.campoJ1, 0, otro.campoJ1.length);
        System.arraycopy(otro.campoJ2, 0, this.campoJ2, 0, otro.campoJ2.length);
    }

    // ─── Operaciones sobre el campo ───────────────────────────────────────────

    /** Coloca una unidad en la posición indicada del campo del jugador. */
    public boolean colocarUnidad(Jugador jugador, Unidad unidad, int posicion) {
        if (jugador == null || unidad == null || !esPosicionValida(posicion)) return false;
        Unidad[] campo = obtenerCampoPorJugador(jugador);
        if (campo == null || campo[posicion] != null) return false;
        campo[posicion] = unidad;
        return true;
    }

    /** Devuelve la unidad en la posición indicada sin eliminarla. */
    public Unidad obtenerUnidad(Jugador jugador, int posicion) {
        if (!esPosicionValida(posicion)) return null;
        Unidad[] campo = obtenerCampoPorJugador(jugador);
        return campo == null ? null : campo[posicion];
    }

    /** Elimina y devuelve la unidad en la posición indicada. */
    public Unidad eliminarUnidad(Jugador jugador, int posicion) {
        if (!esPosicionValida(posicion)) return null;
        Unidad[] campo = obtenerCampoPorJugador(jugador);
        if (campo == null) return null;
        Unidad u = campo[posicion];
        campo[posicion] = null;
        return u;
    }

    /** Devuelve un array compacto con todas las unidades vivas del jugador. */
    public Unidad[] obtenerUnidadesVivas(Jugador jugador) {
        Unidad[] campo = obtenerCampoPorJugador(jugador);
        if (campo == null) return new Unidad[0];
        int count = 0;
        for (Unidad u : campo) if (u != null && u.estaViva()) count++;
        Unidad[] vivas = new Unidad[count];
        int idx = 0;
        for (Unidad u : campo) if (u != null && u.estaViva()) vivas[idx++] = u;
        return vivas;
    }

    public boolean hayUnidadesVivas(Jugador jugador) {
        Unidad[] campo = obtenerCampoPorJugador(jugador);
        if (campo == null) return false;
        for (Unidad u : campo) if (u != null && u.estaViva()) return true;
        return false;
    }

    /** Elimina del campo las unidades con vida <= 0. */
    public void limpiarUnidadesMuertas(Jugador jugador) {
        Unidad[] campo = obtenerCampoPorJugador(jugador);
        if (campo == null) return;
        for (int i = 0; i < campo.length; i++) {
            if (campo[i] != null && !campo[i].estaViva()) {
                campo[i] = null;
            }
        }
    }

    public boolean esPosicionValida(int posicion) {
        return posicion >= 0 && posicion < campoJ1.length;
    }

    // ─── Internos ─────────────────────────────────────────────────────────────

    /** Devuelve el campo del jugador usando identidad de referencia. */
    private Unidad[] obtenerCampoPorJugador(Jugador jugador) {
        if (jugador == null) return null;
        if (jugador == jugador1) return campoJ1;
        if (jugador == jugador2) return campoJ2;
        return null;
    }

    // ─── Getters y setters ────────────────────────────────────────────────────

    public Jugador getJugador1() { return jugador1; }
    public void setJugador1(Jugador jugador1) { this.jugador1 = jugador1; }

    public Jugador getJugador2() { return jugador2; }
    public void setJugador2(Jugador jugador2) { this.jugador2 = jugador2; }

    public Unidad[] getCampoJ1() { return campoJ1; }
    public Unidad[] getCampoJ2() { return campoJ2; }

    /** Devuelve el campo del jugador (acceso directo para la consola). */
    public Unidad[] getCampo(Jugador jugador) {
        return obtenerCampoPorJugador(jugador);
    }

    private String formatearCampo(Unidad[] campo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campo.length; i++) {
            sb.append("[").append(i).append(":");
            if (campo[i] != null) sb.append(campo[i].getNombre()).append(" V:").append(campo[i].getVida());
            else sb.append("---");
            sb.append("] ");
        }
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        String j1n = jugador1 != null ? jugador1.getNombre() : "?";
        String j2n = jugador2 != null ? jugador2.getNombre() : "?";
        return "Tablero{\n"
                + "  " + j1n + ": " + formatearCampo(campoJ1) + "\n"
                + "  " + j2n + ": " + formatearCampo(campoJ2) + "\n"
                + "}";
    }
}
