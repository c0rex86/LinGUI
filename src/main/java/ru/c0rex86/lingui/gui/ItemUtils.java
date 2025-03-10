package ru.c0rex86.lingui.gui;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.c0rex86.lingui.config.ItemConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemUtils {
    
    public static ItemStack createItem(String material, String name) {
        Material mat = Material.getMaterial(material.toUpperCase());
        if (mat == null) {
            mat = Material.STONE;
        }
        
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public static ItemStack createItem(String material, String name, List<String> lore) {
        ItemStack item = createItem(material, name);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public static ItemStack createItem(String material, String name, List<String> lore, boolean glow) {
        ItemStack item = createItem(material, name, lore);
        
        if (glow) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                Enchantment enchantment = null;
                try {
                    enchantment = Enchantment.getByKey(NamespacedKey.minecraft("durability"));
                } catch (Exception e) {
                    if (Enchantment.values().length > 0) {
                        enchantment = Enchantment.values()[0];
                    }
                }
                
                if (enchantment != null) {
                    meta.addEnchant(enchantment, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);
                }
            }
        }
        
        return item;
    }
    
    public static ItemStack createItem(Material material, String name, List<String> lore, Map<Enchantment, Integer> enchantments, List<ItemFlag> flags) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            
            if (lore != null) {
                meta.setLore(lore);
            }
            
            if (enchantments != null) {
                enchantments.forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));
            }
            
            if (flags != null) {
                flags.forEach(meta::addItemFlags);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public static ItemConfig toConfig(ItemStack itemStack) {
        ItemConfig config = new ItemConfig();
        config.setMaterial(itemStack.getType().name());
        
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                config.setName(meta.getDisplayName());
            }
            
            if (meta.hasLore()) {
                config.setLore(meta.getLore());
            }
            
            if (!meta.getEnchants().isEmpty()) {
                config.setEnchants(meta.getEnchants().entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey().getKey().getKey(),
                                Map.Entry::getValue
                        )));
            }
            
            if (!meta.getItemFlags().isEmpty()) {
                config.setFlags(meta.getItemFlags().stream()
                        .map(ItemFlag::name)
                        .collect(Collectors.toList()));
            }
        }
        
        config.setAmount(itemStack.getAmount());
        
        return config;
    }
    
    public static ItemStack fromConfig(ItemConfig config) {
        Material material = Material.getMaterial(config.getMaterial());
        if (material == null) {
            material = Material.STONE;
        }
        
        ItemStack item = new ItemStack(material, config.getAmount());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (config.getName() != null) {
                meta.setDisplayName(config.getName());
            }
            
            if (config.getLore() != null) {
                meta.setLore(config.getLore());
            }
            
            if (config.getEnchants() != null) {
                config.getEnchants().forEach((enchantName, level) -> {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
                    if (enchantment != null) {
                        meta.addEnchant(enchantment, level, true);
                    }
                });
            }
            
            if (config.getFlags() != null) {
                config.getFlags().forEach(flagName -> {
                    try {
                        ItemFlag flag = ItemFlag.valueOf(flagName);
                        meta.addItemFlags(flag);
                    } catch (IllegalArgumentException ignored) {
                    }
                });
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
} 