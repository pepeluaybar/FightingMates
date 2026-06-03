package fightingmates.view;

import fightingmates.Carta;
import fightingmates.Objeto;
import fightingmates.Unidad;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
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

        getChildren().addAll(imageBox, nameLabel, metadataLabel, descriptionLabel, statsLabel);
    }

    public Carta getCarta() {
        return carta;
    }

    public void setCarta(Carta carta) {
        this.carta = carta;
        getStyleClass().removeAll(java.util.List.of("card-unit", "card-object", "rarity-perro", "rarity-oca"));
        imageBox.getChildren().clear();

        if (carta == null) {
            nameLabel.setText("Vacío");
            metadataLabel.setText("-");
            descriptionLabel.setText("Sin carta");
            statsLabel.setText("");
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
            String estado = unidad.getEstadoActual().isBlank() ? "" : " · Estado: " + unidad.getEstadoActual();
            String activa = unidad.esActiva() ? " · Lista" : " · Ya actuó";
            return "ATQ " + unidad.getAtaqueEfectivo() + " · VIDA " + unidad.getVida() + "/" + unidad.getVidaMaxima() + estado + activa;
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
