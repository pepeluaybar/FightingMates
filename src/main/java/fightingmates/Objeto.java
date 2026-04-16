package fightingmates;

/**
 * Carta de tipo Objeto. Se usa desde la mano y se descarta tras aplicarse.
 *
 * Tipos de efecto soportados en v1:
 *   DANIO        → daña a una unidad enemiga
 *   CURA         → cura a una unidad aliada
 *   DANIO_JUGADOR→ daña directamente al jugador rival
 *   BONUS_ATAQUE → aplica x1.5 de daño a la unidad aliada objetivo (permanente hasta fin de partida)
 */
public class Objeto extends Carta {
    private String tipoEfecto;
    private int valor; // cantidad de daño o cura; 0 si el efecto es multiplicativo

    // Constructor por defecto
    public Objeto() {
        this(0, "", "", "", 0);
    }

    // Constructor por parámetros
    public Objeto(int id, String nombre, String descripcion, String tipoEfecto, int valor) {
        super(id, nombre, descripcion);
        this.tipoEfecto = tipoEfecto != null ? tipoEfecto : "";
        this.valor = valor;
    }

    // Constructor de copia
    public Objeto(Objeto otro) {
        this(otro.getId(), otro.getNombre(), otro.getDescripcion(), otro.tipoEfecto, otro.valor);
    }

    /**
     * Aplica el efecto del objeto.
     * Para BONUS_ATAQUE, objetivo es la unidad aliada a potenciar.
     * Para DANIO_JUGADOR, objetivo puede ser null (el daño va al rival directamente).
     */
    public void usar(Unidad objetivo, Jugador propietario, Jugador rival) {
        switch (tipoEfecto.toUpperCase()) {
            case "DANIO":
                if (objetivo != null) objetivo.recibirDanio(valor);
                break;
            case "CURA":
                if (objetivo != null) objetivo.curar(valor);
                break;
            case "DANIO_JUGADOR":
                if (rival != null) rival.recibirDanio(valor);
                break;
            case "BONUS_ATAQUE":
                // Aplica multiplicador x1.5 a la unidad aliada seleccionada
                if (objetivo != null) objetivo.setMultiplicadorAtaque(1.5f);
                break;
            default:
                // Tipo desconocido: sin efecto en v1
                break;
        }
    }

    /**
     * Valida si el objeto puede usarse sobre la unidad indicada.
     * DANIO_JUGADOR no necesita unidad objetivo (puede pasarse null).
     */
    public boolean esUsableSobre(Unidad objetivo) {
        if ("DANIO_JUGADOR".equalsIgnoreCase(tipoEfecto)) {
            return true; // no necesita unidad objetivo
        }
        return objetivo != null && objetivo.estaViva();
    }

    public String getTipoEfecto() { return tipoEfecto; }
    public void setTipoEfecto(String tipoEfecto) { this.tipoEfecto = tipoEfecto != null ? tipoEfecto : ""; }

    public int getValor() { return valor; }
    public void setValor(int valor) { this.valor = valor; }

    @Override
    public String toString() {
        return getNombre() + " [Objeto tipo:" + tipoEfecto + (valor != 0 ? " val:" + valor : "") + "]";
    }
}
