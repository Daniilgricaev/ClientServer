import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class NioServer{
    private static final int PORT = 8080;

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
    public static void accept(Selector selector, ServerSocketChannel serverSocket) throws IOException{
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector,SelectionKey.OP_READ);
        System.out.println("New connection " + client.getRemoteAddress());
    }
    private static void handleRequest(SelectionKey key) throws IOException{
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = client.read(buffer);

        if(bytesRead == -1){
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