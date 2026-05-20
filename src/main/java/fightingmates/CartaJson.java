package fightingmates;

import java.util.ArrayList;

public class CartaJson {
    private String nombre;
    private String rareza;
    private String tipo;
    private String objetivo;
    private String momento;
    private String descripcion;
    private String claseCarta;
    private String clase;
    private Integer copias;
    private Integer ataque;
    private Integer salud;
    private StatsJson estadisticas;
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