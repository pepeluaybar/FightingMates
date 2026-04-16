package fightingmates;

/**
 * Clase base abstracta para todas las cartas del juego.
 * Unidad y Objeto heredan de esta clase.
 */
public abstract class Carta {
    private int id;
    private String nombre;
    private String descripcion;

    // Constructor por defecto
    public Carta() {
        this(0, "", "");
    }

    // Constructor por parámetros
    public Carta(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre != null ? nombre : "";
        this.descripcion = descripcion != null ? descripcion : "";
    }

    // Constructor de copia
    public Carta(Carta otra) {
        this(otra.id, otra.nombre, otra.descripcion);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre != null ? nombre : ""; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion != null ? descripcion : ""; }

    @Override
    public String toString() {
        return "Carta{id=" + id + ", nombre='" + nombre + "', descripcion='" + descripcion + "'}";
    }
}
