package fightingmates;

public class HabilidadEstado extends Habilidad {
    private String estadoAplicado;
    private int duracion;

    public HabilidadEstado() {
        this("Estado", "Aplica un estado temporal", "", 0);
    }

    public HabilidadEstado(String nombre, String descripcion, String estadoAplicado, int duracion) {
        super(nombre, descripcion);
        this.estadoAplicado = estadoAplicado;
        this.duracion = duracion;
    }

    public HabilidadEstado(HabilidadEstado otra) {
        this(otra.getNombre(), otra.getDescripcion(), otra.estadoAplicado, otra.duracion);
    }

    public String getEstadoAplicado() {
        return estadoAplicado;
    }

    public void setEstadoAplicado(String estadoAplicado) {
        this.estadoAplicado = estadoAplicado;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    @Override
    public void aplicar(Unidad origen, Unidad objetivo, Jugador propietario, Jugador rival) {
        if (objetivo != null && objetivo.estaViva()) {
            objetivo.setEstadoActual(estadoAplicado);
            objetivo.setDuracionEstado(duracion);
        }
    }

    @Override
    public String toString() {
        return "HabilidadEstado{" +
                "estadoAplicado='" + estadoAplicado + '\'' +
                ", duracion=" + duracion +
                ", base=" + super.toString() +
                '}';
    }
}
