package fightingmates;

public class HabilidadCura extends Habilidad {
    private int cantidadCura;

    public HabilidadCura() {
        this("Curación", "Restaura vida", 0);
    }

    public HabilidadCura(String nombre, String descripcion, int cantidadCura) {
        super(nombre, descripcion);
        this.cantidadCura = cantidadCura;
    }

    public HabilidadCura(HabilidadCura otra) {
        this(otra.getNombre(), otra.getDescripcion(), otra.cantidadCura);
    }

    public int getCantidadCura() {
        return cantidadCura;
    }

    public void setCantidadCura(int cantidadCura) {
        this.cantidadCura = cantidadCura;
    }

    @Override
    public void aplicar(Unidad origen, Unidad objetivo, Jugador propietario, Jugador rival) {
        if (objetivo != null && objetivo.estaViva()) {
            objetivo.curar(cantidadCura);
        }
    }

    @Override
    public String toString() {
        return "HabilidadCura{" +
                "cantidadCura=" + cantidadCura +
                ", base=" + super.toString() +
                '}';
    }
}
