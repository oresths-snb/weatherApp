import java.net.*;

public class Server {
    private static int port;
    private static final String SERVER_ADDR = "localhost";

    public static void main(String[] args) {
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else{
            System.out.println("Usage: java Server <port>");
            return;
        }

        // Server Start
        try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(SERVER_ADDR))) {
            System.out.println("\n\nServer online on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");

                socket.close();
                System.out.println("Client disconnected");

               
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}