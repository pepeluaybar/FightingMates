package fightingmates.view;

import fightingmates.Carta;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Punto centralizado para resolver recursos visuales y de audio de la interfaz.
 */
public final class AssetManager {
    public static final String CARD_IMAGES_BASE_PATH = "resources/images/cards/";
    public static final String AUDIO_BASE_PATH = "resources/audio/";

    private static final String[] IMAGE_EXTENSIONS = {".png", ".jpg", ".jpeg", ".gif"};
    private static final String[] AUDIO_EXTENSIONS = {".wav", ".aiff", ".au"};
    private static final int PLACEHOLDER_WIDTH = 180;
    private static final int PLACEHOLDER_HEIGHT = 250;

    private final ImageIcon cardPlaceholder;

    public AssetManager() {
        this.cardPlaceholder = crearPlaceholderCarta();
    }

    /**
     * Busca la imagen de una carta por su id y, como alternativa, por su nombre normalizado.
     * Devuelve siempre un icono válido para que las vistas no tengan que duplicar lógica de fallback.
     */
    public ImageIcon getCardImage(Carta carta) {
        if (carta == null) {
            return cardPlaceholder;
        }

        for (String ruta : obtenerRutasCandidatasCarta(carta)) {
            URL recurso = resolverRecurso(ruta);
            if (recurso != null) {
                return new ImageIcon(recurso);
            }
        }

        return cardPlaceholder;
    }

    /**
     * Método preparado para reproducir sonidos cuando existan grabaciones en resources/audio/.
     */
    public void playSound(String soundId) {
        String idNormalizado = normalizarNombreArchivo(soundId);
        if (idNormalizado.isEmpty()) {
            return;
        }

        for (String extension : AUDIO_EXTENSIONS) {
            URL recurso = resolverRecurso(AUDIO_BASE_PATH + idNormalizado + extension);
            if (recurso != null) {
                reproducirClip(recurso);
                return;
            }
        }
    }

    private List<String> obtenerRutasCandidatasCarta(Carta carta) {
        List<String> rutas = new ArrayList<>();

        if (carta.getId() > 0) {
            for (String extension : IMAGE_EXTENSIONS) {
                rutas.add(CARD_IMAGES_BASE_PATH + carta.getId() + extension);
            }
        }

        String nombreNormalizado = normalizarNombreArchivo(carta.getNombre());
        if (!nombreNormalizado.isEmpty()) {
            for (String extension : IMAGE_EXTENSIONS) {
                rutas.add(CARD_IMAGES_BASE_PATH + nombreNormalizado + extension);
            }
        }

        return rutas;
    }

    private URL resolverRecurso(String ruta) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        URL recurso = contextClassLoader != null ? contextClassLoader.getResource(ruta) : null;
        if (recurso != null) {
            return recurso;
        }

        recurso = AssetManager.class.getClassLoader().getResource(ruta);
        if (recurso != null) {
            return recurso;
        }

        try {
            File archivo = new File(ruta);
            return archivo.isFile() ? archivo.toURI().toURL() : null;
        } catch (IOException ex) {
            return null;
        }
    }

    private void reproducirClip(URL recurso) {
        try (AudioInputStream audio = AudioSystem.getAudioInputStream(recurso)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            System.err.println("No se pudo reproducir el sonido: " + recurso);
        }
    }

    private String normalizarNombreArchivo(String texto) {
        if (texto == null) {
            return "";
        }

        String sinAcentos = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private ImageIcon crearPlaceholderCarta() {
        BufferedImage imagen = new BufferedImage(
                PLACEHOLDER_WIDTH,
                PLACEHOLDER_HEIGHT,
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D graphics = imagen.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(42, 48, 66));
            graphics.fillRoundRect(0, 0, PLACEHOLDER_WIDTH - 1, PLACEHOLDER_HEIGHT - 1, 18, 18);
            graphics.setColor(new Color(111, 124, 154));
            graphics.drawRoundRect(4, 4, PLACEHOLDER_WIDTH - 9, PLACEHOLDER_HEIGHT - 9, 16, 16);
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            graphics.setColor(Color.WHITE);
            dibujarTextoCentrado(graphics, "SIN IMAGEN", PLACEHOLDER_HEIGHT / 2 - 8);
            graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            graphics.setColor(new Color(210, 216, 230));
            dibujarTextoCentrado(graphics, "Fighting Mates", PLACEHOLDER_HEIGHT / 2 + 18);
        } finally {
            graphics.dispose();
        }
        return new ImageIcon(imagen);
    }

    private void dibujarTextoCentrado(Graphics2D graphics, String texto, int y) {
        FontMetrics metrics = graphics.getFontMetrics();
        int x = (PLACEHOLDER_WIDTH - metrics.stringWidth(texto)) / 2;
        graphics.drawString(texto, x, y);
    }
}
