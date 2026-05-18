package fightingmates;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Carga cartas del juego desde un archivo JSON y las adapta al motor actual. */
public class JsonCardLoader {
    public static final String DEFAULT_CARDS_PATH = "resources/cards/cards.json";

    private static final Set<String> SUPPORTED_EFFECTS = new HashSet<>(Arrays.asList(
            "damage", "heal", "status", "apply_status", "bonus_attack", "player_damage",
            "buff_damage", "buff_defense", "buff_technique", "clear_status", "reduce_damage",
            "reduce_next_damage", "skip_next_turn", "evade_next_effect", "random_effect",
            "damage_percent_max_hp", "heal_percent_max_hp", "summon", "conditional_immunity",
            "destroy_all_cards", "destroy_random_card", "return_to_hand"
    ));

    public List<Carta> load(String pathText) throws CardLoadException {
        String text = readJson(pathText);
        Object root = new SimpleJsonParser(text).parse();
        if (!(root instanceof List)) {
            throw new CardLoadException("JSON mal formado: el archivo de cartas debe contener una lista de cartas");
        }

        List<?> rawCards = (List<?>) root;
        List<Carta> cards = new ArrayList<>();
        int nextId = 1;
        for (int i = 0; i < rawCards.size(); i++) {
            if (!(rawCards.get(i) instanceof Map)) {
                throw new CardLoadException("Campos obligatorios vacíos: la carta #" + (i + 1) + " no es un objeto JSON");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> cardData = (Map<String, Object>) rawCards.get(i);
            int copies = Math.max(1, getInt(cardData, "copies", 1));
            for (int copy = 0; copy < copies; copy++) {
                cards.add(toCard(cardData, nextId++, i + 1));
            }
        }
        return cards;
    }

    private String readJson(String pathText) throws CardLoadException {
        String normalizedPath = isBlank(pathText) ? DEFAULT_CARDS_PATH : pathText;
        Path path = Paths.get(normalizedPath);
        if (Files.exists(path)) {
            try {
                return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new CardLoadException("No se pudo leer el archivo JSON de cartas: " + normalizedPath, e);
            }
        }

        InputStream resource = JsonCardLoader.class.getClassLoader().getResourceAsStream(normalizedPath);
        if (resource != null) {
            try {
                byte[] data = resource.readAllBytes();
                return new String(data, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new CardLoadException("No se pudo leer el recurso JSON de cartas: " + normalizedPath, e);
            }
        }

        throw new CardLoadException("Archivo no encontrado: " + normalizedPath);
    }

    private Carta toCard(Map<String, Object> cardData, int id, int cardNumber) throws CardLoadException {
        String name = requiredText(cardData, "name", cardNumber);
        String rarity = requiredText(cardData, "rarity", cardNumber);
        String type = requiredText(cardData, "type", cardNumber);
        String target = requiredText(cardData, "target", cardNumber);
        String timing = requiredText(cardData, "timing", cardNumber);
        String description = requiredText(cardData, "description", cardNumber);
        List<Effect> effects = parseEffects(cardData, cardNumber);

        String cardClass = optionalText(cardData, "cardClass", optionalText(cardData, "class", "unit"));
        Carta card;
        if (isObjectClass(cardClass)) {
            card = createObject(id, name, description, effects);
        } else {
            card = createUnit(id, name, description, cardData, effects);
        }
        card.setRarity(rarity);
        card.setType(type);
        card.setTarget(target);
        card.setTiming(timing);
        card.setEffects(effects);
        return card;
    }

    private Unidad createUnit(int id, String name, String description, Map<String, Object> cardData, List<Effect> effects) {
        int attack = getNestedInt(cardData, "stats", "attack", getInt(cardData, "attack", 1));
        int health = getNestedInt(cardData, "stats", "health", getInt(cardData, "health", 5));
        Habilidad ability = createAbility(name, effects);
        return new Unidad(id, name, description, attack, health, ability, "", 0, true);
    }

    private Objeto createObject(int id, String name, String description, List<Effect> effects) {
        Effect mainEffect = effects.isEmpty() ? new Effect() : effects.get(0);
        String objectEffect = toObjectEffectType(mainEffect.getType());
        return new Objeto(id, name, description, objectEffect, mainEffect.getValue());
    }

    private Habilidad createAbility(String cardName, List<Effect> effects) {
        if (effects.isEmpty()) return null;
        Effect effect = effects.get(0);
        String type = normalize(effect.getType());
        String effectDescription = !isBlank(effect.getDescription()) ? effect.getDescription() : "Efecto de " + cardName;
        String abilityName = extractAbilityName(effectDescription, cardName);
        switch (type) {
            case "damage":
            case "damage_percent_max_hp":
                return new HabilidadDanio(abilityName, effectDescription, effect.getValue());
            case "heal":
            case "heal_percent_max_hp":
                return new HabilidadCura(abilityName, effectDescription, effect.getValue(), isAllyTarget(effect.getTarget()));
            case "status":
            case "apply_status":
                String status = !isBlank(effect.getTextValue()) ? effect.getTextValue() : abilityName;
                return new HabilidadEstado(abilityName, effectDescription, status, Math.max(1, effect.getValue()));
            default:
                return null;
        }
    }

    private List<Effect> parseEffects(Map<String, Object> cardData, int cardNumber) throws CardLoadException {
        if (!cardData.containsKey("effects") || cardData.get("effects") == null) {
            throw new CardLoadException("Campos obligatorios vacíos: la carta #" + cardNumber + " debe tener 'effects' como lista, aunque esté vacía");
        }
        if (!(cardData.get("effects") instanceof List)) {
            throw new CardLoadException("Campos obligatorios vacíos: 'effects' de la carta #" + cardNumber + " debe ser una lista");
        }

        List<Effect> effects = new ArrayList<>();
        List<?> rawEffects = (List<?>) cardData.get("effects");
        for (int i = 0; i < rawEffects.size(); i++) {
            if (!(rawEffects.get(i) instanceof Map)) {
                throw new CardLoadException("JSON mal formado: el efecto #" + (i + 1) + " de la carta #" + cardNumber + " no es un objeto");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> effectData = (Map<String, Object>) rawEffects.get(i);
            String type = requiredText(effectData, "type", cardNumber);
            if (!SUPPORTED_EFFECTS.contains(normalize(type))) {
                throw new CardLoadException("Efecto desconocido: '" + type + "' en la carta #" + cardNumber);
            }
            String target = optionalText(effectData, "target", "");
            String description = optionalText(effectData, "description", "");
            Object rawValue = effectData.get("value");
            int value = toInt(rawValue, 0);
            String textValue = rawValue instanceof String ? ((String) rawValue).trim() : optionalText(effectData, "status", "");
            effects.add(new Effect(type, target, value, textValue, description));
        }
        return effects;
    }

    private String toObjectEffectType(String effectType) {
        switch (normalize(effectType)) {
            case "damage": return "DANIO";
            case "heal": return "CURA";
            case "player_damage": return "DANIO_JUGADOR";
            case "bonus_attack":
            case "buff_damage": return "BONUS_ATAQUE";
            default: return normalize(effectType).toUpperCase(Locale.ROOT);
        }
    }

    private String extractAbilityName(String effectDescription, String cardName) {
        int separator = effectDescription.indexOf(':');
        if (separator > 0) return effectDescription.substring(0, separator).trim();
        return cardName;
    }

    private boolean isObjectClass(String cardClass) {
        String normalized = normalize(cardClass);
        return "object".equals(normalized) || "objeto".equals(normalized) || "item".equals(normalized);
    }

    private boolean isAllyTarget(String target) {
        String normalized = normalize(target);
        return "ally".equals(normalized) || "all_allies".equals(normalized) || "self".equals(normalized)
                || "aliado".equals(normalized) || "jugador".equals(normalized);
    }

    private String requiredText(Map<String, Object> data, String field, int cardNumber) throws CardLoadException {
        String value = optionalText(data, field, "");
        if (isBlank(value)) {
            throw new CardLoadException("Campos obligatorios vacíos: '" + field + "' en la carta #" + cardNumber);
        }
        return value;
    }

    private String optionalText(Map<String, Object> data, String field, String defaultValue) {
        Object value = data.get(field);
        return value instanceof String ? ((String) value).trim() : defaultValue;
    }

    private int getNestedInt(Map<String, Object> data, String objectField, String numberField, int defaultValue) {
        Object nested = data.get(objectField);
        if (!(nested instanceof Map)) return defaultValue;
        @SuppressWarnings("unchecked")
        Map<String, Object> nestedMap = (Map<String, Object>) nested;
        return getInt(nestedMap, numberField, defaultValue);
    }

    private int getInt(Map<String, Object> data, String field, int defaultValue) {
        return toInt(data.get(field), defaultValue);
    }

    private int toInt(Object value, int defaultValue) {
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
