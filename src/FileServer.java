import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// Server som starter og lytter på port 5000
// Konsolmeldinger på dansk
public class FileServer {
    private static final int PORT = 5000;
    private static final String FILES_DIRECTORY = "files";
    private static final String GET_PREFIX = "GET|";
    private static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) {
        System.out.println("Starter FileServer på port " + PORT);

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                System.out.println("Venter på klient...");
                try (Socket client = server.accept()) {
                    System.out.println("Klient forbundet fra " + client.getRemoteSocketAddress());
                    // Læs en kommando fra klienten (fx: GET|filnavn) og svar tilbage
                    try (DataInputStream dis = new DataInputStream(client.getInputStream());
                         DataOutputStream dos = new DataOutputStream(client.getOutputStream())) {
                        handleRequest(dis, dos);
                    } catch (java.io.EOFException eof) {
                        System.err.println("Forbindelsen blev afbrudt af klienten.");
                    } catch (IOException e) {
                        System.err.println("Fejl ved kommunikation med klient: " + e.getMessage());
                    }
                    System.out.println("Lukker forbindelse og venter på næste klient.");
                }
            }
        } catch (IOException e) {
            System.err.println("Fejl i server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleRequest(DataInputStream dis, DataOutputStream dos) throws IOException {
        String command = dis.readUTF();
        System.out.println("Modtog kommando fra klient: " + command);

        // Enkel validering: skal starte med GET|
        if (command != null && command.startsWith(GET_PREFIX)) {
            String filename = command.substring(GET_PREFIX.length());
            java.io.File target = resolveValidatedFile(filename);
            if (target == null) {
                sendError(dos, "ERROR|Ugyldigt filnavn");
                System.out.println("Sendte svar: ERROR|Ugyldigt filnavn");
            } else if (!target.exists() || !target.isFile()) {
                sendError(dos, "ERROR|File not found");
                System.out.println("Sendte svar: ERROR|File not found (" + target.getPath() + ")");
            } else {
                long fileSize = target.length();
                dos.writeUTF("OK");
                dos.writeLong(fileSize);
                System.out.println("Sender fil (" + target.getPath() + ") størrelse " + fileSize + " bytes");

                sendFile(dos, target);
                System.out.println("Færdig med at sende fil.");
            }
        } else {
            sendError(dos, "ERROR|Unknown command");
            System.out.println("Sendte svar: ERROR|Unknown command");
        }
    }

    private static void sendError(DataOutputStream dos, String message) throws IOException {
        dos.writeUTF(message);
    }

    private static void sendFile(DataOutputStream dos, java.io.File target) throws IOException {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(target)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, read);
            }
            dos.flush();
        }
    }

    private static java.io.File resolveValidatedFile(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return null;
        }

        try {
            java.io.File baseDir = new java.io.File(FILES_DIRECTORY);
            java.io.File target = new java.io.File(baseDir, filename);
            String canonicalBaseDir = baseDir.getCanonicalPath();
            String canonicalTarget = target.getCanonicalPath();
            if (!canonicalTarget.startsWith(canonicalBaseDir + java.io.File.separator)) {
                return null;
            }
            return target;
        } catch (IOException e) {
            return null;
        }
    }
}
