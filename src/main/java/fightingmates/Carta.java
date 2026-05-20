package fightingmates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase base abstracta para todas las cartas del juego.
 * Unidad y Objeto heredan de esta clase.
 */
public abstract class Carta {
    private int id;
    private String nombre;
    private String descripcion;
    private String rareza;
    private String tipo;
    private String objetivo;
    private String momento; // Timing
    private List<Efecto> efectos;

    // Constructor por defecto
    public Carta() {
        this(0, "", "");
    }

    // Constructor por parámetros
    public Carta(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre != null ? nombre : "";
        this.descripcion = descripcion != null ? descripcion : "";
        this.rareza = "";
        this.tipo = "";
        this.objetivo = "";
        this.momento = "";
        this.efectos = new ArrayList<>();
    }

    // Constructor de copia
    public Carta(Carta otra) {
        this(otra.id, otra.nombre, otra.descripcion);
        copiarMetadatosDesde(otra);
    }

    protected void copiarMetadatosDesde(Carta otra) {
        if (otra == null) return;
        this.rareza = otra.rareza;
        this.tipo = otra.tipo;
        this.objetivo = otra.objetivo;
        this.momento = otra.momento;
        setEfectos(otra.efectos);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre != null ? nombre : ""; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion != null ? descripcion : ""; }

    public String getRareza() { return rareza; }
    public void setRareza(String rareza) { this.rareza = rareza != null ? rareza : ""; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo != null ? tipo : ""; }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo != null ? objetivo : ""; }

    public String getMomento() { return momento; }
    public void setMomento(String momento) { this.momento = momento != null ? momento : ""; }

    public List<Efecto> getEfectos() { return Collections.unmodifiableList(efectos); }
    public void setEfectos(List<Efecto> efectos) {
        this.efectos = new ArrayList<>();
        if (efectos == null) return;
        for (Efecto efecto : efectos) {
            if (efecto != null) this.efectos.add(new Efecto(efecto));
        }
    }

    @Override
    public String toString() {
        return "Carta{id=" + id + ", nombre='" + nombre + "', rareza='" + rareza + "', tipo='" + tipo + "'}";
    }
}
