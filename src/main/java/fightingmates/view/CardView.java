package fightingmates.view;

import fightingmates.Carta;
import fightingmates.Objeto;
import fightingmates.Unidad;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;

/**
 * Vista reutilizable para representar visualmente una carta en el tablero.
 */
public class CardView extends JPanel {
    private static final Color DEFAULT_BACKGROUND = new Color(245, 245, 245);
    private static final Color DEFAULT_BORDER = new Color(90, 90, 90);
    private static final Color COMMON_COLOR = new Color(210, 210, 210);
    private static final Color UNCOMMON_COLOR = new Color(120, 190, 120);
    private static final Color RARE_COLOR = new Color(90, 145, 220);
    private static final Color EPIC_COLOR = new Color(155, 95, 205);
    private static final Color LEGENDARY_COLOR = new Color(225, 155, 55);
    private static final Color UNIT_COLOR = new Color(235, 245, 255);
    private static final Color OBJECT_COLOR = new Color(255, 248, 225);

    private final JLabel imagePlaceholder;
    private final JLabel nameLabel;
    private final JLabel descriptionLabel;
    private final JLabel rarityLabel;
    private final JLabel typeLabel;
    private final JPanel detailsPanel;

    private Carta carta;

    public CardView() {
        setLayout(new BorderLayout(8, 8));
        setPreferredSize(new Dimension(180, 260));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DEFAULT_BORDER, 2, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        imagePlaceholder = createImagePlaceholder();
        add(imagePlaceholder, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        nameLabel = createLabel("Sin carta", Font.BOLD, 16, SwingConstants.CENTER);
        descriptionLabel = createLabel("", Font.PLAIN, 12, SwingConstants.CENTER);
        rarityLabel = createLabel("Rareza: -", Font.PLAIN, 12, SwingConstants.LEFT);
        typeLabel = createLabel("Tipo: -", Font.PLAIN, 12, SwingConstants.LEFT);

        contentPanel.add(nameLabel);
        contentPanel.add(descriptionLabel);
        contentPanel.add(rarityLabel);
        contentPanel.add(typeLabel);

        detailsPanel = new JPanel(new GridLayout(0, 2, 6, 4));
        detailsPanel.setOpaque(false);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(detailsPanel);

        add(contentPanel, BorderLayout.CENTER);
        setCarta(null);
    }

    public CardView(Carta carta) {
        this();
        setCarta(carta);
    }

    public Carta getCarta() {
        return carta;
    }

    public final void setCarta(Carta carta) {
        this.carta = carta;
        detailsPanel.removeAll();

        if (carta == null) {
            nameLabel.setText("Sin carta");
            descriptionLabel.setText("");
            rarityLabel.setText("Rareza: -");
            typeLabel.setText("Tipo: -");
            setBackground(DEFAULT_BACKGROUND);
            setBorderColor(DEFAULT_BORDER);
        } else {
            nameLabel.setText(carta.getNombre());
            descriptionLabel.setText(toHtml(carta.getDescripcion()));
            rarityLabel.setText("Rareza: " + valueOrDash(carta.getRareza()));
            typeLabel.setText("Tipo: " + valueOrDash(carta.getTipo()));
            setBackground(resolveBackgroundColor(carta));
            setBorderColor(resolveRarityColor(carta.getRareza()));
            addSpecificDetails(carta);
        }

        revalidate();
        repaint();
    }

    private JLabel createImagePlaceholder() {
        JLabel label = createLabel("Imagen", Font.ITALIC, 12, SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(160, 90));
        label.setOpaque(true);
        label.setBackground(new Color(225, 225, 225));
        label.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 1, true));
        return label;
    }

    private JLabel createLabel(String text, int style, int size, int alignment) {
        JLabel label = new JLabel(text, alignment);
        label.setFont(label.getFont().deriveFont(style, size));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void addSpecificDetails(Carta carta) {
        if (carta instanceof Unidad unidad) {
            addDetail("Ataque", String.valueOf(unidad.getAtaque()));
            addDetail("Vida", unidad.getVida() + "/" + unidad.getVidaMaxima());
        } else if (carta instanceof Objeto objeto) {
            addDetail("Efecto", valueOrDash(objeto.getTipoEfecto()));
            addDetail("Valor", String.valueOf(objeto.getValor()));
        }
    }

    private void addDetail(String label, String value) {
        detailsPanel.add(createLabel(label + ":", Font.BOLD, 12, SwingConstants.LEFT));
        detailsPanel.add(createLabel(valueOrDash(value), Font.PLAIN, 12, SwingConstants.LEFT));
    }

    private Color resolveBackgroundColor(Carta carta) {
        if (carta instanceof Unidad) {
            return UNIT_COLOR;
        }
        if (carta instanceof Objeto) {
            return OBJECT_COLOR;
        }
        return DEFAULT_BACKGROUND;
    }

    private Color resolveRarityColor(String rarity) {
        String normalizedRarity = normalize(rarity);
        return switch (normalizedRarity) {
            case "comun", "common" -> COMMON_COLOR;
            case "infrecuente", "uncommon" -> UNCOMMON_COLOR;
            case "rara", "rare" -> RARE_COLOR;
            case "epica", "epic" -> EPIC_COLOR;
            case "legendaria", "legendary" -> LEGENDARY_COLOR;
            default -> DEFAULT_BORDER;
        };
    }

    private void setBorderColor(Color color) {
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
    }

    private String toHtml(String text) {
        String value = valueOrDash(text);
        return "<html><body style='width: 145px'>" + escapeHtml(value) + "</body></html>";
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
