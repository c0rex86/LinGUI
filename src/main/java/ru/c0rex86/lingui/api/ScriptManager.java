package ru.c0rex86.lingui.api;

import org.bukkit.entity.Player;
import ru.c0rex86.lingui.script.ScriptContext;
import ru.c0rex86.lingui.script.ScriptResult;

import java.io.File;
import java.util.Map;
import java.util.Optional;

public interface ScriptManager {
    
    /**
     * Execute a script with the given content, player, and variables.
     *
     * @param scriptContent The script content to execute
     * @param player The player executing the script
     * @param variables Variables to use in the script
     * @return The result of the script execution
     */
    ScriptResult executeScript(String scriptContent, Player player, Map<String, Object> variables);
    
    /**
     * Execute a script with the given content and script context.
     *
     * @param scriptContent The script content to execute
     * @param context The script context
     * @return The result of the script execution
     */
    ScriptResult executeScript(String scriptContent, ScriptContext context);
    
    /**
     * Execute a script from a file.
     *
     * @param scriptFile The script file to execute
     * @param player The player executing the script
     * @param variables Variables to use in the script
     * @return The result of the script execution
     */
    default ScriptResult executeScriptFile(File scriptFile, Player player, Map<String, Object> variables) {
        throw new UnsupportedOperationException("executeScriptFile not implemented");
    }
    
    /**
     * Register a script function.
     *
     * @param name The name of the function
     * @param function The function implementation
     * @return True if the function was registered, false if it already exists
     */
    boolean registerScriptFunction(String name, ScriptFunction function);
    
    /**
     * Unregister a script function.
     *
     * @param name The name of the function to unregister
     * @return True if the function was unregistered, false if it didn't exist
     */
    boolean unregisterScriptFunction(String name);
    
    /**
     * Get a script function by name.
     *
     * @param name The name of the function
     * @return An optional containing the function, or empty if not found
     */
    Optional<ScriptFunction> getScriptFunction(String name);
    
    /**
     * Create a new script context for a player.
     *
     * @param player The player
     * @return A new script context
     */
    ScriptContext createContext(Player player);
    
    /**
     * Create a new script context for a player with variables.
     *
     * @param player The player
     * @param variables The variables
     * @return A new script context
     */
    ScriptContext createContext(Player player, Map<String, Object> variables);
    
    /**
     * Get all registered script functions.
     *
     * @return A map of function names to functions
     */
    Map<String, ScriptFunction> getRegisteredFunctions();
    
    /**
     * Evaluate a condition within a script context.
     *
     * @param condition The condition to evaluate
     * @param context The script context
     * @return The result of the condition evaluation
     */
    default boolean evaluateCondition(String condition, ScriptContext context) {
        throw new UnsupportedOperationException("evaluateCondition not implemented");
    }
    
    /**
     * Interface for script functions.
     */
    interface ScriptFunction {
        /**
         * Execute the function.
         *
         * @param context The script context
         * @param args The arguments
         * @return The result of the function execution
         */
        Object execute(ScriptContext context, Object... args);
    }
} 