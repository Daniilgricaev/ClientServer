import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
//        System.out.println("Enter your name :");
//        String userName = scanner.nextLine();
        User user = new User(null, null, null);
        user.reg();

        try (Socket socket = new Socket("localhost", 8080);
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            pw.println(user.name);

            System.out.println("Client connected to socket.\n");
            System.out.println("Client writing channel = oos & reading channel = ois initialized.");

            // Поток для чтения ответов от сервера
            Thread readerThread = new Thread(() -> {
                try{
                    String response;
                    while ((response = in.readLine()) != null){
                        System.out.println(response);
                    }
                }catch (IOException e){
                    System.out.println("Connection closed");
                }
            });
            readerThread.start();

            // Основной цикл для отправки сообщений
            String clientMessage;
            while ((clientMessage = br.readLine()) != null){
                pw.println(clientMessage);
                if(clientMessage.equalsIgnoreCase("quit")){
                    break;
                }
            }

            socket.close();
            System.out.println("Closing connections & channels on client side - DONE.");

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        }
    }
}