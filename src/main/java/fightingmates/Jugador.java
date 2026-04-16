package fightingmates;

public class Jugador {
    public static final int VIDA_INICIAL = 30;
    public static final int MANO_MAXIMA = 10;
    public static final int DESCARTE_MAXIMO = 45;

    private String nombre;
    private int vida;
    private Mazo mazo;
    private Carta[] mano;
    private int numCartasMano;
    private Carta[] descarte;
    private int numCartasDescarte;
    private boolean primerTurno;
    private Tablero tablero;
    private Jugador rival;

    public Jugador() {
        this("Jugador", VIDA_INICIAL, new Mazo());
    }

    public Jugador(String nombre, int vida, Mazo mazo) {
        this.nombre = nombre;
        this.vida = Math.max(0, vida);
        this.mazo = mazo;
        this.mano = new Carta[MANO_MAXIMA];
        this.numCartasMano = 0;
        this.descarte = new Carta[DESCARTE_MAXIMO];
        this.numCartasDescarte = 0;
        this.primerTurno = true;
    }

    public Jugador(Jugador otro) {
        this(otro.nombre, otro.vida, new Mazo(otro.mazo));
        for (int i = 0; i < otro.numCartasMano; i++) {
            this.mano[i] = otro.mano[i];
        }
        this.numCartasMano = otro.numCartasMano;
        for (int i = 0; i < otro.numCartasDescarte; i++) {
            this.descarte[i] = otro.descarte[i];
        }
        this.numCartasDescarte = otro.numCartasDescarte;
        this.primerTurno = otro.primerTurno;
        this.tablero = otro.tablero;
        this.rival = otro.rival;
    }

    public Carta robarCarta() {
        Carta carta = mazo.robar();
        if (carta == null) {
            return null;
        }
        if (!anadirCartaAMano(carta)) {
            anadirAlDescarte(carta);
            return null;
        }
        return carta;
    }

    public boolean anadirCartaAMano(Carta carta) {
        if (carta == null || numCartasMano >= mano.length) {
            return false;
        }
        mano[numCartasMano++] = carta;
        return true;
    }

    public Carta eliminarCartaDeMano(int indice) {
        if (indice < 0 || indice >= numCartasMano) {
            return null;
        }

        Carta eliminada = mano[indice];
        for (int i = indice; i < numCartasMano - 1; i++) {
            mano[i] = mano[i + 1];
        }
        mano[numCartasMano - 1] = null;
        numCartasMano--;
        return eliminada;
    }

    public boolean anadirAlDescarte(Carta carta) {
        if (carta == null || numCartasDescarte >= descarte.length) {
            return false;
        }
        descarte[numCartasDescarte++] = carta;
        return true;
    }

    public boolean jugarUnidad(int indiceMano, int posicionTablero) {
        Carta carta = obtenerCartaMano(indiceMano);
        if (!(carta instanceof Unidad) || tablero == null) {
            return false;
        }

        Unidad unidad = (Unidad) carta;
        boolean colocada = tablero.colocarUnidad(this, unidad, posicionTablero);
        if (colocada) {
            eliminarCartaDeMano(indiceMano);
        }
        return colocada;
    }

    public boolean usarObjeto(int indiceMano, Unidad objetivo) {
        Carta carta = obtenerCartaMano(indiceMano);
        if (!(carta instanceof Objeto)) {
            return false;
        }

        Objeto objeto = (Objeto) carta;
        if (!objeto.esUsableSobre(objetivo)) {
            return false;
        }

        objeto.usar(objetivo, this, rival);
        Carta usada = eliminarCartaDeMano(indiceMano);
        return anadirAlDescarte(usada);
    }

    public void recibirDanio(int cantidad) {
        if (cantidad > 0) {
            vida = Math.max(0, vida - cantidad);
        }
    }

    public void curar(int cantidad) {
        if (cantidad > 0) {
            vida += cantidad;
        }
    }

    public boolean estaDerrotado() {
        return vida <= 0;
    }

    public Carta obtenerCartaMano(int indice) {
        if (indice < 0 || indice >= numCartasMano) {
            return null;
        }
        return mano[indice];
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getVida() {
        return vida;
    }

    public void setVida(int vida) {
        this.vida = Math.max(0, vida);
    }

    public Mazo getMazo() {
        return mazo;
    }

    public void setMazo(Mazo mazo) {
        this.mazo = mazo;
    }

    public Carta[] getMano() {
        return mano;
    }

    public int getNumCartasMano() {
        return numCartasMano;
    }

    public Carta[] getDescarte() {
        return descarte;
    }

    public int getNumCartasDescarte() {
        return numCartasDescarte;
    }

    public boolean isPrimerTurno() {
        return primerTurno;
    }

    public void setPrimerTurno(boolean primerTurno) {
        this.primerTurno = primerTurno;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    public Jugador getRival() {
        return rival;
    }

    public void setRival(Jugador rival) {
        this.rival = rival;
    }

    @Override
    public String toString() {
        return "Jugador{" +
                "nombre='" + nombre + '\'' +
                ", vida=" + vida +
                ", cartasEnMazo=" + (mazo != null ? mazo.getNumCartas() : 0) +
                ", cartasEnMano=" + numCartasMano +
                ", cartasEnDescarte=" + numCartasDescarte +
                ", primerTurno=" + primerTurno +
                '}';
    }
}
