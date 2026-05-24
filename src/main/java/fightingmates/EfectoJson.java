package fightingmates;

import com.google.gson.annotations.SerializedName;

public class EfectoJson {
    @SerializedName(value = "tipo", alternate = {"type"})
    private String tipo;
    @SerializedName(value = "objetivo", alternate = {"target"})
    private String objetivo;
    @SerializedName(value = "valor", alternate = {"value"})
    private Integer valor;
    @SerializedName(value = "valorTexto", alternate = {"textValue"})
    private String valorTexto;
    @SerializedName(value = "estado", alternate = {"status"})
    private String estado;
    @SerializedName(value = "descripcion", alternate = {"description"})
    private String descripcion;

    public EfectoJson() {
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
    public Integer getValor() { return valor; }
    public void setValor(Integer valor) { this.valor = valor; }
    public String getValorTexto() { return valorTexto; }
    public void setValorTexto(String valorTexto) { this.valorTexto = valorTexto; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
