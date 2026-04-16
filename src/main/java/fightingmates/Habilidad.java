package fightingmates;

/**
 * Clase base abstracta para habilidades de unidades.
 * Subtipos: HabilidadDanio, HabilidadCura, HabilidadEstado.
 */
public abstract class Habilidad {
    private String nombre;
    private String descripcion;

    // Constructor por defecto
    public Habilidad() {
        this("", "");
    }

    // Constructor por parámetros
    public Habilidad(String nombre, String descripcion) {
        this.nombre = nombre != null ? nombre : "";
        this.descripcion = descripcion != null ? descripcion : "";
    }

    // Constructor de copia
    public Habilidad(Habilidad otra) {
        this(otra.nombre, otra.descripcion);
    }

    /**
     * Aplica el efecto de la habilidad.
     * Las subclases definen la lógica concreta.
     */
    public abstract void aplicar(Unidad origen, Unidad objetivo, Jugador propietario, Jugador rival);

    /**
     * Indica si esta habilidad solo puede usarse sobre unidades aliadas.
     * Por defecto false. HabilidadCura con soloAliados=true lo sobreescribe.
     */
    public boolean esSoloAliados() {
        return false;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre != null ? nombre : ""; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion != null ? descripcion : ""; }

    @Override
    public String toString() {
        return "Habilidad{nombre='" + nombre + "', descripcion='" + descripcion + "'}";
    }
}
