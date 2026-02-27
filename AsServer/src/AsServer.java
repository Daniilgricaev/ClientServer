import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AsServer {
    public static void main(String[] args)throws IOException  {
        try(ServerSocket server = new ServerSocket(9090)){
            Socket client =  server.accept();

            System.out.println("Connection accepted");

            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            System.out.println("DataOutputStream created");

            DataInputStream in = new DataInputStream(client.getInputStream());
            System.out.println("DataInputStream created");

            while(!client.isClosed()) {
                String message = in.readUTF();

                System.out.println("From client message - " + message);

                System.out.println("Server try writing to channel");

                if (message.equalsIgnoreCase("quit")) {
                    System.out.println("Client disconnected . . .");
                    out.writeUTF("Server reply - " + message + "- OK");
                    out.flush();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("The flow was interrupted");
                    }
                    break;
                }
                out.writeUTF("Server reply - " + message + "- OK ");
                System.out.println("Server wrote message to client.");

                out.flush();
            }
            System.out.println("Client disconnected");
            System.out.println("Closing connections and channels.");

            in.close();
            out.close();

            client.close();

            System.out.println("Closing connections and channels - DONE");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}