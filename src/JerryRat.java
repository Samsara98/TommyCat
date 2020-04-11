public class JerryRat implements Runnable {
    @Override
    public void run() {
        System.out.println("I'm Jerry the rat!");
    }

    public static void main(String[] args){
        JerryRat jerryRat = new JerryRat();
        jerryRat.run();
    }
}
