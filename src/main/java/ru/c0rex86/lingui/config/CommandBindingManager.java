package ru.c0rex86.lingui.config;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.c0rex86.lingui.LinGUi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

public class CommandBindingManager {
    
    private final LinGUi plugin;
    private final Map<String, String> commandBindings;
    private final List<BukkitCommand> registeredCommands;
    private CommandMap bukkitCommandMap;
    
    public CommandBindingManager(LinGUi plugin) {
        this.plugin = plugin;
        this.commandBindings = new HashMap<>();
        this.registeredCommands = new ArrayList<>();
        
        try {
            final Field bukkitCommandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMapField.setAccessible(true);
            bukkitCommandMap = (CommandMap) bukkitCommandMapField.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to access Bukkit CommandMap", e);
        }
    }
    
    public void loadBindings() {
        commandBindings.clear();
        unregisterCommands();
        
        File file = plugin.getConfigManager().getCommandBindingsFile();
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            if (config.contains("bindings")) {
                for (String commandName : config.getConfigurationSection("bindings").getKeys(false)) {
                    String guiId = config.getString("bindings." + commandName);
                    if (guiId != null && !guiId.isEmpty()) {
                        commandBindings.put(commandName, guiId);
                        registerCommand(commandName, guiId);
                    }
                }
            }
            
            if (config.contains("default_bindings") && config.getBoolean("default_bindings")) {
                createDefaultBindings();
            }
        } else {
            createDefaultBindings();
            saveBindings();
        }
    }
    
    private void createDefaultBindings() {
        if (!commandBindings.containsKey("admin")) {
            commandBindings.put("admin", "admin_menu");
            registerCommand("admin", "admin_menu");
        }
    }
    
    private void registerCommand(String commandName, String guiId) {
        if (bukkitCommandMap == null) {
            return;
        }
        
        BukkitCommand command = new BukkitCommand(commandName) {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be executed by a player!");
                    return true;
                }
                
                Player player = (Player) sender;
                
                if (!player.hasPermission("lingui.command." + commandName) && !player.hasPermission("lingui.command.*") && !player.hasPermission("lingui.admin")) {
                    player.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }
                
                return plugin.getGuiManager().openGui(player, guiId);
            }
        };
        
        command.setDescription("Open GUI: " + guiId);
        command.setUsage("/" + commandName);
        command.setPermission("lingui.command." + commandName);
        
        bukkitCommandMap.register("lingui", command);
        registeredCommands.add(command);
    }
    
    private void unregisterCommands() {
        if (bukkitCommandMap == null) {
            return;
        }
        
        try {
            final Field knownCommandsField = CommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            Map<String, org.bukkit.command.Command> knownCommands = (Map<String, org.bukkit.command.Command>) knownCommandsField.get(bukkitCommandMap);
            
            for (BukkitCommand command : registeredCommands) {
                knownCommands.remove(command.getName());
                knownCommands.remove("lingui:" + command.getName());
                
                for (String alias : command.getAliases()) {
                    knownCommands.remove(alias);
                    knownCommands.remove("lingui:" + alias);
                }
            }
            
            registeredCommands.clear();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to unregister commands", e);
        }
    }
    
    public void saveBindings() {
        File file = plugin.getConfigManager().getCommandBindingsFile();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        config.set("bindings", null);
        for (Map.Entry<String, String> entry : commandBindings.entrySet()) {
            config.set("bindings." + entry.getKey(), entry.getValue());
        }
        
        config.set("default_bindings", true);
        
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save command bindings", e);
        }
    }
    
    public Map<String, String> getCommandBindings() {
        return new HashMap<>(commandBindings);
    }
    
    public boolean addBinding(String commandName, String guiId) {
        if (commandBindings.containsKey(commandName)) {
            return false;
        }
        
        commandBindings.put(commandName, guiId);
        registerCommand(commandName, guiId);
        saveBindings();
        
        return true;
    }
    
    public boolean removeBinding(String commandName) {
        if (!commandBindings.containsKey(commandName)) {
            return false;
        }
        
        commandBindings.remove(commandName);
        loadBindings();
        saveBindings();
        
        return true;
    }
} 