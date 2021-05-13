package http;

public class Response1_0 {

    StatusLine statusLine;
    ResponseHead responseHead;
    EntityBody entityBody;

    public Response1_0() {
    }

    public Response1_0(StatusLine statusLine, ResponseHead responseHead, EntityBody entityBody) {
        this.statusLine = statusLine;
        this.responseHead = responseHead;
        this.entityBody = entityBody;
    }

    public StatusLine getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(StatusLine statusLine) {
        this.statusLine = statusLine;
    }

    public ResponseHead getResponseHead() {
        return responseHead;
    }

    public void setResponseHead(ResponseHead responseHead) {
        this.responseHead = responseHead;
    }

    public EntityBody getEntityBody() {
        return entityBody;
    }

    public void setEntityBody(EntityBody entityBody) {
        this.entityBody = entityBody;
    }

    @Override
    public String toString() {
        String statusLineS = statusLine == null ? "" : statusLine.toString();
        String responseHeadS = responseHead == null ? "" : responseHead.toString();
        String entityBodyS = entityBody == null ? "" : entityBody.toString();
        return statusLineS + responseHeadS + ((entityBodyS.equals("")) ? "" : ("\r\n" + entityBodyS));
    }
}
