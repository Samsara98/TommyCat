import java.io.*;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JerryRat implements Runnable {

    public static final String SERVER_PORT = "8080";
    public static final String WEB_ROOT = "res/webroot";
    ServerSocket serverSocket;

    public JerryRat() throws IOException {
        serverSocket = new ServerSocket(Integer.parseInt(SERVER_PORT));
    }

    @Override
    public void run() {
        while (true) {
            try (
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ) {
                String request = in.readLine();
                while (request != null) {
                    System.out.println(request);
                    String entityBody;
                    String[] req = request.split(" ");
                    String requestMethod = req[0];

                    //Status-Line
                    out.print("HTTP/1.0 ");
                    if (!requestMethod.equals("GET")) {
                        out.println("400 Bad Request");
                        request = in.readLine();
                        continue;
                    }
                    String requestURL = req[1];
                    File requestFile = new File(WEB_ROOT + requestURL);
                    if (!requestFile.exists()) {
                        out.println("404 Not Found");
                    }

                    long contentLength;
                    String contentType;
                    long lastModified;
                    if (requestFile.isDirectory()) {
                        requestFile = new File(requestFile, "/index.html");

                        contentLength = requestFile.length();
                        lastModified = requestFile.lastModified();

                        if (!requestFile.exists()) {
                            out.println("404 Not Found");
                            request = in.readLine();
                            continue;
                        }
                        FileReader fos = new FileReader(requestFile);
                        char[] content = new char[(int) requestFile.length()];
                        fos.read(content);
                        entityBody = String.valueOf(content);
                        contentType = "html";
                    } else {
                        contentLength = requestFile.length();
                        lastModified = requestFile.lastModified();

                        FileReader fos = new FileReader(requestFile);
                        char[] content = new char[(int) requestFile.length()];
                        fos.read(content);
                        entityBody = String.valueOf(content);
                        String[] urls = requestURL.split("\\.");
                        int length = urls.length;
                        if (length > 1) {
                            contentType = urls[length - 1];
                        } else {
                            contentType = "html";
                        }
                    }
                    out.println("200 OK");
                    //Date头
                    Date date = new Date();
                    out.println("Date: " + date);
                    //Server头
                    out.println("Server: JerryRat/1.0 (Linux)");

                    //Content-Length头
                    out.println("Content-Length: " + contentLength);

                    //Content-Type头
                    out.println("Content-Type: text/" + contentType);

                    //Last-Modified头
                    out.println("Last-Modified: " + new Date(lastModified));

                    //EntityBody
                    out.println(entityBody);
                    request = in.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("TCP连接错误！");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        JerryRat jerryRat = new JerryRat();
        new Thread(jerryRat).start();
    }
}
