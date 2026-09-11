import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

// Simpel klient som forbinder til localhost:5000
// Viser besked fra server hvis der er en
public class FileClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;
        System.out.println("Starter FileClient, forsøger at forbinde til " + host + ":" + port);

        String command = readUserCommand();
        if (command == null) {
            return;
        }

        CommandParser parsedCommand = CommandParser.parse(command);
        if (parsedCommand == null) {
            return;
        }

        if (!FileValidator.isValid(parsedCommand.getFilename())) {
            return;
        }

        handleClientRequest(host, port, parsedCommand.getRawCommand(), parsedCommand.getFilename());
    }

    private static String readUserCommand() {
        String command = null;
        java.io.Console console = System.console();
        if (console != null) {
            command = console.readLine("Skriv kommando (fx GET|filnavn): ");
        } else {
            System.out.print("Skriv kommando (fx GET|filnavn): ");
            try (BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in))) {
                command = consoleIn.readLine();
            } catch (IOException e) {
                System.err.println("Fejl ved læsning fra konsol: " + e.getMessage());
            }
        }

        if (command == null || command.trim().isEmpty()) {
            System.err.println("Ingen kommando angivet, afslutter.");
            return null;
        }

        return command.trim();
    }

    private static void handleClientRequest(String host, int port, String command, String requestedFilename) {
        try (Socket socket = new Socket(host, port)) {
            System.out.println("Forbundet til server: " + socket.getRemoteSocketAddress());
            try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                dos.writeUTF(command);
                System.out.println("Sendt kommando til server: " + command);

                try {
                    String response = dis.readUTF();
                    System.out.println("Modtaget svar fra server: " + response);

                    if (response.startsWith("OK")) {
                        long size = dis.readLong();
                        System.out.println("Server sender fil på " + size + " bytes. Gemmer lokalt...");
                        saveReceivedFile(dis, requestedFilename, size);
                    } else if (response.startsWith("ERROR")) {
                        System.err.println("Server fejl: " + response);
                    } else {
                        System.err.println("Uventet svar fra server: " + response);
                    }
                } catch (java.io.EOFException eof) {
                    System.err.println("Forbindelsen blev afbrudt af serveren.");
                }
            }
        } catch (IOException e) {
            System.err.println("Fejl ved kommunikation med server: " + e.getMessage());
        }
    }

    private static void saveReceivedFile(DataInputStream dis, String requestedFilename, long size) throws IOException {
        java.io.File outDir = new java.io.File("downloads");
        if (!outDir.exists()) outDir.mkdirs();
        java.io.File outFile = new java.io.File(outDir, requestedFilename);

        if (outFile.exists()) {
            System.out.println("Advarsel: Filen '" + requestedFilename + "' findes allerede i downloads-mappen.");
            System.out.print("Vil du overskrive den? (j/n): ");
            String svar = new java.util.Scanner(System.in).nextLine().trim().toLowerCase();
            if (!svar.equals("j")) {
                System.out.println("Overførsel annulleret af brugeren.");
                return;
            }
        }

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile)) {
            byte[] buffer = new byte[8192];
            long remaining = size;
            while (remaining > 0) {
                int toRead = (int) Math.min(buffer.length, remaining);
                int read = dis.read(buffer, 0, toRead);
                if (read == -1) break;
                fos.write(buffer, 0, read);
                remaining -= read;
            }

            if (remaining == 0) {
                System.out.println("Filen blev modtaget korrekt.");
            } else {
                System.err.println("Filen blev ikke modtaget korrekt. Forventede " + size +
                        " bytes, men modtog kun " + (size - remaining) + " bytes.");
            }
            fos.flush();
        }

        System.out.println("Færdig med at modtage fil. Gemt som: " + outFile.getPath());
    }
}
