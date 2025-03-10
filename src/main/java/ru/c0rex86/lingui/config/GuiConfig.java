package ru.c0rex86.lingui.config;

import java.util.HashMap;
import java.util.Map;

public class GuiConfig {
    
    private String title;
    private int rows;
    private Map<Integer, ItemConfig> items;
    
    public GuiConfig() {
        this.title = "GUI";
        this.rows = 3;
        this.items = new HashMap<>();
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public int getRows() {
        return rows;
    }
    
    public void setRows(int rows) {
        this.rows = Math.min(6, Math.max(1, rows));
    }
    
    public Map<Integer, ItemConfig> getItems() {
        return items;
    }
    
    public void setItems(Map<Integer, ItemConfig> items) {
        this.items = items;
    }
    
    public void addItem(int slot, ItemConfig item) {
        items.put(slot, item);
    }
    
    public ItemConfig getItem(int slot) {
        return items.get(slot);
    }
    
    public void removeItem(int slot) {
        items.remove(slot);
    }
} 