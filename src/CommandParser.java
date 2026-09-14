public class CommandParser {
    private final String filename;
    private final String rawCommand;

    private CommandParser(String rawCommand, String filename) {
        this.rawCommand = rawCommand;
        this.filename = filename;
    }

    public static CommandParser parse(String command) {
        if (command == null || !command.startsWith("GET|")) {
            System.err.println("Ugyldig kommando: Kommandoen skal starte med GET|.");
            return null;
        }

        return new CommandParser(command, command.substring(4));
    }

    public String getFilename() {
        return filename;
    }

    public String getRawCommand() {
        return rawCommand;
    }
}
