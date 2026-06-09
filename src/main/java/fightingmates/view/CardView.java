package fightingmates.view;

import fightingmates.Carta;
import fightingmates.HabilidadCura;
import fightingmates.HabilidadDanio;
import fightingmates.HabilidadEstado;
import fightingmates.Objeto;
import fightingmates.Unidad;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Representacion visual JavaFX reutilizable de una carta o unidad del modelo. */
public class CardView extends VBox {
    private static final double CARD_WIDTH = 184;
    private static final double CARD_HEIGHT = 268;
    private static final double COMPACT_CARD_WIDTH = 158;
    private static final double COMPACT_CARD_HEIGHT = 224;
    private static final double IMAGE_WIDTH = 158;
    private static final double IMAGE_HEIGHT = 58;
    private static final double COMPACT_IMAGE_WIDTH = 134;
    private static final double COMPACT_IMAGE_HEIGHT = 48;
    private static final int VISIBLE_ABILITY_CHARS = 120;

    private final AssetManager assetManager;
    private final HBox headerBox;
    private final VBox titleBox;
    private final HBox badgeBox;
    private final StackPane imageBox;
    private final HBox statsBox;
    private final VBox abilityBox;
    private final HBox statusBox;
    private final Label nameLabel;
    private final Label typeLineLabel;
    private final Label abilityTypeLabel;
    private final Label abilityTextLabel;
    private final Label actionLabel;
    private Carta carta;
    private Tooltip tooltip;

    public CardView() {
        this(null, new AssetManager());
    }

    public CardView(Carta carta) {
        this(carta, new AssetManager());
    }

    public CardView(Carta carta, AssetManager assetManager) {
        this.assetManager = assetManager;
        this.headerBox = new HBox(6);
        this.titleBox = new VBox(1);
        this.badgeBox = new HBox(3);
        this.imageBox = new StackPane();
        this.statsBox = new HBox(5);
        this.abilityBox = new VBox(3);
        this.statusBox = new HBox(4);
        this.nameLabel = new Label();
        this.typeLineLabel = new Label();
        this.abilityTypeLabel = new Label();
        this.abilityTextLabel = new Label();
        this.actionLabel = new Label();
        configurar();
        setCarta(carta);
    }

    private void configurar() {
        getStyleClass().add("card");
        setSpacing(6);
        setAlignment(Pos.TOP_CENTER);
        aplicarTamano(CARD_WIDTH, CARD_HEIGHT);

        headerBox.getStyleClass().add("card-header");
        headerBox.setAlignment(Pos.TOP_LEFT);

        titleBox.getStyleClass().add("card-title-block");
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        nameLabel.getStyleClass().add("card-name");
        nameLabel.setWrapText(true);
        typeLineLabel.getStyleClass().add("card-type-line");

        badgeBox.getStyleClass().add("card-badge-row");
        badgeBox.setAlignment(Pos.TOP_RIGHT);

        titleBox.getChildren().addAll(nameLabel, typeLineLabel);
        headerBox.getChildren().addAll(titleBox, badgeBox);

        imageBox.getStyleClass().add("card-image");
        aplicarTamanoImagen(IMAGE_WIDTH, IMAGE_HEIGHT);

        statsBox.getStyleClass().add("card-stat-row");
        statsBox.setAlignment(Pos.CENTER);

        abilityBox.getStyleClass().add("card-ability");
        abilityTypeLabel.getStyleClass().add("card-section-title");
        abilityTextLabel.getStyleClass().add("card-ability-text");
        abilityTextLabel.setWrapText(true);
        abilityBox.getChildren().addAll(abilityTypeLabel, abilityTextLabel);

        statusBox.getStyleClass().add("card-status-row");
        statusBox.setAlignment(Pos.CENTER_LEFT);

        actionLabel.getStyleClass().add("action-badge");

        getChildren().addAll(headerBox, imageBox, statsBox, abilityBox, statusBox, actionLabel);
    }

    public void usarTamanoCompacto() {
        aplicarTamano(COMPACT_CARD_WIDTH, COMPACT_CARD_HEIGHT);
        aplicarTamanoImagen(COMPACT_IMAGE_WIDTH, COMPACT_IMAGE_HEIGHT);
        ajustarImagenRenderizada(COMPACT_IMAGE_WIDTH, COMPACT_IMAGE_HEIGHT);
        getStyleClass().add("card-compact");
    }

    public Carta getCarta() {
        return carta;
    }

    public void setCarta(Carta carta) {
        this.carta = carta;
        limpiarClasesDinamicas();
        imageBox.getChildren().clear();
        statsBox.getChildren().clear();
        statusBox.getChildren().clear();

        if (carta == null) {
            renderCartaVacia();
            return;
        }

        getStyleClass().add(carta instanceof Objeto ? "card-object" : "card-unit");
        aplicarRareza(carta.getRareza());
        renderHeader(carta);
        renderImage(carta);
        renderStats(carta);
        renderAbility(carta);
        renderStatus(carta);
        renderAction(carta);
        actualizarTooltip(new Tooltip(textoTooltip(carta)));
    }

    public void setSelected(boolean selected) {
        cambiarClase("selected", selected);
    }

    public void setTargetSelected(boolean selected) {
        cambiarClase("target-selected", selected);
    }

    private void renderCartaVacia() {
        nameLabel.setText("<empty>");
        typeLineLabel.setText("slot: none");
        badgeBox.getChildren().clear();
        renderPlaceholder();
        abilityTypeLabel.setText("SIN CARTA");
        abilityTextLabel.setText("Esperando modulo...");
        actionLabel.setText("Sin carta");
        actionLabel.getStyleClass().add("action-neutral");
        actualizarTooltip(null);
    }

    private void renderHeader(Carta carta) {
        String nombre = valor(carta.getNombre(), "Carta sin nombre");
        nameLabel.setText(nombre);
        cambiarClase(nameLabel, "card-name-long", nombre.length() > 24);
        typeLineLabel.setText("class: " + valor(carta.getTipo(), "unknown")
                + " | timing: " + valor(carta.getMomento(), "n/a"));

        badgeBox.getChildren().clear();
        if (!valor(carta.getRareza(), "").isBlank()) {
            badgeBox.getChildren().add(crearBadge(carta.getRareza(), "card-badge-rarity"));
        }
    }

    private void renderImage(Carta carta) {
        Image image = assetManager.getCardImage(carta);
        if (image == null || image.isError()) {
            renderPlaceholder();
            return;
        }

        imageBox.getStyleClass().remove("card-image-placeholder");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(imageBox.getPrefWidth());
        imageView.setFitHeight(imageBox.getPrefHeight());
        imageView.setPreserveRatio(true);
        imageBox.getChildren().add(imageView);
    }

    private void renderPlaceholder() {
        imageBox.getStyleClass().add("card-image-placeholder");
        VBox placeholder = new VBox(1);
        placeholder.setAlignment(Pos.CENTER);

        Label symbol = new Label("{ }");
        symbol.getStyleClass().add("card-placeholder-symbol");
        Label text = new Label("<no_sprite/>");
        text.getStyleClass().add("card-placeholder-text");
        placeholder.getChildren().addAll(symbol, text);
        imageBox.getChildren().add(placeholder);
    }

    private void renderStats(Carta carta) {
        if (carta instanceof Unidad unidad) {
            statsBox.getChildren().add(crearStat("ATQ", String.valueOf(unidad.getAtaqueEfectivo()), "card-stat-attack"));
            statsBox.getChildren().add(crearStat("VIDA", unidad.getVida() + "/" + unidad.getVidaMaxima(), "card-stat-health"));

            if (unidad.getVida() < unidad.getVidaMaxima()) {
                getStyleClass().add("card-damaged");
            }
            if (unidad.getVida() <= Math.max(1, unidad.getVidaMaxima() / 3)) {
                getStyleClass().add("card-low-health");
            }
            return;
        }

        if (carta instanceof Objeto objeto) {
            statsBox.getChildren().add(crearStat("EFECTO", valor(objeto.getTipoEfecto(), "N/D"), "card-stat-effect"));
            statsBox.getChildren().add(crearStat("VALOR", String.valueOf(objeto.getValor()), "card-stat-value"));
        }
    }

    private void renderAbility(Carta carta) {
        abilityTypeLabel.setText(etiquetaHabilidad(carta));
        abilityTextLabel.setText(resumenVisible(textoHabilidad(carta)));
    }

    private void renderStatus(Carta carta) {
        statusBox.setVisible(false);
        statusBox.setManaged(false);

        if (!(carta instanceof Unidad unidad) || unidad.getEstadoActual().isBlank()) {
            return;
        }

        statusBox.setVisible(true);
        statusBox.setManaged(true);

        Label status = new Label(textoEstadoCorto(unidad));
        status.getStyleClass().addAll("card-status-chip", claseEstado(unidad.getEstadoActual()));
        Tooltip estadoTooltip = new Tooltip(unidad.getDescripcionEstado());
        estadoTooltip.setWrapText(true);
        estadoTooltip.setMaxWidth(280);
        status.setTooltip(estadoTooltip);
        statusBox.getChildren().add(status);
        getStyleClass().add(claseEstadoCarta(unidad.getEstadoActual()));
    }

    private void renderAction(Carta carta) {
        actionLabel.getStyleClass().removeAll("action-ready", "action-used", "action-blocked", "action-warning", "action-neutral");

        if (carta instanceof Unidad unidad) {
            if (!unidad.esActiva()) {
                actionLabel.setText("Accion usada");
                actionLabel.getStyleClass().add("action-used");
                getStyleClass().add("card-used-action");
            } else if (!unidad.puedeUsarHabilidad()) {
                actionLabel.setText("Habilidad bloqueada");
                actionLabel.getStyleClass().add("action-blocked");
                getStyleClass().add("card-blocked");
            } else if (!unidad.puedeAtacar()) {
                actionLabel.setText("Ataque bloqueado");
                actionLabel.getStyleClass().add("action-warning");
            } else {
                actionLabel.setText("Accion disponible");
                actionLabel.getStyleClass().add("action-ready");
                getStyleClass().add("card-action-ready");
            }
            return;
        }

        actionLabel.setText("Objeto listo");
        actionLabel.getStyleClass().add("action-ready");
    }

    private Label crearBadge(String texto, String clase) {
        Label badge = new Label(texto.trim().toUpperCase());
        badge.getStyleClass().addAll("card-badge", clase);
        return badge;
    }

    private Label crearStat(String etiqueta, String valor, String clase) {
        Label stat = new Label(etiqueta + " " + valor);
        stat.getStyleClass().addAll("card-stat", clase);
        HBox.setHgrow(stat, Priority.ALWAYS);
        stat.setMaxWidth(Double.MAX_VALUE);
        return stat;
    }

    private String etiquetaHabilidad(Carta carta) {
        if (carta instanceof Objeto objeto) {
            return "OBJETO :: " + valor(objeto.getTipoEfecto(), "EFECTO");
        }

        if (carta instanceof Unidad unidad) {
            if (unidad.getHabilidad() == null) {
                return "SIN HABILIDAD";
            }
            if ("passive".equalsIgnoreCase(carta.getMomento())) {
                return "PASIVA";
            }
            if (unidad.getHabilidad() instanceof HabilidadEstado) {
                return "ESTADO";
            }
            if (unidad.getHabilidad() instanceof HabilidadCura) {
                return "CURA";
            }
            if (unidad.getHabilidad() instanceof HabilidadDanio) {
                return "DANIO";
            }
        }

        return "HABILIDAD";
    }

    private String textoHabilidad(Carta carta) {
        if (carta instanceof Unidad unidad && unidad.getHabilidad() != null
                && !unidad.getHabilidad().getDescripcion().isBlank()) {
            return unidad.getHabilidad().getDescripcion();
        }
        return valor(carta.getDescripcion(), "Sin descripcion.");
    }

    private String resumenVisible(String texto) {
        String limpio = texto.replaceAll("\\s+", " ").trim();
        if (limpio.length() <= VISIBLE_ABILITY_CHARS) {
            return limpio;
        }

        int corte = limpio.lastIndexOf(' ', VISIBLE_ABILITY_CHARS);
        if (corte < 40) {
            corte = VISIBLE_ABILITY_CHARS;
        }
        return limpio.substring(0, corte).trim() + "\nTexto completo en tooltip";
    }

    private String textoEstadoCorto(Unidad unidad) {
        String estado = unidad.getEstadoActual().toLowerCase();
        String codigo;
        if (estado.contains("conf")) {
            codigo = "CONF";
        } else if (estado.contains("pres") || estado.contains("depres")) {
            codigo = "PRES";
        } else if (estado.contains("bloq") || estado.contains("block")) {
            codigo = "LOCK";
        } else if (estado.contains("proteg") || estado.contains("protect")) {
            codigo = "SHIELD";
        } else {
            codigo = estado.toUpperCase();
        }

        return unidad.getDuracionEstado() > 0 ? codigo + " " + unidad.getDuracionEstado() + "T" : codigo;
    }

    private String claseEstado(String estado) {
        String normalizado = estado == null ? "" : estado.toLowerCase();
        if (normalizado.contains("conf")) return "status-confused";
        if (normalizado.contains("pres") || normalizado.contains("depres")) return "status-pressured";
        if (normalizado.contains("bloq") || normalizado.contains("block")) return "status-blocked";
        if (normalizado.contains("proteg") || normalizado.contains("protect")) return "status-protected";
        return "status-generic";
    }

    private String claseEstadoCarta(String estado) {
        return "card-" + claseEstado(estado);
    }

    private String textoTooltip(Carta carta) {
        StringBuilder sb = new StringBuilder();
        sb.append(valor(carta.getNombre(), "Carta sin nombre")).append("\n");
        sb.append("Rareza: ").append(valor(carta.getRareza(), "N/D")).append("\n");
        sb.append("Tipo: ").append(valor(carta.getTipo(), "N/D")).append("\n");
        sb.append("Timing: ").append(valor(carta.getMomento(), "N/D")).append("\n\n");
        sb.append("Descripcion:\n").append(valor(carta.getDescripcion(), "Sin descripcion.")).append("\n");

        if (carta instanceof Unidad unidad) {
            sb.append("\nStats: ATQ ").append(unidad.getAtaqueEfectivo())
                    .append(" | VIDA ").append(unidad.getVida()).append("/").append(unidad.getVidaMaxima());
            if (unidad.getHabilidad() != null) {
                sb.append("\nHabilidad: ").append(unidad.getHabilidad().getNombre())
                        .append("\n").append(unidad.getHabilidad().getDescripcion());
            }
            if (!unidad.getEstadoActual().isBlank()) {
                sb.append("\nEstado: ").append(textoEstadoCorto(unidad))
                        .append("\n").append(unidad.getDescripcionEstado());
            }
            sb.append("\nAccion: ").append(textoAccionTooltip(unidad));
        } else if (carta instanceof Objeto objeto) {
            sb.append("\nObjeto: ").append(valor(objeto.getTipoEfecto(), "N/D"))
                    .append(" | Valor ").append(objeto.getValor());
        }

        return sb.toString();
    }

    private String textoAccionTooltip(Unidad unidad) {
        if (!unidad.esActiva()) {
            return "ya uso su accion este turno.";
        }
        if (!unidad.puedeUsarHabilidad()) {
            return "no puede usar habilidad por Bloqueado.";
        }
        if (!unidad.puedeAtacar()) {
            return "no puede atacar por Confundido.";
        }
        return "puede actuar.";
    }

    private void actualizarTooltip(Tooltip nuevoTooltip) {
        if (tooltip != null) {
            Tooltip.uninstall(this, tooltip);
        }
        tooltip = nuevoTooltip;
        if (tooltip != null) {
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(330);
            Tooltip.install(this, tooltip);
        }
    }

    private void aplicarTamano(double ancho, double alto) {
        setPrefSize(ancho, alto);
        setMinSize(ancho, alto);
        setMaxSize(ancho, alto);
    }

    private void aplicarTamanoImagen(double ancho, double alto) {
        imageBox.setPrefSize(ancho, alto);
        imageBox.setMinSize(ancho, alto);
        imageBox.setMaxSize(ancho, alto);
    }

    private void ajustarImagenRenderizada(double ancho, double alto) {
        for (javafx.scene.Node node : imageBox.getChildren()) {
            if (node instanceof ImageView imageView) {
                imageView.setFitWidth(ancho);
                imageView.setFitHeight(alto);
            }
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

    private void limpiarClasesDinamicas() {
        getStyleClass().removeAll("card-unit", "card-object", "rarity-perro", "rarity-oca",
                "card-action-ready", "card-used-action", "card-blocked", "card-damaged", "card-low-health",
                "card-status-confused", "card-status-pressured", "card-status-blocked",
                "card-status-protected", "card-status-generic");
        nameLabel.getStyleClass().remove("card-name-long");
        imageBox.getStyleClass().remove("card-image-placeholder");
    }

    private void cambiarClase(String clase, boolean enabled) {
        if (enabled && !getStyleClass().contains(clase)) {
            getStyleClass().add(clase);
        } else if (!enabled) {
            getStyleClass().remove(clase);
        }
    }

    private void cambiarClase(Label label, String clase, boolean enabled) {
        if (enabled && !label.getStyleClass().contains(clase)) {
            label.getStyleClass().add(clase);
        } else if (!enabled) {
            label.getStyleClass().remove(clase);
        }
    }

    private String valor(String texto, String fallback) {
        return texto == null || texto.isBlank() ? fallback : texto.trim();
    }
}
