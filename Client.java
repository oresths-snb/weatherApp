import java.net.*;

public class Client {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Client <server_address> <port>");
            return;
        }

        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = new Client();
        client.connectToServer(serverAddress, port);
    }
    
    public void connectToServer(String serverAddress, int port) {
        try (Socket socket = new Socket(serverAddress, port)) {
            System.out.println("Connected to server at " + serverAddress + ":" + port);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
