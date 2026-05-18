package fightingmates;

/** Error controlado al cargar cartas desde JSON. */
public class CardLoadException extends Exception {
    public CardLoadException(String message) {
        super(message);
    }

    public CardLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
