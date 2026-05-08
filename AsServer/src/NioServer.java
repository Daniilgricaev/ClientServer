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
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if(key.isAcceptable()){accept(selector,serverSocket);}
                if(key.isReadable()){handleRequest(key);}
            }
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
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);

        ByteBuffer buffer = ByteBuffer.wrap(("Hello\n Enter your name:").getBytes());
        client.write(buffer);

        System.out.println("New connection " + client.getRemoteAddress());
    }
    private static void handleRequest(SelectionKey key) throws IOException{
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = client.read(buffer);

        if(bytesRead == -1){
            String name = names.get(client);
            if(name != null){
                broadcast(name + " left the chat ", null);
            }
            clients.remove(client);
            names.remove(client);
            client.close();
            return;
        }

        buffer.flip();
        String message = new String(buffer.array(), 0, bytesRead);

        if (!names.containsKey(client)) {
            names.put(client, message);
            broadcast(message+ "Joined the chat",client);
            System.out.println("User "+message+"connected");
        }else{
            String userName = names.get(client);
            broadcast(userName + ": "+ message, client);
            System.out.println(userName+ ": "+ message);
        }
    }
}