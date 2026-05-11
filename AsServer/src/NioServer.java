import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;

public class NioServer{
    private static final int PORT = 8080;
    static final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private static final Set<SocketChannel> clients = ConcurrentHashMap.newKeySet();
    private static final Map<SocketChannel,String>names = new ConcurrentHashMap<>();
    private static final Map<SocketChannel, UserState>states = new ConcurrentHashMap<>();
    private static final Map<SocketChannel, String>pendingNames = new ConcurrentHashMap<>();

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
        states.put(client,UserState.WAITING_NAME);
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
        String data = new String(buffer.array(), 0, bytesRead);

        UserState state = states.get(client);

        if(state == UserState.WAITING_NAME){
            pendingNames.put(client, data);
            states.put(client, UserState.WAITING_PASSWORD);
            ByteBuffer buffer2 = ByteBuffer.wrap(("Enter your password :").getBytes());
            client.write(buffer2);
        }
        if(state == UserState.WAITING_PASSWORD){
            String name = pendingNames.get(client);
            executorService.submit(() -> {
                if(AuthService.check(name, data)){
                    states.put(client,UserState.AUTHENTICATED);
                    pendingNames.remove(client);
                    names.put(client, name);
                    synchronized (client){
                        ByteBuffer buffer1 = ByteBuffer.wrap(("Welcome " + name + " to server chat").getBytes());
                        try {
                            client.write(buffer1);
                        }catch (IOException ex){
                            System.out.println(ex.getMessage());
                        }
                    }
                }else{
                    System.out.println("Wrong password");
                    try {
                        client.close();
                    }catch(IOException ex){
                        System.out.println("Wrong password");
                    }
                }
            });
        }
        if(state == UserState.AUTHENTICATED){
            String username = names.get(client);
            broadcast(username + ':' + data, client);
        }

        if (!names.containsKey(client)) {
            names.put(client, data);
            broadcast(data+ "Joined the chat",client);
            System.out.println("User "+data+"connected");
        }else{
            String userName = names.get(client);
            broadcast(userName + ": "+ data, client);
            System.out.println(userName+ ": "+ data);
        }
    }
}