package ru.c0rex86.lingui.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class GuiItem {
    
    private ItemStack itemStack;
    private BiPredicate<Player, GuiPage> clickHandler;
    private BiFunction<Player, GuiPage, ItemStack> dynamicItemSupplier;
    private Map<String, Object> metadata;
    private boolean dynamic;
    
    public GuiItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.metadata = new HashMap<>();
        this.dynamic = false;
    }
    
    public GuiItem(ItemStack itemStack, BiPredicate<Player, GuiPage> clickHandler) {
        this(itemStack);
        this.clickHandler = clickHandler;
    }
    
    public GuiItem(BiFunction<Player, GuiPage, ItemStack> dynamicItemSupplier, BiPredicate<Player, GuiPage> clickHandler) {
        this(dynamicItemSupplier.apply(null, null), clickHandler);
        this.dynamicItemSupplier = dynamicItemSupplier;
        this.dynamic = true;
    }
    
    public ItemStack getItemStack() {
        return itemStack.clone();
    }
    
    public ItemStack getItemStack(Player player, GuiPage guiPage) {
        if (dynamic && dynamicItemSupplier != null) {
            return dynamicItemSupplier.apply(player, guiPage);
        }
        return getItemStack();
    }
    
    public boolean onClick(Player player, GuiPage guiPage) {
        return clickHandler != null && clickHandler.test(player, guiPage);
    }
    
    public GuiItem setClickHandler(BiPredicate<Player, GuiPage> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }
    
    public GuiItem setDynamicItemSupplier(BiFunction<Player, GuiPage, ItemStack> dynamicItemSupplier) {
        this.dynamicItemSupplier = dynamicItemSupplier;
        this.dynamic = true;
        return this;
    }
    
    public boolean isDynamic() {
        return dynamic;
    }
    
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
} 