package fightingmates;

/**
 * Efecto declarativo cargado desde JSON.
 * Conserva los datos originales de la carta aunque la v1 solo ejecute
 * algunos efectos como habilidades u objetos del motor actual.
 */
public class Efecto {
    private String tipo;
    private String objetivo;
    private int valor;
    private String valorTexto;
    private String descripcion;

    public Efecto() {
        this("", "", 0, "", "");
    }

    public Efecto(String tipo, String objetivo, int valor, String valorTexto, String descripcion) {
        this.tipo = tipo != null ? tipo : "";
        this.objetivo = objetivo != null ? objetivo : "";
        this.valor = valor;
        this.valorTexto = valorTexto != null ? valorTexto : "";
        this.descripcion = descripcion != null ? descripcion : "";
    }

    public Efecto(Efecto otro) {
        this(otro.tipo, otro.objetivo, otro.valor, otro.valorTexto, otro.descripcion);
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo != null ? tipo : ""; }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo != null ? objetivo : ""; }

    public int getValor() { return valor; }
    public void setValor(int valor) { this.valor = valor; }

    public String getValorTexto() { return valorTexto; }
    public void setValorTexto(String valorTexto) { this.valorTexto = valorTexto != null ? valorTexto : ""; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion != null ? descripcion : ""; }

    @Override
    public String toString() {
        String textoValor = !valorTexto.isEmpty() ? valorTexto : String.valueOf(valor);
        return "Efecto{tipo='" + tipo + "', objetivo='" + objetivo + "', valor='" + textoValor + "'}";
    }
}