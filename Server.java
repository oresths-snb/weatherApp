import java.io.*;
import java.net.*;
import java.util.Properties;

public class Server {
    private static int port;
    private static final String SERVER_ADDR = "localhost";
    private static String apiKey;

    Server() {
    }

    public static void main(String[] args) {
        // Check for correct usage
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else{
            System.out.println("Usage: java Server <port>");
            return;
        }

        // Load API key
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("config")) {
            properties.load(input);
            apiKey = properties.getProperty("api.key");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Server Start
        try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(SERVER_ADDR))) {
            System.out.println("\n\nServer online on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");

                ServerThread serverThread = new ServerThread(socket, port, apiKey);
                serverThread.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}