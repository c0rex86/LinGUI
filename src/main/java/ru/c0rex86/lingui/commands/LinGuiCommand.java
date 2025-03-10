package ru.c0rex86.lingui.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.c0rex86.lingui.LinGUi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LinGuiCommand implements CommandExecutor, TabCompleter {
    
    private final LinGUi plugin;
    private final List<String> subCommands;
    
    public LinGuiCommand(LinGUi plugin) {
        this.plugin = plugin;
        this.subCommands = Arrays.asList("reload", "help", "info", "list", "bind", "unbind");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                if (!hasPermission(sender, "lingui.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                plugin.reload();
                sender.sendMessage(ChatColor.GREEN + "LinGUi has been reloaded successfully!");
                break;
            
            case "help":
                sendHelp(sender);
                break;
            
            case "info":
                sendInfo(sender);
                break;
            
            case "list":
                if (!hasPermission(sender, "lingui.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                sendGuiList(sender);
                break;
            
            case "bind":
                if (!hasPermission(sender, "lingui.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " bind <command> <guiId>");
                    return true;
                }
                
                String cmdName = args[1].toLowerCase();
                String guiId = args[2];
                
                if (!plugin.getGuiManager().getGui(guiId).isPresent()) {
                    sender.sendMessage(ChatColor.RED + "GUI not found: " + guiId);
                    return true;
                }
                
                boolean success = plugin.getCommandBindingManager().addBinding(cmdName, guiId);
                if (success) {
                    sender.sendMessage(ChatColor.GREEN + "Command /" + cmdName + " is now bound to GUI: " + guiId);
                } else {
                    sender.sendMessage(ChatColor.RED + "Command /" + cmdName + " is already bound to a GUI!");
                }
                break;
            
            case "unbind":
                if (!hasPermission(sender, "lingui.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " unbind <command>");
                    return true;
                }
                
                cmdName = args[1].toLowerCase();
                success = plugin.getCommandBindingManager().removeBinding(cmdName);
                
                if (success) {
                    sender.sendMessage(ChatColor.GREEN + "Command /" + cmdName + " has been unbound!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Command /" + cmdName + " is not bound to any GUI!");
                }
                break;
            
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission) || sender.hasPermission("lingui.admin");
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== LinGUi Help ===");
        sender.sendMessage(ChatColor.YELLOW + "/lingui reload" + ChatColor.GRAY + " - Reload the plugin");
        sender.sendMessage(ChatColor.YELLOW + "/lingui help" + ChatColor.GRAY + " - Show this help");
        sender.sendMessage(ChatColor.YELLOW + "/lingui info" + ChatColor.GRAY + " - Show plugin info");
        sender.sendMessage(ChatColor.YELLOW + "/lingui list" + ChatColor.GRAY + " - List all GUIs");
        sender.sendMessage(ChatColor.YELLOW + "/lingui bind <command> <guiId>" + ChatColor.GRAY + " - Bind a command to a GUI");
        sender.sendMessage(ChatColor.YELLOW + "/lingui unbind <command>" + ChatColor.GRAY + " - Unbind a command");
        sender.sendMessage(ChatColor.YELLOW + "/opengui <guiId> [player]" + ChatColor.GRAY + " - Open a GUI");
    }
    
    private void sendInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== LinGUi Info ===");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.WHITE + "c0re");
        sender.sendMessage(ChatColor.YELLOW + "Website: " + ChatColor.WHITE + "https://c0rex86.ru");
        sender.sendMessage(ChatColor.YELLOW + "GitHub: " + ChatColor.WHITE + "https://github.com/c0rex86/");
        sender.sendMessage(ChatColor.YELLOW + "Registered GUIs: " + ChatColor.WHITE + plugin.getGuiManager().getRegisteredGuiIds().size());
        sender.sendMessage(ChatColor.YELLOW + "Command Bindings: " + ChatColor.WHITE + plugin.getCommandBindingManager().getCommandBindings().size());
    }
    
    private void sendGuiList(CommandSender sender) {
        List<String> guiIds = plugin.getGuiManager().getRegisteredGuiIds();
        
        if (guiIds.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No GUIs registered!");
            return;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== Registered GUIs ===");
        for (String guiId : guiIds) {
            sender.sendMessage(ChatColor.YELLOW + "- " + guiId);
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .filter(s -> hasPermission(sender, "lingui.admin") || s.equals("help") || s.equals("info"))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("bind")) {
                return new ArrayList<>();
            } else if (args[0].equalsIgnoreCase("unbind")) {
                return plugin.getCommandBindingManager().getCommandBindings().keySet().stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("bind")) {
                return plugin.getGuiManager().getRegisteredGuiIds().stream()
                        .filter(s -> s.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
} 