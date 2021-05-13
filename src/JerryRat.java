import http.EntityBody;
import http.Response1_0;
import http.ResponseHead;
import http.StatusLine;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JerryRat implements Runnable {

    public static final String SERVER_PORT = "8080";
    public static final String WEB_ROOT = "res/webroot";
    public static final String HTTP_VERSION = "HTTP/1.1";
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
                Response1_0 response = new Response1_0();
                while (true) {
                    StatusLine statusLine = new StatusLine();
                    String[] req = request.split(" ");
                    if (request.length() == 0) {
                        break;
                    }
                    String requestHead = req[0];
                    if ("GET".equals(requestHead)) {
                        String contentType = "";
                        String requestURL = checkRequest(out, req);
                        if (requestURL == null) break;
                        requestURL = URLDecoder.decode(requestURL, StandardCharsets.UTF_8);
                        if (requestURL.equals("/endpoints/user-agent")) {
                            continue;
                        }
                        File requestFile = new File(WEB_ROOT + requestURL);
                        requestFile = getFileName(out, requestFile);
                        if (requestFile == null) break;

                        System.out.println(request);
                        //HTTP 0.9
                        if (!request.strip().toUpperCase(Locale.ROOT).endsWith(HTTP_VERSION)) {
                            response.setEntityBody(new EntityBody(getFileContent(requestFile)));
                            break;
                        }
                        contentType = getRequestFileType(contentType, requestURL, requestFile);

                        response = getResponse1_0(statusLine, requestFile, contentType);
                    } else if (requestHead.equals("User-Agent:")) {
                        statusLine.setStatusCode(STATUS200);
                        String fieldValue = req[1];
                        response.setEntityBody(new EntityBody(fieldValue));
                    }
//                    else {
//                        out.print(getErrorResponse(STATUS400));
//                        out.flush();
//                        break;
//                    }
                    request = in.readLine();
                }
                out.print(response);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("TCP连接错误！");
            }
        }
    }

    private String checkRequest(PrintWriter out, String[] req) {
        if (req.length <= 1) {
            out.print(getErrorResponse(STATUS400));
            out.flush();
            return null;
        }
        StringBuilder requestURL = new StringBuilder(req[1]);
        int x = req.length - 1;
        if (req[req.length - 1].toUpperCase(Locale.ROOT).equals(HTTP_VERSION)) {
            x = req.length - 2;
        }
        if (req.length >= 3) {
            for (int i = 2; i <= x; i++) {
                requestURL.append(" ").append(req[i]);
            }
        }
        return requestURL.toString();
    }

    private String getRequestFileType(String contentType, String requestURL, File requestFile) {
        if (requestFile.getName().endsWith("html")) {
            contentType = "html";
        }
        String[] urls = requestURL.split("\\.");
        int length = urls.length;
        if (length > 1) {
            contentType = urls[length - 1];
        }
        return contentType;
    }

    private File getFileName(PrintWriter out, File requestFile) {
        if (!requestFile.exists()) {
            out.print(getErrorResponse(STATUS404));
            out.flush();
            return null;
        }
        if (requestFile.isDirectory()) {
            requestFile = new File(requestFile, "/index.html");
            if (!requestFile.exists()) {
                out.print(getErrorResponse(STATUS404));
                out.flush();
                return null;
            }
        }
        return requestFile;
    }

    private Response1_0 getResponse1_0(StatusLine statusLine, File requestFile, String contentType) throws IOException {
        ResponseHead responseHead = new ResponseHead();
        Response1_0 response;
        EntityBody entityBody;
        contentType = getContentType("." + contentType);
        //Status-Line
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
        return response;
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

    private Response1_0 getErrorResponse(String statusCode) {
        StatusLine statusLine = new StatusLine();
        Response1_0 response1_0 = new Response1_0();
        switch (statusCode) {
            case STATUS400:
                statusLine.setStatusCode(STATUS400);
                break;
            case STATUS404:
                statusLine.setStatusCode(STATUS404);
                break;
        }
        response1_0.setStatusLine(statusLine);
        return response1_0;
    }

    public static void main(String[] args) throws IOException {
        JerryRat jerryRat = new JerryRat();
        new Thread(jerryRat).start();
    }
}
