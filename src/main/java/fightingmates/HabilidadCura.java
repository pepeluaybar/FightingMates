package fightingmates;

/**
 * Habilidad de curación. Puede configurarse para usarse solo en aliados.
 * Cuando soloAliados=true, NO puede aplicarse a unidades enemigas ni al jugador rival:
 * la validación de objetivo válido se hace en la fase de ataque del Main.
 */
public class HabilidadCura extends Habilidad {
    private int cantidadCura;
    private boolean soloAliados;

    // Constructor por defecto
    public HabilidadCura() {
        this("Curación", "Restaura vida", 0, false);
    }

    // Constructor por parámetros
    public HabilidadCura(String nombre, String descripcion, int cantidadCura, boolean soloAliados) {
        super(nombre, descripcion);
        this.cantidadCura = Math.max(0, cantidadCura);
        this.soloAliados = soloAliados;
    }

    // Constructor de copia
    public HabilidadCura(HabilidadCura otra) {
        this(otra.getNombre(), otra.getDescripcion(), otra.cantidadCura, otra.soloAliados);
    }

    @Override
    public void aplicar(Unidad origen, Unidad objetivo, Jugador propietario, Jugador rival) {
        if (objetivo != null && objetivo.estaViva()) {
            objetivo.curar(cantidadCura);
        }
    }

    /**
     * Si devuelve true, esta habilidad solo puede usarse sobre unidades aliadas
     * (nunca sobre enemigas ni sobre el jugador rival).
     */
    @Override
    public boolean esSoloAliados() {
        return soloAliados;
    }

    public int getCantidadCura() { return cantidadCura; }
    public void setCantidadCura(int cantidadCura) { this.cantidadCura = Math.max(0, cantidadCura); }

    public boolean isSoloAliados() { return soloAliados; }
    public void setSoloAliados(boolean soloAliados) { this.soloAliados = soloAliados; }

    @Override
    public String toString() {
        return "HabilidadCura{cantidadCura=" + cantidadCura
                + ", soloAliados=" + soloAliados
                + ", base=" + super.toString() + "}";
    }
}
