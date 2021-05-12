package http;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ResponseHead {
    Date date;
    String server;
    long contentLength;
    String contentType;
    Date lastModified;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = new Date(lastModified);
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        return "Date: " + sdf.format(date) + "\r\n" +
                "Server: " + server + "\r\n" +
                "Content-Length: " + contentLength + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Last-Modified: " + sdf.format(lastModified) + "\r\n";
    }
}
