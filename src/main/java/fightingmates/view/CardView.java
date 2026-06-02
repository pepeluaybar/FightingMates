package fightingmates.view;

import fightingmates.Carta;
import fightingmates.Objeto;
import fightingmates.Unidad;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Representación visual reutilizable de una carta del modelo.
 */
public class CardView extends JPanel {
    private static final int CARD_WIDTH = 180;
    private static final int CARD_HEIGHT = 245;

    private final AssetManager assetManager;
    private final JLabel imageLabel;
    private final JLabel nameLabel;
    private final JLabel metadataLabel;
    private final JTextArea descriptionArea;
    private final JLabel statsLabel;
    private Carta carta;

    public CardView() {
        this(null, new AssetManager());
    }

    public CardView(Carta carta) {
        this(carta, new AssetManager());
    }

    public CardView(Carta carta, AssetManager assetManager) {
        this.assetManager = assetManager;
        this.imageLabel = new JLabel();
        this.nameLabel = new JLabel();
        this.metadataLabel = new JLabel();
        this.descriptionArea = new JTextArea();
        this.statsLabel = new JLabel();
        configurarComponentes();
        setCarta(carta);
    }

    public void setCarta(Carta carta) {
        this.carta = carta;
        refrescar();
    }

    public Carta getCarta() {
        return carta;
    }

    private void configurarComponentes() {
        setLayout(new BorderLayout(8, 6));
        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 52, 65), 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(CARD_WIDTH - 20, 78));

        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        metadataLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

        descriptionArea.setOpaque(false);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        statsLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        header.add(nameLabel);
        header.add(metadataLabel);

        JPanel body = new JPanel(new BorderLayout(4, 4));
        body.setOpaque(false);
        body.add(header, BorderLayout.NORTH);
        body.add(descriptionArea, BorderLayout.CENTER);
        body.add(statsLabel, BorderLayout.SOUTH);

        add(imageLabel, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    private void refrescar() {
        if (carta == null) {
            setBackground(new Color(238, 241, 246));
            imageLabel.setIcon(null);
            nameLabel.setText("Vacío");
            metadataLabel.setText("-");
            descriptionArea.setText("Sin carta");
            statsLabel.setText(" ");
            return;
        }

        setBackground(colorPara(carta));
        ImageIcon image = assetManager.getCardImage(carta);
        imageLabel.setIcon(image);
        nameLabel.setText(carta.getNombre().isBlank() ? "Carta sin nombre" : carta.getNombre());
        metadataLabel.setText("Rareza: " + valor(carta.getRareza()) + " · Tipo: " + valor(carta.getTipo()));
        descriptionArea.setText(carta.getDescripcion());
        statsLabel.setText(textoStats(carta));
    }

    private String textoStats(Carta carta) {
        if (carta instanceof Unidad unidad) {
            return "ATQ " + unidad.getAtaqueEfectivo() + " · VIDA " + unidad.getVida() + "/" + unidad.getVidaMaxima();
        }
        if (carta instanceof Objeto objeto) {
            return "EFECTO " + valor(objeto.getTipoEfecto()) + " · VALOR " + objeto.getValor();
        }
        return " ";
    }

    private Color colorPara(Carta carta) {
        String rareza = carta.getRareza() == null ? "" : carta.getRareza().toLowerCase();
        String tipo = carta.getTipo() == null ? "" : carta.getTipo().toLowerCase();

        if (rareza.contains("legend")) return new Color(255, 232, 177);
        if (rareza.contains("epic") || rareza.contains("épica")) return new Color(225, 210, 255);
        if (rareza.contains("rara") || rareza.contains("rare")) return new Color(207, 231, 255);
        if (carta instanceof Unidad || tipo.contains("unidad")) return new Color(220, 245, 224);
        if (carta instanceof Objeto || tipo.contains("objeto")) return new Color(255, 232, 218);
        return new Color(238, 241, 246);
    }

    private String valor(String texto) {
        return texto == null || texto.isBlank() ? "N/D" : texto;
    }
}
