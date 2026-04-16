package fightingmates;

public class Juego {
    public static final int MANO_INICIAL = 4;

    private Jugador jugador1;
    private Jugador jugador2;
    private Jugador jugadorActual;
    private Tablero tablero;
    private int turnosJugados;

    public Juego() {
        this(new Jugador("J1", Jugador.VIDA_INICIAL, new Mazo()),
                new Jugador("J2", Jugador.VIDA_INICIAL, new Mazo()));
    }

    public Juego(Jugador jugador1, Jugador jugador2) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.tablero = new Tablero(jugador1, jugador2, Tablero.TAMANIO_CAMPO);
        this.jugadorActual = jugador1;
        this.turnosJugados = 0;
        configurarJugadores();
    }

    public Juego(Juego otro) {
        this(otro.jugador1, otro.jugador2);
        this.jugadorActual = otro.jugadorActual;
        this.turnosJugados = otro.turnosJugados;
    }

    private void configurarJugadores() {
        jugador1.setTablero(tablero);
        jugador2.setTablero(tablero);
        jugador1.setRival(jugador2);
        jugador2.setRival(jugador1);
    }

    public void iniciarPartida() {
        jugador1.getMazo().barajar();
        jugador2.getMazo().barajar();

        for (int i = 0; i < MANO_INICIAL; i++) {
            jugador1.robarCarta();
            jugador2.robarCarta();
        }
    }

    public void realizarTurno() {
        jugadorActual.robarCarta();
        // v1: la lógica completa de acciones por turno se implementará por fases.
        resolverAtaques();
        jugadorActual.setPrimerTurno(false);
        turnosJugados++;
        cambiarTurno();
    }

    public void resolverAtaques() {
        Jugador atacante = jugadorActual;
        Jugador defensor = atacante.equals(jugador1) ? jugador2 : jugador1;
        Unidad[] unidadesAtacantes = tablero.obtenerUnidadesVivas(atacante);
        Unidad[] unidadesDefensoras = tablero.obtenerUnidadesVivas(defensor);

        for (Unidad unidadAtacante : unidadesAtacantes) {
            if (unidadAtacante == null || !unidadAtacante.estaViva()) {
                continue;
            }

            if (unidadesDefensoras.length > 0) {
                ejecutarAtaque(unidadAtacante, unidadesDefensoras[0]);
                if (!unidadesDefensoras[0].estaViva()) {
                    unidadesDefensoras = tablero.obtenerUnidadesVivas(defensor);
                }
            } else {
                defensor.recibirDanio(unidadAtacante.getAtaque());
            }
        }
    }

    public void ejecutarAtaque(Unidad atacante, Unidad objetivo) {
        if (atacante == null || !atacante.estaViva()) {
            return;
        }

        if (objetivo != null && objetivo.estaViva()) {
            atacante.atacar(objetivo);
            atacante.aplicarHabilidad(objetivo, jugadorActual, obtenerRival(jugadorActual));
        }
    }

    public Jugador comprobarGanador() {
        if (jugador1.estaDerrotado()) {
            return jugador2;
        }
        if (jugador2.estaDerrotado()) {
            return jugador1;
        }
        return null;
    }

    public void cambiarTurno() {
        jugadorActual = jugadorActual.equals(jugador1) ? jugador2 : jugador1;
    }

    private Jugador obtenerRival(Jugador jugador) {
        return jugador.equals(jugador1) ? jugador2 : jugador1;
    }

    public Jugador getJugador1() {
        return jugador1;
    }

    public void setJugador1(Jugador jugador1) {
        this.jugador1 = jugador1;
        configurarJugadores();
    }

    public Jugador getJugador2() {
        return jugador2;
    }

    public void setJugador2(Jugador jugador2) {
        this.jugador2 = jugador2;
        configurarJugadores();
    }

    public Jugador getJugadorActual() {
        return jugadorActual;
    }

    public void setJugadorActual(Jugador jugadorActual) {
        this.jugadorActual = jugadorActual;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
        configurarJugadores();
    }

    public int getTurnosJugados() {
        return turnosJugados;
    }

    @Override
    public String toString() {
        return "Juego{" +
                "jugador1=" + jugador1.getNombre() +
                ", jugador2=" + jugador2.getNombre() +
                ", jugadorActual=" + jugadorActual.getNombre() +
                ", turnosJugados=" + turnosJugados +
                '}';
    }
}
