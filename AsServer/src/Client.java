import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your name :");
        String userName = scanner.nextLine();

        try (Socket socket = new Socket("localhost", 9090);
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
             DataInputStream ois = new DataInputStream(socket.getInputStream())) {

            System.out.println("Client connected to socket.\n");
            System.out.println("Client writing channel = oos & reading channel = ois initialized.");

            // Поток для чтения ответов от сервера
            Thread readerThread = new Thread(() -> {
                try {
                    while (!socket.isClosed()) {
                        if (ois.available() > 0) { // Проверяем, есть ли данные
                            String response = ois.readUTF();
                            System.out.println("\nServer response: " + response);
                        }
                        Thread.sleep(100); // Небольшая задержка
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println("Reader thread stopped");
                }
            });
            readerThread.start();

            // Основной цикл для отправки сообщений
            while (true) {
                System.out.print("Enter message (or 'quit' to exit): ");
                String clientCommand = br.readLine();

                if (clientCommand != null) {
                    oos.writeUTF(clientCommand);
                    oos.flush();
                    System.out.println(userName + " sent message: " + clientCommand);

                    if (clientCommand.equalsIgnoreCase("quit")) {
                        System.out.println("Closing connection...");
                        Thread.sleep(1000);
                        break;
                    }
                }
            }

            socket.close();
            System.out.println("Closing connections & channels on client side - DONE.");

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage());
        }
    }
}