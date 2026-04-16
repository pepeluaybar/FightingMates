package fightingmates;

public class HabilidadDanio extends Habilidad {
    private int cantidadDanio;

    public HabilidadDanio() {
        this("Golpe", "Inflige daño adicional", 0);
    }

    public HabilidadDanio(String nombre, String descripcion, int cantidadDanio) {
        super(nombre, descripcion);
        this.cantidadDanio = cantidadDanio;
    }

    public HabilidadDanio(HabilidadDanio otra) {
        this(otra.getNombre(), otra.getDescripcion(), otra.cantidadDanio);
    }

    public int getCantidadDanio() {
        return cantidadDanio;
    }

    public void setCantidadDanio(int cantidadDanio) {
        this.cantidadDanio = cantidadDanio;
    }

    @Override
    public void aplicar(Unidad origen, Unidad objetivo, Jugador propietario, Jugador rival) {
        if (objetivo != null && objetivo.estaViva()) {
            objetivo.recibirDanio(cantidadDanio);
        }
    }

    @Override
    public String toString() {
        return "HabilidadDanio{" +
                "cantidadDanio=" + cantidadDanio +
                ", base=" + super.toString() +
                '}';
    }
}
