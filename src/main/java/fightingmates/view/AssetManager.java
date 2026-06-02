package fightingmates.view;

import fightingmates.Carta;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Centraliza la resolución de recursos visuales y sonoros para evitar rutas dispersas en la vista.
 */
public class AssetManager {
    public static final String CARD_IMAGES_BASE_PATH = "resources/images/cards/";
    public static final String AUDIO_BASE_PATH = "resources/audio/";

    private static final String CLASSPATH_CARD_IMAGES_BASE_PATH = "images/cards/";
    private static final ImageIcon PLACEHOLDER = crearPlaceholder();

    public ImageIcon getCardImage(Carta carta) {
        if (carta == null) {
            return PLACEHOLDER;
        }

        ImageIcon porId = buscarImagen(carta.getId() + ".png");
        if (porId != null) {
            return porId;
        }

        String nombreNormalizado = normalizarNombre(carta.getNombre());
        if (!nombreNormalizado.isBlank()) {
            ImageIcon porNombre = buscarImagen(nombreNormalizado + ".png");
            if (porNombre != null) {
                return porNombre;
            }
        }

        return PLACEHOLDER;
    }

    public void playSound(String soundId) {
        // Reservado para una futura integración de audio cuando existan recursos grabados.
    }

    private ImageIcon buscarImagen(String nombreArchivo) {
        ImageIcon desdeRutaPublica = cargarDesdeClasspath(CARD_IMAGES_BASE_PATH + nombreArchivo);
        return desdeRutaPublica != null
                ? desdeRutaPublica
                : cargarDesdeClasspath(CLASSPATH_CARD_IMAGES_BASE_PATH + nombreArchivo);
    }

    private ImageIcon cargarDesdeClasspath(String ruta) {
        URL recurso = Thread.currentThread().getContextClassLoader().getResource(ruta);
        return recurso == null ? null : new ImageIcon(recurso);
    }

    private String normalizarNombre(String nombre) {
        if (nombre == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private static ImageIcon crearPlaceholder() {
        BufferedImage imagen = new BufferedImage(160, 110, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(39, 48, 63));
        g.fillRoundRect(0, 0, 159, 109, 16, 16);
        g.setColor(new Color(111, 126, 153));
        g.drawRoundRect(3, 3, 153, 103, 14, 14);
        g.setColor(new Color(219, 226, 239));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.drawString("Sin imagen", 43, 58);
        g.dispose();
        return new ImageIcon(imagen);
    }
}
