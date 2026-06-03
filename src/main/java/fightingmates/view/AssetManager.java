package fightingmates.view;

import fightingmates.Carta;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.Locale;

/** Centraliza la resolución de imágenes opcionales para la interfaz JavaFX. */
public class AssetManager {
    private static final String CARD_IMAGES_BASE_PATH = "/fightingmates/images/cards/";

    public Image getCardImage(Carta carta) {
        if (carta == null) {
            return null;
        }

        Image porId = cargar(CARD_IMAGES_BASE_PATH + carta.getId() + ".png");
        if (porId != null) {
            return porId;
        }

        String nombreNormalizado = normalizarNombre(carta.getNombre());
        if (!nombreNormalizado.isBlank()) {
            return cargar(CARD_IMAGES_BASE_PATH + nombreNormalizado + ".png");
        }
        return null;
    }

    public void playSound(String soundId) {
        // Los sonidos son opcionales y no bloquean el funcionamiento del juego.
    }

    private Image cargar(String ruta) {
        InputStream recurso = getClass().getResourceAsStream(ruta);
        return recurso == null ? null : new Image(recurso, 150, 90, true, true);
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
}
