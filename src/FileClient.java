import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

// Simpel klient som forbinder til localhost:5000
// Viser besked fra server hvis der er en
public class FileClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;
    private static final String OUTPUT_DIRECTORY = "downloads";
    private static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) {
        runClient();
    }

    private static void runClient() {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        String command = readCommand(consoleReader);
        if (command == null || command.trim().isEmpty()) {
            System.err.println("Ingen kommando angivet, afslutter.");
            return;
        }

        CommandParser.CommandRequest parsedCommand = CommandParser.parse(command);
        if (parsedCommand == null) {
            return;
        }

        String requestedFilename = parsedCommand.getFilename();
        if (!FileValidator.isValid(requestedFilename)) {
            System.err.println("Ugyldigt filnavn: '..', '/' eller '\\' er ikke tilladt.");
            return;
        }

        java.io.File outDir = new java.io.File(OUTPUT_DIRECTORY);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        java.io.File outFile = new java.io.File(outDir, requestedFilename);

        if (outFile.exists() && !confirmOverwrite(consoleReader)) {
            System.out.println("Filoverskrivning afbrudt.");
            return;
        }

        System.out.println("Starter FileClient, forsøger at forbinde til " + HOST + ":" + PORT);

        try (Socket socket = new Socket(HOST, PORT)) {
            System.out.println("Forbundet til server: " + socket.getRemoteSocketAddress());

            // Send kommando og læs svar fra server
            try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                dos.writeUTF(parsedCommand.getRawCommand());
                System.out.println("Sendt kommando til server: " + parsedCommand.getRawCommand());

                try {
                    String response = dis.readUTF();
                    System.out.println("Modtaget svar fra server: " + response);
                    handleResponse(dis, outFile, response);
                } catch (java.io.EOFException eof) {
                    System.err.println("Forbindelsen blev afbrudt af serveren.");
                }
            } catch (IOException e) {
                System.err.println("Fejl ved kommunikation med server: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Kunne ikke forbinde til server: " + e.getMessage());
        }
    }

    private static void handleResponse(DataInputStream dis, java.io.File outFile, String response) throws IOException {
        if (response.startsWith("OK")) {
            handleOkResponse(dis, outFile);
            return;
        }

        if (response.startsWith("ERROR")) {
            handleErrorResponse(response);
            return;
        }

        handleUnexpectedResponse(response);
    }

    private static void handleOkResponse(DataInputStream dis, java.io.File outFile) throws IOException {
        long size = dis.readLong();
        System.out.println("Server sender fil på " + size + " bytes. Gemmer lokalt...");

        long remaining = downloadFile(dis, outFile, size);
        if (remaining == 0) {
            System.out.println("Filen blev modtaget korrekt: " + outFile.getPath());
        } else {
            System.err.println("Fejl ved filmodtagelse: forventede " + size + " bytes, men modtog " + (size - remaining) + " bytes.");
        }
    }

    private static void handleErrorResponse(String response) {
        System.err.println("Server fejl: " + response);
    }

    private static void handleUnexpectedResponse(String response) {
        System.err.println("Uventet svar fra server: " + response);
    }

    private static String readCommand(BufferedReader consoleReader) {
        System.out.print("Skriv kommando (fx GET|filnavn): ");
        try {
            return consoleReader.readLine();
        } catch (IOException e) {
            System.err.println("Fejl ved læsning fra konsol: " + e.getMessage());
            return null;
        }
    }

    private static boolean confirmOverwrite(BufferedReader consoleReader) {
        System.out.print("Filen findes allerede. Overskriv? (j/n): ");
        try {
            String answer = consoleReader.readLine();
            return answer != null && answer.equalsIgnoreCase("j");
        } catch (IOException e) {
            System.err.println("Fejl ved læsning af svar: " + e.getMessage());
            return false;
        }
    }

    private static long downloadFile(DataInputStream dis, java.io.File outFile, long size) throws IOException {
        long remaining = size;
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile, false)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (remaining > 0) {
                int toRead = (int) Math.min(buffer.length, remaining);
                int read = dis.read(buffer, 0, toRead);
                if (read == -1) {
                    break;
                }
                fos.write(buffer, 0, read);
                remaining -= read;
            }
            fos.flush();
        }
        return remaining;
    }
}
