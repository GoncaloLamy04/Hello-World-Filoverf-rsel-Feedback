public class CommandParser {
    private static final String GET_PREFIX = "GET|";

    public static CommandRequest parse(String command) {
        if (command == null) {
            System.err.println("Ugyldig kommando: kommandoen er tom.");
            return null;
        }

        String trimmedCommand = command.trim();
        if (!trimmedCommand.startsWith(GET_PREFIX)) {
            System.err.println("Ugyldig kommando: brug formatet GET|filnavn.");
            return null;
        }

        String filename = trimmedCommand.substring(GET_PREFIX.length());
        if (filename.isEmpty()) {
            System.err.println("Ugyldig kommando: filnavn mangler.");
            return null;
        }

        return new CommandRequest(trimmedCommand, filename);
    }

    public static class CommandRequest {
        private final String rawCommand;
        private final String filename;

        public CommandRequest(String rawCommand, String filename) {
            this.rawCommand = rawCommand;
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }

        public String getRawCommand() {
            return rawCommand;
        }
    }
}
