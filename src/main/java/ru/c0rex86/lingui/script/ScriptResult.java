package ru.c0rex86.lingui.script;

public class ScriptResult {
    
    private final boolean success;
    private final Object result;
    private final Exception error;
    
    private ScriptResult(boolean success, Object result, Exception error) {
        this.success = success;
        this.result = result;
        this.error = error;
    }
    
    public static ScriptResult success(Object result) {
        return new ScriptResult(true, result, null);
    }
    
    public static ScriptResult failure(Exception error) {
        return new ScriptResult(false, null, error);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public Object getResult() {
        return result;
    }
    
    public Exception getError() {
        return error;
    }
    
    public String getErrorMessage() {
        return error != null ? error.getMessage() : "No error";
    }
} 