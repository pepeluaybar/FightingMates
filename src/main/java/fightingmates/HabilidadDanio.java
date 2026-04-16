package fightingmates;

/**
 * Habilidad que inflige daño adicional al objetivo.
 * Se activa automáticamente al atacar.
 */
public class HabilidadDanio extends Habilidad {
    private int cantidadDanio;

    // Constructor por defecto
    public HabilidadDanio() {
        this("Golpe", "Inflige daño adicional", 0);
    }

    // Constructor por parámetros
    public HabilidadDanio(String nombre, String descripcion, int cantidadDanio) {
        super(nombre, descripcion);
        this.cantidadDanio = Math.max(0, cantidadDanio);
    }

    // Constructor de copia
    public HabilidadDanio(HabilidadDanio otra) {
        this(otra.getNombre(), otra.getDescripcion(), otra.cantidadDanio);
    }

    @Override
    public void aplicar(Unidad origen, Unidad objetivo, Jugador propietario, Jugador rival) {
        if (objetivo != null && objetivo.estaViva()) {
            objetivo.recibirDanio(cantidadDanio);
        }
    }

    public int getCantidadDanio() { return cantidadDanio; }
    public void setCantidadDanio(int cantidadDanio) { this.cantidadDanio = Math.max(0, cantidadDanio); }

    @Override
    public String toString() {
        return "HabilidadDanio{cantidadDanio=" + cantidadDanio + ", base=" + super.toString() + "}";
    }
}
