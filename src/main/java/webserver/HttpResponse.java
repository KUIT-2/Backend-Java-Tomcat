package webserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static webserver.Url.ROOT;

public class HttpResponse {

    private DataOutputStream dos;

    byte[] body = new byte[0];
    private static final Logger log = Logger.getLogger(HttpResponse.class.getName());

    public HttpResponse(DataOutputStream dos) throws IOException {
        this.dos = dos;
    }

    public static HttpResponse from(DataOutputStream dos) throws IOException {
        return new HttpResponse(dos);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void responseBody() {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    public void response200Header() {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes(HttpHeader.CONTENT_TYPE_HTML.getMessage());
            dos.writeBytes(HttpHeader.CONTENT_LENGTH.getMessage() + body.length + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
    public void response302Header(String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes(HttpHeader.LOCATION.getMessage() + url + "\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    public void responseCssHeader() {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes(HttpHeader.CONTENT_TYPE_CSS.getMessage());
            dos.writeBytes(HttpHeader.CONTENT_LENGTH.getMessage() + body.length + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    public void responseLoginSuccess(String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes(HttpHeader.LOCATION.getMessage() + url + "\r\n");
            dos.writeBytes("Set-Cookie: logined=true\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
}
