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
    private String rarity;
    private String type;
    private String target;
    private String timing;
    private List<Effect> effects;

    // Constructor por defecto
    public Carta() {
        this(0, "", "");
    }

    // Constructor por parámetros
    public Carta(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre != null ? nombre : "";
        this.descripcion = descripcion != null ? descripcion : "";
        this.rarity = "";
        this.type = "";
        this.target = "";
        this.timing = "";
        this.effects = new ArrayList<>();
    }

    // Constructor de copia
    public Carta(Carta otra) {
        this(otra.id, otra.nombre, otra.descripcion);
        copiarMetadatosDesde(otra);
    }

    protected void copiarMetadatosDesde(Carta otra) {
        if (otra == null) return;
        this.rarity = otra.rarity;
        this.type = otra.type;
        this.target = otra.target;
        this.timing = otra.timing;
        setEffects(otra.effects);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre != null ? nombre : ""; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion != null ? descripcion : ""; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity != null ? rarity : ""; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type != null ? type : ""; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target != null ? target : ""; }

    public String getTiming() { return timing; }
    public void setTiming(String timing) { this.timing = timing != null ? timing : ""; }

    public List<Effect> getEffects() { return Collections.unmodifiableList(effects); }
    public void setEffects(List<Effect> effects) {
        this.effects = new ArrayList<>();
        if (effects == null) return;
        for (Effect effect : effects) {
            if (effect != null) this.effects.add(new Effect(effect));
        }
    }

    @Override
    public String toString() {
        return "Carta{id=" + id + ", nombre='" + nombre + "', rarity='" + rarity + "', type='" + type + "'}";
    }
}
