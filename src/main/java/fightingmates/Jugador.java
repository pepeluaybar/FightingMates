package fightingmates;

/**
 * Representa a un jugador de la partida.
 * Gestiona su vida, mano, mazo y descarte.
 *
 * NOTA: equals() no está sobreescrito a propósito en v1; se usa identidad de objeto.
 * Tablero y Juego comparan jugadores por referencia, lo que es correcto
 * ya que cada Juego crea exactamente dos instancias de Jugador.
 */
public class Jugador {
    public static final int VIDA_INICIAL  = 30;
    public static final int VIDA_MAXIMA   = 30; // techo de curación
    public static final int MANO_MAXIMA   = 10;
    public static final int DESCARTE_MAX  = 45;

    private String nombre;
    private int vida;
    private Mazo mazo;
    private Carta[] mano;
    private int numCartasMano;
    private Carta[] descarte;
    private int numCartasDescarte;
    private boolean primerTurno;

    // Referencia al tablero y al rival: las asigna Juego tras construir ambos jugadores
    private Tablero tablero;
    private Jugador rival;

    // Constructor por defecto
    public Jugador() {
        this("Jugador", VIDA_INICIAL, new Mazo());
    }

    // Constructor por parámetros
    public Jugador(String nombre, int vida, Mazo mazo) {
        this.nombre = nombre != null ? nombre : "Jugador";
        this.vida = Math.max(0, vida);
        this.mazo = mazo;
        this.mano = new Carta[MANO_MAXIMA];
        this.numCartasMano = 0;
        this.descarte = new Carta[DESCARTE_MAX];
        this.numCartasDescarte = 0;
        this.primerTurno = true;
    }

    // Constructor de copia (copia estado; tablero y rival se reasignan externamente)
    public Jugador(Jugador otro) {
        this(otro.nombre, otro.vida, new Mazo(otro.mazo));
        for (int i = 0; i < otro.numCartasMano; i++) this.mano[i] = otro.mano[i];
        this.numCartasMano = otro.numCartasMano;
        for (int i = 0; i < otro.numCartasDescarte; i++) this.descarte[i] = otro.descarte[i];
        this.numCartasDescarte = otro.numCartasDescarte;
        this.primerTurno = otro.primerTurno;
        // tablero y rival no se copian; deben reasignarse
    }

    // ─── Gestión de cartas ────────────────────────────────────────────────────

    /** Roba una carta del mazo y la añade a la mano. Si la mano está llena, va al descarte. */
    public Carta robarCarta() {
        Carta carta = mazo.robar();
        if (carta == null) return null;
        if (!anadirCartaAMano(carta)) {
            anadirAlDescarte(carta);
            return null;
        }
        return carta;
    }

    /** Añade una carta a la mano. Devuelve false si la mano está llena o la carta es null. */
    public boolean anadirCartaAMano(Carta carta) {
        if (carta == null || numCartasMano >= mano.length) return false;
        mano[numCartasMano++] = carta;
        return true;
    }

    /** Elimina y devuelve la carta en el índice indicado de la mano. */
    public Carta eliminarCartaDeMano(int indice) {
        if (indice < 0 || indice >= numCartasMano) return null;
        Carta eliminada = mano[indice];
        for (int i = indice; i < numCartasMano - 1; i++) mano[i] = mano[i + 1];
        mano[numCartasMano - 1] = null;
        numCartasMano--;
        return eliminada;
    }

    /** Añade una carta al descarte. */
    public boolean anadirAlDescarte(Carta carta) {
        if (carta == null || numCartasDescarte >= descarte.length) return false;
        descarte[numCartasDescarte++] = carta;
        return true;
    }

    /** Devuelve la carta en el índice indicado sin eliminarla. */
    public Carta obtenerCartaMano(int indice) {
        if (indice < 0 || indice >= numCartasMano) return null;
        return mano[indice];
    }

    // ─── Acciones de juego ────────────────────────────────────────────────────

    /**
     * Juega una unidad desde la mano al tablero en la posición indicada.
     * Devuelve true si se colocó correctamente.
     */
    public boolean jugarUnidad(int indiceMano, int posicionTablero) {
        Carta carta = obtenerCartaMano(indiceMano);
        if (!(carta instanceof Unidad) || tablero == null) return false;
        Unidad unidad = (Unidad) carta;
        if (!tablero.colocarUnidad(this, unidad, posicionTablero)) return false;
        eliminarCartaDeMano(indiceMano);
        return true;
    }

    /**
     * Usa un objeto sobre una unidad objetivo.
     * El objeto se descarta tras su uso.
     */
    public boolean usarObjeto(int indiceMano, Unidad objetivo) {
        Carta carta = obtenerCartaMano(indiceMano);
        if (!(carta instanceof Objeto)) return false;
        Objeto objeto = (Objeto) carta;
        if (!objeto.esUsableSobre(objetivo)) return false;
        objeto.usar(objetivo, this, rival);
        Carta usada = eliminarCartaDeMano(indiceMano);
        anadirAlDescarte(usada);
        return true;
    }

    // ─── Vida ─────────────────────────────────────────────────────────────────

    public void recibirDanio(int cantidad) {
        if (cantidad > 0) vida = Math.max(0, vida - cantidad);
    }

    /** Cura al jugador sin superar VIDA_MAXIMA. */
    public void curar(int cantidad) {
        if (cantidad > 0) vida = Math.min(VIDA_MAXIMA, vida + cantidad);
    }

    public boolean estaDerrotado() {
        return vida <= 0;
    }

    // ─── Getters y setters ────────────────────────────────────────────────────

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre != null ? nombre : "Jugador"; }

    public int getVida() { return vida; }
    public void setVida(int vida) { this.vida = Math.max(0, Math.min(vida, VIDA_MAXIMA)); }

    public Mazo getMazo() { return mazo; }
    public void setMazo(Mazo mazo) { this.mazo = mazo; }

    public Carta[] getMano() { return mano; }
    public int getNumCartasMano() { return numCartasMano; }

    public Carta[] getDescarte() { return descarte; }
    public int getNumCartasDescarte() { return numCartasDescarte; }

    public boolean isPrimerTurno() { return primerTurno; }
    public void setPrimerTurno(boolean primerTurno) { this.primerTurno = primerTurno; }

    public Tablero getTablero() { return tablero; }
    public void setTablero(Tablero tablero) { this.tablero = tablero; }

    public Jugador getRival() { return rival; }
    public void setRival(Jugador rival) { this.rival = rival; }

    @Override
    public String toString() {
        return "Jugador{nombre='" + nombre + "'"
                + ", vida=" + vida + "/" + VIDA_MAXIMA
                + ", mano=" + numCartasMano
                + ", mazo=" + (mazo != null ? mazo.getNumCartas() : 0)
                + ", descarte=" + numCartasDescarte
                + ", primerTurno=" + primerTurno + "}";
    }
}
