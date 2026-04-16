package fightingmates;

/**
 * Carta de tipo Unidad. Puede atacar, recibir daño y tener una habilidad activa.
 * multiplicadorAtaque permite aplicar bonificadores de objetos (p.ej. x1.5).
 */
public class Unidad extends Carta {
    private int ataque;
    private int vida;
    private int vidaMaxima;
    private Habilidad habilidad;
    private String estadoActual;
    private int duracionEstado;
    private boolean activa;
    private float multiplicadorAtaque; // bonus de objetos; 1.0 = sin bonus

    // Constructor por defecto
    public Unidad() {
        this(0, "", "", 0, 1, null, "", 0, true);
    }

    // Constructor por parámetros
    public Unidad(int id, String nombre, String descripcion,
                  int ataque, int vidaMaxima,
                  Habilidad habilidad, String estadoActual,
                  int duracionEstado, boolean activa) {
        super(id, nombre, descripcion);
        this.ataque = Math.max(0, ataque);
        this.vidaMaxima = Math.max(1, vidaMaxima);
        this.vida = this.vidaMaxima;
        this.habilidad = habilidad;
        this.estadoActual = estadoActual != null ? estadoActual : "";
        this.duracionEstado = Math.max(0, duracionEstado);
        this.activa = activa;
        this.multiplicadorAtaque = 1.0f;
    }

    // Constructor de copia
    public Unidad(Unidad otra) {
        this(otra.getId(), otra.getNombre(), otra.getDescripcion(),
                otra.ataque, otra.vidaMaxima,
                otra.habilidad, otra.estadoActual,
                otra.duracionEstado, otra.activa);
        this.vida = otra.vida;
        this.multiplicadorAtaque = otra.multiplicadorAtaque;
    }

    // ─── Combate ──────────────────────────────────────────────────────────────

    /**
     * Ataca a la unidad objetivo con el daño efectivo (ataque * multiplicador).
     */
    public void atacar(Unidad objetivo) {
        if (objetivo != null && estaViva() && activa) {
            objetivo.recibirDanio(getAtaqueEfectivo());
        }
    }

    public void recibirDanio(int cantidad) {
        if (cantidad > 0) {
            vida = Math.max(0, vida - cantidad);
        }
    }

    public void curar(int cantidad) {
        if (cantidad > 0 && estaViva()) {
            vida = Math.min(vidaMaxima, vida + cantidad);
        }
    }

    public boolean estaViva() {
        return vida > 0;
    }

    /**
     * Aplica la habilidad activa de esta unidad sobre el objetivo indicado.
     * Las habilidades de curación (soloAliados=true) se activan manualmente
     * desde la fase de ataque; las de daño/estado se activan aquí automáticamente.
     */
    public void aplicarHabilidad(Unidad objetivo, Jugador propietario, Jugador rival) {
        if (habilidad != null) {
            habilidad.aplicar(this, objetivo, propietario, rival);
        }
    }

    // ─── Getters y setters ────────────────────────────────────────────────────

    /** Daño efectivo teniendo en cuenta el multiplicador de objetos. */
    public int getAtaqueEfectivo() {
        return (int)(ataque * multiplicadorAtaque);
    }

    public int getAtaque() { return ataque; }
    public void setAtaque(int ataque) { this.ataque = Math.max(0, ataque); }

    public int getVida() { return vida; }
    public void setVida(int vida) { this.vida = Math.max(0, Math.min(vida, vidaMaxima)); }

    public int getVidaMaxima() { return vidaMaxima; }
    public void setVidaMaxima(int vidaMaxima) {
        this.vidaMaxima = Math.max(1, vidaMaxima);
        this.vida = Math.min(this.vida, this.vidaMaxima);
    }

    public Habilidad getHabilidad() { return habilidad; }
    public void setHabilidad(Habilidad habilidad) { this.habilidad = habilidad; }

    public String getEstadoActual() { return estadoActual; }
    public void setEstadoActual(String estadoActual) { this.estadoActual = estadoActual != null ? estadoActual : ""; }

    public int getDuracionEstado() { return duracionEstado; }
    public void setDuracionEstado(int duracionEstado) { this.duracionEstado = Math.max(0, duracionEstado); }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public float getMultiplicadorAtaque() { return multiplicadorAtaque; }
    public void setMultiplicadorAtaque(float multiplicadorAtaque) {
        this.multiplicadorAtaque = Math.max(1.0f, multiplicadorAtaque);
    }

    @Override
    public String toString() {
        String bonus = multiplicadorAtaque != 1.0f ? " (x" + multiplicadorAtaque + "atk)" : "";
        String estado = !estadoActual.isEmpty() ? " [" + estadoActual + " " + duracionEstado + "t]" : "";
        return getNombre()
                + " ATK:" + ataque + bonus
                + " VID:" + vida + "/" + vidaMaxima
                + estado
                + (habilidad != null ? " HAB:" + habilidad.getNombre() : "");
    }
}
