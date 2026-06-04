package fightingmates.view;

import fightingmates.Carta;
import fightingmates.Objeto;
import fightingmates.Unidad;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Representación visual JavaFX reutilizable de una carta o unidad del modelo. */
public class CardView extends VBox {
    private final AssetManager assetManager;
    private final StackPane imageBox;
    private final Label nameLabel;
    private final Label metadataLabel;
    private final Label descriptionLabel;
    private final Label statsLabel;
    private final Label stateLabel;
    private final Label actionLabel;
    private Carta carta;

    public CardView() {
        this(null, new AssetManager());
    }

    public CardView(Carta carta) {
        this(carta, new AssetManager());
    }

    public CardView(Carta carta, AssetManager assetManager) {
        this.assetManager = assetManager;
        this.imageBox = new StackPane();
        this.nameLabel = new Label();
        this.metadataLabel = new Label();
        this.descriptionLabel = new Label();
        this.statsLabel = new Label();
        this.stateLabel = new Label();
        this.actionLabel = new Label();
        configurar();
        setCarta(carta);
    }

    private void configurar() {
        getStyleClass().add("card");
        setSpacing(6);
        setAlignment(Pos.TOP_CENTER);
        setPrefSize(170, 245);
        setMinSize(150, 220);
        setMaxWidth(185);

        imageBox.getStyleClass().add("card-image");
        imageBox.setPrefSize(145, 85);
        nameLabel.getStyleClass().add("card-name");
        metadataLabel.getStyleClass().add("card-meta");
        descriptionLabel.getStyleClass().add("card-description");
        descriptionLabel.setWrapText(true);
        statsLabel.getStyleClass().add("card-stats");
        stateLabel.getStyleClass().add("state-badge");
        actionLabel.getStyleClass().add("action-badge");

        getChildren().addAll(imageBox, nameLabel, metadataLabel, descriptionLabel, statsLabel, stateLabel, actionLabel);
    }

    public Carta getCarta() {
        return carta;
    }

    public void setCarta(Carta carta) {
        this.carta = carta;
        getStyleClass().removeAll(java.util.List.of("card-unit", "card-object", "rarity-perro", "rarity-oca", "card-used-action",
                "status-confused", "status-pressured", "status-blocked", "status-protected"));
        imageBox.getChildren().clear();

        if (carta == null) {
            nameLabel.setText("Vacío");
            metadataLabel.setText("-");
            descriptionLabel.setText("Sin carta");
            statsLabel.setText("");
            stateLabel.setText("");
            actionLabel.setText("");
            setTooltip(null);
            imageBox.getChildren().add(new Label("Sin imagen"));
            return;
        }

        getStyleClass().add(carta instanceof Objeto ? "card-object" : "card-unit");
        aplicarRareza(carta.getRareza());
        Image image = assetManager.getCardImage(carta);
        if (image == null || image.isError()) {
            imageBox.getChildren().add(new Label("Sin imagen"));
        } else {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(145);
            imageView.setFitHeight(85);
            imageView.setPreserveRatio(true);
            imageBox.getChildren().add(imageView);
        }

        nameLabel.setText(valor(carta.getNombre(), "Carta sin nombre"));
        metadataLabel.setText("Rareza: " + valor(carta.getRareza(), "N/D") + " · Tipo: " + valor(carta.getTipo(), "N/D"));
        descriptionLabel.setText(valor(carta.getDescripcion(), "Sin descripción"));
        statsLabel.setText(textoStats(carta));
        actualizarEstadoYAccion(carta);
        setTooltip(new Tooltip(textoTooltip(carta)));
    }

    public void setSelected(boolean selected) {
        cambiarClase("selected", selected);
    }

    public void setTargetSelected(boolean selected) {
        cambiarClase("target-selected", selected);
    }

    private void cambiarClase(String clase, boolean enabled) {
        if (enabled && !getStyleClass().contains(clase)) {
            getStyleClass().add(clase);
        } else if (!enabled) {
            getStyleClass().remove(clase);
        }
    }

    private void actualizarEstadoYAccion(Carta carta) {
        stateLabel.setVisible(false);
        stateLabel.setManaged(false);
        actionLabel.setVisible(false);
        actionLabel.setManaged(false);

        if (!(carta instanceof Unidad unidad)) return;

        if (!unidad.getEstadoVisible().isBlank()) {
            stateLabel.setText(unidad.getEstadoVisible());
            stateLabel.setVisible(true);
            stateLabel.setManaged(true);
            aplicarClaseEstado(unidad.getEstadoActual());
        }

        actionLabel.setText(unidad.esActiva() ? "Acción disponible" : "Acción usada");
        actionLabel.setVisible(true);
        actionLabel.setManaged(true);
        cambiarClase("card-used-action", !unidad.esActiva());
    }

    private void aplicarClaseEstado(String estado) {
        String normalizado = estado == null ? "" : estado.toLowerCase();
        if (normalizado.contains("conf")) cambiarClase("status-confused", true);
        if (normalizado.contains("pres") || normalizado.contains("depres")) cambiarClase("status-pressured", true);
        if (normalizado.contains("bloq") || normalizado.contains("block")) cambiarClase("status-blocked", true);
        if (normalizado.contains("proteg") || normalizado.contains("protect")) cambiarClase("status-protected", true);
    }

    private String textoTooltip(Carta carta) {
        StringBuilder sb = new StringBuilder();
        sb.append(valor(carta.getNombre(), "Carta sin nombre")).append("\n");
        sb.append(valor(carta.getDescripcion(), "Sin descripción"));
        if (carta instanceof Unidad unidad) {
            if (unidad.getHabilidad() != null) {
                sb.append("\nHabilidad: ").append(unidad.getHabilidad().getDescripcion());
            }
            if (!unidad.getEstadoActual().isBlank()) {
                sb.append("\nEstado: ").append(unidad.getDescripcionEstado());
            }
        }
        return sb.toString();
    }

    private void aplicarRareza(String rareza) {
        String normalizada = rareza == null ? "" : rareza.toLowerCase();
        if (normalizada.contains("perro")) {
            getStyleClass().add("rarity-perro");
        } else if (normalizada.contains("oca")) {
            getStyleClass().add("rarity-oca");
        }
    }

    private String textoStats(Carta carta) {
        if (carta instanceof Unidad unidad) {
            String habilidad = unidad.getHabilidad() == null ? "" : " · HAB " + unidad.getHabilidad().getNombre();
            return "ATQ " + unidad.getAtaqueEfectivo() + " · VIDA " + unidad.getVida() + "/" + unidad.getVidaMaxima() + habilidad;
        }
        if (carta instanceof Objeto objeto) {
            return "EFECTO " + valor(objeto.getTipoEfecto(), "N/D") + " · VALOR " + objeto.getValor();
        }
        return "";
    }

    private String valor(String texto, String fallback) {
        return texto == null || texto.isBlank() ? fallback : texto;
    }
}
