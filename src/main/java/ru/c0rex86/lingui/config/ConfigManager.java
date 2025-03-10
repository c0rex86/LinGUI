package ru.c0rex86.lingui.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.c0rex86.lingui.LinGUi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {
    
    private final LinGUi plugin;
    private final File guiFolder;
    private final File commandBindingsFile;
    private final Map<String, GuiConfig> guiConfigs;
    
    public ConfigManager(LinGUi plugin) {
        this.plugin = plugin;
        this.guiConfigs = new HashMap<>();
        
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        this.guiFolder = new File(dataFolder, "guis");
        if (!guiFolder.exists()) {
            guiFolder.mkdirs();
        }
        
        this.commandBindingsFile = new File(dataFolder, "command_bindings.yml");
        if (!commandBindingsFile.exists()) {
            try {
                plugin.saveResource("command_bindings.yml", false);
            } catch (IllegalArgumentException e) {
                try {
                    commandBindingsFile.createNewFile();
                } catch (IOException ioException) {
                    plugin.getLogger().log(Level.SEVERE, "Could not create command_bindings.yml", ioException);
                }
            }
        }
        
        createDefaultGuis();
    }
    
    private void createDefaultGuis() {
        File adminMenuFile = new File(guiFolder, "admin_menu.yml");
        if (!adminMenuFile.exists()) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(adminMenuFile);
                config.set("title", "Admin Menu");
                config.set("rows", 3);
                
                config.set("items.13.material", "COMMAND_BLOCK");
                config.set("items.13.name", "§6Server Control");
                config.set("items.13.lore", java.util.Arrays.asList("§7Click to manage server", "§7settings and commands"));
                config.set("items.13.script", "cmd: op {player}");
                
                config.save(adminMenuFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create admin_menu.yml", e);
            }
        }
    }
    
    public void loadConfigs() {
        loadGuiConfigs();
    }
    
    private void loadGuiConfigs() {
        guiConfigs.clear();
        
        File[] files = guiFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                try {
                    String guiId = file.getName().substring(0, file.getName().length() - 4);
                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    GuiConfig guiConfig = new GuiConfig();
                    
                    guiConfig.setTitle(config.getString("title", "GUI"));
                    guiConfig.setRows(config.getInt("rows", 3));
                    
                    if (config.contains("items")) {
                        for (String slotStr : config.getConfigurationSection("items").getKeys(false)) {
                            try {
                                int slot = Integer.parseInt(slotStr);
                                String path = "items." + slotStr + ".";
                                
                                ItemConfig itemConfig = new ItemConfig();
                                itemConfig.setMaterial(config.getString(path + "material", "STONE"));
                                itemConfig.setName(config.getString(path + "name"));
                                itemConfig.setLore(config.getStringList(path + "lore"));
                                itemConfig.setAmount(config.getInt(path + "amount", 1));
                                
                                if (config.contains(path + "script")) {
                                    itemConfig.setScript(config.getString(path + "script"));
                                }
                                
                                guiConfig.addItem(slot, itemConfig);
                            } catch (NumberFormatException e) {
                                plugin.getLogger().warning("Invalid slot number in GUI config: " + slotStr);
                            }
                        }
                    }
                    
                    guiConfigs.put(guiId, guiConfig);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load GUI config: " + file.getName(), e);
                }
            }
        }
    }
    
    public void saveGuiConfig(String guiId, GuiConfig config) throws IOException {
        File file = new File(guiFolder, guiId + ".yml");
        FileConfiguration yamlConfig = YamlConfiguration.loadConfiguration(file);
        
        yamlConfig.set("title", config.getTitle());
        yamlConfig.set("rows", config.getRows());
        yamlConfig.set("items", null);
        
        for (Map.Entry<Integer, ItemConfig> entry : config.getItems().entrySet()) {
            int slot = entry.getKey();
            ItemConfig itemConfig = entry.getValue();
            String path = "items." + slot + ".";
            
            yamlConfig.set(path + "material", itemConfig.getMaterial());
            yamlConfig.set(path + "name", itemConfig.getName());
            yamlConfig.set(path + "lore", itemConfig.getLore());
            yamlConfig.set(path + "amount", itemConfig.getAmount());
            
            if (itemConfig.hasScript()) {
                yamlConfig.set(path + "script", itemConfig.getScript());
            }
            
            if (itemConfig.getEnchants() != null && !itemConfig.getEnchants().isEmpty()) {
                for (Map.Entry<String, Integer> enchant : itemConfig.getEnchants().entrySet()) {
                    yamlConfig.set(path + "enchants." + enchant.getKey(), enchant.getValue());
                }
            }
            
            if (itemConfig.getFlags() != null && !itemConfig.getFlags().isEmpty()) {
                yamlConfig.set(path + "flags", itemConfig.getFlags());
            }
        }
        
        yamlConfig.save(file);
        guiConfigs.put(guiId, config);
    }
    
    public Map<String, GuiConfig> getGuiConfigs() {
        return new HashMap<>(guiConfigs);
    }
    
    public GuiConfig getGuiConfig(String guiId) {
        return guiConfigs.get(guiId);
    }
    
    public File getCommandBindingsFile() {
        return commandBindingsFile;
    }
} 