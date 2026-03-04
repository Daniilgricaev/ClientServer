import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {
    public static void main(String[] args) throws IOException {
        try(Socket socket = new Socket("localhost",9090);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
            DataInputStream ois = new DataInputStream(socket.getInputStream()); ){

            System.out.println("Client connected to socket.\n");
            System.out.println("Clietn writing channel = oos & reading channel = ois initialized.");

            while(!socket.isOutputShutdown()){
                if(br.ready()){
                    System.out.println("Client start writing in channel...");
                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException e){
                        Thread.currentThread().interrupt();
                        System.out.println("The flow was interrupted");
                    }
                    String clientCommand = br.readLine();
                    oos.writeUTF(clientCommand);
                    oos.flush();
                    System.out.println("Client sent message " + clientCommand + "to server.");
                    try {
                        Thread.sleep(1000);
                    }catch(InterruptedException e){
                        Thread.currentThread().interrupt();
                        System.out.println("The flow was interrupted");
                    }

                    if(clientCommand.equalsIgnoreCase("quit")){
                        System.out.println("Client kill connections");
                        try{
                            Thread.sleep(2000);
                        }catch(InterruptedException e){
                            Thread.currentThread().interrupt();
                            System.out.println("The flow was interrupted");
                        }

                        if(ois.read() > -1){
                            System.out.println("reading. . .");
                            String in = ois.readUTF();
                            System.out.println(in);
                        }

                        break;

                    }
                }
                System.out.println("Client sent message & start waiting for data form server . . .");
                try{
                    Thread.sleep(2000);
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                    System.out.println("The flow was interrupted");
                }

                if(ois.read() > -1){
                    System.out.println("reading . . .");
                    String in = ois.readUTF();
                    System.out.println(in);
                }
            }
            System.out.println("Closing connections & channels on clientSife - DONE.");
        }catch(UnknownHostException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }



    }
}
