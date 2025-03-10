package ru.c0rex86.lingui.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import ru.c0rex86.lingui.LinGUi;

public class GuiListener implements Listener {
    
    private final LinGUi plugin;
    
    public GuiListener(LinGUi plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        if (plugin.getGuiManager().isGuiOpen(player.getUniqueId())) {
            event.setCancelled(true);
            
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < event.getInventory().getSize()) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null) {
                    plugin.getGuiManager().handleGuiClick(player, slot, clickedItem);
                }
            }
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        if (plugin.getGuiManager().isGuiOpen(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        if (plugin.getGuiManager().isGuiOpen(player.getUniqueId())) {
            plugin.getGuiManager().closeGui(player);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getGuiManager().isGuiOpen(player.getUniqueId())) {
            plugin.getGuiManager().closeGui(player);
        }
    }
} 