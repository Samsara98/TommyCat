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
    public static final String HTTP_VERSION = "HTTP/";
    public static final Integer TIME_OUT = 5000;
    public static final String SERVER = "JerryRat/1.0 (Linux)";
    public static final String STATUS200 = " 200 OK";
    public static final String STATUS201 = " 201 Created";
    public static final String STATUS204 = " 204 No Content";
    public static final String STATUS400 = " 400 Bad Request";
    public static final String STATUS404 = " 404 Not Found";
    public Map<String, String> map;


    ServerSocket serverSocket;

    public JerryRat() throws IOException {
        serverSocket = new ServerSocket(Integer.parseInt(SERVER_PORT));
    }

    @Override
    public void run() {
        app:
        while (true) {
            try (
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ) {
                clientSocket.setSoTimeout(TIME_OUT);
                String request = in.readLine();
                Response1_0 response = simpleResponse(STATUS200);

                String requestMethod = "";
                String requestURL = "";
                int requestContentLength = -1;
                StringBuilder requestBody = null;
                label:
                while (request != null) {
                    System.out.println("request:"+request);
                    String[] req = request.split(" ");
                    String requestHead = req[0];
                    if (request.equals("")) {
                        if (requestMethod.equals("POST")) {

                            if (requestContentLength <= 0) {
                                response = simpleResponse(STATUS400);
                            }
                            if (requestURL.equals("/endpoints/null")) {
                                response = POSTMethodResponse(in, requestURL, requestContentLength);
                            }
                            if (requestURL.startsWith("/emails")) {
                                File dir = new File(WEB_ROOT, "/emails");
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                File postFile = new File(WEB_ROOT, requestURL);
                                if (!postFile.exists()) {
                                    postFile.createNewFile();
                                }
                                response = POSTMethodResponse(in, requestURL, requestContentLength);
                            }
                        }
                        break;
                    }
                    switch (requestHead) {
                        case "GET":
                        case "HEAD":
                            requestMethod = requestHead;
                            requestURL = req[1];
                            requestURL = URLDecoder.decode(requestURL, StandardCharsets.UTF_8);
                            if (requestURL.equals("/endpoints/user-agent")) {
                                request = in.readLine();
                                continue;
                            }
                            if (req.length >= 3 && !req[req.length - 1].toUpperCase(Locale.ROOT).startsWith(HTTP_VERSION)) {
                                response = simpleResponse(STATUS400);
                                break label;
                            }
                            File requestFile = new File(WEB_ROOT + requestURL);
                            requestFile = getFileName(requestFile);
                            if (!requestFile.exists()) {
                                response = simpleResponse(STATUS404);
                                request = in.readLine();
                                continue ;
                            }
                            response = GETMethodResponse(requestFile, getRequestFileType(requestFile));
                            if (requestMethod.equals("GET")) {
                                //HTTP 0.9
                                if (!req[req.length - 1].toUpperCase(Locale.ROOT).startsWith(HTTP_VERSION)) {
                                    response = new Response1_0();
                                    response.setEntityBody(new EntityBody(getFileContent(requestFile)));
                                    out.print(response);
                                    out.flush();
                                    continue app;
                                }
                                EntityBody entityBody = new EntityBody<>(new String(getFileContent(requestFile), StandardCharsets.UTF_8));

                                response.setEntityBody(entityBody);

                            }
                            break;
                        case "User-Agent:":
                            if (requestMethod.equals("GET") && requestURL.equals("/endpoints/user-agent")) {
                                String fieldValue = request.substring(12);
                                response.getResponseHead().setContentLength(fieldValue.getBytes().length);
                                response.setEntityBody(new EntityBody(request.substring(12)));
                            }
                            break;
                        case "Content-Length:":
                            requestContentLength = Integer.parseInt(req[1]);
                            break;
                        case "POST":
                            requestMethod = requestHead;
                            requestURL = req[1];
                            requestURL = URLDecoder.decode(requestURL, StandardCharsets.UTF_8);
//                            if (requestURL.equals("/endpoints/null")) {
//                                response = (simpleResponse(STATUS204));
////                                request = in.readLine();
//                            }
                            break;
                        default:
                            break;
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

    private Response1_0 POSTMethodResponse(BufferedReader in, String requestURL, int requestContentLength) throws IOException {
        Response1_0 response = null;
        char[] chars = new char[requestContentLength];
        in.read(chars);
        if(requestURL.startsWith("/emails")){
            FileWriter fis = new FileWriter(WEB_ROOT + requestURL);
            BufferedWriter bw = new BufferedWriter(fis);
            bw.write(chars);
            bw.flush();
            bw.close();
            response = simpleResponse(STATUS201);
        }else if(requestURL.equals("/endpoints/null")){
            response = simpleResponse(STATUS204);
        }
        return response;
    }

    private void getUserAgent(String request, Response1_0 response) {
        if (response.getEntityBody() != null) {

        }
    }

    private String getRequestFileType(File requestFile) {
        String[] urls = requestFile.getName().split("\\.");
        int length = urls.length;
        if (length > 1) {
            return urls[length - 1];
        }
        return "";
    }

    private File getFileName(File requestFile) {
        if (requestFile.isDirectory()) {
            requestFile = new File(requestFile, "/index.html");
        }
        return requestFile;
    }

    private Response1_0 GETMethodResponse(File requestFile, String contentType) throws IOException {
        StatusLine statusLine = new StatusLine();
        ResponseHead responseHead = new ResponseHead();
        Response1_0 response;
        EntityBody entityBody = null;
        contentType = getContentType("." + contentType);
        //Status-Line
        statusLine.setStatusCode(STATUS200);
        //Date头
        responseHead.setDate(new Date());
        //Server头
        responseHead.setServer(SERVER);
        //Content-Length头
        responseHead.setContentLength(requestFile.length());
        //Content-Type头
        responseHead.setContentType(contentType);
        //Last-Modified头
        responseHead.setLastModified(requestFile.lastModified());
        //EntityBody
        response = new Response1_0(statusLine, responseHead, entityBody);
        return response;
    }

    private byte[] getFileContent(File requestFile) throws IOException {
        FileInputStream fos = new FileInputStream(requestFile);
        BufferedInputStream bis = new BufferedInputStream(fos);
        byte[] content = new byte[(int) requestFile.length()];
        bis.read(content);
        bis.close();
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

    private Response1_0 simpleResponse(String statusCode) {
        StatusLine statusLine = new StatusLine();
        Response1_0 response1_0 = new Response1_0();
        statusLine.setStatusCode(statusCode);

        ResponseHead responseHead = new ResponseHead();
        responseHead.setServer(SERVER);
        responseHead.setContentLength(0);
        responseHead.setContentType("text/plain");

        response1_0.setStatusLine(statusLine);
        response1_0.setResponseHead(responseHead);
        return response1_0;
    }

    public static void main(String[] args) throws IOException {
        JerryRat jerryRat = new JerryRat();
        new Thread(jerryRat).start();
    }
}
