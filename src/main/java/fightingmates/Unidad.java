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
    private boolean estadoRecienAplicado;
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
        this.estadoRecienAplicado = false;
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
        this.estadoRecienAplicado = otra.estadoRecienAplicado;
        this.multiplicadorAtaque = otra.multiplicadorAtaque;
        copiarMetadatosDesde(otra);
    }

    // ─── Combate ──────────────────────────────────────────────────────────────

    /**
     * Ataca a la unidad objetivo con el daño efectivo (ataque * multiplicador).
     */
    public void atacar(Unidad objetivo) {
        if (objetivo != null && estaViva() && activa && puedeAtacar()) {
            objetivo.recibirDanio(getAtaqueEfectivo());
            activa = false;
        }
    }

    public void recibirDanio(int cantidad) {
        if (cantidad <= 0) return;

        int danioFinal = cantidad;
        if (tieneEstado("protegido")) {
            danioFinal = Math.max(0, cantidad - 2);
            limpiarEstado();
        }

        vida = Math.max(0, vida - danioFinal);
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

    public boolean usarHabilidadManual(Unidad objetivo, Jugador propietario, Jugador rival) {
        if (!estaViva() || !activa || habilidad == null || !puedeUsarHabilidad()) {
            return false;
        }

        habilidad.aplicar(this, objetivo, propietario, rival);
        activa = false;
        return true;
    }

    public boolean tieneEstado(String estado) {
        return estado != null && !estado.isBlank()
                && estadoActual != null
                && normalizarEstado(estadoActual).equals(normalizarEstado(estado));
    }

    public void aplicarEstado(String estado, int duracion) {
        estadoActual = normalizarEstadoAplicado(estado);
        duracionEstado = Math.max(0, duracion);
        estadoRecienAplicado = duracionEstado > 0 && !estadoActual.isBlank();
    }

    public void reducirDuracionEstado() {
        if (estadoRecienAplicado) {
            estadoRecienAplicado = false;
            return;
        }
        if (duracionEstado > 0) {
            duracionEstado--;
        }
        if (duracionEstado == 0 && estadoActual != null && !estadoActual.isBlank()) {
            limpiarEstado();
        }
    }

    public void limpiarEstado() {
        estadoActual = "";
        duracionEstado = 0;
        estadoRecienAplicado = false;
    }

    public boolean puedeAtacar() {
        return !tieneEstado("confundido");
    }

    public boolean puedeUsarHabilidad() {
        return !tieneEstado("bloqueado");
    }

    public String getEstadoVisible() {
        if (estadoActual == null || estadoActual.isBlank()) return "";

        String nombre = switch (normalizarEstado(estadoActual)) {
            case "confusion", "confundido" -> "? Confundido";
            case "presionado", "presion", "depresion" -> "! Presionado";
            case "bloqueado", "blocked" -> "🔒 Bloqueado";
            case "protegido", "protected" -> "🛡 Protegido";
            default -> estadoActual;
        };
        return nombre + (duracionEstado > 0 ? " " + duracionEstado + "T" : "");
    }

    public String getDescripcionEstado() {
        if (estadoActual == null || estadoActual.isBlank()) return "Sin estado alterado.";

        return switch (normalizarEstado(estadoActual)) {
            case "confusion", "confundido" -> "Confundido: no puede atacar mientras dure el estado.";
            case "presionado", "presion", "depresion" -> "Presionado: hace 1 punto menos de daño al atacar.";
            case "bloqueado", "blocked" -> "Bloqueado: no puede usar su habilidad mientras dure el estado.";
            case "protegido", "protected" -> "Protegido: reduce en 2 el próximo daño recibido.";
            default -> estadoActual + ": estado temporal durante " + duracionEstado + " turno(s).";
        };
    }

    private String normalizarEstado(String estado) {
        return estado == null ? "" : estado.trim().toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");
    }

    // ─── Getters y setters ────────────────────────────────────────────────────

    /** Daño efectivo teniendo en cuenta el multiplicador de objetos. */
    public int getAtaqueEfectivo() {
        int ataqueFinal = (int)(ataque * multiplicadorAtaque);
        if (tieneEstado("presionado") || tieneEstado("depresion")) {
            ataqueFinal = Math.max(0, ataqueFinal - 1);
        }
        return ataqueFinal;
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
    public void setEstadoActual(String estadoActual) { this.estadoActual = normalizarEstadoAplicado(estadoActual); }

    public int getDuracionEstado() { return duracionEstado; }
    public void setDuracionEstado(int duracionEstado) { this.duracionEstado = Math.max(0, duracionEstado); }

    private String normalizarEstadoAplicado(String estado) {
        String normalizado = normalizarEstado(estado);
        return switch (normalizado) {
            case "confusion" -> "confundido";
            case "depresion", "presion" -> "presionado";
            case "blocked" -> "bloqueado";
            case "protected" -> "protegido";
            default -> estado != null ? estado : "";
        };
    }

    public boolean esActiva() { return activa; }
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
