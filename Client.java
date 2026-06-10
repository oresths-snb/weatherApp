import java.net.*;
import java.util.Scanner;
import java.io.*;

public class Client {
    BufferedReader in;
    PrintWriter out;
    Scanner scanner;
    private Socket socket;
    String serverAddress = "localhost";
    int port = 12345;
    
    public void connectToServer(String serverAddress, int port) {
        try{
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to server at " + serverAddress + ":" + port);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void disconnectFromServer(Socket socket) {
        try {
            if (socket != null && !socket.isClosed()) {
                out.println("exit");
                socket.close();
                System.out.println("Disconnected from server");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getWeatherByCity(String city){
        try {
            String toSend = "CITY:" + city;
            out.println(toSend);
            out.flush();
            String response = in.readLine();
            while (!response.isEmpty()){
                System.out.println(response);
                response = in.readLine();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getWeatherByCoords(String lat, String lon){
        try {
            String toSend = "COORDINATES:" + lat + "," + lon;
            out.println(toSend);
            out.flush();
            String response = in.readLine();
            while (!response.isEmpty()){
                System.out.println(response);
                response = in.readLine();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
