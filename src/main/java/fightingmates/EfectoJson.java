package fightingmates;

public class EfectoJson {
    private String type;
    private String target;
    private Integer value;
    private String textValue;
    private String status;
    private String description;

    public EfectoJson() {
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
    public String getTextValue() { return textValue; }
    public void setTextValue(String textValue) { this.textValue = textValue; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}