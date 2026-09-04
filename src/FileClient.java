import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

// Simpel klient som forbinder til localhost:5000
// Viser besked fra server hvis der er en
public class FileClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;
        System.out.println("Starter FileClient, forsøger at forbinde til " + host + ":" + port);

        try (Socket socket = new Socket(host, port)) {
            System.out.println("Forbundet til server: " + socket.getRemoteSocketAddress());

            // Læs kommando fra bruger (fx: GET|filnavn)
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
                return;
            }

            // Hvis brugeren kun skrev filnavn, præfiks med GET|
            if (!command.contains("|")) {
                command = "GET|" + command.trim();
            }

            // Valider filnavn for at undgå path-separatorer eller '..'
            String requestedFilename = command.contains("|") ? command.substring(command.indexOf('|')+1) : command;
            if (requestedFilename.contains("..") || requestedFilename.contains("/") || requestedFilename.contains("\\")) {
                System.err.println("Ugyldigt filnavn: path traversal eller separatorer er ikke tilladt.");
                return;
            }

            // Send kommando og læs svar fra server
            try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                dos.writeUTF(command);
                System.out.println("Sendt kommando til server: " + command);

                try {
                    String response = dis.readUTF();
                    System.out.println("Modtaget svar fra server: " + response);

                    if (response.startsWith("OK")) {
                        // Læs filstørrelse efter OK
                        long size = dis.readLong();
                        System.out.println("Server sender fil på " + size + " bytes. Gemmer lokalt...");

                        java.io.File outDir = new java.io.File("downloads");
                        if (!outDir.exists()) outDir.mkdirs();
                        java.io.File outFile = new java.io.File(outDir, requestedFilename);

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
                            fos.flush();
                        }

                        System.out.println("Færdig med at modtage fil. Gemt som: " + outFile.getPath());
                    } else if (response.startsWith("ERROR")) {
                        System.err.println("Server fejl: " + response);
                    } else {
                        System.err.println("Uventet svar fra server: " + response);
                    }
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
}
