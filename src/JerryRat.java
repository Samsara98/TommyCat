import http.EntityBody;
import http.Response1_0;
import http.ResponseHead;
import http.StatusLine;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JerryRat implements Runnable {

    public static final String SERVER_PORT = "8080";
    public static final String WEB_ROOT = "res/webroot";
    public static final String HTTP_VERSION = "HTTP/1.0";
    public static final String STATUS200 = " 200 OK";
    public static final String STATUS400 = " 400 Bad Request";
    public static final String STATUS404 = " 404 Not Found";
    public Map<String, String> map;


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

                StatusLine statusLine = new StatusLine();
                ResponseHead responseHead = new ResponseHead();
                EntityBody entityBody;
                Response1_0 response = new Response1_0();

                String[] req = request.split(" ");
                String requestURL = req[1];
                //Status-Line
                statusLine.setHttpVersion(HTTP_VERSION);
                if (!req[0].equals("GET") || req.length < 3 || !req[2].toUpperCase(Locale.ROOT).equals(HTTP_VERSION)) {
                    statusLine.setStatusCode(STATUS400);
                    response.setStatusLine(statusLine);
                    out.println(statusLine);
                    continue;
                }
                File requestFile = new File(WEB_ROOT + requestURL);
                if (!requestFile.exists()) {
                    statusLine.setStatusCode(STATUS404);
                    response.setStatusLine(statusLine);
                    out.println(response);
                    continue;
                }
                String contentType = "";
                if (requestFile.isDirectory()) {
                    requestFile = new File(requestFile, "/index.html");
                    if (!requestFile.exists()) {
                        statusLine.setStatusCode(STATUS404);
                        response.setStatusLine(statusLine);
                        out.println(response);
                        continue;
                    }
                    contentType = "html";
                } else {
                    String[] urls = requestURL.split("\\.");
                    int length = urls.length;
                    if (length > 1) {
                        contentType = urls[length - 1];
                    }
                }
                contentType = getContentType("." + contentType);
                statusLine.setStatusCode(STATUS200);
                //Date头
                responseHead.setDate(new Date());
                //Server头
                responseHead.setServer("JerryRat/1.0 (Linux)");
                //Content-Length头
                responseHead.setContentLength(requestFile.length());
                //Content-Type头
                responseHead.setContentType(contentType);
                //Last-Modified头
                responseHead.setLastModified(requestFile.lastModified());
                //EntityBody
                entityBody = new EntityBody(getFileContent(requestFile));

                response = new Response1_0(statusLine, responseHead, entityBody);
                out.println(response);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("TCP连接错误！");
            }
        }
    }

    private byte[] getFileContent(File requestFile) throws IOException {
        FileInputStream fos = new FileInputStream(requestFile);
        BufferedInputStream bis = new BufferedInputStream(fos);
        byte[] content = new byte[(int) requestFile.length()];
        bis.read(content);
        return content;

    }

    private String getContentType(String content) throws IOException {
        map = new HashMap<>();
        try (
                FileReader fos = new FileReader("res/contentType.txt");
                BufferedReader br = new BufferedReader(fos)
        ) {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                String[] lines = line.split(" ");
                for (int i = 0; i < lines.length; i += 2) {
                    map.put(lines[i].strip(), lines[i + 1].strip());
                }
            }
            if (map.get(content) == null) {
                return "application/octet-stream";
            }
            return map.get(content);
        }

    }

    public static void main(String[] args) throws IOException {
        JerryRat jerryRat = new JerryRat();
        new Thread(jerryRat).start();
    }
}
