import java.io.IOException;

public class JerryRat implements Runnable {
    @Override
    public void run() {
        System.out.println("I'm Jerry the rat!");
    }

    public static void main(String[] args) throws IOException{
        EchoServer echoServer = new EchoServer();
        new Thread(echoServer).run();
    }
}
