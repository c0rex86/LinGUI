package ru.c0rex86.lingui.config;

import java.util.List;
import java.util.Map;

public class ItemConfig {
    
    private String material;
    private String name;
    private List<String> lore;
    private int amount;
    private Map<String, Integer> enchants;
    private List<String> flags;
    private String script;
    
    public ItemConfig() {
        this.material = "STONE";
        this.amount = 1;
    }
    
    public String getMaterial() {
        return material;
    }
    
    public void setMaterial(String material) {
        this.material = material;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<String> getLore() {
        return lore;
    }
    
    public void setLore(List<String> lore) {
        this.lore = lore;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public void setAmount(int amount) {
        this.amount = Math.max(1, Math.min(64, amount));
    }
    
    public Map<String, Integer> getEnchants() {
        return enchants;
    }
    
    public void setEnchants(Map<String, Integer> enchants) {
        this.enchants = enchants;
    }
    
    public List<String> getFlags() {
        return flags;
    }
    
    public void setFlags(List<String> flags) {
        this.flags = flags;
    }
    
    public String getScript() {
        return script;
    }
    
    public void setScript(String script) {
        this.script = script;
    }
    
    public boolean hasScript() {
        return script != null && !script.isEmpty();
    }
} 