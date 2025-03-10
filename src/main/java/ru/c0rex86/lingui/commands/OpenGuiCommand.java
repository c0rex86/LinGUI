package ru.c0rex86.lingui.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.c0rex86.lingui.LinGUi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OpenGuiCommand implements CommandExecutor, TabCompleter {
    
    private final LinGUi plugin;
    
    public OpenGuiCommand(LinGUi plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <guiId> [player]");
            return true;
        }
        
        String guiId = args[0];
        
        if (!plugin.getGuiManager().getGui(guiId).isPresent()) {
            sender.sendMessage(ChatColor.RED + "GUI not found: " + guiId);
            return true;
        }
        
        Player target;
        
        if (args.length > 1) {
            if (!sender.hasPermission("lingui.admin") && !sender.hasPermission("lingui.openother")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to open GUIs for other players!");
                return true;
            }
            
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player!");
                return true;
            }
            
            target = (Player) sender;
        }
        
        if (!sender.hasPermission("lingui.admin") && !sender.hasPermission("lingui.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        boolean success = plugin.getGuiManager().openGui(target, guiId);
        
        if (success) {
            if (sender != target) {
                sender.sendMessage(ChatColor.GREEN + "Opened GUI " + guiId + " for " + target.getName());
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to open GUI for " + target.getName());
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("lingui.admin") && !sender.hasPermission("lingui.use")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return plugin.getGuiManager().getRegisteredGuiIds().stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (sender.hasPermission("lingui.admin") || sender.hasPermission("lingui.openother")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
} 