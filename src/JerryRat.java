import java.io.IOException;

public class JerryRat implements Runnable {
    @Override
    public void run() {
        System.out.println("I'm Jerry the rat!");
    }

    public static void main(String[] args){
        EchoServer echoServer = null;
        try {
            echoServer = new EchoServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        echoServer.run();
    }
}
