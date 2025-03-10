package ru.c0rex86.lingui;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.c0rex86.lingui.api.GuiManager;
import ru.c0rex86.lingui.api.ScriptManager;
import ru.c0rex86.lingui.commands.LinGuiCommand;
import ru.c0rex86.lingui.commands.OpenGuiCommand;
import ru.c0rex86.lingui.config.CommandBindingManager;
import ru.c0rex86.lingui.config.ConfigManager;
import ru.c0rex86.lingui.gui.GuiManagerImpl;
import ru.c0rex86.lingui.listeners.GuiListener;
import ru.c0rex86.lingui.script.ScriptManagerImpl;

import java.util.logging.Level;

public class LinGUi extends JavaPlugin {
    
    private static LinGUi instance;
    private GuiManager guiManager;
    private ScriptManager scriptManager;
    private ConfigManager configManager;
    private CommandBindingManager commandBindingManager;
    private static final int BSTATS_ID = 20000;
    
    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        guiManager = new GuiManagerImpl(this);
        scriptManager = new ScriptManagerImpl(this);
        commandBindingManager = new CommandBindingManager(this);
        
        registerCommands();
        registerListeners();
        registerMetrics();
        
        commandBindingManager.loadBindings();
        
        getLogger().info("LinGUi has been enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        guiManager.closeAllGuis();
        getLogger().info("LinGUi has been disabled!");
    }
    
    private void registerCommands() {
        getCommand("lingui").setExecutor(new LinGuiCommand(this));
        getCommand("opengui").setExecutor(new OpenGuiCommand(this));
    }
    
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
    }
    
    private void registerMetrics() {
        new Metrics(this, BSTATS_ID);
    }
    
    public void reload() {
        try {
            guiManager.closeAllGuis();
            reloadConfig();
            configManager.loadConfigs();
            commandBindingManager.loadBindings();
            guiManager.reloadGuis();
            getLogger().info("LinGUi has been reloaded successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to reload LinGUi:", e);
        }
    }
    
    public static LinGUi getInstance() {
        return instance;
    }
    
    public GuiManager getGuiManager() {
        return guiManager;
    }
    
    public ScriptManager getScriptManager() {
        return scriptManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public CommandBindingManager getCommandBindingManager() {
        return commandBindingManager;
    }
} 