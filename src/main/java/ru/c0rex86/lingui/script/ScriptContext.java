package ru.c0rex86.lingui.script;

import org.bukkit.entity.Player;
import ru.c0rex86.lingui.LinGUi;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ScriptContext {
    
    private final Player player;
    private final Map<String, Object> variables;
    private Object result;
    private boolean cancelled;
    private int commandCount;
    private long startTime;
    private final Stack<Object> dataStack;
    private final Stack<Boolean> conditionStack;
    private boolean skipMode;
    private final LinGUi plugin;
    
    public ScriptContext(Player player, LinGUi plugin) {
        this.player = player;
        this.variables = new HashMap<>();
        this.result = null;
        this.cancelled = false;
        this.commandCount = 0;
        this.startTime = System.currentTimeMillis();
        this.dataStack = new Stack<>();
        this.conditionStack = new Stack<>();
        this.skipMode = false;
        this.plugin = plugin;
    }
    
    public ScriptContext(Player player, Map<String, Object> variables, LinGUi plugin) {
        this.player = player;
        this.variables = new HashMap<>(variables);
        this.result = null;
        this.cancelled = false;
        this.commandCount = 0;
        this.startTime = System.currentTimeMillis();
        this.dataStack = new Stack<>();
        this.conditionStack = new Stack<>();
        this.skipMode = false;
        this.plugin = plugin;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Map<String, Object> getVariables() {
        return variables;
    }
    
    public Object getVariable(String name) {
        return variables.get(name);
    }
    
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }
    
    public Object getResult() {
        return result;
    }
    
    public void setResult(Object result) {
        this.result = result;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public void cancel() {
        this.cancelled = true;
    }
    
    public void incrementCommandCount() {
        this.commandCount++;
        
        int maxCommands = plugin.getConfig().getInt("script.max-commands", 10);
        if (maxCommands > 0 && this.commandCount > maxCommands) {
            throw new RuntimeException("Script exceeded maximum command limit of " + maxCommands);
        }
    }
    
    public boolean isTimedOut() {
        long timeout = plugin.getConfig().getLong("script.timeout", 1000);
        if (timeout <= 0) {
            return false;
        }
        
        return System.currentTimeMillis() - startTime > timeout;
    }
    
    public void resetStartTime() {
        this.startTime = System.currentTimeMillis();
    }
    
    public void pushData(Object data) {
        dataStack.push(data);
    }
    
    public Object popData() {
        if (dataStack.isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }
        return dataStack.pop();
    }
    
    public Object peekData() {
        if (dataStack.isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }
        return dataStack.peek();
    }
    
    public void pushCondition(boolean condition) {
        conditionStack.push(condition);
        updateSkipMode();
    }
    
    public boolean popCondition() {
        if (conditionStack.isEmpty()) {
            throw new RuntimeException("Condition stack is empty");
        }
        boolean result = conditionStack.pop();
        updateSkipMode();
        return result;
    }
    
    public boolean peekCondition() {
        if (conditionStack.isEmpty()) {
            return true;
        }
        return conditionStack.peek();
    }
    
    private void updateSkipMode() {
        skipMode = !conditionStack.isEmpty() && !conditionStack.peek();
    }
    
    public boolean isSkipMode() {
        return skipMode;
    }
    
    public void flipLastCondition() {
        if (!conditionStack.isEmpty()) {
            boolean current = conditionStack.pop();
            conditionStack.push(!current);
            updateSkipMode();
        }
    }
    
    public LinGUi getPlugin() {
        return plugin;
    }
} 