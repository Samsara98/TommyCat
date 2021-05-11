import java.io.IOException;

public class JerryRat implements Runnable {
    @Override
    public void run(){
        System.out.println("I'm Jerry the rat!");
        EchoServer echoServer = null;
        try {
            echoServer = new EchoServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(echoServer).start();
    }

    public static void main(String[] args){
        JerryRat jerryRat = new JerryRat();
        jerryRat.run();
    }
}
