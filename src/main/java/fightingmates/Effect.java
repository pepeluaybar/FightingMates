package fightingmates;

/**
 * Efecto declarativo cargado desde JSON.
 * Conserva los datos originales de la carta aunque la v1 solo ejecute
 * algunos efectos como habilidades u objetos del motor actual.
 */
public class Effect {
    private String type;
    private String target;
    private int value;
    private String textValue;
    private String description;

    public Effect() {
        this("", "", 0, "", "");
    }

    public Effect(String type, String target, int value, String textValue, String description) {
        this.type = type != null ? type : "";
        this.target = target != null ? target : "";
        this.value = value;
        this.textValue = textValue != null ? textValue : "";
        this.description = description != null ? description : "";
    }

    public Effect(Effect other) {
        this(other.type, other.target, other.value, other.textValue, other.description);
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type != null ? type : ""; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target != null ? target : ""; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public String getTextValue() { return textValue; }
    public void setTextValue(String textValue) { this.textValue = textValue != null ? textValue : ""; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description != null ? description : ""; }

    @Override
    public String toString() {
        String valueText = !textValue.isEmpty() ? textValue : String.valueOf(value);
        return "Effect{type='" + type + "', target='" + target + "', value='" + valueText + "'}";
    }
}
