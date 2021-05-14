package http;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ResponseHead {
    Date date;
    String server;
    long contentLength;
    String contentType;
    String location;
    String WWWAuthenticate;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWWWAuthenticate() {
        return WWWAuthenticate;
    }

    public void setWWWAuthenticate(String WWWAuthenticate) {
        this.WWWAuthenticate = WWWAuthenticate;
    }

    public ResponseHead() {
        this.date = new Date();
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return ("Date: " + sdf.format(date) + "\r\n") +
                ("Server: " + server + "\r\n") +
                ("Content-Length: " + contentLength + "\r\n") +
                ((location == null) ? "" : ("Location: " + location + "\r\n") )+
                ((WWWAuthenticate == null) ? "" : ("WWW-Authenticate: " + WWWAuthenticate + "\r\n") )+
                ((contentType == null) ? "" : ("Content-Type: " + contentType + "\r\n")) +
                ((lastModified == null) ? "" : ("Last-Modified: " + sdf.format(lastModified) + "\r\n"));
    }
}
