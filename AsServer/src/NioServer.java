import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class NioServer{
    private static final int PORT = 8080;
    private static final HashSet<SocketChannel> clients = new HashSet<>();
    private static final Map<SocketChannel,String>names = new HashMap<>();

    public static void main(String[] args) throws IOException {

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(PORT));
        serverSocket.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started in port " + PORT);

        while(true){
            selector.select();
            for(SelectionKey key : selector.selectedKeys()){
                if(key.isAcceptable()) accept(selector, serverSocket);
                if(key.isReadable()) handleRequest(key);
            }
            selector.selectedKeys().clear();
        }

    }
    public static void broadcast(String message, SocketChannel sender){
        Iterator<SocketChannel>iterator = clients.iterator();
        while (iterator.hasNext()){
            SocketChannel client = iterator.next();
            if(client == sender){
                continue;
            }
            if(client.isOpen()){
                try{
                    ByteBuffer buffer = ByteBuffer.wrap((message+"\n").getBytes());
                    client.write(buffer);
                }catch (IOException e){
                    iterator.remove();
                    names.remove(client);
                    try {client.close();}catch (IOException ex){}
                }
            }else{
                iterator.remove();
                try{client.close();}catch (IOException ex){}
            }
        }
    }
    public static void accept(Selector selector, ServerSocketChannel serverSocket) throws IOException{
        SocketChannel client = serverSocket.accept();
        clients.add(client);
        System.out.println("New connection " + client.getRemoteAddress());
    }
    private static void handleRequest(SelectionKey key) throws IOException{
        SocketChannel client = (SocketChannel) key.channel();
        Iterator<SocketChannel> iterator
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = client.read(buffer);

        if(bytesRead == -1){
            String name = names.remove(client);
            clients.remove(client);
            broadcast(name + " left the chat ", null);
            client.close();
            return;
        }

        buffer.flip();
        String request = new String(buffer.array(), 0, bytesRead);
        System.out.println("Request :" + request + "\n");
        String response = "HTTP/1.1 200 OK\r\nConected-Length: 13\r\n\rHello, Client !";
        ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
        client.write(responseBuffer);
        client.close();
    }
}