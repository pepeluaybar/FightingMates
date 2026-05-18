package fightingmates;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Parser JSON mínimo para evitar dependencias externas en el esqueleto Java actual. */
public class SimpleJsonParser {
    private final String text;
    private int pos;

    public SimpleJsonParser(String text) {
        this.text = text != null ? text : "";
        this.pos = 0;
    }

    public Object parse() throws CardLoadException {
        Object value = parseValue();
        skipWhitespace();
        if (!isAtEnd()) {
            throw error("JSON mal formado: contenido inesperado después del documento");
        }
        return value;
    }

    private Object parseValue() throws CardLoadException {
        skipWhitespace();
        if (isAtEnd()) throw error("JSON mal formado: fin de archivo inesperado");
        char c = peek();
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') return parseNull();
        if (c == '-' || Character.isDigit(c)) return parseNumber();
        throw error("JSON mal formado: valor no válido");
    }

    private Map<String, Object> parseObject() throws CardLoadException {
        expect('{');
        Map<String, Object> object = new LinkedHashMap<>();
        skipWhitespace();
        if (consumeIf('}')) return object;
        while (true) {
            skipWhitespace();
            if (peek() != '"') throw error("JSON mal formado: se esperaba una clave entre comillas");
            String key = parseString();
            skipWhitespace();
            expect(':');
            object.put(key, parseValue());
            skipWhitespace();
            if (consumeIf('}')) return object;
            expect(',');
        }
    }

    private List<Object> parseArray() throws CardLoadException {
        expect('[');
        List<Object> array = new ArrayList<>();
        skipWhitespace();
        if (consumeIf(']')) return array;
        while (true) {
            array.add(parseValue());
            skipWhitespace();
            if (consumeIf(']')) return array;
            expect(',');
        }
    }

    private String parseString() throws CardLoadException {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd()) {
            char c = text.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (isAtEnd()) throw error("JSON mal formado: escape incompleto");
                char escaped = text.charAt(pos++);
                switch (escaped) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u': sb.append(parseUnicode()); break;
                    default: throw error("JSON mal formado: escape no válido");
                }
            } else {
                sb.append(c);
            }
        }
        throw error("JSON mal formado: cadena sin cerrar");
    }

    private char parseUnicode() throws CardLoadException {
        if (pos + 4 > text.length()) throw error("JSON mal formado: unicode incompleto");
        String hex = text.substring(pos, pos + 4);
        pos += 4;
        try {
            return (char) Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            throw error("JSON mal formado: unicode no válido");
        }
    }

    private Boolean parseBoolean() throws CardLoadException {
        if (text.startsWith("true", pos)) {
            pos += 4;
            return Boolean.TRUE;
        }
        if (text.startsWith("false", pos)) {
            pos += 5;
            return Boolean.FALSE;
        }
        throw error("JSON mal formado: booleano no válido");
    }

    private Object parseNull() throws CardLoadException {
        if (!text.startsWith("null", pos)) throw error("JSON mal formado: null no válido");
        pos += 4;
        return null;
    }

    private Number parseNumber() throws CardLoadException {
        int start = pos;
        if (peek() == '-') pos++;
        while (!isAtEnd() && Character.isDigit(peek())) pos++;
        if (!isAtEnd() && peek() == '.') {
            pos++;
            while (!isAtEnd() && Character.isDigit(peek())) pos++;
        }
        String raw = text.substring(start, pos);
        try {
            return raw.contains(".") ? Double.parseDouble(raw) : Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw error("JSON mal formado: número no válido");
        }
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(text.charAt(pos))) pos++;
    }

    private void expect(char expected) throws CardLoadException {
        skipWhitespace();
        if (isAtEnd() || text.charAt(pos) != expected) {
            throw error("JSON mal formado: se esperaba '" + expected + "'");
        }
        pos++;
    }

    private boolean consumeIf(char expected) {
        skipWhitespace();
        if (!isAtEnd() && text.charAt(pos) == expected) {
            pos++;
            return true;
        }
        return false;
    }

    private char peek() {
        return text.charAt(pos);
    }

    private boolean isAtEnd() {
        return pos >= text.length();
    }

    private CardLoadException error(String message) {
        return new CardLoadException(message + " (posición " + pos + ")");
    }
}
