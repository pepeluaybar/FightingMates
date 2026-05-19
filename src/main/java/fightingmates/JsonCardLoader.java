package fightingmates;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Locale;

/** Carga cartas del juego desde JSON con Gson y clases simples. */
public class JsonCardLoader {
    public static final String DEFAULT_CARDS_PATH = "resources/cards/cards.json";

    public ArrayList<Carta> load(String pathText) throws CardLoadException {
        String normalizedPath = isBlank(pathText) ? DEFAULT_CARDS_PATH : pathText;
        File file = new File(normalizedPath);
        if (!file.exists()) {
            throw new CardLoadException("Archivo no encontrado: " + normalizedPath);
        }

        CartaJson[] cardsData;
        try (FileReader reader = new FileReader(file)) {
            cardsData = new Gson().fromJson(reader, CartaJson[].class);
        } catch (FileNotFoundException e) {
            throw new CardLoadException("No se pudo leer el archivo JSON de cartas: " + normalizedPath, e);
        } catch (JsonSyntaxException e) {
            throw new CardLoadException("JSON mal formado: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CardLoadException("Error leyendo el JSON de cartas: " + normalizedPath, e);
        }

        if (cardsData == null) {
            throw new CardLoadException("JSON mal formado: el archivo de cartas debe contener una lista de cartas");
        }

        ArrayList<Carta> cards = new ArrayList<>();
        int nextId = 1;
        for (int i = 0; i < cardsData.length; i++) {
            CartaJson cardData = cardsData[i];
            int copies = cardData != null && cardData.getCopies() != null ? Math.max(1, cardData.getCopies()) : 1;
            for (int copy = 0; copy < copies; copy++) {
                cards.add(toCard(cardData, nextId++, i + 1));
            }
        }
        return cards;
    }

    private Carta toCard(CartaJson data, int id, int cardNumber) throws CardLoadException {
        if (data == null) {
            throw new CardLoadException("JSON mal formado: la carta #" + cardNumber + " no es un objeto JSON");
        }

        String name = requiredText(data.getName(), "name", cardNumber);
        String rarity = requiredText(data.getRarity(), "rarity", cardNumber);
        String type = requiredText(data.getType(), "type", cardNumber);
        String target = requiredText(data.getTarget(), "target", cardNumber);
        String timing = requiredText(data.getTiming(), "timing", cardNumber);
        String description = requiredText(data.getDescription(), "description", cardNumber);
        ArrayList<Effect> effects = parseEffects(data.getEffects(), cardNumber);

        String rawClass = !isBlank(data.getCardClass()) ? data.getCardClass() : data.getClazz();
        String cardClass = isBlank(rawClass) ? "unit" : rawClass;

        Carta card;
        if (isObjectClass(cardClass)) {
            card = createObject(id, name, description, effects);
        } else {
            card = createUnit(id, name, description, data, effects);
        }

        card.setRarity(rarity);
        card.setType(type);
        card.setTarget(target);
        card.setTiming(timing);
        card.setEffects(effects);
        return card;
    }

    private Unidad createUnit(int id, String name, String description, CartaJson data, ArrayList<Effect> effects) {
        int attack = getAttack(data);
        int health = getHealth(data);
        Habilidad ability = createAbility(name, effects);
        return new Unidad(id, name, description, attack, health, ability, "", 0, true);
    }

    private int getAttack(CartaJson data) {
        if (data.getStats() != null && data.getStats().getAttack() != null) return data.getStats().getAttack();
        if (data.getAttack() != null) return data.getAttack();
        return 1;
    }

    private int getHealth(CartaJson data) {
        if (data.getStats() != null && data.getStats().getHealth() != null) return data.getStats().getHealth();
        if (data.getHealth() != null) return data.getHealth();
        return 5;
    }

    private Objeto createObject(int id, String name, String description, ArrayList<Effect> effects) {
        Effect mainEffect = effects.isEmpty() ? new Effect() : effects.get(0);
        String objectEffect = toObjectEffectType(mainEffect.getType());
        return new Objeto(id, name, description, objectEffect, mainEffect.getValue());
    }

    private Habilidad createAbility(String cardName, ArrayList<Effect> effects) {
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

    private ArrayList<Effect> parseEffects(ArrayList<EffectJson> rawEffects, int cardNumber) throws CardLoadException {
        if (rawEffects == null) {
            throw new CardLoadException("Campos obligatorios vacíos: la carta #" + cardNumber + " debe tener 'effects' como lista, aunque esté vacía");
        }

        ArrayList<Effect> effects = new ArrayList<>();
        for (int i = 0; i < rawEffects.size(); i++) {
            EffectJson effectData = rawEffects.get(i);
            if (effectData == null) {
                throw new CardLoadException("JSON mal formado: el efecto #" + (i + 1) + " de la carta #" + cardNumber + " no es un objeto");
            }
            String type = requiredText(effectData.getType(), "type", cardNumber);
            if (!isSupportedEffect(type)) {
                throw new CardLoadException("Efecto desconocido: '" + type + "' en la carta #" + cardNumber);
            }

            String target = safeText(effectData.getTarget());
            String description = safeText(effectData.getDescription());
            int value = effectData.getValue() != null ? effectData.getValue() : 0;
            String textValue = !isBlank(effectData.getTextValue()) ? effectData.getTextValue() : safeText(effectData.getStatus());
            effects.add(new Effect(type, target, value, textValue, description));
        }
        return effects;
    }

    private boolean isSupportedEffect(String type) {
        switch (normalize(type)) {
            case "damage":
            case "heal":
            case "status":
            case "apply_status":
            case "bonus_attack":
            case "player_damage":
            case "buff_damage":
            case "buff_defense":
            case "buff_technique":
            case "clear_status":
            case "reduce_damage":
            case "reduce_next_damage":
            case "skip_next_turn":
            case "evade_next_effect":
            case "random_effect":
            case "damage_percent_max_hp":
            case "heal_percent_max_hp":
            case "summon":
            case "conditional_immunity":
            case "destroy_all_cards":
            case "destroy_random_card":
            case "return_to_hand":
                return true;
            default:
                return false;
        }
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

    private String requiredText(String value, String field, int cardNumber) throws CardLoadException {
        String safe = safeText(value);
        if (isBlank(safe)) {
            throw new CardLoadException("Campos obligatorios vacíos: '" + field + "' en la carta #" + cardNumber);
        }
        return safe;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
