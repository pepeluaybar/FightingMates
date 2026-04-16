package fightingmates;

/**
 * Controlador principal de la partida.
 * Gestiona el estado global: jugadores, tablero, turno actual.
 *
 * La lógica interactiva (elección de objetivos, menús) vive en Main.
 * Juego expone operaciones atómicas que Main orquesta.
 */
public class Juego {
    public static final int MANO_INICIAL = 4;

    private Jugador jugador1;
    private Jugador jugador2;
    private Jugador jugadorActual;
    private Tablero tablero;
    private int turnosJugados;

    // Constructor por defecto (jugadores sin nombre real, para pruebas)
    public Juego() {
        this(new Jugador("J1", Jugador.VIDA_INICIAL, new Mazo()),
             new Jugador("J2", Jugador.VIDA_INICIAL, new Mazo()));
    }

    // Constructor por parámetros
    public Juego(Jugador jugador1, Jugador jugador2) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.tablero = new Tablero(jugador1, jugador2, Tablero.TAMANIO_CAMPO);
        this.jugadorActual = jugador1;
        this.turnosJugados = 0;
        vincularJugadores();
    }

    // Constructor de copia (copia profunda de jugadores)
    public Juego(Juego otro) {
        this(new Jugador(otro.jugador1), new Jugador(otro.jugador2));
        this.turnosJugados = otro.turnosJugados;
        // jugadorActual apunta al jugador1 o jugador2 de ESTA copia
        this.jugadorActual = otro.jugadorActual == otro.jugador1 ? this.jugador1 : this.jugador2;
    }

    /** Conecta a cada jugador su tablero y su rival. */
    private void vincularJugadores() {
        jugador1.setTablero(tablero);
        jugador2.setTablero(tablero);
        jugador1.setRival(jugador2);
        jugador2.setRival(jugador1);
    }

    // ─── Flujo de partida ─────────────────────────────────────────────────────

    /** Baraja los mazos y reparte la mano inicial a ambos jugadores. */
    public void iniciarPartida() {
        jugador1.getMazo().barajar();
        jugador2.getMazo().barajar();
        for (int i = 0; i < MANO_INICIAL; i++) {
            jugador1.robarCarta();
            jugador2.robarCarta();
        }
    }

    /**
     * Ejecuta un ataque de atacante sobre objetivo.
     * Las habilidades de curación (soloAliados=true) NO se activan aquí:
     * son acciones manuales elegidas por el jugador en su fase de ataque.
     */
    public void ejecutarAtaque(Unidad atacante, Unidad objetivo) {
        if (atacante == null || !atacante.estaViva()) return;
        if (objetivo == null || !objetivo.estaViva()) return;
        atacante.atacar(objetivo);
        // Solo aplica habilidad automáticamente si NO es de curación exclusiva para aliados
        Habilidad hab = atacante.getHabilidad();
        if (hab != null && !hab.esSoloAliados()) {
            atacante.aplicarHabilidad(objetivo, jugadorActual, getJugadorRival(jugadorActual));
        }
    }

    /** Una unidad ataca directamente al jugador rival (cuando no hay unidades enemigas). */
    public void atacarJugador(Unidad atacante, Jugador defensor) {
        if (atacante == null || !atacante.estaViva() || defensor == null) return;
        defensor.recibirDanio(atacante.getAtaqueEfectivo());
    }

    /** Devuelve el ganador si hay uno, null si la partida continúa. */
    public Jugador comprobarGanador() {
        if (jugador1.estaDerrotado()) return jugador2;
        if (jugador2.estaDerrotado()) return jugador1;
        return null;
    }

    /** Marca fin de turno: primerTurno=false, incrementa contador, cambia turno. */
    public void finalizarTurno() {
        jugadorActual.setPrimerTurno(false);
        turnosJugados++;
        cambiarTurno();
    }

    public void cambiarTurno() {
        jugadorActual = (jugadorActual == jugador1) ? jugador2 : jugador1;
    }

    // ─── Utilidades ───────────────────────────────────────────────────────────

    public Jugador getJugadorRival(Jugador jugador) {
        return (jugador == jugador1) ? jugador2 : jugador1;
    }

    // ─── Getters y setters ────────────────────────────────────────────────────

    public Jugador getJugador1() { return jugador1; }
    public void setJugador1(Jugador jugador1) {
        this.jugador1 = jugador1;
        vincularJugadores();
    }

    public Jugador getJugador2() { return jugador2; }
    public void setJugador2(Jugador jugador2) {
        this.jugador2 = jugador2;
        vincularJugadores();
    }

    public Jugador getJugadorActual() { return jugadorActual; }
    public void setJugadorActual(Jugador jugadorActual) { this.jugadorActual = jugadorActual; }

    public Tablero getTablero() { return tablero; }
    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
        vincularJugadores();
    }

    public int getTurnosJugados() { return turnosJugados; }

    @Override
    public String toString() {
        return "Juego{turno=" + turnosJugados
                + ", actual=" + jugadorActual.getNombre()
                + ", J1=" + jugador1.getVida() + "hp"
                + ", J2=" + jugador2.getVida() + "hp}";
    }
}
