package fightingmates;

/**
 * Habilidad que aplica un estado temporal (veneno, parálisis, etc.) a la unidad objetivo.
 * Se activa automáticamente al atacar.
 */
public class HabilidadEstado extends Habilidad {
    private String estadoAplicado;
    private int duracion;

    // Constructor por defecto
    public HabilidadEstado() {
        this("Estado", "Aplica un estado temporal", "", 0);
    }

    // Constructor por parámetros
    public HabilidadEstado(String nombre, String descripcion, String estadoAplicado, int duracion) {
        super(nombre, descripcion);
        this.estadoAplicado = estadoAplicado != null ? estadoAplicado : "";
        this.duracion = Math.max(0, duracion);
    }

    // Constructor de copia
    public HabilidadEstado(HabilidadEstado otra) {
        this(otra.getNombre(), otra.getDescripcion(), otra.estadoAplicado, otra.duracion);
    }

    @Override
    public void aplicar(Unidad origen, Unidad objetivo, Jugador propietario, Jugador rival) {
        if (objetivo != null && objetivo.estaViva()) {
            objetivo.setEstadoActual(estadoAplicado);
            objetivo.setDuracionEstado(duracion);
        }
    }

    public String getEstadoAplicado() { return estadoAplicado; }
    public void setEstadoAplicado(String estadoAplicado) { this.estadoAplicado = estadoAplicado != null ? estadoAplicado : ""; }

    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = Math.max(0, duracion); }

    @Override
    public String toString() {
        return "HabilidadEstado{estadoAplicado='" + estadoAplicado
                + "', duracion=" + duracion
                + ", base=" + super.toString() + "}";
    }
}
