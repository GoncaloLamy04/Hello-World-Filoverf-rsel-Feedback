import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// Server som starter og lytter på port 5000
// Konsolmeldinger på dansk
public class FileServer {
    public static void main(String[] args) {
        int port = 5000;
        System.out.println("Starter FileServer på port " + port);
        startServer(port);
    }

    private static void startServer(int port) {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Venter på klient...");
            while (true) {
                try (Socket client = server.accept()) {
                    System.out.println("Klient forbundet fra " + client.getRemoteSocketAddress());
                    handleServerConnection(client);
                    System.out.println("Lukker klientforbindelse.");
                }
            }
        } catch (IOException e) {
            System.err.println("Fejl i server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleServerConnection(Socket client) {
        try (DataInputStream dis = new DataInputStream(client.getInputStream());
             DataOutputStream dos = new DataOutputStream(client.getOutputStream())) {
            String command = dis.readUTF();
            System.out.println("Modtog kommando fra klient: " + command);

            if (command != null && command.startsWith("GET|")) {
                String filename = command.substring(4);
                if (!FileValidator.isValid(filename)) {
                    dos.writeUTF("ERROR|Invalid filename");
                    System.out.println("Sendte svar: ERROR|Invalid filename (" + filename + ")");
                } else {
                    java.io.File baseDir = new java.io.File("files");
                    java.io.File target = new java.io.File(baseDir, filename);
                    try {
                        String baseCanonical = baseDir.getCanonicalPath();
                        String targetCanonical = target.getCanonicalPath();
                        if (!targetCanonical.startsWith(baseCanonical + java.io.File.separator)) {
                            dos.writeUTF("ERROR|Invalid filename");
                            System.out.println("Sendte svar: ERROR|Invalid filename (" + filename + ")");
                        } else if (!target.exists() || !target.isFile()) {
                            dos.writeUTF("ERROR|File not found");
                            System.out.println("Sendte svar: ERROR|File not found (" + target.getPath() + ")");
                        } else {
                            long fileSize = target.length();
                            dos.writeUTF("OK");
                            dos.writeLong(fileSize);
                            System.out.println("Sender fil (" + target.getPath() + ") størrelse " + fileSize + " bytes");
                            sendFileBytes(dos, target);
                            System.out.println("Færdig med at sende fil.");
                        }
                    } catch (IOException ioe) {
                        dos.writeUTF("ERROR|Server error");
                        System.err.println("Fejl ved validering af filsti: " + ioe.getMessage());
                    }
                }
            } else {
                dos.writeUTF("ERROR|Unknown command");
                System.out.println("Sendte svar: ERROR|Unknown command");
            }
        } catch (java.io.EOFException eof) {
            System.err.println("Forbindelsen blev afbrudt af klienten.");
        } catch (IOException e) {
            System.err.println("Fejl ved kommunikation med klient: " + e.getMessage());
        }
    }

    private static void sendFileBytes(DataOutputStream dos, java.io.File target) throws IOException {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(target)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, read);
            }
            dos.flush();
        }
    }
}
