import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) throws IOException {
        try(Socket socket = new Socket("localhost",9090);
            BufferedInputStream br = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
            DataInputStream ois = new DataInputStream(socket.getInputStream()); ){

            System.out.println("Client connected to socket.\n");
            System.out.println("Clietn writing channel = oos & reading channel = ois initialized.");

            while(!socket.isOutputShutdown()){
                if(br.ready()){
                    System.out.println("Client start writing in channel...");
                    Thread.sleep(1000);
                    String clientCommand = br.readLine();

                }
            }
        }

    }
}
