import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// Server som starter og lytter på port 5000
// Konsolmeldinger på dansk
public class FileServer {
    public static void main(String[] args) {
        int port = 5000;
        System.out.println("Starter FileServer på port " + port);

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Venter på klient...");
            // Accepter én klient og luk
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
                System.out.println("Lukker forbindelse og stopper serveren.");
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
        if (command != null && command.startsWith("GET|")) {
            String filename = command.substring(4);
            java.io.File target = resolveValidatedFile(filename);
            if (target == null) {
                dos.writeUTF("ERROR|Ugyldigt filnavn");
                System.out.println("Sendte svar: ERROR|Ugyldigt filnavn");
            } else if (!target.exists() || !target.isFile()) {
                dos.writeUTF("ERROR|File not found");
                System.out.println("Sendte svar: ERROR|File not found (" + target.getPath() + ")");
            } else {
                long fileSize = target.length();
                dos.writeUTF("OK");
                dos.writeLong(fileSize);
                System.out.println("Sender fil (" + target.getPath() + ") størrelse " + fileSize + " bytes");

                // Send filens bytes
                try (java.io.FileInputStream fis = new java.io.FileInputStream(target)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, read);
                    }
                    dos.flush();
                }

                System.out.println("Færdig med at sende fil.");
            }
        } else {
            dos.writeUTF("ERROR|Unknown command");
            System.out.println("Sendte svar: ERROR|Unknown command");
        }
    }

    private static java.io.File resolveValidatedFile(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return null;
        }

        try {
            java.io.File baseDir = new java.io.File("files");
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
