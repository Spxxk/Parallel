package zork;

public class Command {
    private String commandWord;
    private String statement;
    
    /**
    * Create a command obsject. First and second word must be supplied, but either
    * one (or both) can be null. The command word should be null to indicate that
    * this was a command that is not recognised by this game.
    */
    public Command(String firstWord, String statement) {
        commandWord = firstWord;
        this.statement = statement;
    }
    
    public Command(String command) {
        commandWord = command;
    }

    public Command(OpenableObject obj) {}
    
    /**
    * Return the command word (the first word) of this command. If the command was
    * not understood, the result is null.
    */
    public String getCommandWord() {
        return commandWord;
    }
    
    /**
    * Return the second word of this command. Returns null if there was no second
    * word.
    */
    public String getStatement() {
        return statement;
    }
    
    /**
    * Return true if this command was not understood.
    */
    public boolean isUnknown() {
        return (commandWord == null);
    }
    
    /**
    * Return true if the command has a second word.
    */
    public boolean hasStatement() {
        return (statement != null);
    }
}
