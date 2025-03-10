package ru.c0rex86.lingui.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class GuiPage {
    
    private final String id;
    private String title;
    private final int rows;
    private final Inventory inventory;
    private final Map<Integer, GuiItem> items;
    private final Map<UUID, Map<String, Object>> playerData;
    private Consumer<Player> openAction;
    private Consumer<Player> closeAction;
    
    public GuiPage(String id, String title, int rows) {
        this.id = id;
        this.title = title;
        this.rows = Math.min(6, Math.max(1, rows));
        this.inventory = Bukkit.createInventory(null, this.rows * 9, title);
        this.items = new HashMap<>();
        this.playerData = new HashMap<>();
    }
    
    public String getId() {
        return id;
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
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public Map<Integer, GuiItem> getItems() {
        return new HashMap<>(items);
    }
    
    public void setItem(int slot, GuiItem item) {
        if (slot >= 0 && slot < rows * 9) {
            items.put(slot, item);
            inventory.setItem(slot, item.getItemStack());
        }
    }
    
    public void removeItem(int slot) {
        items.remove(slot);
        inventory.setItem(slot, null);
    }
    
    public Optional<GuiItem> getItem(int slot) {
        return Optional.ofNullable(items.get(slot));
    }
    
    public void clear() {
        items.clear();
        inventory.clear();
    }
    
    public void setOpenAction(Consumer<Player> openAction) {
        this.openAction = openAction;
    }
    
    public void setCloseAction(Consumer<Player> closeAction) {
        this.closeAction = closeAction;
    }
    
    public void onOpen(Player player) {
        playerData.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        if (openAction != null) {
            openAction.accept(player);
        }
    }
    
    public void onClose(Player player) {
        if (closeAction != null) {
            closeAction.accept(player);
        }
        playerData.remove(player.getUniqueId());
    }
    
    public Map<String, Object> getPlayerData(UUID playerUuid) {
        return playerData.getOrDefault(playerUuid, new HashMap<>());
    }
    
    public void setPlayerData(UUID playerUuid, String key, Object value) {
        playerData.computeIfAbsent(playerUuid, k -> new HashMap<>()).put(key, value);
    }
    
    public Optional<Object> getPlayerData(UUID playerUuid, String key) {
        return Optional.ofNullable(getPlayerData(playerUuid).get(key));
    }
    
    public boolean handleClick(Player player, int slot, ItemStack clickedItem) {
        return getItem(slot).map(item -> {
            return item.onClick(player, this);
        }).orElse(false);
    }
    
    public void update(Player player) {
        for (Map.Entry<Integer, GuiItem> entry : items.entrySet()) {
            GuiItem item = entry.getValue();
            if (item.isDynamic()) {
                ItemStack updatedItem = item.getItemStack(player, this);
                inventory.setItem(entry.getKey(), updatedItem);
            }
        }
    }
} 