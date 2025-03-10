package ru.c0rex86.lingui.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.c0rex86.lingui.gui.GuiBuilder;
import ru.c0rex86.lingui.gui.GuiPage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface GuiManager {
    
    GuiBuilder createGui(String id, String title, int rows);
    
    boolean registerGui(GuiPage gui);
    
    boolean unregisterGui(String id);
    
    Optional<GuiPage> getGui(String id);
    
    List<String> getRegisteredGuiIds();
    
    boolean openGui(Player player, String guiId);
    
    boolean closeGui(Player player);
    
    void closeAllGuis();
    
    Optional<GuiPage> getCurrentGui(UUID playerUuid);
    
    boolean handleGuiClick(Player player, int slot, ItemStack clickedItem);
    
    boolean isGuiOpen(UUID playerUuid);
    
    Map<UUID, GuiPage> getOpenGuis();
    
    void reloadGuis();
    
    GuiBuilder loadGuiFromConfig(String guiId);
    
    boolean saveGuiToConfig(GuiPage gui);
} 