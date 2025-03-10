package ru.c0rex86.lingui.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class GuiBuilder {
    
    private final GuiPage guiPage;
    
    public GuiBuilder(String id, String title, int rows) {
        this.guiPage = new GuiPage(id, title, rows);
    }
    
    public GuiBuilder setItem(int slot, ItemStack itemStack) {
        guiPage.setItem(slot, new GuiItem(itemStack));
        return this;
    }
    
    public GuiBuilder setItem(int slot, ItemStack itemStack, BiPredicate<Player, GuiPage> clickHandler) {
        guiPage.setItem(slot, new GuiItem(itemStack, clickHandler));
        return this;
    }
    
    public GuiBuilder setDynamicItem(int slot, BiFunction<Player, GuiPage, ItemStack> itemSupplier, BiPredicate<Player, GuiPage> clickHandler) {
        guiPage.setItem(slot, new GuiItem(itemSupplier, clickHandler));
        return this;
    }
    
    public GuiBuilder setItemMetadata(int slot, String key, Object value) {
        guiPage.getItem(slot).ifPresent(item -> item.setMetadata(key, value));
        return this;
    }
    
    public GuiBuilder fillRow(int row, ItemStack itemStack) {
        if (row >= 0 && row < guiPage.getRows()) {
            for (int i = 0; i < 9; i++) {
                setItem(row * 9 + i, itemStack);
            }
        }
        return this;
    }
    
    public GuiBuilder fillColumn(int column, ItemStack itemStack) {
        if (column >= 0 && column < 9) {
            for (int i = 0; i < guiPage.getRows(); i++) {
                setItem(i * 9 + column, itemStack);
            }
        }
        return this;
    }
    
    public GuiBuilder fillBorder(ItemStack itemStack) {
        fillRow(0, itemStack);
        fillRow(guiPage.getRows() - 1, itemStack);
        
        for (int i = 1; i < guiPage.getRows() - 1; i++) {
            setItem(i * 9, itemStack);
            setItem(i * 9 + 8, itemStack);
        }
        
        return this;
    }
    
    public GuiBuilder fillEmpty(ItemStack itemStack) {
        for (int i = 0; i < guiPage.getRows() * 9; i++) {
            if (!guiPage.getItem(i).isPresent()) {
                setItem(i, itemStack);
            }
        }
        return this;
    }
    
    public GuiBuilder setOpenAction(Consumer<Player> openAction) {
        guiPage.setOpenAction(openAction);
        return this;
    }
    
    public GuiBuilder setCloseAction(Consumer<Player> closeAction) {
        guiPage.setCloseAction(closeAction);
        return this;
    }
    
    public GuiPage build() {
        return guiPage;
    }
} 