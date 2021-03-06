package http;


public class StatusLine {
    String httpVersion;
    String statusCode;

    public StatusLine() {
        this.httpVersion = "HTTP/1.0";
    }


    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return (httpVersion + " " + statusCode + "\r\n");
    }
}
