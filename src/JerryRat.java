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
    public static final String HTTP_VERSION = "HTTP/1.";
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

        while (true) {
            try (
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ) {
                String request = in.readLine();
                Response1_0 response = new Response1_0();

                String requestMethod = "GET";
                String requestURL = "";
                label:
                while (request != null) {
                    StatusLine statusLine = new StatusLine();
                    String[] req = request.split(" ");
                    String requestHead = req[0];
                    if (request.equals("")) {
                        break;
                    }
                    switch (requestHead) {
                        case "GET":
                        case "HEAD":
                            requestMethod = requestHead;
                            requestURL = checkRequest(out, req);
                            if (requestURL == null) break label;
                            requestURL = URLDecoder.decode(requestURL, StandardCharsets.UTF_8);
                            if (requestURL.equals("/endpoints/user-agent")) {
                                response.setEntityBody(new EntityBody(request));
                                request = in.readLine();
                                continue;
                            }
                            File requestFile = new File(WEB_ROOT + requestURL);
                            requestFile = getFileName(out, requestFile);
                            if (requestFile == null) break label;

                            if ("GET".equals(requestHead)) {
                                //HTTP 0.9
                                if (!req[req.length - 1].toUpperCase(Locale.ROOT).startsWith(HTTP_VERSION)) {
                                    response.setEntityBody(new EntityBody(getFileContent(requestFile)));
                                    break label;
                                }
                                EntityBody entityBody = new EntityBody<>(new String(getFileContent(requestFile), StandardCharsets.UTF_8));
                                response.setEntityBody(entityBody);
                            }
                            response = GETMethodResponse(requestFile, getRequestFileType(requestFile));
                            break;
                        case "User-Agent:":
                            getUserAgent(request, response, statusLine);
                            break;
                        case "POST":
                            requestMethod = requestHead;
                            requestURL = req[1];
                            if (requestURL.equals("/endpoints/null")) {
                                out.print(simpleResponse(STATUS204));
                                out.flush();
                                break label;
                            }
                            File postFile = new File(WEB_ROOT, requestURL);
                            if (!postFile.exists()) {
                                postFile.createNewFile();
                            }
                            break;
                        default:
                            if (requestMethod.equals("POST")) {
                                FileWriter fis = new FileWriter(WEB_ROOT + requestURL);
                                BufferedWriter bw = new BufferedWriter(fis);
                                bw.write(request);
                                bw.flush();
                                out.print(simpleResponse(STATUS201));
                                out.flush();
                                break label;
                            }
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

    private void getUserAgent(String request, Response1_0 response, StatusLine statusLine) {
        if (response.getEntityBody() != null) {

            String fieldValue = request.substring(12);
            response = simpleResponse(STATUS200);
            response.getResponseHead().setContentLength(fieldValue.getBytes().length);

            if (response.getEntityBody().toString().startsWith("GET")) {
                response.setEntityBody(new EntityBody(request.substring(12)));
            } else {
                response.setEntityBody(null);
            }
        }
    }

    private String checkRequest(PrintWriter out, String[] req) {
        String requestURL = req[1];

        if (req.length >= 3 && !req[req.length - 1].toUpperCase(Locale.ROOT).startsWith(HTTP_VERSION)) {
            out.print(simpleResponse(STATUS400));
            out.flush();
            return null;
        }
        return requestURL;
    }

    private String getRequestFileType(File requestFile) {
        String[] urls = requestFile.getName().split("\\.");
        int length = urls.length;
        if (length > 1) {
            return urls[length - 1];
        }
        return "";
    }

    private File getFileName(PrintWriter out, File requestFile) {
        if (!requestFile.exists()) {
            out.print(simpleResponse(STATUS404));
            out.flush();
            return null;
        }
        if (requestFile.isDirectory()) {
            requestFile = new File(requestFile, "/index.html");
            if (!requestFile.exists()) {
                out.print(simpleResponse(STATUS404));
                out.flush();
                return null;
            }
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
