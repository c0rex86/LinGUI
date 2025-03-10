package ru.c0rex86.lingui.script;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.c0rex86.lingui.LinGUi;
import ru.c0rex86.lingui.api.ScriptManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptManagerImpl implements ScriptManager {
    
    private final LinGUi plugin;
    private final Map<String, ScriptFunction> functions;
    
    // Basic command patterns
    private static final Pattern COMMAND_PATTERN = Pattern.compile("cmd:\\s*([^\\{\\}]+)(?:\\{([^\\}]+)\\})?(.*)");
    private static final Pattern OPEN_GUI_PATTERN = Pattern.compile("open:\\s*([^\\s]+)(?:\\s+([^\\s]+))?");
    private static final Pattern CLOSE_GUI_PATTERN = Pattern.compile("close");
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("msg:\\s*(.+)");
    private static final Pattern CONSOLE_PATTERN = Pattern.compile("console:\\s*(.+)");
    private static final Pattern PERMISSION_PATTERN = Pattern.compile("perm:\\s*(.+)");
    
    // Advanced patterns
    private static final Pattern IF_PATTERN = Pattern.compile("if\\s+(.+)");
    private static final Pattern ELSE_PATTERN = Pattern.compile("else");
    private static final Pattern ENDIF_PATTERN = Pattern.compile("endif");
    private static final Pattern LOOP_PATTERN = Pattern.compile("loop\\s+(\\d+)");
    private static final Pattern ENDLOOP_PATTERN = Pattern.compile("endloop");
    private static final Pattern BREAK_PATTERN = Pattern.compile("break");
    private static final Pattern CONTINUE_PATTERN = Pattern.compile("continue");
    private static final Pattern SET_PATTERN = Pattern.compile("set\\s+([a-zA-Z0-9_]+)\\s+(.+)");
    private static final Pattern MATH_PATTERN = Pattern.compile("math\\s+([a-zA-Z0-9_]+)\\s+([+\\-*/])\\s+(.+)");
    private static final Pattern DELAY_PATTERN = Pattern.compile("delay\\s+(\\d+)");
    private static final Pattern RANDOM_PATTERN = Pattern.compile("random\\s+([a-zA-Z0-9_]+)\\s+(\\d+)\\s+(\\d+)");
    
    // Comparison patterns
    private static final Pattern EQUALS_PATTERN = Pattern.compile("(.+)\\s+==\\s+(.+)");
    private static final Pattern NOT_EQUALS_PATTERN = Pattern.compile("(.+)\\s+!=\\s+(.+)");
    private static final Pattern GREATER_PATTERN = Pattern.compile("(.+)\\s+>\\s+(.+)");
    private static final Pattern LESS_PATTERN = Pattern.compile("(.+)\\s+<\\s+(.+)");
    private static final Pattern GREATER_EQUALS_PATTERN = Pattern.compile("(.+)\\s+>=\\s+(.+)");
    private static final Pattern LESS_EQUALS_PATTERN = Pattern.compile("(.+)\\s+<=\\s+(.+)");
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^\\}]+)\\}");
    
    public ScriptManagerImpl(LinGUi plugin) {
        this.plugin = plugin;
        this.functions = new ConcurrentHashMap<>();
        
        registerDefaultFunctions();
    }
    
    private void registerDefaultFunctions() {
        // Command execution
        registerScriptFunction("cmd", (context, args) -> {
            if (args.length < 1) return false;
            
            String command = String.valueOf(args[0]);
            Player player = context.getPlayer();
            
            command = replacePlaceholders(command, context);
            context.incrementCommandCount();
            return player.performCommand(command);
        });
        
        // GUI operations
        registerScriptFunction("open", (context, args) -> {
            if (args.length < 1) return false;
            
            String guiId = String.valueOf(args[0]);
            Player target = args.length > 1 ? Bukkit.getPlayerExact(String.valueOf(args[1])) : context.getPlayer();
            
            if (target == null) return false;
            
            context.incrementCommandCount();
            return context.getPlugin().getGuiManager().openGui(target, guiId);
        });
        
        registerScriptFunction("close", (context, args) -> {
            context.incrementCommandCount();
            return context.getPlugin().getGuiManager().closeGui(context.getPlayer());
        });
        
        // Player messaging
        registerScriptFunction("msg", (context, args) -> {
            if (args.length < 1) return false;
            
            String message = String.valueOf(args[0]);
            message = replacePlaceholders(message, context);
            
            context.getPlayer().sendMessage(message);
            return true;
        });
        
        // Console commands
        registerScriptFunction("console", (context, args) -> {
            if (args.length < 1) return false;
            if (!context.getPlugin().getConfig().getBoolean("script.allow-console-commands", true)) {
                context.getPlayer().sendMessage("§cConsole commands are disabled in the configuration.");
                return false;
            }
            
            String command = String.valueOf(args[0]);
            command = replacePlaceholders(command, context);
            
            context.incrementCommandCount();
            return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        });
        
        // Permission check
        registerScriptFunction("perm", (context, args) -> {
            if (args.length < 1) return false;
            String permission = String.valueOf(args[0]);
            return context.getPlayer().hasPermission(permission);
        });
        
        // Variable operations
        registerScriptFunction("set", (context, args) -> {
            if (args.length < 2) return false;
            
            String varName = String.valueOf(args[0]);
            Object value = args[1];
            
            if (value instanceof String) {
                value = replacePlaceholders((String) value, context);
            }
            
            context.setVariable(varName, value);
            return true;
        });
        
        // Math operations
        registerScriptFunction("math", (context, args) -> {
            if (args.length < 3) return false;
            
            String varName = String.valueOf(args[0]);
            String operator = String.valueOf(args[1]);
            String valueStr = String.valueOf(args[2]);
            valueStr = replacePlaceholders(valueStr, context);
            
            try {
                double currentValue = 0;
                if (context.getVariable(varName) != null) {
                    currentValue = Double.parseDouble(context.getVariable(varName).toString());
                }
                
                double operand = Double.parseDouble(valueStr);
                double result = 0;
                
                switch (operator) {
                    case "+":
                        result = currentValue + operand;
                        break;
                    case "-":
                        result = currentValue - operand;
                        break;
                    case "*":
                        result = currentValue * operand;
                        break;
                    case "/":
                        if (operand == 0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        result = currentValue / operand;
                        break;
                    default:
                        return false;
                }
                
                context.setVariable(varName, result);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        
        // Random number generation
        registerScriptFunction("random", (context, args) -> {
            if (args.length < 3) return false;
            
            String varName = String.valueOf(args[0]);
            String minStr = String.valueOf(args[1]);
            String maxStr = String.valueOf(args[2]);
            
            try {
                int min = Integer.parseInt(minStr);
                int max = Integer.parseInt(maxStr);
                
                if (min >= max) {
                    return false;
                }
                
                int random = min + (int) (Math.random() * (max - min + 1));
                context.setVariable(varName, random);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        
        // Delay execution
        registerScriptFunction("delay", (context, args) -> {
            if (args.length < 1) return false;
            
            try {
                long delay = Long.parseLong(String.valueOf(args[0]));
                if (delay <= 0) return true;
                
                if (delay > 10000) {
                    delay = 10000; // Maximum 10 seconds
                }
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        
        // Load and execute another script
        registerScriptFunction("include", (context, args) -> {
            if (args.length < 1) return false;
            
            String scriptName = String.valueOf(args[0]);
            scriptName = replacePlaceholders(scriptName, context);
            
            File scriptFile = new File(plugin.getDataFolder(), "scripts/" + scriptName + ".script");
            if (!scriptFile.exists()) {
                return false;
            }
            
            try {
                String scriptContent = Files.readString(scriptFile.toPath());
                executeScript(scriptContent, context);
                return true;
            } catch (IOException e) {
                return false;
            }
        });
        
        // Звуковые эффекты
        registerScriptFunction("sound", (context, args) -> {
            if (args.length < 1) return false;
            
            String soundName = String.valueOf(args[0]);
            float volume = args.length > 1 ? Float.parseFloat(String.valueOf(args[1])) : 1.0f;
            float pitch = args.length > 2 ? Float.parseFloat(String.valueOf(args[2])) : 1.0f;
            
            try {
                Player player = context.getPlayer();
                org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, volume, pitch);
                return true;
            } catch (IllegalArgumentException e) {
                context.getPlayer().sendMessage("§cНеизвестный звук: " + soundName);
                return false;
            }
        });
        
        // Визуальные заголовки
        registerScriptFunction("title", (context, args) -> {
            if (args.length < 1) return false;
            
            String title = String.valueOf(args[0]);
            int fadeIn = args.length > 1 ? Integer.parseInt(String.valueOf(args[1])) : 10;
            int stay = args.length > 2 ? Integer.parseInt(String.valueOf(args[2])) : 70;
            int fadeOut = args.length > 3 ? Integer.parseInt(String.valueOf(args[3])) : 20;
            
            title = replacePlaceholders(title, context);
            context.getPlayer().sendTitle(title, "", fadeIn, stay, fadeOut);
            return true;
        });
        
        // Подзаголовки
        registerScriptFunction("subtitle", (context, args) -> {
            if (args.length < 1) return false;
            
            String subtitle = String.valueOf(args[0]);
            int fadeIn = args.length > 1 ? Integer.parseInt(String.valueOf(args[1])) : 10;
            int stay = args.length > 2 ? Integer.parseInt(String.valueOf(args[2])) : 70;
            int fadeOut = args.length > 3 ? Integer.parseInt(String.valueOf(args[3])) : 20;
            
            subtitle = replacePlaceholders(subtitle, context);
            context.getPlayer().sendTitle("", subtitle, fadeIn, stay, fadeOut);
            return true;
        });
        
        // Action bar сообщения
        registerScriptFunction("actionbar", (context, args) -> {
            if (args.length < 1) return false;
            
            String message = String.valueOf(args[0]);
            message = replacePlaceholders(message, context);
            
            try {
                // Используем современный API для actionbar
                context.getPlayer().spigot().sendMessage(
                    net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message)[0]
                );
                return true;
            } catch (Exception e) {
                context.getPlayer().sendMessage(message); // Запасной вариант
                return false;
            }
        });
        
        // Эффекты частиц
        registerScriptFunction("particle", (context, args) -> {
            if (args.length < 1) return false;
            
            String particleName = String.valueOf(args[0]);
            int count = args.length > 1 ? Integer.parseInt(String.valueOf(args[1])) : 10;
            double offsetX = args.length > 2 ? Double.parseDouble(String.valueOf(args[2])) : 0.5;
            double offsetY = args.length > 3 ? Double.parseDouble(String.valueOf(args[3])) : 0.5;
            double offsetZ = args.length > 4 ? Double.parseDouble(String.valueOf(args[4])) : 0.5;
            
            try {
                Player player = context.getPlayer();
                org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleName.toUpperCase());
                player.getWorld().spawnParticle(
                    particle,
                    player.getLocation().add(0, 1, 0), // На уровне головы
                    count,
                    offsetX, offsetY, offsetZ,
                    0.1 // Скорость
                );
                return true;
            } catch (IllegalArgumentException e) {
                context.getPlayer().sendMessage("§cНеизвестный эффект частиц: " + particleName);
                return false;
            }
        });
        
        // Фейерверк
        registerScriptFunction("firework", (context, args) -> {
            try {
                Player player = context.getPlayer();
                org.bukkit.entity.Firework firework = player.getWorld().spawn(
                    player.getLocation(), 
                    org.bukkit.entity.Firework.class
                );
                
                org.bukkit.inventory.meta.FireworkMeta meta = firework.getFireworkMeta();
                meta.setPower(1); // Высота взрыва
                
                // Цвета
                org.bukkit.Color color1 = org.bukkit.Color.RED;
                org.bukkit.Color color2 = org.bukkit.Color.BLUE;
                
                if (args.length > 0) {
                    String colorName = String.valueOf(args[0]);
                    if (colorName.equalsIgnoreCase("random")) {
                        java.util.Random rand = new java.util.Random();
                        color1 = org.bukkit.Color.fromRGB(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
                        color2 = org.bukkit.Color.fromRGB(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
                    }
                }
                
                org.bukkit.FireworkEffect effect = org.bukkit.FireworkEffect.builder()
                    .withColor(color1, color2)
                    .with(org.bukkit.FireworkEffect.Type.BALL_LARGE)
                    .withTrail()
                    .withFlicker()
                    .build();
                
                meta.addEffect(effect);
                firework.setFireworkMeta(meta);
                
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        
        // Дополнительные функции игрока
        registerScriptFunction("heal", (context, args) -> {
            double amount = args.length > 0 ? Double.parseDouble(String.valueOf(args[0])) : 20.0;
            Player player = context.getPlayer();
            
            double newHealth = Math.min(player.getHealth() + amount, player.getMaxHealth());
            player.setHealth(newHealth);
            return true;
        });
        
        registerScriptFunction("feed", (context, args) -> {
            int amount = args.length > 0 ? Integer.parseInt(String.valueOf(args[0])) : 20;
            Player player = context.getPlayer();
            
            int newFoodLevel = Math.min(player.getFoodLevel() + amount, 20);
            player.setFoodLevel(newFoodLevel);
            return true;
        });
    }
    
    @Override
    public ScriptResult executeScript(String scriptContent, Player player, Map<String, Object> variables) {
        return executeScript(scriptContent, createContext(player, variables));
    }
    
    @Override
    public ScriptResult executeScript(String scriptContent, ScriptContext context) {
        try {
            String[] lines = scriptContent.split("\n");
            Object result = null;
            int lineIndex = 0;
            
            while (lineIndex < lines.length) {
                if (context.isTimedOut()) {
                    throw new RuntimeException("Script execution timed out");
                }
                
                if (context.isCancelled()) {
                    break;
                }
                
                String line = lines[lineIndex].trim();
                lineIndex++;
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Skip execution if in skip mode (inside an if block that evaluated to false)
                if (context.isSkipMode()) {
                    // Process control flow even when skipping
                    Matcher ifMatcher = IF_PATTERN.matcher(line);
                    Matcher elseMatcher = ELSE_PATTERN.matcher(line);
                    Matcher endifMatcher = ENDIF_PATTERN.matcher(line);
                    
                    if (ifMatcher.matches()) {
                        context.pushCondition(false); // Push false when in skip mode
                    } else if (elseMatcher.matches()) {
                        context.flipLastCondition();
                    } else if (endifMatcher.matches()) {
                        context.popCondition();
                    }
                    
                    continue;
                }
                
                result = executeLine(line, context);
            }
            
            return ScriptResult.success(result);
        } catch (Exception e) {
            return ScriptResult.failure(e);
        }
    }
    
    private Object executeLine(String line, ScriptContext context) {
        // First check for flow control statements
        Matcher ifMatcher = IF_PATTERN.matcher(line);
        if (ifMatcher.matches()) {
            String condition = ifMatcher.group(1).trim();
            boolean result = evaluateCondition(condition, context);
            context.pushCondition(result);
            return result;
        }
        
        Matcher elseMatcher = ELSE_PATTERN.matcher(line);
        if (elseMatcher.matches()) {
            context.flipLastCondition();
            return null;
        }
        
        Matcher endifMatcher = ENDIF_PATTERN.matcher(line);
        if (endifMatcher.matches()) {
            return context.popCondition();
        }
        
        Matcher loopMatcher = LOOP_PATTERN.matcher(line);
        if (loopMatcher.matches()) {
            // Loop logic would go here
            return null;
        }
        
        Matcher endloopMatcher = ENDLOOP_PATTERN.matcher(line);
        if (endloopMatcher.matches()) {
            // End loop logic would go here
            return null;
        }
        
        // Check for basic command patterns
        Matcher cmdMatcher = COMMAND_PATTERN.matcher(line);
        if (cmdMatcher.matches()) {
            String cmd = cmdMatcher.group(1).trim();
            String playerName = cmdMatcher.group(2);
            String args = cmdMatcher.group(3);
            
            Player player = playerName != null ? Bukkit.getPlayerExact(playerName) : context.getPlayer();
            if (player != null) {
                context.incrementCommandCount();
                return player.performCommand(cmd + (args != null ? " " + args.trim() : ""));
            }
            return false;
        }
        
        Matcher guiMatcher = OPEN_GUI_PATTERN.matcher(line);
        if (guiMatcher.matches()) {
            String guiId = guiMatcher.group(1);
            String targetName = guiMatcher.group(2);
            
            Player target = targetName != null ? Bukkit.getPlayerExact(targetName) : context.getPlayer();
            if (target != null) {
                context.incrementCommandCount();
                return context.getPlugin().getGuiManager().openGui(target, guiId);
            }
            return false;
        }
        
        Matcher closeMatcher = CLOSE_GUI_PATTERN.matcher(line);
        if (closeMatcher.matches()) {
            return context.getPlugin().getGuiManager().closeGui(context.getPlayer());
        }
        
        Matcher msgMatcher = MESSAGE_PATTERN.matcher(line);
        if (msgMatcher.matches()) {
            String message = msgMatcher.group(1);
            message = replacePlaceholders(message, context);
            context.getPlayer().sendMessage(message);
            return true;
        }
        
        Matcher consoleMatcher = CONSOLE_PATTERN.matcher(line);
        if (consoleMatcher.matches()) {
            if (!context.getPlugin().getConfig().getBoolean("script.allow-console-commands", true)) {
                context.getPlayer().sendMessage("§cConsole commands are disabled in the configuration.");
                return false;
            }
            
            String command = consoleMatcher.group(1);
            command = replacePlaceholders(command, context);
            context.incrementCommandCount();
            return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
        
        Matcher permMatcher = PERMISSION_PATTERN.matcher(line);
        if (permMatcher.matches()) {
            String permission = permMatcher.group(1);
            return context.getPlayer().hasPermission(permission);
        }
        
        Matcher setMatcher = SET_PATTERN.matcher(line);
        if (setMatcher.matches()) {
            String varName = setMatcher.group(1);
            String value = setMatcher.group(2);
            value = replacePlaceholders(value, context);
            context.setVariable(varName, value);
            return true;
        }
        
        Matcher mathMatcher = MATH_PATTERN.matcher(line);
        if (mathMatcher.matches()) {
            String varName = mathMatcher.group(1);
            String operator = mathMatcher.group(2);
            String valueStr = mathMatcher.group(3);
            valueStr = replacePlaceholders(valueStr, context);
            
            try {
                double currentValue = 0;
                if (context.getVariable(varName) != null) {
                    currentValue = Double.parseDouble(context.getVariable(varName).toString());
                }
                
                double operand = Double.parseDouble(valueStr);
                double result = 0;
                
                switch (operator) {
                    case "+":
                        result = currentValue + operand;
                        break;
                    case "-":
                        result = currentValue - operand;
                        break;
                    case "*":
                        result = currentValue * operand;
                        break;
                    case "/":
                        if (operand == 0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        result = currentValue / operand;
                        break;
                    default:
                        return false;
                }
                
                context.setVariable(varName, result);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        // If not a special command, try to execute a registered function
        if (line.contains(":")) {
            String[] parts = line.split(":", 2);
            String functionName = parts[0].trim();
            String args = parts[1].trim();
            
            Optional<ScriptFunction> function = getScriptFunction(functionName);
            if (function.isPresent()) {
                String[] argArray = args.split("\\s+");
                for (int i = 0; i < argArray.length; i++) {
                    argArray[i] = replacePlaceholders(argArray[i], context);
                }
                return function.get().execute(context, (Object[]) argArray);
            }
        }
        
        throw new IllegalArgumentException("Unknown script command: " + line);
    }
    
    @Override
    public boolean evaluateCondition(String condition, ScriptContext context) {
        condition = replacePlaceholders(condition, context);
        
        // Handle equality comparison
        Matcher equalsMatcher = EQUALS_PATTERN.matcher(condition);
        if (equalsMatcher.matches()) {
            String left = equalsMatcher.group(1).trim();
            String right = equalsMatcher.group(2).trim();
            return left.equals(right);
        }
        
        // Handle inequality comparison
        Matcher notEqualsMatcher = NOT_EQUALS_PATTERN.matcher(condition);
        if (notEqualsMatcher.matches()) {
            String left = notEqualsMatcher.group(1).trim();
            String right = notEqualsMatcher.group(2).trim();
            return !left.equals(right);
        }
        
        // Handle numeric comparisons
        try {
            // Greater than
            Matcher greaterMatcher = GREATER_PATTERN.matcher(condition);
            if (greaterMatcher.matches()) {
                double left = Double.parseDouble(greaterMatcher.group(1).trim());
                double right = Double.parseDouble(greaterMatcher.group(2).trim());
                return left > right;
            }
            
            // Less than
            Matcher lessMatcher = LESS_PATTERN.matcher(condition);
            if (lessMatcher.matches()) {
                double left = Double.parseDouble(lessMatcher.group(1).trim());
                double right = Double.parseDouble(lessMatcher.group(2).trim());
                return left < right;
            }
            
            // Greater than or equal
            Matcher greaterEqualsMatcher = GREATER_EQUALS_PATTERN.matcher(condition);
            if (greaterEqualsMatcher.matches()) {
                double left = Double.parseDouble(greaterEqualsMatcher.group(1).trim());
                double right = Double.parseDouble(greaterEqualsMatcher.group(2).trim());
                return left >= right;
            }
            
            // Less than or equal
            Matcher lessEqualsMatcher = LESS_EQUALS_PATTERN.matcher(condition);
            if (lessEqualsMatcher.matches()) {
                double left = Double.parseDouble(lessEqualsMatcher.group(1).trim());
                double right = Double.parseDouble(lessEqualsMatcher.group(2).trim());
                return left <= right;
            }
        } catch (NumberFormatException e) {
            // If we can't parse as numbers, fall back to string comparison
        }
        
        // If none of the comparison patterns match, treat condition as a boolean itself
        return Boolean.parseBoolean(condition) || 
               "true".equalsIgnoreCase(condition) || 
               "yes".equalsIgnoreCase(condition) || 
               "1".equals(condition);
    }
    
    private String replacePlaceholders(String text, ScriptContext context) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = "";
            
            if (placeholder.equalsIgnoreCase("player")) {
                replacement = context.getPlayer().getName();
            } else if (placeholder.equalsIgnoreCase("uuid")) {
                replacement = context.getPlayer().getUniqueId().toString();
            } else if (placeholder.equalsIgnoreCase("world")) {
                replacement = context.getPlayer().getWorld().getName();
            } else if (context.getVariables().containsKey(placeholder)) {
                Object value = context.getVariables().get(placeholder);
                replacement = value != null ? value.toString() : "";
            }
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    @Override
    public boolean registerScriptFunction(String name, ScriptFunction function) {
        if (functions.containsKey(name)) {
            return false;
        }
        
        functions.put(name, function);
        return true;
    }
    
    @Override
    public boolean unregisterScriptFunction(String name) {
        return functions.remove(name) != null;
    }
    
    @Override
    public Optional<ScriptFunction> getScriptFunction(String name) {
        return Optional.ofNullable(functions.get(name));
    }
    
    @Override
    public ScriptContext createContext(Player player) {
        return new ScriptContext(player, plugin);
    }
    
    @Override
    public ScriptContext createContext(Player player, Map<String, Object> variables) {
        return new ScriptContext(player, variables, plugin);
    }
    
    @Override
    public Map<String, ScriptFunction> getRegisteredFunctions() {
        return new HashMap<>(functions);
    }
} 