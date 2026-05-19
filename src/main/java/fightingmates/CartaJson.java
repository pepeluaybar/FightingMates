package fightingmates;

import java.util.ArrayList;

public class CartaJson {
    private String name;
    private String rarity;
    private String type;
    private String target;
    private String timing;
    private String description;
    private String cardClass;
    private String clazz;
    private Integer copies;
    private Integer attack;
    private Integer health;
    private StatsJson stats;
    private ArrayList<EffectJson> effects;

    public CartaJson() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getTiming() { return timing; }
    public void setTiming(String timing) { this.timing = timing; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCardClass() { return cardClass; }
    public void setCardClass(String cardClass) { this.cardClass = cardClass; }
    public String getClazz() { return clazz; }
    public void setClazz(String clazz) { this.clazz = clazz; }
    public Integer getCopies() { return copies; }
    public void setCopies(Integer copies) { this.copies = copies; }
    public Integer getAttack() { return attack; }
    public void setAttack(Integer attack) { this.attack = attack; }
    public Integer getHealth() { return health; }
    public void setHealth(Integer health) { this.health = health; }
    public StatsJson getStats() { return stats; }
    public void setStats(StatsJson stats) { this.stats = stats; }
    public ArrayList<EffectJson> getEffects() { return effects; }
    public void setEffects(ArrayList<EffectJson> effects) { this.effects = effects; }
}
