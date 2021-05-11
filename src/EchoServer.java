import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

public class EchoServer implements Runnable {
    public static final String SERVER_PORT = "8080";
    public static final String WEB_ROOT = "res/webroot";
    ServerSocket serverSocket;

    public EchoServer() throws IOException {
        serverSocket = new ServerSocket(Integer.parseInt(SERVER_PORT));
    }

    @Override
    public void run() {
        try (
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String request = in.readLine();
            while (request != null) {
                String entityBody;
                String[] req = request.split(" ");
                String requestMethod = req[0];
                if (!requestMethod.toLowerCase(Locale.ROOT).equals("get")) {
                    request = in.readLine();
                    continue;
                }
                String requestPath = req[1];
                File requestFile = new File(WEB_ROOT + requestPath);
                if (requestFile.isDirectory()) {
                    requestFile = new File(requestFile.getAbsolutePath() + "/index.html");
                    FileReader fos = new FileReader(requestFile);
                    char[] content = new char[(int) requestFile.length()];
                    fos.read(content);
                    entityBody = String.valueOf(content);
                    out.println(entityBody);
                } else {
                    FileReader fos = new FileReader(requestFile);
                    char[] content = new char[(int) requestFile.length()];
                    fos.read(content);
                    entityBody = String.valueOf(content);
                    out.println(entityBody);
                }
                request = in.readLine();
            }
        } catch (IOException e) {
            System.err.println("TCP连接错误！");
        }
    }

    public static void main(String[] args) throws IOException {
        EchoServer echoServer = new EchoServer();
        new Thread(echoServer).run();
    }
}