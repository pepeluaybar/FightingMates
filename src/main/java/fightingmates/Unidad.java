package fightingmates;

public class Unidad extends Carta {
    private int ataque;
    private int vida;
    private int vidaMaxima;
    private Habilidad habilidad;
    private String estadoActual;
    private int duracionEstado;
    private boolean activa;

    public Unidad() {
        this(0, "", "", 0, 0, null, "", 0, true);
    }

    public Unidad(int id, String nombre, String descripcion, int ataque, int vidaMaxima,
                  Habilidad habilidad, String estadoActual, int duracionEstado, boolean activa) {
        super(id, nombre, descripcion);
        this.ataque = ataque;
        this.vidaMaxima = Math.max(1, vidaMaxima);
        this.vida = this.vidaMaxima;
        this.habilidad = habilidad;
        this.estadoActual = estadoActual;
        this.duracionEstado = Math.max(0, duracionEstado);
        this.activa = activa;
    }

    public Unidad(Unidad otra) {
        this(otra.getId(), otra.getNombre(), otra.getDescripcion(), otra.ataque, otra.vidaMaxima,
                otra.habilidad, otra.estadoActual, otra.duracionEstado, otra.activa);
        this.vida = otra.vida;
    }

    public int getAtaque() {
        return ataque;
    }

    public void setAtaque(int ataque) {
        this.ataque = Math.max(0, ataque);
    }

    public int getVida() {
        return vida;
    }

    public void setVida(int vida) {
        this.vida = Math.max(0, Math.min(vida, vidaMaxima));
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public void setVidaMaxima(int vidaMaxima) {
        this.vidaMaxima = Math.max(1, vidaMaxima);
        this.vida = Math.min(this.vida, this.vidaMaxima);
    }

    public Habilidad getHabilidad() {
        return habilidad;
    }

    public void setHabilidad(Habilidad habilidad) {
        this.habilidad = habilidad;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public int getDuracionEstado() {
        return duracionEstado;
    }

    public void setDuracionEstado(int duracionEstado) {
        this.duracionEstado = Math.max(0, duracionEstado);
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public void atacar(Unidad objetivo) {
        if (objetivo != null && estaViva() && activa) {
            objetivo.recibirDanio(ataque);
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

    public void aplicarHabilidad(Unidad objetivo, Jugador propietario, Jugador rival) {
        if (habilidad != null) {
            habilidad.aplicar(this, objetivo, propietario, rival);
        }
    }

    @Override
    public String toString() {
        return "Unidad{" +
                "ataque=" + ataque +
                ", vida=" + vida +
                ", vidaMaxima=" + vidaMaxima +
                ", estadoActual='" + estadoActual + '\'' +
                ", duracionEstado=" + duracionEstado +
                ", activa=" + activa +
                ", carta=" + super.toString() +
                '}';
    }
}
