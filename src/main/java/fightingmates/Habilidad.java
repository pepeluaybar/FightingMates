package fightingmates;

/**
 * Define el comportamiento base de una habilidad de unidad.
 */
public abstract class Habilidad {
    private String nombre;
    private String descripcion;

    public Habilidad() {
        this("", "");
    }

    public Habilidad(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Habilidad(Habilidad otra) {
        this(otra.nombre, otra.descripcion);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public abstract void aplicar(Unidad origen, Unidad objetivo, Jugador propietario, Jugador rival);

    @Override
    public String toString() {
        return "Habilidad{" +
                "nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
