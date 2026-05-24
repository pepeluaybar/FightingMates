package fightingmates;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CartaJson {
    @SerializedName(value = "nombre", alternate = {"name"})
    private String nombre;
    @SerializedName(value = "rareza", alternate = {"rarity"})
    private String rareza;
    @SerializedName(value = "tipo", alternate = {"type"})
    private String tipo;
    @SerializedName(value = "objetivo", alternate = {"target"})
    private String objetivo;
    @SerializedName(value = "momento", alternate = {"timing"})
    private String momento;
    @SerializedName(value = "descripcion", alternate = {"description"})
    private String descripcion;
    @SerializedName(value = "claseCarta", alternate = {"cardClass"})
    private String claseCarta;
    @SerializedName(value = "clase", alternate = {"clazz"})
    private String clase;
    @SerializedName(value = "copias", alternate = {"copies"})
    private Integer copias;
    @SerializedName(value = "ataque", alternate = {"attack"})
    private Integer ataque;
    @SerializedName(value = "salud", alternate = {"health"})
    private Integer salud;
    @SerializedName(value = "estadisticas", alternate = {"stats"})
    private StatsJson estadisticas;
    @SerializedName(value = "efectos", alternate = {"effects"})
    private ArrayList<EfectoJson> efectos;

    public CartaJson() {
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRareza() { return rareza; }
    public void setRareza(String rareza) { this.rareza = rareza; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }

    public String getMomento() { return momento; }
    public void setMomento(String momento) { this.momento = momento; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getClaseCarta() { return claseCarta; }
    public void setClaseCarta(String claseCarta) { this.claseCarta = claseCarta; }

    public String getClase() { return clase; }
    public void setClase(String clase) { this.clase = clase; }

    public Integer getCopias() { return copias; }
    public void setCopias(Integer copias) { this.copias = copias; }

    public Integer getAtaque() { return ataque; }
    public void setAtaque(Integer ataque) { this.ataque = ataque; }

    public Integer getSalud() { return salud; }
    public void setSalud(Integer salud) { this.salud = salud; }

    public StatsJson getEstadisticas() { return estadisticas; }
    public void setEstadisticas(StatsJson estadisticas) { this.estadisticas = estadisticas; }

    public ArrayList<EfectoJson> getEfectos() { return efectos; }
    public void setEfectos(ArrayList<EfectoJson> efectos) { this.efectos = efectos; }
}
