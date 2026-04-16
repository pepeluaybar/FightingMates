package fightingmates;

public class Objeto extends Carta {
    private String tipoEfecto;
    private int valor;

    public Objeto() {
        this(0, "", "", "", 0);
    }

    public Objeto(int id, String nombre, String descripcion, String tipoEfecto, int valor) {
        super(id, nombre, descripcion);
        this.tipoEfecto = tipoEfecto;
        this.valor = valor;
    }

    public Objeto(Objeto otro) {
        this(otro.getId(), otro.getNombre(), otro.getDescripcion(), otro.tipoEfecto, otro.valor);
    }

    public String getTipoEfecto() {
        return tipoEfecto;
    }

    public void setTipoEfecto(String tipoEfecto) {
        this.tipoEfecto = tipoEfecto;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }

    public void usar(Unidad objetivo, Jugador propietario, Jugador rival) {
        if ("DANIO".equalsIgnoreCase(tipoEfecto) && objetivo != null) {
            objetivo.recibirDanio(valor);
        } else if ("CURA".equalsIgnoreCase(tipoEfecto) && objetivo != null) {
            objetivo.curar(valor);
        } else if ("DANIO_JUGADOR".equalsIgnoreCase(tipoEfecto) && rival != null) {
            rival.recibirDanio(valor);
        }
    }

    public boolean esUsableSobre(Unidad objetivo) {
        if ("DANIO_JUGADOR".equalsIgnoreCase(tipoEfecto)) {
            return true;
        }
        return objetivo != null && objetivo.estaViva();
    }

    @Override
    public String toString() {
        return "Objeto{" +
                "tipoEfecto='" + tipoEfecto + '\'' +
                ", valor=" + valor +
                ", carta=" + super.toString() +
                '}';
    }
}
