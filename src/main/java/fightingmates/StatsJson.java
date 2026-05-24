package fightingmates;

import com.google.gson.annotations.SerializedName;

public class StatsJson {
    @SerializedName(value = "ataque", alternate = {"attack"})
    private Integer ataque;
    @SerializedName(value = "salud", alternate = {"health"})
    private Integer salud;

    public StatsJson() {
    }

    public Integer getAtaque() { return ataque; }
    public void setAtaque(Integer ataque) { this.ataque = ataque; }

    public Integer getSalud() { return salud; }
    public void setSalud(Integer salud) { this.salud = salud; }
}
