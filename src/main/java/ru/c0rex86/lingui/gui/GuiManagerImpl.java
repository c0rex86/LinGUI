package ru.c0rex86.lingui.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.c0rex86.lingui.LinGUi;
import ru.c0rex86.lingui.api.GuiManager;
import ru.c0rex86.lingui.config.GuiConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GuiManagerImpl implements GuiManager {
    
    private final LinGUi plugin;
    private final Map<String, GuiPage> registeredGuis;
    private final Map<UUID, GuiPage> openGuis;
    
    public GuiManagerImpl(LinGUi plugin) {
        this.plugin = plugin;
        this.registeredGuis = new HashMap<>();
        this.openGuis = new ConcurrentHashMap<>();
        loadDefaultGuis();
    }
    
    private void loadDefaultGuis() {
        registerGui(createGui("main_menu", "Main Menu", 3)
                .fillBorder(ItemUtils.createItem("BLACK_STAINED_GLASS_PANE", " "))
                .build());
    }
    
    @Override
    public GuiBuilder createGui(String id, String title, int rows) {
        return new GuiBuilder(id, title, rows);
    }
    
    @Override
    public boolean registerGui(GuiPage gui) {
        if (registeredGuis.containsKey(gui.getId())) {
            return false;
        }
        registeredGuis.put(gui.getId(), gui);
        return true;
    }
    
    @Override
    public boolean unregisterGui(String id) {
        if (!registeredGuis.containsKey(id)) {
            return false;
        }
        
        closeGuiForPlayers(id);
        registeredGuis.remove(id);
        return true;
    }
    
    private void closeGuiForPlayers(String id) {
        for (Iterator<Map.Entry<UUID, GuiPage>> it = openGuis.entrySet().iterator(); it.hasNext();) {
            Map.Entry<UUID, GuiPage> entry = it.next();
            if (entry.getValue().getId().equals(id)) {
                Player player = plugin.getServer().getPlayer(entry.getKey());
                if (player != null) {
                    player.closeInventory();
                }
                it.remove();
            }
        }
    }
    
    @Override
    public Optional<GuiPage> getGui(String id) {
        return Optional.ofNullable(registeredGuis.get(id));
    }
    
    @Override
    public List<String> getRegisteredGuiIds() {
        return new ArrayList<>(registeredGuis.keySet());
    }
    
    @Override
    public boolean openGui(Player player, String guiId) {
        Optional<GuiPage> optionalGui = getGui(guiId);
        if (!optionalGui.isPresent()) {
            return false;
        }
        
        GuiPage gui = optionalGui.get();
        closeGui(player);
        
        gui.update(player);
        player.openInventory(gui.getInventory());
        openGuis.put(player.getUniqueId(), gui);
        gui.onOpen(player);
        
        return true;
    }
    
    @Override
    public boolean closeGui(Player player) {
        UUID playerUuid = player.getUniqueId();
        if (openGuis.containsKey(playerUuid)) {
            GuiPage gui = openGuis.get(playerUuid);
            gui.onClose(player);
            openGuis.remove(playerUuid);
            return true;
        }
        return false;
    }
    
    @Override
    public void closeAllGuis() {
        for (Iterator<Map.Entry<UUID, GuiPage>> it = openGuis.entrySet().iterator(); it.hasNext();) {
            Map.Entry<UUID, GuiPage> entry = it.next();
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null) {
                player.closeInventory();
                entry.getValue().onClose(player);
            }
            it.remove();
        }
    }
    
    @Override
    public Optional<GuiPage> getCurrentGui(UUID playerUuid) {
        return Optional.ofNullable(openGuis.get(playerUuid));
    }
    
    @Override
    public boolean handleGuiClick(Player player, int slot, ItemStack clickedItem) {
        return getCurrentGui(player.getUniqueId())
                .map(gui -> gui.handleClick(player, slot, clickedItem))
                .orElse(false);
    }
    
    @Override
    public boolean isGuiOpen(UUID playerUuid) {
        return openGuis.containsKey(playerUuid);
    }
    
    @Override
    public Map<UUID, GuiPage> getOpenGuis() {
        return new HashMap<>(openGuis);
    }
    
    @Override
    public void reloadGuis() {
        Map<UUID, GuiPage> currentOpenGuis = new HashMap<>(openGuis);
        closeAllGuis();
        
        registeredGuis.clear();
        loadDefaultGuis();
        
        plugin.getConfigManager().getGuiConfigs().forEach((id, config) -> {
            try {
                GuiBuilder builder = loadGuiFromConfig(id);
                registerGui(builder.build());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load GUI: " + id + ". " + e.getMessage());
            }
        });
        
        currentOpenGuis.forEach((playerUuid, gui) -> {
            Player player = plugin.getServer().getPlayer(playerUuid);
            if (player != null && registeredGuis.containsKey(gui.getId())) {
                openGui(player, gui.getId());
            }
        });
    }
    
    @Override
    public GuiBuilder loadGuiFromConfig(String guiId) {
        GuiConfig config = plugin.getConfigManager().getGuiConfig(guiId);
        if (config == null) {
            throw new IllegalArgumentException("GUI config not found: " + guiId);
        }
        
        GuiBuilder builder = createGui(guiId, config.getTitle(), config.getRows());
        
        config.getItems().forEach((slot, itemConfig) -> {
            ItemStack item = ItemUtils.fromConfig(itemConfig);
            if (itemConfig.hasScript()) {
                builder.setItem(slot, item, (player, gui) -> {
                    String script = itemConfig.getScript();
                    Map<String, Object> vars = new HashMap<>();
                    vars.put("player", player);
                    vars.put("gui", gui);
                    plugin.getScriptManager().executeScript(script, player, vars);
                    return true;
                });
            } else {
                builder.setItem(slot, item);
            }
        });
        
        return builder;
    }
    
    @Override
    public boolean saveGuiToConfig(GuiPage gui) {
        try {
            GuiConfig config = new GuiConfig();
            config.setTitle(gui.getTitle());
            config.setRows(gui.getRows());
            
            gui.getItems().forEach((slot, item) -> {
                if (item != null) {
                    config.addItem(slot, ItemUtils.toConfig(item.getItemStack()));
                }
            });
            
            plugin.getConfigManager().saveGuiConfig(gui.getId(), config);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save GUI: " + gui.getId() + ". " + e.getMessage());
            return false;
        }
    }
} 